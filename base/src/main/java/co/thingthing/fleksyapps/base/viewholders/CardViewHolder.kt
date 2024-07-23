package co.thingthing.fleksyapps.base.viewholders

import android.view.ViewGroup
import co.thingthing.fleksyapps.base.BaseResult
import co.thingthing.fleksyapps.base.BaseViewHolder
import co.thingthing.fleksyapps.base.databinding.LayoutCardItemBinding

class CardViewHolder(private val binding: LayoutCardItemBinding) :
    BaseViewHolder<BaseResult>(binding.root) {

    override fun bind(viewModel: BaseResult) {
        super.bind(viewModel)

        val cardView = (viewModel as BaseResult.Card).view
        (cardView.parent as? ViewGroup)?.removeView(cardView)

        binding.cardContainer.apply {
            removeAllViews()
            addView(cardView)
        }
    }
}
