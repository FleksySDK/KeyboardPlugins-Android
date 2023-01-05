package co.thingthing.fleksyapps.base

import android.view.ViewGroup
import co.thingthing.fleksyapps.base.viewholders.CategoryViewHolder
import co.thingthing.fleksyapps.core.AppTheme

class BaseCategoryAdapter : BaseAdapter<BaseCategory>() {

    override fun create(parent: ViewGroup, viewType: Int): BaseViewHolder<BaseCategory> =
        CategoryViewHolder(parent)

    fun onItemSelected(category: BaseCategory) {
        val oldSelected = items.indexOfFirst { it.selected }
        val newSelected = items.indexOf(category)

        if (oldSelected >= 0) {
            items[oldSelected].selected = false
            notifyItemChanged(oldSelected)
        }

        if (newSelected >= 0) {
            items[newSelected].selected = true
            notifyItemChanged(newSelected)
        }
    }

    fun onItemSelected(category: String) {
        items.firstOrNull { (it.value ?: it.label) == category }?.also {
            onItemSelected(it)
        }
    }

    fun onThemeChanged(theme: AppTheme) {
        items.forEach { it.onThemeChanged(theme) }
        notifyDataSetChanged()
    }

    fun clearSelected() {
        items.indexOfFirst { it.selected }.also {
            if (it >= 0) {
                items[it].selected = false
                notifyItemChanged(it)
            }
        }
    }
}
