package co.thingthing.fleksyapps.base.viewholders

import android.view.ViewGroup
import co.thingthing.fleksyapps.base.BaseResult
import co.thingthing.fleksyapps.base.BaseViewHolder
import co.thingthing.fleksyapps.base.R
import kotlinx.android.synthetic.main.layout_card_item.view.*

class CardViewHolder(parent: ViewGroup) :
    BaseViewHolder<BaseResult>(parent, R.layout.layout_card_item) {

    override fun bind(viewModel: BaseResult) {
        super.bind(viewModel)

        val cardView = (viewModel as BaseResult.Card).view
        (cardView.parent as? ViewGroup)?.removeView(cardView)

        itemView.cardContainer.apply {
            removeAllViews()
            addView(cardView)
        }
    }
}
