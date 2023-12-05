package com.example.wsspringdatalearn.jpa

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(
    properties = ["spring.jpa.properties.hibernate.jdbc.batch_size=50"],
)
class ImplicitFlushingBehaviourTests : BaseTestSetup() {
    @BeforeEach
    override fun setup() {
        transactionTemplate.execute {
            stockWithSequenceRepository.persistAll(
                (1..NUMBER_OF_STOCKS).map { StockWithSequence("TSLA$it", "Tesla") },
            )
        }!!
        super.setup()
    }

    @Test
    fun `be careful when using native queries in a loop`() {
        benchmark {
            transactionTemplate.execute {
                val stocks = stockWithSequenceRepository.findByStatisticsIsNull()
                stocks.forEach {
                    it.statistics = stockWithSequenceRepository.calculateStatisticsNative(it.ticker)
                }
            }!!
        }
    }

    @Test
    fun `use hibernate hints to tell it that your query don't touch nothing`() {
        benchmark {
            transactionTemplate.execute {
                val stocks = stockWithSequenceRepository.findByStatisticsIsNull()
                stocks.forEach {
                    it.statistics = stockWithSequenceRepository.calculateStatisticsNativeHint(it.ticker)
                }
            }!!
        }
    }

    companion object {
        private const val NUMBER_OF_STOCKS = 10000
    }
}
