package com.example.wsspringdatalearn.jpa

import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import org.hibernate.SessionFactory
import org.hibernate.stat.Statistics
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.support.TransactionTemplate

abstract class BaseTestSetup {
    @Autowired
    protected lateinit var stockRepository: StockRepository

    @Autowired
    protected lateinit var stockWithSequenceRepository: StockWithSequenceRepository

    @Autowired
    protected lateinit var stockWithSequencePoolRepository: StockWithSequencePoolRepository

    @Autowired
    protected lateinit var entityManager: EntityManager

    @Autowired
    protected lateinit var transactionTemplate: TransactionTemplate

    @Autowired
    private lateinit var entityManagerFactory: EntityManagerFactory

    private lateinit var statistics: Statistics

    @BeforeEach
    protected open fun setup() {
        val sessionFactory = entityManagerFactory.unwrap(SessionFactory::class.java)
        statistics = sessionFactory.statistics
        statistics.clear()
    }

    @AfterEach
    fun tearDown() {
        statistics.logSummary()

        transactionTemplate.execute {
            entityManager.createNativeQuery("DELETE FROM stock").executeUpdate()
            entityManager.createNativeQuery("DELETE FROM stock_with_sequence").executeUpdate()
            entityManager.createNativeQuery("DELETE FROM stock_with_sequence_pool").executeUpdate()
        }!!
    }

    protected fun benchmark(block: () -> Unit) {
        val start = System.currentTimeMillis()
        block()
        val end = System.currentTimeMillis()
        println("========= Time taken: ${end - start}ms ============")
    }
}