package co.thingthing.fleksyapps.base.viewholders

import android.view.View
import androidx.core.view.isVisible
import co.thingthing.fleksyapps.base.BaseResult
import co.thingthing.fleksyapps.base.BaseViewHolder
import co.thingthing.fleksyapps.base.R
import co.thingthing.fleksyapps.base.databinding.LayoutVideoItemBinding
import co.thingthing.fleksyapps.base.utils.FrescoImageLoader
import co.thingthing.fleksyapps.base.utils.hide
import co.thingthing.fleksyapps.base.utils.preferredImage

class VideoViewHolder(
    private val binding: LayoutVideoItemBinding,
    private val onUnMuteClicked: (item: BaseResult.Video) -> Unit,
) :
    BaseViewHolder<BaseResult>(binding.root) {
    private val frescoImageLoader: FrescoImageLoader by lazy { FrescoImageLoader() }

    override fun bind(viewModel: BaseResult) {
        super.bind(viewModel)
        (viewModel as BaseResult.Video).let { vm ->
            binding.audioButton.apply {
                isVisible = vm.showTitleAndSound
                setOnClickListener {
                    onUnMuteClicked(vm)
                }
            }
            vm.label?.let {
                binding.title.isVisible = vm.showTitleAndSound
                binding.title.text = it
            } ?: run { binding.title.hide() }
            vm.thumbnail?.preferredImage(DEFAULT_CONTENT_TYPES)?.also { image ->
                frescoImageLoader.load(
                    binding.image,
                    vm.theme.background,
                    vm.theme.foreground,
                    image.width.toFloat(),
                    image.height.toFloat(),
                    image.url,
                    image.url
                )
            }
            vm.duration?.let {
                binding.duration.text =
                    itemView.context.getString(R.string.duration, vm.duration.toString())
            } ?: run { binding.duration.visibility = View.GONE }
        }
    }
}