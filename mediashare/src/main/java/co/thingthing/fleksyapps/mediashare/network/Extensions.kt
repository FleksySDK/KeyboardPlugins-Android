package co.thingthing.fleksyapps.mediashare.network

import android.content.Context
import android.webkit.WebView
import co.thingthing.fleksyapps.mediashare.MediaShareApp
import co.thingthing.fleksyapps.mediashare.network.models.MediaShareRequestDTO
import co.thingthing.fleksyapps.mediashare.utils.DeviceInfoProvider.Companion.INVALID_IDFA
import com.google.android.gms.ads.identifier.AdvertisingIdClient


internal fun MediaShareApp.ContentType.toNetworkContentType() = when (this) {
    MediaShareApp.ContentType.CLIPS -> MediaShareRequestDTO.ContentType.Clips
    MediaShareApp.ContentType.GIFS -> MediaShareRequestDTO.ContentType.Gifs
    MediaShareApp.ContentType.STICKERS -> MediaShareRequestDTO.ContentType.Stickers
}

internal fun Context?.getUserAgent() = try {
    this?.let { WebView(it).settings.userAgentString }.orEmpty()
} catch (e: Exception) {
    ""
}

internal fun Context?.getDeviceIfa() = try {
    val idfa = this?.let { AdvertisingIdClient.getAdvertisingIdInfo(it).id }
    idfa.takeIf { it != INVALID_IDFA }
} catch (e: Exception) {
    null
}

