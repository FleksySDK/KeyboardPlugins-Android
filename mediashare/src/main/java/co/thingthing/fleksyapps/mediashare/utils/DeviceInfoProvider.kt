package co.thingthing.fleksyapps.mediashare.utils

import android.content.Context
import android.os.Build
import co.thingthing.fleksyapps.mediashare.network.getDeviceIfa
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * Provides device-related information for the current Android device.
 */
interface DeviceInfoProvider {
    /** The version of the operating system running on the device. */
    val operatingSystemVersion: String

    /** The hardware version of the device */
    val hardwareVersion: String

    /** The manufacturer of the device */
    val deviceMake: String

    /** The model name of the device */
    val deviceModel: String

    /** The advertising identifier (IFA) of the device, if available */
    val deviceIfa: String?
}

class DeviceInfoProviderImpl(private val context: Context?) : DeviceInfoProvider {

    companion object {
        const val DEFAULT_TIMEOUT_SEC = 2L
    }

    override val operatingSystemVersion: String = Build.VERSION.RELEASE
    override val hardwareVersion: String = Build.HARDWARE
    override val deviceMake: String = Build.MANUFACTURER
    override val deviceModel: String = Build.MODEL
    override val deviceIfa: String? = loadDeviceIfa()

    /**
     * Loads the advertising identifier (IFA) of the device, if available.
     * @return the advertising identifier (IFA) or null if the device does not support or has restricted access to this identifier
     */
    private fun loadDeviceIfa() = try {
        Single.create { emitter ->
            try {
                val deviceIfa = context.getDeviceIfa().orEmpty()
                if (emitter.isDisposed.not()) {
                    emitter.onSuccess(deviceIfa)
                }
            } catch (e: Exception) {
                if (emitter.isDisposed.not()) {
                    emitter.onError(e)
                }
            }
        }.subscribeOn(Schedulers.io())
            .timeout(DEFAULT_TIMEOUT_SEC, TimeUnit.SECONDS)
            .blockingGet()
    } catch (e: Exception) {
        null
    }
}