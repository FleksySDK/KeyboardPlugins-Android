package co.thingthing.fleksyapps.base.viewholders

import android.net.Uri
import android.view.View
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import co.thingthing.fleksyapps.base.BaseMedia
import co.thingthing.fleksyapps.base.BaseResult
import co.thingthing.fleksyapps.base.BaseViewHolder
import co.thingthing.fleksyapps.base.R
import co.thingthing.fleksyapps.base.databinding.LayoutVideoItemBinding
import co.thingthing.fleksyapps.base.utils.FrescoImageLoader
import co.thingthing.fleksyapps.base.utils.preferredImage

class VideoViewHolder(
    private val binding: LayoutVideoItemBinding,
    private val onMuteClicked: (BaseResult) -> Unit,
) : BaseViewHolder<BaseResult>(binding.root) {
    private var exoPlayer: ExoPlayer? = null

    override fun bind(viewModel: BaseResult) {
        super.bind(viewModel)
        (viewModel as BaseResult.Video).let { vm ->
            exoPlayer = ExoPlayer.Builder(binding.root.context).build()
            val contentTypes = listOf("image/webp", "video/mp4", "image/gif", "image/jpeg")
            renderVideo(item = vm, contentTypes = contentTypes)
            renderDuration(vm.duration)
            renderLabel(vm.label)
            renderAudioButton(vm)
        }
    }

    private fun getAudioIcon(isMuted: Boolean) = if (isMuted) R.drawable.ic_mute else R.drawable.ic_unmute
    private fun BaseResult.Video.getCurrentVolume() = if (isMuted) 0F else 1F

    private fun renderDuration(duration: Long?) {
        duration?.let {
            binding.duration.text =
                itemView.context.getString(R.string.duration, duration.toString())
        } ?: run { binding.duration.visibility = View.GONE }
    }

    private fun renderLabel(label: String?) {
        label?.let {
            binding.title.text = it
        } ?: run { binding.title.visibility = View.GONE }
    }

    private fun renderAudioButton(item: BaseResult.Video) {
        binding.audioButton.run {
            setOnClickListener {
                changeMute(item)
                setImageResource(getAudioIcon(isMuted = item.isMuted))
            }
            setImageResource(getAudioIcon(isMuted = item.isMuted))
        }
    }

    private fun renderVideo(
        item: BaseResult.Video,
        contentTypes: List<String>,
    ) {
        item.video.preferredImage(contentTypes)?.also { video ->
            exoPlayer?.apply {
                binding.root.post {
                    preparePreview(video = video)
                    preparePlayer(exoPlayer = this, item = item, video = video)
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun preparePreview(video: BaseMedia) {
        val height = binding.cardView.measuredHeight.toFloat()
        val aspectRatio = video.width.toFloat() / video.height.toFloat()
        val width = (height * aspectRatio).toInt()
        binding.cardView.changeSize(width, height.toInt())
        binding.image.changeSize(width, height.toInt())
    }

    private fun View.changeSize(width: Int, height: Int) {
        val lp = layoutParams
        lp.width = width
        lp.height = height
        layoutParams = lp
    }

    private fun preparePlayer(exoPlayer: ExoPlayer, item: BaseResult.Video, video: BaseMedia) {
        exoPlayer.run {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY) {
                        binding.image.animate()
                            .alpha(1f)
                            .setDuration(FrescoImageLoader.FADE_DURATION.toLong())
                            .start()
                    }
                }
            })
            binding.image.player = this
            setMediaItem(MediaItem.fromUri(Uri.parse(video.url)))
            repeatMode = Player.REPEAT_MODE_ALL
            playWhenReady = true
            volume = item.getCurrentVolume()
            prepare()
        }
    }
    override fun onRecycled() {
        exoPlayer?.release()
        exoPlayer = null
    }

    private fun changeMute(item: BaseResult.Video) {
        item.isMuted = item.isMuted.not()
        exoPlayer?.volume = item.getCurrentVolume()
        onMuteClicked(item)
    }
}
