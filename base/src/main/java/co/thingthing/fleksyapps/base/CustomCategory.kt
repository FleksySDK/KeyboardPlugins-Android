package co.thingthing.fleksyapps.base

import androidx.annotation.Keep
import androidx.annotation.StringRes

@Keep
data class CustomCategory(
	@StringRes val label: Int,
	val value: String? = null
)
