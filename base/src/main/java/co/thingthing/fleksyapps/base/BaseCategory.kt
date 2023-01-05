package co.thingthing.fleksyapps.base

import android.graphics.Typeface
import co.thingthing.fleksyapps.core.AppTheme

data class BaseCategory(
    val label: String,
    var theme: AppTheme,
    val isTrending: Boolean = false,
    var selected: Boolean = false,
	val value: String? = null,
    val typeface: Typeface? = null
) {
    fun onThemeChanged(theme: AppTheme) {
        this.theme = theme
    }
}
