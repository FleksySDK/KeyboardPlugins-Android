package co.thingthing.fleksyapps.base.viewholders

import android.view.ViewGroup
import co.thingthing.fleksyapps.base.BaseResult
import co.thingthing.fleksyapps.base.BaseViewHolder
import co.thingthing.fleksyapps.base.R
import co.thingthing.fleksyapps.base.utils.FrescoImageLoader
import co.thingthing.fleksyapps.base.utils.preferredImage
import kotlinx.android.synthetic.main.layout_image_item.view.*

class ImageViewHolder(parent: ViewGroup) :
    BaseViewHolder<BaseResult>(parent, R.layout.layout_image_item) {
    private val frescoImageLoader: FrescoImageLoader by lazy { FrescoImageLoader() }

    override fun bind(viewModel: BaseResult) {
        super.bind(viewModel)

        (viewModel as? BaseResult.Image)?.also { vm ->
            val contentTypes = listOf("image/webp", "video/mp4", "image/gif", "image/jpeg")
            (vm.thumbnail ?: vm.image).preferredImage(contentTypes)?.also { image ->
                frescoImageLoader.load(
                    itemView.image,
                    vm.theme.background,
                    vm.theme.foreground,
                    image.width.toFloat(),
                    image.height.toFloat(),
                    image.url,
                    vm.placeholder?.preferredImage(
                        contentTypes
                    )?.url
                )
            }
        }
    }
}
