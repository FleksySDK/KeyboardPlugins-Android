package co.thingthing.fleksyapps.mediashare.models

import android.graphics.Typeface
import co.thingthing.fleksyapps.base.BaseCategory
import co.thingthing.fleksyapps.core.AppTheme

data class PopularTagsResponse(
    val tags: List<String>?
)

internal fun PopularTagsResponse.toCategories(
    appTheme: AppTheme,
    typeface: Typeface?
): List<BaseCategory> =
    if (tags.isNullOrEmpty()) listOf()
    else {
        val categories = tags.map { BaseCategory(label = it, appTheme, typeface = typeface) }
        listOf(BaseCategory("Trending", appTheme, typeface = typeface)) + categories
    }