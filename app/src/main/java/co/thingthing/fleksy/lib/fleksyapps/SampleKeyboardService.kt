package co.thingthing.fleksy.lib.fleksyapps

import co.thingthing.fleksy.core.keyboard.KeyboardConfiguration
import co.thingthing.fleksy.core.keyboard.KeyboardConfiguration.*
import co.thingthing.fleksy.core.keyboard.KeyboardService
import co.thingthing.fleksyapps.giphy.GiphyApp
import java.util.concurrent.TimeUnit

class SampleKeyboardService : KeyboardService() {

    private val apps by lazy {
        listOf(
            GiphyApp(GIPHY_API_KEY)
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
            )
        )

    override val appIcon get() = R.drawable.fleksy_logo

    companion object {
        const val GIPHY_API_KEY = "ADD_YOUR_GIPHY_KEY_HERE"
    }
}
