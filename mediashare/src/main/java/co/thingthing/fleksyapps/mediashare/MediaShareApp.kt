package co.thingthing.fleksyapps.mediashare

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.provider.Settings
import androidx.appcompat.content.res.AppCompatResources
import co.thingthing.fleksyapps.base.BaseCategory
import co.thingthing.fleksyapps.base.BaseKeyboardApp
import co.thingthing.fleksyapps.base.BaseResult
import co.thingthing.fleksyapps.base.CustomCategory
import co.thingthing.fleksyapps.base.Pagination
import co.thingthing.fleksyapps.base.Typefaces
import co.thingthing.fleksyapps.core.KeyboardAppViewMode
import co.thingthing.fleksyapps.mediashare.models.MediaShareResponse
import co.thingthing.fleksyapps.mediashare.models.toCategories
import co.thingthing.fleksyapps.mediashare.network.MediaShareService
import co.thingthing.fleksyapps.mediashare.network.toNetworkContentType
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


@SuppressLint("HardwareIds")
class MediaShareApp(
    private val contentType: ContentType,
    private val apiKey: String,
    private val sdkLicenseKey: String,
    private val icon: Drawable? = null,
    private val categories: List<CustomCategory>? = null,
    override val customTypefaces: Typefaces? = null
) : BaseKeyboardApp() {

    /**
     * The types of content that the MediaShare app supports.
     */
    enum class ContentType {

        /**
         * Keyboard app for sharing clips. Requires using a FleksySDK license that includes the `fleksyapp_clips` capability.
         */
        CLIPS,

        /**
         * Keyboard app for sharing gifs. Requires using a FleksySDK license that includes the `fleksyapp_gifs` capability.
         */
        GIFS,

        /**
         * Keyboard app for sharing stickers. Requires using a FleksySDK license that includes the `fleksyapp_stickers` capability.
         */
        STICKERS
    }

    override val appId: String
        get() {
            val appIdSuffix = when (contentType) {
                ContentType.CLIPS -> "clips"
                ContentType.GIFS -> "gifs"
                ContentType.STICKERS -> "stickers"
            }
            return "com.fleksy.app.MediaShare.$appIdSuffix"
        }

    override val appName: String
        get() {
            val appIdSuffix = when (contentType) {
                ContentType.CLIPS -> "Clips"
                ContentType.GIFS -> "Gifs"
                ContentType.STICKERS -> "Stickers"
            }
            return "MediaShare $appIdSuffix"
        }
    override val keywords: List<String>
        get() {
            val keyword = when (contentType) {
                ContentType.CLIPS -> "clips"
                ContentType.GIFS -> "gifs"
                ContentType.STICKERS -> "stickers"
            }

            return listOf("media", keyword)
        }

    override val defaultMode: KeyboardAppViewMode
        get() = KeyboardAppViewMode.FullView

    override val configuration by lazy { MediaShareConfiguration.get() }

    override val defaultCategory
        get() =
            categories?.firstOrNull()?.let { it.value ?: context?.getString(it.label) }
                ?: MEDIA_SHARE_TRENDING

    override fun appIcon(context: Context) =
        icon ?: when(contentType){
            ContentType.CLIPS -> AppCompatResources.getDrawable(context, R.drawable.icon_clips)
            ContentType.GIFS -> AppCompatResources.getDrawable(context, R.drawable.icon_gif)
            ContentType.STICKERS -> AppCompatResources.getDrawable(context, R.drawable.icon_stickers)
        }



    private val androidId: String by lazy {
        context?.let {
            Settings.Secure.getString(it.contentResolver, Settings.Secure.ANDROID_ID)
        } ?: ""
    }

    override fun default(pagination: Pagination): Single<List<BaseResult>> =
        service.getContent(
            MediaShareService.Content.Trending(pagination.page)
        )
            .subscribeOn(Schedulers.io()) // Ensure initial work is done on IO thread
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                context?.let { toResults(it, response) }
            }


    override fun category(
        category: BaseCategory,
        pagination: Pagination
    ): Single<List<BaseResult>> =
        search(category.value ?: category.label, pagination)

    override fun query(query: String, pagination: Pagination): Single<List<BaseResult>> =
        if (query.isNotEmpty()) search(query, pagination) else default(pagination)

    override fun categories(): Single<List<BaseCategory>> =
        customCategories ?: remoteCategories

    private fun search(query: String, pagination: Pagination): Single<List<BaseResult>> =
        service.getContent(
            MediaShareService.Content.Search(query = query, pagination.page)
        )
            .subscribeOn(Schedulers.io()) // Ensure initial work is done on IO thread
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                context?.let { toResults(it, response, query) }
            }

    private fun toResults(
        context: Context,
        response: MediaShareResponse,
        sourceQuery: String? = null
    ): List<BaseResult> =
        response.toResults(context, theme, contentType, sourceQuery)

    private val remoteCategories
        get() = service.getTags(androidId)
            .map { category ->
                category.toCategories(appTheme = theme, typeface = customTypefaces?.bold)
            }.onErrorResumeNext(localCategories)


    private val localCategories
        get() = Single.just(
            FIXED_CATEGORIES.map { label ->
                BaseCategory(
                    label,
                    theme,
                    typeface = customTypefaces?.bold
                )
            }
        )

    private val customCategories: Single<List<BaseCategory>>?
        get() =
            categories?.let { categories ->
                context?.let { context ->
                    Single.just(
                        categories.map { category ->
                            val label = context.getString(category.label)
                            val value = category.value ?: label
                            BaseCategory(
                                label,
                                theme,
                                value = value,
                                typeface = customTypefaces?.bold
                            )
                        }
                    )
                }
            }

    private val service by lazy {
        MediaShareService(
            contentType = contentType.toNetworkContentType(),
            mediaShareApiKey = apiKey,
            sdkLicenseId = sdkLicenseKey,
            userId = androidId
        )
    }

    companion object {
        private const val MEDIA_SHARE_TRENDING = "Trending"

        val FIXED_CATEGORIES =
            listOf("Trending", "Happy", "Love", "Lol", "Okay", "Thanks", "Wow", "Hello", "Sad")
    }
}
