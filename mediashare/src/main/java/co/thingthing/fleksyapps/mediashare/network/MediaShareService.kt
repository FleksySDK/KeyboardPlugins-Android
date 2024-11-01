package co.thingthing.fleksyapps.mediashare.network

import android.os.SystemClock
import co.thingthing.fleksyapps.mediashare.models.MediaShareResponse
import co.thingthing.fleksyapps.mediashare.models.PopularTagsResponse
import co.thingthing.fleksyapps.mediashare.models.SimpleResultResponse
import co.thingthing.fleksyapps.mediashare.network.models.MediaShareRequestDTO
import io.reactivex.Single
import java.util.concurrent.TimeUnit.MINUTES

internal class MediaShareService(
    private val contentType: MediaShareRequestDTO.ContentType,
    private val mediaShareApiKey: String,
    private val sdkLicenseId: String,
    private val userId: String
) {

    private var lastRequestTime: Long = 0

    companion object {
        val HEALTH_CHECK_MIN_WAIT_TIME = MINUTES.toMillis(10)
    }

    init {
        performHealthCheckRequestIfNeeded()
    }

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

    enum class ImpressionType {
        VIEW,
        SHARE
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

    fun sendImpression(
        type: ImpressionType,
        content: MediaShareResponse.Content
    ): Single<SimpleResultResponse> {
        val feature = when (type) {
            ImpressionType.VIEW -> MediaShareRequestDTO.Feature.ViewTrigger(contentId = content.id)
            ImpressionType.SHARE -> MediaShareRequestDTO.Feature.ShareTrigger(contentId = content.id)
        }

        val requestDTO = MediaShareRequestDTO(
            contentType, feature, userId
        )

        return service.sendImpression(getHeadersMap(), requestDTO)
    }

    private fun performHealthCheckRequestIfNeeded() {
        val currentTime = SystemClock.elapsedRealtime()

        if (currentTime - lastRequestTime >= HEALTH_CHECK_MIN_WAIT_TIME) {
            lastRequestTime = currentTime
            val requestDTO = MediaShareRequestDTO(
                contentType,
                MediaShareRequestDTO.Feature.HealthCheck,
                userId
            )
            service.getHealthCheck(getHeadersMap(), requestDTO)
        } else {
            // Too soon to make a new health check request
        }
    }

    private fun getHeadersMap(): Map<String, String> = mapOf(
        "x-api-key" to mediaShareApiKey,
        "capability" to contentType.requiredCapability,
        "licenseId" to sdkLicenseId,
        "Content-Type" to "application/json",
        "Accept" to "application/json",
    )
}