package co.thingthing.fleksyapps.mediashare.utils

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

/**
 * Method finds a range of visible item's positions and casts it to Set<Int>
 *
 * @return the Set<Int> of positions of the elements that are visible on the screen
 */
fun RecyclerView.LayoutManager.getVisibleItemPositions(): Set<Int> {
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