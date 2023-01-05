package co.thingthing.fleksyapps.base.utils

import co.thingthing.fleksyapps.base.BaseMedia

class ContentTypeComparator(private val contentTypes: List<String>) : Comparator<BaseMedia> {
    override fun compare(o1: BaseMedia, o2: BaseMedia): Int =
        when {
            contentTypes.contains(o1.contentType) && contentTypes.contains(o2.contentType) ->
                contentTypes.indexOf(o1.contentType) - contentTypes.indexOf(o2.contentType)
            contentTypes.contains(o1.contentType) -> -1
            contentTypes.contains(o2.contentType) -> 1
            else -> 0
        }
}
