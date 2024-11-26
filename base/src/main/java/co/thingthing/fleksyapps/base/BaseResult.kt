package co.thingthing.fleksyapps.base

import android.view.View
import co.thingthing.fleksyapps.core.AppTheme

sealed class BaseResult(
    val source: Any?,
    var theme: AppTheme,
    open val id: String? = null,
    val sourceQuery: String? = null
) {
    fun onThemeChanged(theme: AppTheme) {
        this.theme = theme
    }

    open class Image(
        override val id: String,
        val image: List<BaseMedia>,
        val thumbnail: List<BaseMedia>? = null,
        val placeholder: List<BaseMedia>? = null,
        val link: String? = null,
        val label: String? = null,
        source: Any? = null,
        theme: AppTheme,
        sourceQuery: String? = null
    ) : BaseResult(source, theme, id, sourceQuery) {

        fun preferredImageFor(contentTypes: List<String>): BaseMedia? {
            val preferredContentType =
                contentTypes.firstOrNull { accepted -> image.any { it.contentType == accepted } }

            return image.firstOrNull { it.contentType == preferredContentType }
                ?: image.firstOrNull { it.contentType == "image/gif" }
                ?: image.firstOrNull { it.contentType == "video/mp4" }
                ?: image.firstOrNull()
        }
    }

    open class VideoWithSound(
        override val id: String? = null,
        open val video: List<BaseMedia>,
        open val duration: Long?,
        open val thumbnail: List<BaseMedia>?,
        open val link: String?,
        open val label: String?,
        source: Any?,
        theme: AppTheme
    ) : BaseResult(source, theme, id) {
        fun toMutedVideo() = Video(id, video, duration, thumbnail, link, label, source, theme, showTitleAndSound = true)
    }

    open class Video(
        override val id: String? = null,
        open val video: List<BaseMedia>,
        open val duration: Long?,
        open val thumbnail: List<BaseMedia>?,
        open val link: String?,
        open val label: String?,
        source: Any?,
        theme: AppTheme,
        val showTitleAndSound: Boolean = false,
    ) : BaseResult(source, theme, id) {
        fun toUnMutedVideo() = VideoWithSound(id, video, duration, thumbnail, link, label, source, theme)
    }

    open class Card(
        source: Any? = null,
        theme: AppTheme,
        val view: View,
        val url: String
    ) : BaseResult(source, theme, null)
}

