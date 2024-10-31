package co.thingthing.fleksyapps.mediashare.models

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import co.thingthing.fleksyapps.base.BaseMedia
import co.thingthing.fleksyapps.base.BaseResult
import co.thingthing.fleksyapps.core.AppTheme
import co.thingthing.fleksyapps.mediashare.AdViewContainer
import co.thingthing.fleksyapps.mediashare.MediaShareApp

data class MediaShareResponse(
    val contents: List<Content>,
    val advertisements: List<Advertisement>,
    val page: Int,
    val hasNext: Boolean
) {
    data class Content(
        val id: String,
        val title: String,
        val file: FileFormats
    ) {
        data class FileFormats(
            val gif: MediaItem?,
            val webp: MediaItem?,
            val mp4: MediaItem?
        ) {
            data class MediaItem(
                val default: File?,
                val hd: File?,
                val md: File?,
                val sm: File?,
                val xs: File?
            ) {
                data class File(
                    val url: String,
                    val width: Int,
                    val height: Int,
                    val size: Int?
                )

                val fileForFinalContent: File?
                    get() = hd ?: md ?: sm ?: xs ?: default

                val fileForThumbnailContent: File?
                    get() = sm ?: md ?: hd ?: default
            }


            private val preferWebp: MediaItem?
                get() = webp ?: gif ?: mp4

            val thumbnailFile: MediaItem.File?
                get() = preferWebp?.fileForThumbnailContent

            val videoItemWithExtension: Pair<MediaItem, String>?
                get() =
                    when {
                        mp4 != null -> Pair(mp4, EXTENSION_MP4)
                        webp != null -> Pair(webp, EXTENSION_WEBP)
                        else -> null
                    }
        }
    }

    data class Advertisement(
        val content: String,
        val width: Int,
        val height: Int,
        @ColorInt val backgroundColor: Int = Color.TRANSPARENT
    ) {
        fun toBaseResult(context: Context, appTheme: AppTheme): BaseResult {
            return BaseResult.Card(
                theme = appTheme,
                view = AdViewContainer(context = context, adContent = this),
                url = "",
            )
        }
    }

    fun toResults(
        context: Context,
        theme: AppTheme,
        contentType: MediaShareApp.ContentType,
        sourceQuery: String?
    ): List<BaseResult> {
        val ads = advertisements.map {
            it.toBaseResult(context, theme)
        }
        val contents = contents.mapNotNull { it.toBaseResult(theme, contentType, sourceQuery) }
        return if (contentType != MediaShareApp.ContentType.CLIPS) ads + contents else contents
    }

    companion object {
        private const val EXTENSION_MP4 = "mp4"
        private const val EXTENSION_WEBP = "webp"
    }

    private fun Content.toBaseResult(
        theme: AppTheme,
        contentType: MediaShareApp.ContentType,
        sourceQuery: String?
    ): BaseResult? {
        val mediaItem = file.mediaItemForSharingContent(contentType)
        val shareFile = mediaItem?.fileForFinalContent ?: return null
        val shareFileURL = shareFile.url
        val videoItemWithExtension = file.videoItemWithExtension ?: return null
        val thumbnailFile =
            (if (mediaItem != file.mp4)
                mediaItem.fileForThumbnailContent
            else
                file.thumbnailFile) ?: return null

        return when (videoItemWithExtension.second) {
            EXTENSION_MP4 -> {
                if (contentType == MediaShareApp.ContentType.CLIPS) {
                    BaseResult.VideoWithSound(
                        video = listOf(
                            BaseMedia(
                                url = shareFile.url,
                                width = shareFile.width,
                                height = shareFile.height,
                            )
                        ),
                        thumbnail = listOf(
                            BaseMedia(
                                url = thumbnailFile.url,
                                width = thumbnailFile.width,
                                height = thumbnailFile.height,
                            )
                        ),
                        link = shareFileURL,
                        label = title,
                        theme = theme,
                        duration = null,
                        source = this,
                        id = id
                    )
                } else {
                    BaseResult.Video(
                        video = listOf(
                            BaseMedia(
                                url = shareFile.url,
                                width = shareFile.width,
                                height = shareFile.height,
                            )
                        ),
                        thumbnail = listOf(
                            BaseMedia(
                                url = thumbnailFile.url,
                                width = thumbnailFile.width,
                                height = thumbnailFile.height,
                            )
                        ),
                        link = shareFileURL,
                        label = title,
                        theme = theme,
                        duration = null,
                        source = this,
                        id = id
                    )
                }
            }

            else -> BaseResult.Image(
                source = this,
                image = listOf(
                    BaseMedia(
                        shareFile.url,
                        shareFile.width,
                        shareFile.height
                    )
                ),
                thumbnail = listOf(
                    BaseMedia(
                        thumbnailFile.url,
                        thumbnailFile.width,
                        thumbnailFile.height
                    )
                ),
                placeholder = listOf(
                    BaseMedia(
                        thumbnailFile.url,
                        thumbnailFile.width,
                        thumbnailFile.height
                    )
                ),
                link = shareFileURL,
                label = title,
                theme = theme,
                sourceQuery = sourceQuery
            )
        }
    }
}

private fun MediaShareResponse.Content.FileFormats.mediaItemForSharingContent(contentType: MediaShareApp.ContentType): MediaShareResponse.Content.FileFormats.MediaItem? {
    return when (contentType) {
        MediaShareApp.ContentType.CLIPS -> mp4
        MediaShareApp.ContentType.GIFS -> gif ?: webp ?: mp4
        MediaShareApp.ContentType.STICKERS -> webp ?: gif ?: mp4
    }
}