package co.thingthing.fleksyapps.giphy.models

data class CategoriesResponse(val data: List<Category>) {
    data class Category(val name: String)

    fun toListOfLabels() = data.map { it.name }
}
