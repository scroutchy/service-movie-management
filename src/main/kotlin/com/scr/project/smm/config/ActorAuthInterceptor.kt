package com.scr.project.smm.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import okhttp3.Interceptor
import okhttp3.Response
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit.MINUTES

@Component
class ActorAuthInterceptor(@Value("\${actor.api.jwt.secretKey}") private val secretKey: String) : Interceptor {

    private val tokenValidityMinutes: Long = 15

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestWithAuthorization = originalRequest.newBuilder()
            .header(AUTHORIZATION, "Bearer ${generateJwtToken()}")
            .build()

        return chain.proceed(requestWithAuthorization)
    }

    private fun generateJwtToken(): String {
        val algorithm = Algorithm.HMAC256(secretKey)

        return JWT.create()
            .withIssuer("movie-management-service")
            .withExpiresAt(Instant.now().plus(tokenValidityMinutes, MINUTES))
            .sign(algorithm)
    }
}