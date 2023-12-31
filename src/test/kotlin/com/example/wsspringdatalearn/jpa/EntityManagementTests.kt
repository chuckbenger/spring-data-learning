package com.example.wsspringdatalearn.jpa

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class EntityManagementTests : BaseTestSetup() {
    /**
     * Understanding how JPA manages entities is important to understand how to use it correctly and efficiently.
     * These tests are meant to help you understand how JPA manages entities.
     *
     * Some additional reading:
     * - https://vladmihalcea.com/a-beginners-guide-to-jpa-hibernate-entity-state-transitions/
     */

    @Test
    fun `detached entity`() {
        /**
         * When an entity is initial created it's in a detached state. Which means it's not being tracked by the
         * entity manager. And it's not in the database.
         */
        val stock = Stock("TSLA", "Tesla")
        val foundStock = stockRepository.findByTicker(stock.name)

        assertThat(stock.id).isNull() // Id is initially null since we are letter Hibernate manage generating it
        assertThat(foundStock).isNull() // And we can't find it in the database
        assertThat(entityManager.contains(stock)).isFalse() // And it's not being tracked by the entity manager
    }

    @Test
    fun `detached to managed no overall transaction`() {
        /**
         * Detached entities can be moved to a managed state by calling persist on them.
         *
         * Calling persist doesn't automatically flush the entity to the database. It just moves it to a managed state.
         * And then the entity manager will flush it to the database when it needs to (generally when the transaction ends)
         *
         * An important thing to understand is that the entity manager is only tracking entities within a transaction.
         */

        val detachedStock = Stock("TSLA", "Tesla") // Initially Detached like above

        // Persist moves it from detached -> managed
        // However since we are not in a transaction it will also flush the entity to the database immediately
        stockRepository.persist(detachedStock)

        // Now we can find the stock in the database
        val foundStock = stockRepository.findById(detachedStock.id).get()

        assertThat(foundStock).isNotNull // Now we found a stock!
        assertThat(detachedStock.id).isNotNull() // And our original has its id updated
        assertThat(foundStock.id).isEqualTo(detachedStock.id) // And here we see that the id's match

        // But one important thing is we can see that the foundStock != detachedStock objects are not equal.
        // This is because the entity manager is only tracking entities within a transaction.
        assertThat(foundStock).isNotEqualTo(detachedStock)
        assertThat(entityManager.contains(detachedStock)).isFalse()
        assertThat(entityManager.contains(foundStock)).isFalse()
    }

    @Test
    fun `detached to managed transaction`() {
        /**
         * Now let do the same thing as above but execute everything within a transaction.
         */
        transactionTemplate.execute {
            val detachedStock = Stock("TSLA", "Tesla")

            stockRepository.persist(detachedStock) // Persist moves it from detached -> managed

            val foundStock = stockRepository.findById(detachedStock.id).get()

            // Now we can se the foundStock == detachedStock. I.E they are the same object
            // This is because within this transaction JPA is tracking all the managed entities.
            // when we called persist our detachedStock became a managed entity.
            // And then when we go and query for the stock it's able to just pull it straight the entity manager
            // If you want to see what's happening place some breakpoints in SessionImpl to see what's happening
            assertThat(foundStock).isEqualTo(detachedStock)
            assertThat(entityManager.contains(detachedStock)).isTrue()
            assertThat(entityManager.contains(foundStock)).isTrue()
        }
    }

    @Test
    fun `updating existing entities - BAD!!!!`() {
        val numberOfStocks = 1000
        /**
         * When updating existing entities, let the framework handle flushing when possible
         * The following still works, but it's not efficient.
         * Behind the scenes an update statement is being executed for each persistAndFlush call.
         */
        transactionTemplate.execute {
            val stocks = (1..numberOfStocks).map { Stock("TSLA$it", "Tesla") }
            stockRepository.persistAll(stocks)
        }!!
        transactionTemplate.execute {
            val stocks = stockRepository.findByName("Tesla")

            stocks.forEach { stock ->
                stock.name = "UPDATED"
                stockRepository.persistAndFlush(stock) // Avoid
            }
        }!!

        val stocks = stockRepository.findByName("Tesla")
        stocks.forEach { stock ->
            assertThat(stock.name).isEqualTo("UPDATED")
        }
    }

    @Test
    fun `updating existing entities - GOOD`() {
        val numberOfStocks = 1000
        // When updating existing entities, let the framework handle flushing when possible
        // I.E avoid doing manual flushed on managed entities!

        transactionTemplate.execute {
            val stocks = (1..numberOfStocks).map { Stock("TSLA$it", "Tesla") }
            stockRepository.persistAll(stocks)
        }!!
        transactionTemplate.execute {
            val stocks = stockRepository.findByName("Tesla")

            stocks.forEach { stock ->
                stock.name = "UPDATED"
            }
        }!!

        val stocks = stockRepository.findByName("Tesla")
        stocks.forEach { stock ->
            assertThat(stock.name).isEqualTo("UPDATED")
        }
    }

    @Test
    fun `projections are not in the persistence context and are read only`() {
        transactionTemplate.execute {
            stockRepository.persistAll(listOf(Stock("TSLA", "Tesla"), Stock("TSLA2", "Tesla")))
        }!!

        // Projections are not in the persistence context and are read only
        // They allow you to limit the amount of data you are pulling back from the database
        // And they are not tracked by the entity manager
        // This can be useful for read only data, and also batching if you want to pull back the
        // ids for many entities and then use them to pull back the full entities later through
        // the Entity which is tracked by the entity manager

        // The below tries to update a value on a projection and you can see UnsupportedOperationException is thrown
        assertThrows<UnsupportedOperationException> {
            transactionTemplate.execute {
                val stocks = stockRepository.findByTickerIn(listOf("TSLA", "TSLA2"))
                stocks.forEach { stock ->
                    stock.name = "UPDATED"
                }
            }
        }
    }
}
