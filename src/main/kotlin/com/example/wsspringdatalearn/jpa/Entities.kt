package com.example.wsspringdatalearn.jpa

import io.hypersistence.utils.spring.repository.BaseJpaRepository
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.QueryHint
import jakarta.persistence.SequenceGenerator
import org.hibernate.jpa.HibernateHints
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.stereotype.Repository

@Entity
class Stock(
    @Column(nullable = false, unique = true)
    val ticker: String,

    @Column(nullable = false)
    var name: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
)

@Repository
interface StockRepository : BaseJpaRepository<Stock, Long> {
    fun findByTicker(ticket: String): Stock?
    fun findByName(name: String): List<Stock>
    fun findByTickerIn(tickers: List<String>): List<StockNameOnly>

    interface StockNameOnly {
        var name: String
    }
}

@Entity
class StockWithSequence(
    @Column(nullable = false, unique = true)
    val ticker: String,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = true)
    var statistics: String? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    val id: Long? = null,
)

@Repository
interface StockWithSequenceRepository : BaseJpaRepository<StockWithSequence, Long> {
    fun findByStatisticsIsNull(): List<StockWithSequence>

    @Query(
        nativeQuery = true,
        value = """
            SELECT 'Price Change: ' || (RANDOM() * 100) || '%, Volume: ' || (RANDOM() * 10000)
            FROM stock_with_sequence 
            WHERE ticker = :ticker
        """,
    )
    fun calculateStatisticsNative(ticker: String): String

    @Query(
        nativeQuery = true,
        value = """
            SELECT 'Price Change: ' || (RANDOM() * 100) || '%, Volume: ' || (RANDOM() * 10000)
            FROM stock_with_sequence 
            WHERE ticker = :ticker
        """,
    )
    @QueryHints(QueryHint(name = HibernateHints.HINT_FLUSH_MODE, value = "COMMIT"))
    fun calculateStatisticsNativeHint(ticker: String): String
}

@Entity
class StockWithSequencePool(
    @Column(nullable = false, unique = true)
    val ticker: String,

    @Column(nullable = false)
    var name: String,

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stock_id")
    @SequenceGenerator(name = "stock_id", sequenceName = "stock_with_sequence_pool_id_seq", allocationSize = 100)
    val id: Long? = null,
)

@Repository
interface StockWithSequencePoolRepository : BaseJpaRepository<StockWithSequencePool, Long>
