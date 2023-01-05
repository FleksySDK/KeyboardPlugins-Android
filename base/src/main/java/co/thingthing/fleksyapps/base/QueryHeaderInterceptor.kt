package co.thingthing.fleksyapps.base

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer

class QueryHeaderInterceptor(private val key: String, private val value: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response =
        chain.proceed(
            chain.request().newBuilder().header(key, value).build().also {
                val copy = chain.request().newBuilder().build()
                val buffer = Buffer()
                copy.body?.writeTo(buffer)
                buffer.readUtf8()
                Log.e("OPI", buffer.readUtf8())
            }
        )
}