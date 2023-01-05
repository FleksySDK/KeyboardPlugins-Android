package co.thingthing.fleksyapps.giphy.models

import co.thingthing.fleksyapps.base.BaseMedia
import co.thingthing.fleksyapps.base.BaseResult
import co.thingthing.fleksyapps.core.AppTheme
import com.google.gson.annotations.SerializedName

data class GifResponse(
    val data: List<Gif>,
    val pagination: Pagination,
    val meta: Meta
) {
    fun toBaseResults(theme: AppTheme, sourceQuery: String? = null) =
        data.mapNotNull { it.toBaseResult(theme, sourceQuery) }

    data class Gif(
        val type: String,
        val id: String,
        val url: String,
        val rating: String,
        val images: Images,
        val title: String
    ) {
        fun toBaseResult(theme: AppTheme, sourceQuery: String? = null) =
            images.original.toBaseMedias()?.let {
                BaseResult.Image(
                    source = this,
                    image = it,
                    thumbnail = images.fixedHeightSmall.toBaseMedias(),
                    placeholder = images.fixedHeightSmallStill.toBaseMedias(),
                    link = url,
                    label = title,
                    theme = theme,
                    sourceQuery = sourceQuery
                )
            }

        data class Images(
            @SerializedName("original") val original: Image,
            @SerializedName("fixed_height_small") val fixedHeightSmall: Image,
            @SerializedName("fixed_height_small_still") val fixedHeightSmallStill: Image
        )

        data class Image(
            val url: String?,
            val width: String?,
            val height: String?,
            val webp: String?,
            val mp4: String?
        ) {
            fun toBaseMedias(): List<BaseMedia>? {
                val width = width?.toIntOrNull() ?: return null
                val height = height?.toIntOrNull() ?: return null

                return mutableListOf<BaseMedia>().apply {
                    webp?.also { add(BaseMedia(webp, width, height, "image/webp")) }
                    mp4?.also { add(BaseMedia(mp4, width, height, "video/mp4")) }
                    url?.also { add(BaseMedia(url, width, height, "image/gif")) }
                }
            }
        }
    }

    data class Pagination(
        val offset: Int,
        @SerializedName("total_count") val totalCount: Int,
        val count: Int
    )

    data class Meta(
        val msg: String,
        val status: Int,
        @SerializedName("response_id") val responseId: String
    )
}
