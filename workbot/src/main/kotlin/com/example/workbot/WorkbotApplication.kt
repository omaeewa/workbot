package com.example.workbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class WorkbotApplication

fun main(args: Array<String>) {
	runApplication<WorkbotApplication>(*args)
}
