package co.thingthing.fleksyapps.mediashare.network.models

internal data class MediaShareRequestDTO(
    val content: ContentType,
    val feature: Feature,
    val userId: String,
    val platform: String = "android",
    val adMinWidth: Int = 50,
    val adMaxWidth: Int = 320,
    val adMinHeight: Int = 50,
    val adMaxHeight: Int = ALL_SIZES_ADS_HEIGHT,
    val userAgent: String = ""
) {

    companion object {
        /**
         * Maximum ads height
         * With this value, all possible ad sizes will be preloaded.
         */
        const val ALL_SIZES_ADS_HEIGHT = 320
    }

    enum class ContentType(val requiredCapability: String) {
        Clips("fleksyapp_clips"),

        Gifs("fleksyapp_gifs"),

        Stickers("fleksyapp_stickers")
    }

    sealed class Feature {
        data object Tags : Feature()
        data object HealthCheck : Feature()

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

        /**
         * Content displayed to the user.
         * @param contentId: The ID of the content displayed to the user.
         */
        data class ViewTrigger(val contentId: String) : Feature()

        /**
         * Content selected by the user for sharing.
         * @param contentId: The ID of the content displayed to the user.
         */
        data class ShareTrigger(val contentId: String) : Feature()
    }
}