package com.gijun.fds.transaction.application.handler

import com.gijun.fds.common.exception.DomainAlreadyExistsException
import com.gijun.fds.common.exception.DomainNotFoundException
import com.gijun.fds.transaction.application.port.inbound.RegisterTransactionCommand
import com.gijun.fds.transaction.application.port.outbound.TransactionPersistencePort
import com.gijun.fds.transaction.domain.enums.TransactionStatus
import com.gijun.fds.transaction.domain.model.Transaction
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class TransactionHandlerTest {

    private val fixedInstant: Instant = Instant.parse("2026-04-12T00:00:00Z")
    private val fixedClock: Clock = Clock.fixed(fixedInstant, ZoneOffset.UTC)

    private fun command(id: String = "TX-001"): RegisterTransactionCommand = RegisterTransactionCommand(
        transactionId = id,
        userId = "U-1",
        cardNumber = "4111111111111111",
        amount = BigDecimal("1000.00"),
        currency = "USD",
        merchantName = "Amazon",
        merchantCategory = "RETAIL",
        country = "USA",
        city = "Seattle",
        latitude = 47.6,
        longitude = -122.3,
    )

    @Test
    fun `register — 신규 거래를 저장하고 저장된 도메인을 반환한다`() {
        val repo = mockk<TransactionPersistencePort>()
        val handler = TransactionHandler(repo, fixedClock)
        val saved = slot<Transaction>()
        every { repo.save(capture(saved)) } answers { saved.captured }

        val result = handler.register(command())

        result.transactionId shouldBe "TX-001"
        result.status shouldBe TransactionStatus.PENDING
        result.createdAt shouldBe fixedInstant
        result.updatedAt shouldBe fixedInstant
        result.maskedCardNumber shouldBe "411111******1111"
        verify(exactly = 1) { repo.save(any()) }
    }

    @Test
    fun `register — 어댑터가 DomainAlreadyExistsException을 던지면 그대로 전파한다`() {
        val repo = mockk<TransactionPersistencePort>()
        val handler = TransactionHandler(repo, fixedClock)
        every { repo.save(any()) } throws DomainAlreadyExistsException("이미 등록된 거래입니다: TX-DUP")

        assertThrows<DomainAlreadyExistsException> { handler.register(command("TX-DUP")) }
    }

    @Test
    fun `getByTransactionId — 존재하면 도메인을 반환한다`() {
        val repo = mockk<TransactionPersistencePort>()
        val handler = TransactionHandler(repo, fixedClock)
        val tx = Transaction.create(
            transactionId = "TX-1",
            userId = "U-1",
            cardNumber = "4111111111111111",
            amount = BigDecimal.ONE,
            currency = "USD",
            merchantName = "M",
            merchantCategory = "C",
            country = "USA",
            city = "NYC",
            latitude = 0.0,
            longitude = 0.0,
            now = fixedInstant,
        )
        every { repo.findByTransactionId("TX-1") } returns tx

        handler.getByTransactionId("TX-1").transactionId shouldBe "TX-1"
    }

    @Test
    fun `getByTransactionId — 없으면 DomainNotFoundException을 던진다`() {
        val repo = mockk<TransactionPersistencePort>()
        val handler = TransactionHandler(repo, fixedClock)
        every { repo.findByTransactionId("MISSING") } returns null

        assertThrows<DomainNotFoundException> { handler.getByTransactionId("MISSING") }
    }
}
