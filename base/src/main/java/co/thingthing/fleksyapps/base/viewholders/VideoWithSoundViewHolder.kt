package co.thingthing.fleksyapps.base.viewholders

import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.media3.ui.PlayerView
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
    private val onPlayedVideo: (video: BaseMedia, playerView: PlayerView) -> Unit,
) : BaseViewHolder<BaseResult>(binding.root) {

    private val frescoImageLoader: FrescoImageLoader by lazy { FrescoImageLoader() }

    override fun bind(viewModel: BaseResult) {
        super.bind(viewModel)
        (viewModel as BaseResult.VideoWithSound).let { vm ->
            renderVideo(item = vm)
            renderAudioButton(item = vm)
            renderLabel(label = vm.label)
            renderDurationText(duration = vm.duration)
        }
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
        binding.audioButton.setOnClickListener {
            onMuteClicked(item)
        }
    }

    private fun renderVideo(item: BaseResult.VideoWithSound) {
        item.video.preferredImage(DEFAULT_CONTENT_TYPES)?.also { video ->
            binding.root.post {
                prepareVideoPreview(video = video)
                onPlayedVideo(video, binding.playerView)
            }
        }
        item.thumbnail?.preferredImage(DEFAULT_CONTENT_TYPES)?.also { image ->
            frescoImageLoader.load(
                binding.image,
                item.theme.background,
                item.theme.foreground,
                image.width.toFloat(),
                image.height.toFloat(),
                image.url,
                image.url
            )
        }
    }

    private fun prepareVideoPreview(video: BaseMedia) {
        val cardHeight = binding.cardView.measuredHeight.toFloat()
        val aspectRatio = video.width.toFloat() / video.height.toFloat()

        val measuredWidth = (cardHeight * aspectRatio).toInt()
        val measuredHeight = cardHeight.toInt()

        binding.run {
            cardView.updateLayoutParams<ViewGroup.LayoutParams> {
                width = measuredWidth
                height = measuredHeight
            }
        }
    }
}
