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
            addProperty("userAgent", src.userAgent)
            addProperty("deviceOperatingSystemVersion", src.deviceOperatingSystemVersion)
            addProperty("deviceHardwareVersion", src.deviceHardwareVersion)
            addProperty("deviceMake", src.deviceMake)
            addProperty("deviceModel", src.deviceModel)
            src.deviceIfa?.let { addProperty("deviceIfa", it) }

            val requiresAdsParameters: Boolean

            when (src.feature) {
                is MediaShareRequestDTO.Feature.HealthCheck -> {
                    addProperty("feature", "preFillAds")
                    requiresAdsParameters = true
                }
                is MediaShareRequestDTO.Feature.Tags -> {
                    addProperty("feature", "tags")
                    requiresAdsParameters = false
                }
                is MediaShareRequestDTO.Feature.Trending -> {
                    addProperty("feature", "trending")
                    addProperty("page", src.feature.page)
                    requiresAdsParameters = true
                }
                is MediaShareRequestDTO.Feature.Search -> {
                    addProperty("feature", "search")
                    val searchFeature = src.feature
                    addProperty("keyword", searchFeature.query)
                    addProperty("page", searchFeature.page)
                    requiresAdsParameters = true
                }
                is MediaShareRequestDTO.Feature.ViewTrigger -> {
                    addProperty("feature", "viewTrigger")
                    addProperty("contentId", src.feature.contentId)
                    requiresAdsParameters = false
                }
                is MediaShareRequestDTO.Feature.ShareTrigger -> {
                    addProperty("feature", "shareTrigger")
                    addProperty("contentId", src.feature.contentId)
                    requiresAdsParameters = false
                }
            }

            if (requiresAdsParameters) {
                addProperty("adMinWidth", src.adMinWidth)
                addProperty("adMaxWidth", src.adMaxWidth)
                addProperty("adMinHeight", src.adMinHeight)
                addProperty("adMaxHeight", src.adMaxHeight)
            }
        }
        return jsonObject
    }
}
