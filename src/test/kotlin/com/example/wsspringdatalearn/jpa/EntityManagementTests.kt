package com.example.wsspringdatalearn.jpa

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
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
         * And then when the transaction is committed it will flush the entity to the database.
         *
         * An important thing to understand is that the entity manager is only tracking entities within a transaction.
         */

        val detachedStock = Stock("TSLA", "Tesla") // Initially Detached like above

        // Persist moves it from detached -> managed
        // However since we are not in a transaction it will also flush the entity to the database immediately
        stockRepository.persist(detachedStock)

        // Now we can find the stock in the database
        val foundStock = stockRepository.findByTicker(detachedStock.ticker)

        assertThat(foundStock).isNotNull // Now we found a stock!
        assertThat(detachedStock.id).isNotNull() // And our original has its id updated
        assertThat(foundStock?.id).isEqualTo(detachedStock.id) // And here we see that the id's match

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

            val foundStock = stockRepository.findByTicker(detachedStock.ticker)

            // Now we can se the foundStock == detachedStock. I.E they are the same object
            // This is because within this transaction JPA is tracking all the managed entities.
            // when we called persist our detachedStock became a managed entity.
            // And then when we go and query for the stock it's able to just pull it straight the entity manager
            assertThat(foundStock).isEqualTo(detachedStock)
            assertThat(entityManager.contains(detachedStock)).isTrue()
            assertThat(entityManager.contains(foundStock)).isTrue()
        }
    }

    @Test
    fun `updating existing entities - BAD!!!!`() {
        /**
         * When updating existing entities, let the framework handle flushing when possible
         * The following still works, but it's not efficient.
         * Behind the scenes an update statement is being executed for each persistAndFlush call.
         */
        transactionTemplate.execute {
            stockRepository.persist(Stock("TSLA", "Tesla"))
        }!!
        transactionTemplate.execute {
            val stock = stockRepository.findByTicker("TSLA")!!
            stock.name = "UPDATED"
            stockRepository.persistAndFlush(stock) // Avoid
            stock.name = "UPDATED2"
            stockRepository.persistAndFlush(stock) // Avoid
            stock.name = "UPDATED3"
            stockRepository.persistAndFlush(stock) // Avoid
        }!!

        val finalStock = stockRepository.findByTicker("TSLA")!!
        assertThat(finalStock.name).isEqualTo("UPDATED3")
    }

    @Test
    fun `updating existing entities - GOOD`() {
        // When updating existing entities, let the framework handle flushing when possible
        // I.E avoid doing manual flushed on managed entities!

        transactionTemplate.execute {
            stockRepository.persist(Stock("TSLA", "Tesla"))
        }!!
        transactionTemplate.execute {
            val stock = stockRepository.findByTicker("TSLA")!!
            stock.name = "UPDATED"
            stock.name = "UPDATED2"
            stock.name = "UPDATED3"
        }!!

        val finalStock = stockRepository.findByTicker("TSLA")!!
        assertThat(finalStock.name).isEqualTo("UPDATED3")
    }
}
