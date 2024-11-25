package co.thingthing.fleksyapps.mediashare.network

import android.content.Context
import android.webkit.WebView
import co.thingthing.fleksyapps.mediashare.MediaShareApp
import co.thingthing.fleksyapps.mediashare.network.models.MediaShareRequestDTO
import com.google.android.gms.ads.identifier.AdvertisingIdClient


internal fun MediaShareApp.ContentType.toNetworkContentType() = when (this) {
    MediaShareApp.ContentType.CLIPS -> MediaShareRequestDTO.ContentType.Clips
    MediaShareApp.ContentType.GIFS -> MediaShareRequestDTO.ContentType.Gifs
    MediaShareApp.ContentType.STICKERS -> MediaShareRequestDTO.ContentType.Stickers
}

internal fun Context?.getUserAgent() = this?.let { WebView(it).settings.userAgentString }.orEmpty()

internal fun Context?.getDeviceIfa() = try {
    this?.let { AdvertisingIdClient.getAdvertisingIdInfo(it).id }
} catch (e: Exception) {
    null
}

