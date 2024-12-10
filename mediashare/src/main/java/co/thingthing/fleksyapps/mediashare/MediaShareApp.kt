package co.thingthing.fleksyapps.mediashare

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.provider.Settings
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import co.thingthing.fleksyapps.base.BaseCategory
import co.thingthing.fleksyapps.base.BaseKeyboardApp
import co.thingthing.fleksyapps.base.BaseResult
import co.thingthing.fleksyapps.base.BaseResultAdapter
import co.thingthing.fleksyapps.base.CustomCategory
import co.thingthing.fleksyapps.base.Pagination
import co.thingthing.fleksyapps.base.Typefaces
import co.thingthing.fleksyapps.core.KeyboardAppViewMode
import co.thingthing.fleksyapps.mediashare.models.MediaShareResponse
import co.thingthing.fleksyapps.mediashare.models.toCategories
import co.thingthing.fleksyapps.mediashare.network.MediaShareService
import co.thingthing.fleksyapps.mediashare.network.getUserAgent
import co.thingthing.fleksyapps.mediashare.network.models.MediaShareRequestDTO.Companion.ALL_SIZES_ADS_HEIGHT
import co.thingthing.fleksyapps.mediashare.network.toNetworkContentType
import co.thingthing.fleksyapps.mediashare.utils.DeviceInfoProvider
import co.thingthing.fleksyapps.mediashare.utils.DeviceInfoProviderImpl
import co.thingthing.fleksyapps.mediashare.utils.getVisibleItemPositions
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

    /**
     * Set of visible items positions at current moment
     */
    private var visibleItems = mutableSetOf<Int>()
    private val deviceInfoProvider: DeviceInfoProvider by lazy { DeviceInfoProviderImpl(context = context?.applicationContext) }

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
                ContentType.CLIPS -> "clips"
                ContentType.GIFS -> "gifs"
                ContentType.STICKERS -> "stickers"
            }
            return appIdSuffix
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

    private fun getAdMaxHeight(): Int {
        return carouselHeight ?: ALL_SIZES_ADS_HEIGHT
    }

    override fun onItemSelected(result: BaseResult) {
        super.onItemSelected(result)
        result.id?.let { id ->
            service.sendImpression(
                contentId = id,
                type = MediaShareService.ImpressionType.SHARE,
            )
        }
    }

    override fun default(pagination: Pagination): Single<List<BaseResult>> =
        service.getContent(
            content = MediaShareService.Content.Trending(pagination.page),
            adMaxHeight = getAdMaxHeight()
        )
            .subscribeOn(Schedulers.io()) // Ensure initial work is done on IO thread
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                context?.let {
                    toResults(it, response)
                }
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
            content = MediaShareService.Content.Search(query = query, pagination.page),
            adMaxHeight = getAdMaxHeight()
        )
            .subscribeOn(Schedulers.io()) // Ensure initial work is done on IO thread
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                context?.let { toResults(it, response) }
            }

    private fun toResults(
        context: Context,
        response: MediaShareResponse,
        sourceQuery: String? = null
    ): List<BaseResult> =
        response.toResults(
            context = context,
            theme = theme,
            contentType = contentType,
            sourceQuery = sourceQuery,
            maxWidth = carouselWidthPx
        )

    private val remoteCategories
        get() = service.getTags(
            adMaxHeight = getAdMaxHeight()
        ).map { category ->
            category.toCategories(appTheme = theme, typeface = customTypefaces?.bold)
        }

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
            deviceInfoProvider = deviceInfoProvider,
            mediaShareApiKey = apiKey,
            sdkLicenseId = sdkLicenseKey,
            userAgent = context.getUserAgent(),
            userId = androidId,
        )
    }

    /**
     * The method updates the list of visible items.
     * Detects items that have gone out of view and calls `onItemOutOfScreen()` on them.
     *
     * @param adapter The adapter managing the data and view binding in the RecyclerView
     * @param layoutManager The layout manager handling the positioning of items within the RecyclerView
     */
    private fun detectOutOfScreenItems(adapter: BaseResultAdapter?, layoutManager: LayoutManager?) {
        if (adapter != null && layoutManager != null && (layoutManager is LinearLayoutManager || layoutManager is StaggeredGridLayoutManager)) {
            val newVisibleItems = layoutManager.getVisibleItemPositions()
            val itemsOutOfScreen = visibleItems - newVisibleItems
            itemsOutOfScreen.forEach(adapter::onItemOutOfScreen)
            visibleItems.clear()
            visibleItems.addAll(newVisibleItems)
        }
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        detectOutOfScreenItems(resultAdapter, currentItemsRecyclerView().layoutManager)
    }

    companion object {
        private const val MEDIA_SHARE_TRENDING = "Trending"
    }
}
