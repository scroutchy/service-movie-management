package com.scr.project.smm.config

import okhttp3.Interceptor
import okhttp3.Response
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.stereotype.Component

@Component
class AuthInterceptor(@Value("\${mistral.api.token}") private val apiToken: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestWithAuthorization = originalRequest.newBuilder()
            .header(AUTHORIZATION, "Bearer $apiToken")
            .build()

        return chain.proceed(requestWithAuthorization)
    }
}