package com.example.wsspringdatalearn

import io.hypersistence.utils.spring.repository.BaseJpaRepositoryImpl
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableJpaRepositories(
    basePackages = ["com.example.wsspringdatalearn.jpa"],
    repositoryBaseClass = BaseJpaRepositoryImpl::class,
)
class WsSpringDataLearnApplication

fun main(args: Array<String>) {
    runApplication<WsSpringDataLearnApplication>(*args)
}
