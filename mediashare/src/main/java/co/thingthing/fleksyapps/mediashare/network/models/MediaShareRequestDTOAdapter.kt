package co.thingthing.fleksyapps.mediashare.network.models

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

internal class MediaShareRequestDTOAdapter : JsonSerializer<MediaShareRequestDTO> {

    override fun serialize(
        src: MediaShareRequestDTO,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        val jsonObject = JsonObject().apply {
            addProperty("content", src.content.name)
            addProperty("userId", src.userId)
            addProperty("platform", src.platform)
            addProperty("adWidth", src.adWidth)
            addProperty("adHeight", src.adHeight)

            when (src.feature) {
                is MediaShareRequestDTO.Feature.Tags -> addProperty("feature", "tags")
                is MediaShareRequestDTO.Feature.Trending -> {
                    addProperty("feature", "trending")
                    addProperty("page", src.feature.page)
                }

                is MediaShareRequestDTO.Feature.Search -> {
                    addProperty("feature", "search")
                    val searchFeature = src.feature
                    addProperty("keyword", searchFeature.query)
                    addProperty("page", searchFeature.page)
                }
            }
        }
        return jsonObject
    }
}
