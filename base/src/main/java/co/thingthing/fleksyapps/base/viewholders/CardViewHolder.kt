package co.thingthing.fleksyapps.base.viewholders

import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout.LayoutParams
import androidx.core.view.updateLayoutParams
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
            cardView.updateLayoutParams<LayoutParams> {
                /**
                 * If the ad’s final height is smaller than the element’s available height, then the ad is displayed centered vertically
                 */
                gravity = Gravity.CENTER_VERTICAL
            }
        }
    }
}
