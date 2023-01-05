package co.thingthing.fleksyapps.base.utils

import android.content.Context
import co.thingthing.fleksyapps.base.BaseMedia
import java.util.UUID

// region String
fun String.Companion.empty() = ""
// endregion

// region List<BaseMedia>
fun List<BaseMedia>.preferredImage(contentTypes: List<String>) =
    sortedWith(ContentTypeComparator(contentTypes)).firstOrNull()
// endregion

// region Context
@Synchronized
fun Context.getInstallationUniqueId(): String {
    val name = "fleksyAppsSP"
    val key = "uniqueId"
    val sharedPrefs = getSharedPreferences(name, Context.MODE_PRIVATE)
    var uniqueID = sharedPrefs.getString(key, String.empty()) ?: String.empty()
    if (uniqueID.isEmpty()) {
        uniqueID = UUID.randomUUID().toString()
        sharedPrefs.edit().putString(key, uniqueID).apply()
    }
    return uniqueID
}
// endregion
