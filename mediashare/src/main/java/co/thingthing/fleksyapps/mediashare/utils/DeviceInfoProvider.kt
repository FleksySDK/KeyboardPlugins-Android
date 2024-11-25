package co.thingthing.fleksyapps.mediashare.utils

import android.content.Context
import android.os.Build
import co.thingthing.fleksyapps.mediashare.network.getDeviceIfa

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

    /**
     * Method should be called from background thread otherwise it will return null
     * Loads the advertising identifier (IFA) of the device, if available.
     * @return the advertising identifier (IFA) or null if the device does not support or has restricted access to this identifier
     */
    fun loadDeviceIfa(): String?
}

class DeviceInfoProviderImpl(private val context: Context?) : DeviceInfoProvider {
    override val operatingSystemVersion: String = Build.VERSION.RELEASE
    override val hardwareVersion: String = Build.HARDWARE
    override val deviceMake: String = Build.MANUFACTURER
    override val deviceModel: String = Build.MODEL
    override fun loadDeviceIfa() = context.getDeviceIfa()
}