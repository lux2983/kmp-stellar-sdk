package com.soneso.stellar.sdk.datalake

/**
 * Calculates S3/object storage paths for ledger data files.
 * Implements SEP-54 key format specification.
 */
internal object DataLakePathCalculator {
    private const val SEP54_TOKEN_MAX = 0xFFFFFFFFu

    fun getLedgerUrl(baseUrl: String, schema: DataLakeSchema, ledgerSequence: UInt): String {
        val filesPerPartition = schema.partitionSize
        val partition = calculatePartition(ledgerSequence, filesPerPartition)
        val file = calculateFile(ledgerSequence, schema.ledgersPerBatch.toUInt())
        return "$baseUrl$partition/$file"
    }

    private fun hexToken(value: UInt): String {
        val token = SEP54_TOKEN_MAX - value
        return token.toString(16).uppercase().padStart(8, '0')
    }

    private fun calculatePartition(ledgerSequence: UInt, filesPerPartition: UInt): String {
        val partitionStart = (ledgerSequence / filesPerPartition) * filesPerPartition
        val partitionEnd = partitionStart + filesPerPartition - 1u
        val partitionToken = hexToken(partitionStart)
        return "$partitionToken--$partitionStart-$partitionEnd"
    }

    private fun calculateFile(ledgerSequence: UInt, ledgersPerBatch: UInt): String {
        return if (ledgersPerBatch == 1u) {
            val fileToken = hexToken(ledgerSequence)
            "$fileToken--$ledgerSequence.xdr.zst"
        } else {
            val batchStart = (ledgerSequence / ledgersPerBatch) * ledgersPerBatch
            val batchEnd = batchStart + ledgersPerBatch - 1u
            val fileToken = hexToken(batchStart)
            "$fileToken--$batchStart-$batchEnd.xdr.zst"
        }
    }

    fun getBatchStartLedger(ledgerSequence: UInt, ledgersPerBatch: UInt): UInt {
        return (ledgerSequence / ledgersPerBatch) * ledgersPerBatch
    }
}
