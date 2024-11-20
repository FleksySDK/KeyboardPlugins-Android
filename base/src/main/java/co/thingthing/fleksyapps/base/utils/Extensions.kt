package co.thingthing.fleksyapps.base.utils

import android.content.Context
import android.content.res.Resources
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
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

fun View.hide() {
    visibility = View.GONE
}

fun View.show() {
    visibility = View.VISIBLE
}

/**
 * Converts a pixel value to density-independent pixels (dp).
 *
 * @return The equivalent value in dp as an integer.
 */
fun Int.pxToDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()

/**
 * Iterates through all visible ViewHolders in the RecyclerView and performs the specified action on each.
 *
 * @param action A lambda function to be invoked for each ViewHolder. The ViewHolder is passed as the receiver.
 */
fun RecyclerView.forEachViewHolder(action: RecyclerView.ViewHolder.() -> Unit) {
    for(i in 0 until childCount) {
        val view = getChildAt(i)
        val holder = getChildViewHolder(view)
        action(holder)
    }
}