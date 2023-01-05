package co.thingthing.fleksyapps.base

import android.view.ViewGroup
import co.thingthing.fleksyapps.base.viewholders.CardViewHolder
import co.thingthing.fleksyapps.base.viewholders.ImageViewHolder
import co.thingthing.fleksyapps.base.viewholders.VideoViewHolder
import co.thingthing.fleksyapps.core.AppTheme

class BaseResultAdapter : BaseAdapter<BaseResult>() {

    override fun create(parent: ViewGroup, viewType: Int): BaseViewHolder<BaseResult> =
        when (viewType) {
            IMAGE -> ImageViewHolder(parent)
            VIDEO -> VideoViewHolder(parent)
            else -> CardViewHolder(parent)
        }

    override fun getItemViewType(position: Int) =
        when (items[position]) {
            is BaseResult.Image -> IMAGE
            is BaseResult.Video -> VIDEO
            is BaseResult.Card -> CARD
        }

    fun onThemeChanged(theme: AppTheme) {
        items.forEach { it.onThemeChanged(theme) }
        notifyDataSetChanged()
    }

    companion object {
        const val IMAGE = 1
        const val VIDEO = 2
        const val CARD = 3
    }


}
