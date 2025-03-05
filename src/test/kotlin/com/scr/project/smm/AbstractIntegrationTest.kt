package com.scr.project.smm

import org.junit.jupiter.api.TestInstance
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0)
abstract class AbstractIntegrationTest {

    companion object {

        val mongoDBContainer = MongoDBContainer("mongo:6.0").apply { start() }

        @JvmStatic
        @DynamicPropertySource
        fun mongoProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl)
        }
    }
}