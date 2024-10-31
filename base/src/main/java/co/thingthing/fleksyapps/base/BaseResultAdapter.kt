package co.thingthing.fleksyapps.base

import android.view.LayoutInflater
import android.view.ViewGroup
import co.thingthing.fleksyapps.base.databinding.LayoutCardItemBinding
import co.thingthing.fleksyapps.base.databinding.LayoutImageItemBinding
import co.thingthing.fleksyapps.base.databinding.LayoutVideoItemBinding
import co.thingthing.fleksyapps.base.databinding.LayoutVideoWithSoundItemBinding
import co.thingthing.fleksyapps.base.viewholders.CardViewHolder
import co.thingthing.fleksyapps.base.viewholders.ImageViewHolder
import co.thingthing.fleksyapps.base.viewholders.VideoViewHolder
import co.thingthing.fleksyapps.base.viewholders.VideoWithSoundViewHolder
import co.thingthing.fleksyapps.core.AppTheme


class BaseResultAdapter : BaseAdapter<BaseResult>() {

    override fun create(parent: ViewGroup, viewType: Int): BaseViewHolder<BaseResult> {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            IMAGE -> ImageViewHolder(
                binding = LayoutImageItemBinding.inflate(layoutInflater, parent, false)
            )

            VIDEO_WITH_SOUND -> VideoWithSoundViewHolder(
                binding = LayoutVideoWithSoundItemBinding.inflate(layoutInflater, parent, false),
                onMuteClicked = { item -> onMuteClicked(item = item) }
            )

            VIDEO -> VideoViewHolder(
                binding = LayoutVideoItemBinding.inflate(layoutInflater, parent, false),
            )

            else -> CardViewHolder(
                binding = LayoutCardItemBinding.inflate(layoutInflater, parent, false)
            )
        }
    }

    private fun onMuteClicked(item: BaseResult) {
        items
            .find { it is BaseResult.VideoWithSound && it.id != item.id && it.isMuted.not() }
            ?.let {
                (it as? BaseResult.VideoWithSound)?.mute()
                notifyItemChanged(items.indexOf(it))
            }
    }

    override fun getItemViewType(position: Int) =
        when (items[position]) {
            is BaseResult.Image -> IMAGE
            is BaseResult.VideoWithSound -> VIDEO_WITH_SOUND
            is BaseResult.Video -> VIDEO
            is BaseResult.Card -> CARD
        }

    override fun onViewRecycled(holder: BaseViewHolder<BaseResult>) {
        super.onViewRecycled(holder)
        holder.onRecycled()
    }

    fun onThemeChanged(theme: AppTheme) {
        items.forEach { it.onThemeChanged(theme) }
        notifyDataSetChanged()
    }

    companion object {
        const val IMAGE = 1
        const val VIDEO = 2
        const val VIDEO_WITH_SOUND = 3
        const val CARD = 4
    }


}

