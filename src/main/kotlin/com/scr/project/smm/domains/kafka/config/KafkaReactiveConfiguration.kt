package com.scr.project.smm.domains.kafka.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions

@Configuration
class KafkaReactiveConfiguration(private val kafkaAvroProducerProperties: Map<String, Any>) {
    
    @Bean
    fun kafkaSender(): KafkaSender<String, Any> = KafkaSender.create(SenderOptions.create(kafkaAvroProducerProperties))
}