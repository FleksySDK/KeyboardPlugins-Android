package co.thingthing.fleksyapps.mediashare.network

import co.thingthing.fleksyapps.mediashare.MediaShareApp
import co.thingthing.fleksyapps.mediashare.network.models.MediaShareRequestDTO


internal fun MediaShareApp.ContentType.toNetworkContentType() = when (this) {
    MediaShareApp.ContentType.CLIPS -> MediaShareRequestDTO.ContentType.Clips
    MediaShareApp.ContentType.GIFS -> MediaShareRequestDTO.ContentType.Gifs
    MediaShareApp.ContentType.STICKERS -> MediaShareRequestDTO.ContentType.Stickers
}

