package co.thingthing.fleksyapps.mediashare.models

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import co.thingthing.fleksyapps.base.BaseMedia
import co.thingthing.fleksyapps.base.BaseResult
import co.thingthing.fleksyapps.core.AppTheme
import co.thingthing.fleksyapps.mediashare.AdViewContainer
import co.thingthing.fleksyapps.mediashare.MediaShareApp
import kotlin.random.Random

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

                /** The MediaShare app should now show media of sm quality in thumbnails instead of xs */
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
        fun toBaseResult(context: Context, appTheme: AppTheme, maxWidth: Int): BaseResult {
            return BaseResult.Card(
                theme = appTheme,
                view = AdViewContainer(context = context, adContent = this, maxWidth = maxWidth),
                url = "",
            )
        }
    }

    /**
     * Merges `ads` and `items` by inserting elements from `ads` at random intervals
     * within elements from `items`.
     *
     * The first `ads` element is inserted at a random position (0, 1, or 2),
     * and each subsequent `ads` element is spaced by 1 to 3 random `items` elements.
     * Remaining `items` elements are added at the end.
     *
     * @param ads The list with elements to insert.
     * @param contents The list providing spacing elements.
     * @return A merged list with `ads` elements interleaved within `items`.
     */
    private fun mergeLists(ads: List<BaseResult>, contents: List<BaseResult>): List<BaseResult> {
        val result = mutableListOf<BaseResult>()
        val itemsIterator = contents.iterator()
        var position = Random.nextInt(0, 3)

        for (item in ads) {
            while (position > 0 && itemsIterator.hasNext()) {
                result.add(itemsIterator.next())
                position--
            }

            result.add(item)
            position = Random.nextInt(1, 4)
        }

        while (itemsIterator.hasNext()) {
            result.add(itemsIterator.next())
        }

        return result.toList()
    }

    fun toResults(
        context: Context,
        theme: AppTheme,
        contentType: MediaShareApp.ContentType,
        sourceQuery: String?,
        maxWidth: Int,
    ): List<BaseResult> {
        val ads = advertisements.map {
            it.toBaseResult(context, theme, maxWidth)
        }
        val contents = contents.mapNotNull { it.toBaseResult(theme, contentType, sourceQuery) }
        return mergeLists(ads, contents)
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
                        id = id,
                        showTitleAndSound = true
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
                sourceQuery = sourceQuery,
                id = id,
            )
        }
    }
}

private fun MediaShareResponse.Content.FileFormats.mediaItemForSharingContent(contentType: MediaShareApp.ContentType): MediaShareResponse.Content.FileFormats.MediaItem? {
    return when (contentType) {
        MediaShareApp.ContentType.CLIPS -> mp4 ?: webp ?: gif
        MediaShareApp.ContentType.GIFS -> gif ?: webp ?: mp4
        MediaShareApp.ContentType.STICKERS -> webp ?: gif ?: mp4
    }
}