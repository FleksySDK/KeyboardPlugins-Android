package co.thingthing.fleksyapps.base

import androidx.annotation.IntRange
import androidx.annotation.StringRes

open class BaseConfiguration(
    val listMode: ListMode = ListMode.FixedSize,
    @IntRange(from = 0, to = 255) val hintAlpha: Int = 100,
    @StringRes val searchHint: Int = R.string.search_hint,
    @StringRes val searchAppHint: Int = R.string.search_app_hint,
    val requestLimit: Int = 20
)

sealed class ListMode {
    data class VariableSize(val rows: Int = 1) : ListMode()
    object FixedSize : ListMode()
}
