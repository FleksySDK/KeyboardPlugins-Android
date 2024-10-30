package co.thingthing.fleksyapps.base.viewholders

import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import co.thingthing.fleksyapps.base.BaseResult
import co.thingthing.fleksyapps.base.BaseViewHolder
import co.thingthing.fleksyapps.base.R
import co.thingthing.fleksyapps.base.databinding.LayoutVideoItemBinding
import co.thingthing.fleksyapps.base.utils.FrescoImageLoader
import co.thingthing.fleksyapps.base.utils.preferredImage
import com.facebook.fresco.animation.drawable.AnimationListener
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player

class VideoViewHolder(
    private val binding: LayoutVideoItemBinding,
    private val onMuteClicked: (BaseResult) -> Unit,
) : BaseViewHolder<BaseResult>(binding.root) {
    private val frescoImageLoader: FrescoImageLoader by lazy { FrescoImageLoader() }
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
                setMediaItem(MediaItem.fromUri(Uri.parse(video.url)))
                repeatMode = Player.REPEAT_MODE_ALL
                volume = item.getCurrentVolume()
                prepare()
            }
        }
        item.thumbnail?.preferredImage(contentTypes)?.also { video ->
            frescoImageLoader.load(
                binding.image,
                item.theme.background,
                item.theme.foreground,
                video.width.toFloat(),
                video.height.toFloat(),
                video.url,
                video.url,
                object : AnimationListener {
                    override fun onAnimationFrame(drawable: Drawable, frameNumber: Int) {}
                    override fun onAnimationRepeat(drawable: Drawable) { syncSound(item) }
                    override fun onAnimationStart(drawable: Drawable) { syncSound(item) }
                    override fun onAnimationReset(drawable: Drawable) {}
                    override fun onAnimationStop(drawable: Drawable) {}
                },
            )
        }
    }

    private fun syncSound(item: BaseResult.Video) {
        exoPlayer?.run {
            seekTo(0)
            volume = item.getCurrentVolume()
            play()
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
