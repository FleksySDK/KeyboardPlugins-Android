package co.thingthing.fleksyapps.base.viewholders

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import co.thingthing.fleksyapps.base.BaseCategory
import co.thingthing.fleksyapps.base.BaseViewHolder
import co.thingthing.fleksyapps.base.databinding.LayoutCategoryItemBinding

class CategoryViewHolder(
    private val binding: LayoutCategoryItemBinding
) :
    BaseViewHolder<BaseCategory>(binding.root) {


    override fun bind(viewModel: BaseCategory) {
        super.bind(viewModel)
        binding.categoryName.apply {
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
