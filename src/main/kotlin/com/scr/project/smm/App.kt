package com.scr.project.smm

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.scr.project"])
class App

fun main(args: Array<String>) {
	runApplication<App>(*args)
}
