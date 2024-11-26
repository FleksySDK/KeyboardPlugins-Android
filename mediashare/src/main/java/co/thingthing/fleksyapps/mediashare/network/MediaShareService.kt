package co.thingthing.fleksyapps.mediashare.network

import android.annotation.SuppressLint
import android.util.Log
import co.thingthing.fleksyapps.mediashare.models.MediaShareResponse
import co.thingthing.fleksyapps.mediashare.models.PopularTagsResponse
import co.thingthing.fleksyapps.mediashare.network.models.MediaShareRequestDTO
import co.thingthing.fleksyapps.mediashare.network.models.MediaShareRequestDTO.Companion.ALL_SIZES_ADS_HEIGHT
import co.thingthing.fleksyapps.mediashare.utils.DeviceInfoProvider
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit.MINUTES

internal class MediaShareService(
    private val contentType: MediaShareRequestDTO.ContentType,
    private val deviceInfoProvider: DeviceInfoProvider,
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

    private fun createMediaShareRequest(
        feature: MediaShareRequestDTO.Feature,
        adMaxHeight: Int = ALL_SIZES_ADS_HEIGHT,
    ) = Single.create { emitter ->
            try {
                val request = MediaShareRequestDTO(
                    content = contentType,
                    feature = feature,
                    userId = userId,
                    userAgent = userAgent,
                    adMaxHeight = adMaxHeight,
                    deviceOperatingSystemVersion = deviceInfoProvider.operatingSystemVersion,
                    deviceHardwareVersion = deviceInfoProvider.hardwareVersion,
                    deviceMake = deviceInfoProvider.deviceMake,
                    deviceModel = deviceInfoProvider.deviceModel,
                    deviceIfa = deviceInfoProvider.loadDeviceIfa(),
                )
                if (emitter.isDisposed.not()) {
                    emitter.onSuccess(request)
                }
            } catch (e: Exception) {
                if (emitter.isDisposed.not()) {
                    emitter.onError(e)
                }
            }
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

        return createMediaShareRequest(feature = feature, adMaxHeight = adMaxHeight)
            .flatMap { requestDTO ->
                service.getContent(getHeadersMap(), requestDTO)
            }
    }

    fun getTags(
        adMaxHeight: Int,
    ): Single<PopularTagsResponse> {
        performHealthCheckRequestIfNeeded()

        return createMediaShareRequest(feature = MediaShareRequestDTO.Feature.Tags, adMaxHeight = adMaxHeight)
            .flatMap { requestDTO ->
                service.getPopularTags(getHeadersMap(), requestDTO)
            }
    }

    @SuppressLint("CheckResult")
    fun sendImpression(
        contentId: String,
        type: ImpressionType,
    ) {
        val feature = when (type) {
            ImpressionType.VIEW -> MediaShareRequestDTO.Feature.ViewTrigger(contentId = contentId)
            ImpressionType.SHARE -> MediaShareRequestDTO.Feature.ShareTrigger(contentId = contentId)
        }

        createMediaShareRequest(feature = feature)
            .flatMap { requestDTO ->
                service.sendImpression(getHeadersMap(), requestDTO)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ /* ignore result */ }, { Log.e("Fleksy", "Error sending impression", it) })
    }

    @SuppressLint("CheckResult")
    private fun performHealthCheckRequestIfNeeded() {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastRequestTime >= HEALTH_CHECK_MIN_WAIT_TIME) {
            lastRequestTime = currentTime

            createMediaShareRequest(feature = MediaShareRequestDTO.Feature.HealthCheck)
                .flatMap { requestDTO ->
                    service.getHealthCheck(getHeadersMap(), requestDTO)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ /* ignore result */ },  { Log.e("Fleksy", "Error performing healthCheck", it) })
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