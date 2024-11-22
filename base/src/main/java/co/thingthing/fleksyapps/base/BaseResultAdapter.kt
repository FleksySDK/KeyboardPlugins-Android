package co.thingthing.fleksyapps.base

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import co.thingthing.fleksyapps.base.databinding.LayoutCardItemBinding
import co.thingthing.fleksyapps.base.databinding.LayoutImageItemBinding
import co.thingthing.fleksyapps.base.databinding.LayoutVideoItemBinding
import co.thingthing.fleksyapps.base.databinding.LayoutVideoWithSoundItemBinding
import co.thingthing.fleksyapps.base.utils.FrescoImageLoader
import co.thingthing.fleksyapps.base.utils.show
import co.thingthing.fleksyapps.base.viewholders.CardViewHolder
import co.thingthing.fleksyapps.base.viewholders.ImageViewHolder
import co.thingthing.fleksyapps.base.viewholders.VideoViewHolder
import co.thingthing.fleksyapps.base.viewholders.VideoWithSoundViewHolder
import co.thingthing.fleksyapps.core.AppTheme


class BaseResultAdapter(
    private val onVideoUnMuted: (position: Int) -> Unit
) : BaseAdapter<BaseResult>() {
    private var exoPlayer: ExoPlayer? = null
    private var unMutedPosition = 0

    override fun create(parent: ViewGroup, viewType: Int): BaseViewHolder<BaseResult> {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            IMAGE -> ImageViewHolder(
                binding = LayoutImageItemBinding.inflate(layoutInflater, parent, false)
            )

            VIDEO_WITH_SOUND -> VideoWithSoundViewHolder(
                binding = LayoutVideoWithSoundItemBinding.inflate(layoutInflater, parent, false),
                onMuteClicked = ::muteClicked,
                onPreviewRendered = { item, video, playerView ->
                    createExoPlayerIfNeeded(parent.context)
                    prepareVideoPlayer(video, playerView)
                    val position = items.indexOf(item)
                    if (position == NOT_FOUND_INDEX) {
                        /**
                         * Sometimes items list contains an old [BaseResult.Video] item
                         * Instead of new [BaseResult.VideoWithSound] item
                         * In this case we will update the list again
                         */
                        (items[unMutedPosition] as? BaseResult.Video)?.let {
                            unMuteItem(it)
                            onVideoUnMuted(unMutedPosition)
                        }
                    } else {
                        onVideoUnMuted(items.indexOf(item))
                    }
                },
            )

            VIDEO -> VideoViewHolder(
                binding = LayoutVideoItemBinding.inflate(layoutInflater, parent, false),
                onUnMuteClicked = ::muteClicked
            )

            else -> CardViewHolder(
                binding = LayoutCardItemBinding.inflate(layoutInflater, parent, false)
            )
        }
    }

    private fun createExoPlayerIfNeeded(context: Context) {
        if (exoPlayer == null) { exoPlayer = ExoPlayer.Builder(context).build() }
    }

    private fun prepareVideoPlayer(video: BaseMedia, playerView: PlayerView) {
        exoPlayer?.run {
            doWhenPlayerReady {
                /** doing a similar fade animation that we have in webp */
                playerView
                    .animate()
                    .alpha(1f)
                    .setDuration(FrescoImageLoader.FADE_DURATION.toLong())
                    .start()
                playerView.show()
            }
            playerView.player = this
            volume = 1F
            setMediaItem(MediaItem.fromUri(Uri.parse(video.url)))
            repeatMode = Player.REPEAT_MODE_ALL
            playWhenReady = true
            prepare()
        }
    }

    /**
     * Mutes other video items with sound enabled, allowing only one item to have sound at a time.
     *
     * @param item the {@link BaseResult.VideoWithSound} item for which the mute action was triggered.
     */
    private fun muteClicked(item: BaseResult) {
        if (item is BaseResult.Video && item.showTitleAndSound) {
            muteAllVideosExceptItem(item)
            unMuteItem(item)
        } else if (item is BaseResult.VideoWithSound) {
            muteItem(item)
        }
    }

    private fun unMuteItem(item: BaseResult.Video) {
        item.apply {
            val index = items.indexOf(this)
            if (index != NOT_FOUND_INDEX) {
                items[index] = toUnMutedVideo()
                unMutedPosition = index
                notifyItemChanged(index)
            }
        }
    }

    private fun muteAllVideosExceptItem(item: BaseResult) {
        items
            .filterIsInstance<BaseResult.VideoWithSound>()
            .filter { it.id != item.id }
            .forEach { muteItem(it) }
    }

    /**
     * Mutes specific item if it is not muted before
     *
     * @param item that should be muted
     */
    private fun muteItem(item: BaseResult.VideoWithSound) {
        releasePlayer()
        item.apply {
            val index = items.indexOf(this)
            if (index != NOT_FOUND_INDEX) {
                items[index] = toMutedVideo()
                notifyItemChanged(index)
            }
        }
    }

    /**
     * Notifies that the item is no longer visible on the screen
     *
     * @param position of the item that is no longer visible
     */
    fun onItemOutOfScreen(position: Int) {
        if (position < items.size) {
            when (val item = items[position]) {
                is BaseResult.VideoWithSound -> muteItem(item)
                else -> {} // do nothing
            }
        }
    }

    override fun getItemViewType(position: Int) =
        when (items[position]) {
            is BaseResult.Image -> IMAGE
            is BaseResult.VideoWithSound -> VIDEO_WITH_SOUND
            is BaseResult.Video -> VIDEO
            is BaseResult.Card -> CARD
        }

    fun onThemeChanged(theme: AppTheme) {
        items.forEach { it.onThemeChanged(theme) }
        notifyDataSetChanged()
    }

    private fun ExoPlayer.doWhenPlayerReady(action: () -> Unit) {
        addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) action()
            }
        })
    }

    fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
    }

    companion object {
        const val IMAGE = 1
        const val VIDEO = 2
        const val VIDEO_WITH_SOUND = 3
        const val CARD = 4

        const val NOT_FOUND_INDEX = -1
    }


}

