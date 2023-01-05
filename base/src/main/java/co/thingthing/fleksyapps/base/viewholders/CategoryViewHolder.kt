package co.thingthing.fleksyapps.base.viewholders

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.ViewGroup
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import co.thingthing.fleksyapps.base.BaseCategory
import co.thingthing.fleksyapps.base.BaseViewHolder
import co.thingthing.fleksyapps.base.R
import kotlinx.android.synthetic.main.layout_category_item.view.categoryName

class CategoryViewHolder(parent: ViewGroup) :
    BaseViewHolder<BaseCategory>(parent, R.layout.layout_category_item) {

    override fun bind(viewModel: BaseCategory) {
        super.bind(viewModel)
        itemView.categoryName.apply {
            text = viewModel.label
            setTextColor(viewModel.theme.foreground)
            typeface = viewModel.typeface
            (background as? GradientDrawable)?.setColor(backgroundColor(viewModel))
        }
    }

    private fun backgroundColor(viewModel: BaseCategory) =
        if (viewModel.selected)
            viewModel.theme.foreground.let { Color.argb(40, it.red, it.green, it.blue) }
        else
            Color.TRANSPARENT


}
