package com.scr.project.smm.config

import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

//TODO: Move to common library
@Component
class LoggingInterceptor : Interceptor {

    private val logger: Logger = LoggerFactory.getLogger(LoggingInterceptor::class.java)

    init {
        logger.debug("LoggingInterceptor initialized")
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        // Log the request details
        logger.debug("Sending request to URL: ${request.url}")
        logger.debug("Request headers: ${request.headers}")
        val requestBody = request.body
        if (requestBody != null && isPlaintext(requestBody.contentType().toString())) {
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            logger.debug("Request body: ${buffer.readUtf8()}")
        }
        // Proceed with the request
        val response = chain.proceed(request)
        // Log the response details
        logger.debug("Received response for URL: ${response.request.url}")
        logger.debug("Response code: ${response.code}")
        logger.debug("Response headers: ${response.headers}")
        val responseBody = response.peekBody(Long.MAX_VALUE)
        if (isPlaintext(responseBody.contentType().toString())) {
            logger.debug("Response body: ${responseBody.string()}")
        }

        return response
    }

    private fun isPlaintext(contentType: String?): Boolean {
        return listOf("text", "json", "xml").any { contentType?.lowercase()?.contains(it) == true }
    }
}
