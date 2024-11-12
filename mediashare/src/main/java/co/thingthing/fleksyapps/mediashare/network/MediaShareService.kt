package co.thingthing.fleksyapps.mediashare.network

import android.os.SystemClock
import co.thingthing.fleksyapps.mediashare.models.MediaShareResponse
import co.thingthing.fleksyapps.mediashare.models.PopularTagsResponse
import co.thingthing.fleksyapps.mediashare.models.SimpleResultResponse
import co.thingthing.fleksyapps.mediashare.network.models.MediaShareRequestDTO
import co.thingthing.fleksyapps.mediashare.network.models.MediaShareRequestDTO.Companion.ALL_SIZES_ADS_HEIGHT
import io.reactivex.Single
import java.util.concurrent.TimeUnit.MINUTES

internal class MediaShareService(
    private val contentType: MediaShareRequestDTO.ContentType,
    private val mediaShareApiKey: String,
    private val sdkLicenseId: String,
    private val userAgent: String,
    private val userId: String,
) {

    private val service by lazy { MediaShareApi.create() }
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

    fun getContent(
        content: Content,
        adMaxHeight: Int,
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
            content = contentType,
            feature = feature,
            userId = userId,
            userAgent = userAgent,
            adMaxHeight = adMaxHeight,
        )

        return service.getContent(getHeadersMap(), requestDTO)
    }

    fun getTags(
        userId: String,
        adMaxHeight: Int,
    ): Single<PopularTagsResponse> {
        performHealthCheckRequestIfNeeded()

        val requestDTO = MediaShareRequestDTO(
            content = contentType,
            feature = MediaShareRequestDTO.Feature.Tags,
            userId = userId,
            userAgent = userAgent,
            adMaxHeight = adMaxHeight,
        )

        return service.getPopularTags(getHeadersMap(), requestDTO)
    }

    fun sendImpression(
        contentId: String,
        type: ImpressionType,
    ): Single<SimpleResultResponse> {
        val feature = when (type) {
            ImpressionType.VIEW -> MediaShareRequestDTO.Feature.ViewTrigger(contentId = contentId)
            ImpressionType.SHARE -> MediaShareRequestDTO.Feature.ShareTrigger(contentId = contentId)
        }

        val requestDTO = MediaShareRequestDTO(
            content = contentType,
            feature = feature,
            userAgent = userAgent,
            userId = userId,
        )

        return service.sendImpression(getHeadersMap(), requestDTO)
    }

    private fun performHealthCheckRequestIfNeeded() {
        val currentTime = SystemClock.elapsedRealtime()

        if (currentTime - lastRequestTime >= HEALTH_CHECK_MIN_WAIT_TIME) {
            lastRequestTime = currentTime
            val requestDTO = MediaShareRequestDTO(
                content = contentType,
                feature = MediaShareRequestDTO.Feature.HealthCheck,
                userAgent = userAgent,
                userId = userId,
                adMaxHeight = ALL_SIZES_ADS_HEIGHT
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