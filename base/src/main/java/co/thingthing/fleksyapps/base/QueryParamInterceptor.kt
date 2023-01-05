package co.thingthing.fleksyapps.base

import okhttp3.Interceptor
import okhttp3.Response

class QueryParamInterceptor(private val key: String, private val value: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response =
        chain.proceed(
            chain.request().let { request ->
                request.newBuilder().url(
                    request.url.newBuilder().addQueryParameter(key, value).build()
                ).build()
            }
        )
}
