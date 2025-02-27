plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.4.3"
	id("io.spring.dependency-management") version "1.1.7"
    id("jacoco")
}

group = "com.scr.project"
version = "0.0.1-SNAPSHOT"
private val jakartaValidationVersion = "3.0.2"
private val mockkVersion = "1.12.0"
private val commonsCinemaVersion = "1.0.0"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
    maven("https://gitlab.com/api/v4/projects/67204824/packages/maven")
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("jakarta.validation:jakarta.validation-api:$jakartaValidationVersion")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mongodb")
    testImplementation("com.scr.project.commons.cinema.test:commons-cinema-test:$commonsCinemaVersion")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.register("printCoverage") {
    group = "verification"
    description = "Prints the code coverage of the project"
    dependsOn(tasks.jacocoTestReport)
    doLast {
        val reportFile = layout.buildDirectory.file("reports/jacoco/test/jacocoTestReport.xml").get().asFile
        if (reportFile.exists()) {
            val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
            val builder = factory.newDocumentBuilder()
            val document = builder.parse(reportFile)
            val counters = document.getElementsByTagName("counter")
            var covered = 0
            var missed = 0
            for (i in 0 until counters.length) {
                val counter = counters.item(i) as org.w3c.dom.Element
                covered += counter.getAttribute("covered").toInt()
                missed += counter.getAttribute("missed").toInt()
            }
            val totalCoverage = (covered * 100.0) / (covered + missed)
            println("Total Code Coverage: %.2f%%".format(totalCoverage))
        } else {
            println("JaCoCo report file not found!")
        }
    }
}

tasks.withType<Test> {
	useJUnitPlatform()
    finalizedBy("jacocoTestReport", tasks.named("printCoverage"))
}
