package co.thingthing.fleksyapps.mediashare.network

import co.thingthing.fleksyapps.base.BaseComponent
import co.thingthing.fleksyapps.mediashare.models.SimpleResultResponse
import co.thingthing.fleksyapps.mediashare.models.MediaShareResponse
import co.thingthing.fleksyapps.mediashare.models.PopularTagsResponse
import co.thingthing.fleksyapps.mediashare.network.models.MediaShareRequestDTO
import co.thingthing.fleksyapps.mediashare.network.models.MediaShareRequestDTOAdapter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.reactivex.Single
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

internal interface MediaShareApi {

    @POST("routing")
    fun getContent(
        @HeaderMap headers: Map<String, String>,
        @Body request: MediaShareRequestDTO
    ): Single<MediaShareResponse>

    @POST("routing")
    fun getPopularTags(
        @HeaderMap headers: Map<String, String>,
        @Body request: MediaShareRequestDTO
    ): Single<PopularTagsResponse>

    @POST("routing")
    fun getHealthCheck(
        @HeaderMap headers: Map<String, String>,
        @Body request: MediaShareRequestDTO
    ): Single<SimpleResultResponse>

    companion object {
        private const val BASE_URL =
            "https://api.thingthing.co/plugins/api/v1/"

        private fun getClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .cache(BaseComponent.cache)
                .build()
        }

        internal fun create(): MediaShareApi {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(getClient())
                .build()

            return retrofit.create(MediaShareApi::class.java)
        }

        private val gson: Gson = GsonBuilder().registerTypeAdapter(
            MediaShareRequestDTO::class.java,
            MediaShareRequestDTOAdapter()
        ).create()
    }
}
