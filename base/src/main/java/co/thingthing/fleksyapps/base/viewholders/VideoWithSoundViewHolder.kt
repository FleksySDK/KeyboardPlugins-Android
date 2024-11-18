package co.thingthing.fleksyapps.base.viewholders

import android.net.Uri
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import co.thingthing.fleksyapps.base.BaseMedia
import co.thingthing.fleksyapps.base.BaseResult
import co.thingthing.fleksyapps.base.BaseViewHolder
import co.thingthing.fleksyapps.base.R
import co.thingthing.fleksyapps.base.databinding.LayoutVideoWithSoundItemBinding
import co.thingthing.fleksyapps.base.utils.FrescoImageLoader
import co.thingthing.fleksyapps.base.utils.hide
import co.thingthing.fleksyapps.base.utils.preferredImage

class VideoWithSoundViewHolder(
    private val binding: LayoutVideoWithSoundItemBinding,
    private val onMuteClicked: (BaseResult.VideoWithSound) -> Unit,
) : BaseViewHolder<BaseResult>(binding.root) {
    private var exoPlayer: ExoPlayer? = null

    override fun bind(viewModel: BaseResult) {
        super.bind(viewModel)
        (viewModel as BaseResult.VideoWithSound).let { vm ->
            createExoPlayerIfNeeded()
            renderVideo(item = vm)
            renderAudioButton(item = vm)
            renderLabel(label = vm.label)
            renderDurationText(duration = vm.duration)
        }
    }

    private fun createExoPlayerIfNeeded() {
        if (exoPlayer == null) exoPlayer = ExoPlayer.Builder(binding.root.context).build()
    }

    private fun getAudioIcon(isMuted: Boolean) = if (isMuted) R.drawable.ic_mute else R.drawable.ic_unmute

    private fun ExoPlayer.setVolume(isMuted: Boolean) { volume = if (isMuted) 0F else 1F }

    private fun ExoPlayer.doWhenPlayerReady(action: () -> Unit) {
        addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) action()
            }
        })
    }

    private fun renderDurationText(duration: Long?) {
        duration?.let {
            binding.duration.text =
                itemView.context.getString(R.string.duration, duration.toString())
        } ?: run { binding.duration.hide() }
    }

    private fun renderLabel(label: String?) {
        label?.let {
            binding.title.text = it
        } ?: run { binding.title.hide() }
    }

    private fun renderAudioButton(item: BaseResult.VideoWithSound) {
        binding.audioButton.run {
            setOnClickListener {
                item.isMuted = item.isMuted.not()
                exoPlayer?.setVolume(item.isMuted)
                setImageResource(getAudioIcon(isMuted = item.isMuted))
                onMuteClicked(item)
            }
            setImageResource(getAudioIcon(isMuted = item.isMuted))
        }
    }

    private fun renderVideo(item: BaseResult.VideoWithSound) {
        item.video.preferredImage(DEFAULT_CONTENT_TYPES)?.also { video ->
            binding.root.post {
                prepareVideoPreview(video = video)
                prepareVideoPlayer(item = item, video = video)
            }
        }
    }

    private fun prepareVideoPreview(video: BaseMedia) {
        val cardHeight = binding.cardView.measuredHeight.toFloat()
        val aspectRatio = video.width.toFloat() / video.height.toFloat()

        val measuredWidth = (cardHeight * aspectRatio).toInt()
        val measuredHeight = cardHeight.toInt()

        binding.run {
            image.updateLayoutParams<ViewGroup.LayoutParams> {
                width = measuredWidth
                height = measuredHeight
            }
            cardView.updateLayoutParams<ViewGroup.LayoutParams> {
                width = measuredWidth
                height = measuredHeight
            }
        }
    }

    private fun prepareVideoPlayer(item: BaseResult.VideoWithSound, video: BaseMedia) {
        exoPlayer?.run {
            doWhenPlayerReady {
                /** doing a similar fade animation that we have in webp */
                binding.playerView
                    .animate()
                    .alpha(1f)
                    .setDuration(FrescoImageLoader.FADE_DURATION.toLong())
                    .start()
            }
            binding.playerView.player = this
            setMediaItem(MediaItem.fromUri(Uri.parse(video.url)))
            repeatMode = Player.REPEAT_MODE_ALL
            playWhenReady = true
            setVolume(item.isMuted)
            prepare()
        }
    }

    override fun onRecycled() {
        releasePlayer()
    }

    fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
    }
}
