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
        val image: List<BaseMedia>,
        val thumbnail: List<BaseMedia>? = null,
        val placeholder: List<BaseMedia>? = null,
        val link: String? = null,
        val label: String? = null,
        source: Any? = null,
        theme: AppTheme,
        sourceQuery: String? = null
    ) : BaseResult(source, theme, sourceQuery) {

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
        override val video: List<BaseMedia>,
        override val duration: Long?,
        override val thumbnail: List<BaseMedia>?,
        override val link: String?,
        override val label: String?,
        var isMuted: Boolean = true,
        source: Any?,
        theme: AppTheme
    ) : Video(id, video, duration, thumbnail, link, label, source, theme) {
        fun mute() { isMuted = true }
        fun isNotMuted() = isMuted.not()
    }

    open class Video(
        override val id: String? = null,
        open val video: List<BaseMedia>,
        open val duration: Long?,
        open val thumbnail: List<BaseMedia>?,
        open val link: String?,
        open val label: String?,
        source: Any?,
        theme: AppTheme
    ) : BaseResult(source, theme, id)

    open class Card(
        source: Any? = null,
        theme: AppTheme,
        val view: View,
        val url: String
    ) : BaseResult(source, theme)
}

