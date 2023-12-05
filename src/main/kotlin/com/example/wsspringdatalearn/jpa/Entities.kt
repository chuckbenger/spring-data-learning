package com.example.wsspringdatalearn.jpa

import io.hypersistence.utils.spring.repository.BaseJpaRepository
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.SequenceGenerator
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
}

@Entity
class StockWithSequence(
    @Column(nullable = false, unique = true)
    val ticker: String,

    @Column(nullable = false)
    var name: String,

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    val id: Long? = null,
)

@Repository
interface StockWithSequenceRepository : BaseJpaRepository<StockWithSequence, Long>

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
