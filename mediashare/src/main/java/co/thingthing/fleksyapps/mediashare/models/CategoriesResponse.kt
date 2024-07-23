package co.thingthing.fleksyapps.mediashare.models

data class CategoriesResponse(val data: List<Category>) {
    data class Category(val name: String)

    fun toListOfLabels() = data.map { it.name }
}
