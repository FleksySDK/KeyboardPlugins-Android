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

/**
 * Method finds a range of visible item's positions and casts it to Set<Int>
 *
 * @return the Set<Int> of positions of the elements that are visible on the screen
 */
fun LayoutManager.getVisibleItemPositions(): Set<Int> {
    var firstVisibleItemPosition = 0
    var lastVisibleItemPosition = 0
    when (this) {
        is LinearLayoutManager -> {
            firstVisibleItemPosition = findFirstVisibleItemPosition()
            lastVisibleItemPosition = findLastVisibleItemPosition()
        }
        is StaggeredGridLayoutManager -> {
            firstVisibleItemPosition = findFirstVisibleItemPositions(null).minOrNull() ?: 0
            lastVisibleItemPosition = findLastVisibleItemPositions(null).maxOrNull() ?: 0
        }
    }
    return (firstVisibleItemPosition..lastVisibleItemPosition).toSet()
}

/**
 * Attaches a scroll listener to the RecyclerView that triggers a specified action
 * whenever the RecyclerView is scrolled.
 *
 * @param action The action to perform on each scroll event.
 */
fun RecyclerView.onScrolledListener(action: () -> Unit) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            action()
        }
    })
}

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