package co.thingthing.fleksyapps.mediashare

import co.thingthing.fleksyapps.base.BaseConfiguration
import co.thingthing.fleksyapps.base.ListMode

/**
 * An object class holding BaseConfiguration for MediaShareApp
 */
object MediaShareConfiguration {
    /**
     * The number of rows for MediaShareApp: the app should show one row (instead of 2)
     */
    private const val MEDIA_SHARE_ROWS_NUMBER = 1
    private const val MEDIA_SHARE_REQUEST_LIMIT = 20

    fun get() = BaseConfiguration(
        listMode = ListMode.VariableSize(MEDIA_SHARE_ROWS_NUMBER),
        requestLimit = MEDIA_SHARE_REQUEST_LIMIT,
    )
}