package co.thingthing.fleksy.lib.fleksyapps

import co.thingthing.fleksy.core.keyboard.KeyboardConfiguration
import co.thingthing.fleksy.core.keyboard.KeyboardConfiguration.AppsConfiguration
import co.thingthing.fleksy.core.keyboard.KeyboardConfiguration.FeaturesConfiguration
import co.thingthing.fleksy.core.keyboard.KeyboardConfiguration.LicenseConfiguration
import co.thingthing.fleksy.core.keyboard.KeyboardConfiguration.StyleConfiguration
import co.thingthing.fleksy.core.keyboard.KeyboardConfiguration.TypingConfiguration
import co.thingthing.fleksy.core.keyboard.KeyboardService
import co.thingthing.fleksyapps.mediashare.MediaShareApp
import java.util.concurrent.TimeUnit

class SampleKeyboardService : KeyboardService() {

    private val apps by lazy {
        listOf(
            MediaShareApp(
                MediaShareApp.ContentType.GIFS,
                MEDIA_SHARE_API_KEY,
                FLEKSY_LICENSE_KEY
            ),
            MediaShareApp(
                MediaShareApp.ContentType.CLIPS,
                MEDIA_SHARE_API_KEY,
                FLEKSY_LICENSE_KEY
            ),
            MediaShareApp(
                MediaShareApp.ContentType.STICKERS,
                MEDIA_SHARE_API_KEY,
                FLEKSY_LICENSE_KEY
            ),
        )
    }

    override fun createConfiguration() =
        KeyboardConfiguration(
            features = FeaturesConfiguration(
                suggestions = false
            ),
            typing = TypingConfiguration(
                swipeTriggerFactor = 0.6f
            ),
            style = StyleConfiguration(
                swipeDuration = 300,
            ),
            apps = AppsConfiguration(
                keyboardApps = apps,
                shareAuthority = "$packageName.fileprovider",
                shareDirectory = "SharedContent",
                shareContentExpiration = TimeUnit.HOURS.toMillis(1),
                showAppsInCarousel = true,
                showAppsOnStart = true
            ),
            license = LicenseConfiguration(
                licenseKey = FLEKSY_LICENSE_KEY,
                licenseSecret = FLEKSY_LICENSE_SECRET
            )
        )

    override val appIcon get() = R.drawable.fleksy_logo

    companion object {
        const val FLEKSY_LICENSE_KEY = "ADD_YOUR_FLEKSY_LICENSE_KEY"
        const val FLEKSY_LICENSE_SECRET = "ADD_YOUR_FLEKSY_LICENSE_SECRET"
        const val MEDIA_SHARE_API_KEY = "ADD_YOUR_MEDIA_SHARE_KEY_HERE"
    }
}
