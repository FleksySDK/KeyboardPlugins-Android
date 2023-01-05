package co.thingthing.fleksyapps.base

import com.google.gson.Gson
import okhttp3.Cache
import java.io.File

// Reusable components across apps
object BaseComponent {
    val gson by lazy { Gson() }
    val cache by lazy { Cache(File("."), 10 * 1024 * 1024) }
}
