package co.thingthing.fleksyapps.mediashare.network

import co.thingthing.fleksyapps.mediashare.models.SimpleResultResponse
import co.thingthing.fleksyapps.mediashare.models.MediaShareResponse
import co.thingthing.fleksyapps.mediashare.models.PopularTagsResponse
import co.thingthing.fleksyapps.mediashare.network.models.MediaShareRequestDTO
import io.reactivex.Single

internal class MediaShareService(
    private val contentType: MediaShareRequestDTO.ContentType,
    private val mediaShareApiKey: String,
    private val sdkLicenseId: String,
    private val userId: String
) {

    sealed class Content(val page: Int) {
        /**
         * Trending content.
         * @param page: the requested page number. Minimum value 1.
         */
        class Trending(page: Int) : Content(page)

        /**
         * Search content.
         * @param query: The search query for finding relevant content.
         * @param page: the requested page number. Minimum value 1.
         */
        class Search(val query: String, page: Int) : Content(page)
    }

    private val service by lazy { MediaShareApi.create() }

    fun getContent(
        content: Content
    ): Single<MediaShareResponse> {
        performHealthCheckRequestIfNeeded()

        val feature = when (content) {
            is Content.Trending -> MediaShareRequestDTO.Feature.Trending(page = content.page+1)
            is Content.Search -> MediaShareRequestDTO.Feature.Search(
                page = content.page+1,
                query = content.query
            )
        }

        val requestDTO = MediaShareRequestDTO(
            contentType, feature, userId
        )

        return service.getContent(getHeadersMap(), requestDTO)
    }

    fun getTags(userId: String): Single<PopularTagsResponse> {
        performHealthCheckRequestIfNeeded()

        val requestDTO = MediaShareRequestDTO(
            contentType,
            MediaShareRequestDTO.Feature.Tags,
            userId
        )

        return service.getPopularTags(getHeadersMap(), requestDTO)
    }

    private fun performHealthCheckRequestIfNeeded(): Single<SimpleResultResponse> {
        val requestDTO = MediaShareRequestDTO(
            contentType,
            MediaShareRequestDTO.Feature.HealthCheck,
            userId
        )

        return service.getHealthCheck(getHeadersMap(), requestDTO)
    }

    private fun getHeadersMap(): Map<String, String> = mapOf(
        "x-api-key" to mediaShareApiKey,
        "capability" to contentType.requiredCapability,
        "licenseId" to sdkLicenseId,
        "Content-Type" to "application/json",
        "Accept" to "application/json",
    )
}