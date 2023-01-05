package co.thingthing.fleksyapps.base

data class Pagination(
    var page: Int = 0,
    var offset: Int = 0,
    val limit: Int = 20
)
