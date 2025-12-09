package com.soneso.stellar.sdk.datalake

import com.soneso.stellar.sdk.xdr.SCValXdr

data class DataLakeTransaction(
    val hash: String,
    val ledger: UInt,
    val ledgerCloseTime: ULong,
    val sourceAccount: String,
    val fee: Long,
    val operationCount: Int,
    val successful: Boolean,
    val envelopeXdr: ByteArray,
    val resultXdr: ByteArray,
    val resultMetaXdr: ByteArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as DataLakeTransaction
        return hash == other.hash
    }

    override fun hashCode(): Int = hash.hashCode()
}

/**
 * Represents a Soroban contract event from the data lake.
 *
 * @property ledger The ledger number where the event occurred
 * @property ledgerCloseTime Unix timestamp of ledger close
 * @property transactionHash Transaction hash that emitted the event
 * @property contractId Contract ID (C... address) that emitted the event.
 *                      Empty string for system events without a specific contract ID.
 * @property type Event type (CONTRACT, SYSTEM, or DIAGNOSTIC)
 * @property topics Event topics as XDR values
 * @property data Event data as XDR value
 */
data class DataLakeEvent(
    val ledger: UInt,
    val ledgerCloseTime: ULong,
    val transactionHash: String,
    val contractId: String,
    val type: EventType,
    val topics: List<SCValXdr>,
    val data: SCValXdr
)
