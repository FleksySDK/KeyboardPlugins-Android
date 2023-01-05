package co.thingthing.fleksyapps.giphy

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import co.thingthing.fleksyapps.base.*
import co.thingthing.fleksyapps.core.KeyboardAppViewMode
import co.thingthing.fleksyapps.giphy.models.GifResponse
import io.reactivex.Single
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class GiphyApp(
    apiKey: String,
    private val icon: Drawable? = null,
    private val rating: String = "g",
    private val baseUrl: String = "https://api.giphy.com/",
    private val dynamicCategories: Boolean = true,
    private val categories: List<CustomCategory>? = null,
    override val customTypefaces: Typefaces? = null
) : BaseKeyboardApp() {
    override val appId get() = "giphy"
    override val appName get() = "Giphy"
    override val keywords get() = listOf("gif")

    override val defaultMode: KeyboardAppViewMode
        get() = KeyboardAppViewMode.FULL_VIEW

    override val configuration by lazy {
        BaseConfiguration(listMode = ListMode.VariableSize(2), requestLimit = 20)
    }

    override val defaultCategory
        get() =
            categories?.firstOrNull()?.let { it.value ?: context?.getString(it.label) } ?: GIPHY_TRENDING

    override fun appIcon(context: Context) =
        icon ?: AppCompatResources.getDrawable(context, R.drawable.gif_icon)

    override fun default(pagination: Pagination): Single<List<BaseResult>> =
        service.trending(limit = pagination.limit, offset = pagination.offset, rating = rating).map {
            toResultsWithPoweredBy(toResults(it, GIPHY_TRENDING), pagination)
        }

    private fun toResultsWithPoweredBy(results: List<BaseResult>, pagination: Pagination) =
        if (pagination.page == 0) {
            listOf(poweredByGiphy) + results
        } else {
            results
        }

    override fun category(category: BaseCategory, pagination: Pagination): Single<List<BaseResult>> =
        search(category.value ?: category.label, pagination)

    override fun query(query: String, pagination: Pagination): Single<List<BaseResult>> =
        if (query.isNotEmpty()) search(query, pagination) else default(pagination)

    override fun categories(): Single<List<BaseCategory>> =
        customCategories ?: if (dynamicCategories) remoteCategories else localCategories

    private fun search(query: String, pagination: Pagination) =
        service.search(
            query = query,
            limit = pagination.limit,
            offset = pagination.offset,
            language = languageFor(locale),
            rating = rating
        ).map { toResults(it, query) }

    private fun toResults(response: GifResponse, sourceQuery: String?): List<BaseResult> =
        response.toBaseResults(theme, sourceQuery)

    private val poweredByGiphy: BaseResult by lazy {
        BaseResult.Image(
            image = POWERED_BY_GIPHY_MEDIA,
            link = GIPHY_URL,
            label = GIPHY_LABEL,
            theme = theme
        )
    }

    private val remoteCategories
        get() = service.categories().map { category ->
            listOf(BaseCategory(GIPHY_TRENDING, theme, typeface =  customTypefaces?.bold)) +
                category.toListOfLabels().map { label ->
                    BaseCategory(label, theme, typeface =  customTypefaces?.bold)
                }
        }.onErrorResumeNext(localCategories)

    private val localCategories
        get() = Single.just(
            FIXED_CATEGORIES.map { label -> BaseCategory(label, theme, typeface =  customTypefaces?.bold) }
        )

    private val customCategories: Single<List<BaseCategory>>?
        get() =
            categories?.let { categories ->
                context?.let { context ->
                    Single.just(
                        categories.map { category ->
                            val label = context.getString(category.label)
                            val value = category.value ?: label
                            BaseCategory(label, theme, value = value, typeface = customTypefaces?.bold)
                        }
                    )
                }
            }

    private val httpClient by lazy {
        OkHttpClient.Builder().apply {
            cache(BaseComponent.cache)
            addInterceptor(QueryParamInterceptor("api_key", apiKey))
        }.build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(BaseComponent.gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(httpClient)
            .build()
    }

    private val service by lazy { retrofit.create(GiphyService::class.java) }

    private fun languageFor(locale: String?) = (locale ?: "en-US").substringBefore("-")

    companion object {
        private const val GIPHY_LABEL = "Giphy"
        private const val GIPHY_TRENDING = "Trending"
        private const val GIPHY_URL = "https://giphy.com"

        val FIXED_CATEGORIES =
            listOf("Trending", "Happy", "Love", "Lol", "Okay", "Thanks", "Wow", "Hello", "Sad")

        val POWERED_BY_GIPHY_MEDIA =
            listOf(
                BaseMedia(
                    url = "https://s3-eu-west-1.amazonaws.com/tt-fk-static-content/Poweredby_100px_Badge.gif",
                    width = 100,
                    height = 140,
                    contentType = "image/gif"
                )
            )
    }
}
