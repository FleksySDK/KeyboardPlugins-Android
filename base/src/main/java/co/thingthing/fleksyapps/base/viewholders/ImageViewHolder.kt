package co.thingthing.fleksyapps.base.viewholders

import co.thingthing.fleksyapps.base.BaseResult
import co.thingthing.fleksyapps.base.BaseViewHolder
import co.thingthing.fleksyapps.base.databinding.LayoutImageItemBinding
import co.thingthing.fleksyapps.base.utils.FrescoImageLoader
import co.thingthing.fleksyapps.base.utils.preferredImage

class ImageViewHolder(
    private val binding: LayoutImageItemBinding
) :
    BaseViewHolder<BaseResult>(binding.root) {
    private val frescoImageLoader: FrescoImageLoader by lazy { FrescoImageLoader() }

    override fun bind(viewModel: BaseResult) {
        super.bind(viewModel)

        (viewModel as? BaseResult.Image)?.also { vm ->
            (vm.thumbnail ?: vm.image).preferredImage(DEFAULT_CONTENT_TYPES)?.also { image ->
                frescoImageLoader.load(
                    binding.image,
                    vm.theme.background,
                    vm.theme.foreground,
                    image.width.toFloat(),
                    image.height.toFloat(),
                    image.url,
                    vm.placeholder?.preferredImage(
                        DEFAULT_CONTENT_TYPES
                    )?.url
                )
            }
        }
    }
}
