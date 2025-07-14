package com.scr.project.smm.domains.security.error

import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.web.bind.annotation.ResponseStatus

sealed class KeycloakErrors : RuntimeException() {

    @ResponseStatus(INTERNAL_SERVER_ERROR)
    class OnKeycloakError(override val message: String) : KeycloakErrors()
}