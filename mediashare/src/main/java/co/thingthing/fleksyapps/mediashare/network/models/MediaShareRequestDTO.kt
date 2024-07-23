package co.thingthing.fleksyapps.mediashare.network.models

internal data class MediaShareRequestDTO(
    val content: ContentType,
    val feature: Feature,
    val userId: String,
    val platform: String = "android",
    val adWidth: Int = 100,
    val adHeight: Int = 100
) {

    enum class ContentType(val requiredCapability: String) {
        Clips("fleksyapp_clips"),

        Gifs("fleksyapp_gifs"),

        Stickers("fleksyapp_stickers")
    }

    sealed class Feature {
        object Tags : Feature()

        /**
         * Trending content.
         *  @param page: the requested page number. Minimum value 1.
         */
        data class Trending(val page: Int) : Feature()

        /**
         * Search content.
         * @param page: the requested page number. Minimum value 1.
         * @param query: The query String  for finding relevant content.
         */
        data class Search(val page: Int, val query: String) : Feature()
    }
}