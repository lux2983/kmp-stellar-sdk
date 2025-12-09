package com.soneso.stellar.sdk.datalake

import com.soneso.stellar.sdk.StrKey
import com.soneso.stellar.sdk.Util
import com.soneso.stellar.sdk.xdr.*

/**
 * Extension functions for XDR types used in data lake transaction and event extraction.
 * These helpers simplify working with complex XDR structures.
 */

// ByteArray extensions

/**
 * Converts a byte array to a lowercase hexadecimal string.
 */
internal fun ByteArray.toHexString(): String {
    return Util.bytesToHex(this)
}

/**
 * Converts a byte array (32 bytes) to a Stellar contract ID (C... address).
 */
internal fun ByteArray.toContractIdString(): String {
    require(size == 32) { "Contract ID must be 32 bytes" }
    return StrKey.encodeContract(this)
}

// TransactionEnvelopeXdr extensions

/**
 * Extracts the source account ID from a transaction envelope.
 */
internal fun TransactionEnvelopeXdr.getSourceAccountId(): String {
    return when (this) {
        is TransactionEnvelopeXdr.V0 -> {
            // V0 uses raw ed25519 public key
            StrKey.encodeEd25519PublicKey(value.tx.sourceAccountEd25519.value)
        }
        is TransactionEnvelopeXdr.V1 -> {
            // V1 uses MuxedAccount
            value.tx.sourceAccount.toAccountId()
        }
        is TransactionEnvelopeXdr.FeeBump -> {
            // FeeBump has inner transaction
            when (val innerTx = value.tx.innerTx) {
                is FeeBumpTransactionInnerTxXdr.V1 -> {
                    innerTx.value.tx.sourceAccount.toAccountId()
                }
            }
        }
    }
}

/**
 * Gets the number of operations in a transaction envelope.
 */
internal fun TransactionEnvelopeXdr.getOperationCount(): Int {
    return when (this) {
        is TransactionEnvelopeXdr.V0 -> value.tx.operations.size
        is TransactionEnvelopeXdr.V1 -> value.tx.operations.size
        is TransactionEnvelopeXdr.FeeBump -> {
            when (val innerTx = value.tx.innerTx) {
                is FeeBumpTransactionInnerTxXdr.V1 -> innerTx.value.tx.operations.size
            }
        }
    }
}

/**
 * Checks if a transaction invokes a specific contract.
 */
internal fun TransactionEnvelopeXdr.invokesContract(contractId: String): Boolean {
    val operations = when (this) {
        is TransactionEnvelopeXdr.V0 -> value.tx.operations
        is TransactionEnvelopeXdr.V1 -> value.tx.operations
        is TransactionEnvelopeXdr.FeeBump -> {
            when (val innerTx = value.tx.innerTx) {
                is FeeBumpTransactionInnerTxXdr.V1 -> innerTx.value.tx.operations
            }
        }
    }

    return operations.any { operation ->
        when (val body = operation.body) {
            is OperationBodyXdr.InvokeHostFunctionOp -> {
                // Check if this operation invokes the specified contract
                when (val hostFunction = body.value.hostFunction) {
                    is HostFunctionXdr.InvokeContract -> {
                        val invokedContractId = hostFunction.value.contractAddress.toContractId()
                        invokedContractId == contractId
                    }
                    else -> false
                }
            }
            else -> false
        }
    }
}

/**
 * Checks if a transaction has a specific operation type.
 */
internal fun TransactionEnvelopeXdr.hasOperationType(operationType: String): Boolean {
    val operations = when (this) {
        is TransactionEnvelopeXdr.V0 -> value.tx.operations
        is TransactionEnvelopeXdr.V1 -> value.tx.operations
        is TransactionEnvelopeXdr.FeeBump -> {
            when (val innerTx = value.tx.innerTx) {
                is FeeBumpTransactionInnerTxXdr.V1 -> innerTx.value.tx.operations
            }
        }
    }

    return operations.any { operation ->
        operation.body.discriminant.name == operationType
    }
}

/**
 * Converts a transaction envelope to XDR byte array.
 */
internal fun TransactionEnvelopeXdr.toXdrByteArray(): ByteArray {
    val writer = XdrWriter()
    this.encode(writer)
    return writer.toByteArray()
}

// MuxedAccountXdr extensions

/**
 * Converts a MuxedAccount to a Stellar account ID (G... or M... address).
 */
internal fun MuxedAccountXdr.toAccountId(): String {
    return when (this) {
        is MuxedAccountXdr.Ed25519 -> {
            StrKey.encodeEd25519PublicKey(value.value)
        }
        is MuxedAccountXdr.Med25519 -> {
            // Muxed account: 8 bytes ID + 32 bytes ed25519 = 40 bytes
            val bytes = ByteArray(40)
            // Big-endian encoding of ID
            val id = value.id.value
            bytes[0] = (id shr 56).toByte()
            bytes[1] = (id shr 48).toByte()
            bytes[2] = (id shr 40).toByte()
            bytes[3] = (id shr 32).toByte()
            bytes[4] = (id shr 24).toByte()
            bytes[5] = (id shr 16).toByte()
            bytes[6] = (id shr 8).toByte()
            bytes[7] = id.toByte()
            // Copy ed25519 public key
            value.ed25519.value.copyInto(bytes, 8)
            StrKey.encodeMed25519PublicKey(bytes)
        }
    }
}

// SCAddressXdr extensions

/**
 * Converts an SCAddress to a contract ID string (C... address).
 */
internal fun SCAddressXdr.toContractId(): String {
    return when (this) {
        is SCAddressXdr.AccountId -> {
            throw IllegalArgumentException("Cannot convert account address to contract ID")
        }
        is SCAddressXdr.ContractId -> {
            value.value.value.toContractIdString()
        }
        is SCAddressXdr.MuxedAccount -> {
            throw IllegalArgumentException("Cannot convert muxed account address to contract ID")
        }
        is SCAddressXdr.ClaimableBalanceId -> {
            throw IllegalArgumentException("Cannot convert claimable balance ID to contract ID")
        }
        is SCAddressXdr.LiquidityPoolId -> {
            throw IllegalArgumentException("Cannot convert liquidity pool ID to contract ID")
        }
    }
}

// TransactionResultResultXdr extensions

/**
 * Checks if a transaction result indicates success.
 */
internal fun TransactionResultResultXdr.isSuccess(): Boolean {
    return discriminant == TransactionResultCodeXdr.txSUCCESS ||
           discriminant == TransactionResultCodeXdr.txFEE_BUMP_INNER_SUCCESS
}

// TransactionResultXdr extensions

/**
 * Converts a transaction result to XDR byte array.
 */
internal fun TransactionResultXdr.toXdrByteArray(): ByteArray {
    val writer = XdrWriter()
    this.encode(writer)
    return writer.toByteArray()
}

// TransactionResultMetaXdr extensions

/**
 * Converts transaction result meta to XDR byte array.
 */
internal fun TransactionResultMetaXdr.toXdrByteArray(): ByteArray {
    val writer = XdrWriter()
    this.encode(writer)
    return writer.toByteArray()
}

// TransactionResultMetaV1Xdr extensions

/**
 * Converts transaction result meta V1 to XDR byte array.
 */
internal fun TransactionResultMetaV1Xdr.toXdrByteArray(): ByteArray {
    val writer = XdrWriter()
    this.encode(writer)
    return writer.toByteArray()
}

// TransactionMetaXdr extensions

/**
 * Extracts Soroban transaction meta from transaction meta if present.
 * Note: V4 uses SorobanTransactionMetaV2 which contains different structure.
 * For consistency, we only extract from V3 which uses SorobanTransactionMeta.
 */
internal fun TransactionMetaXdr.getSorobanMeta(): SorobanTransactionMetaXdr? {
    return when (this) {
        is TransactionMetaXdr.V3 -> value.sorobanMeta
        else -> null
    }
}

// ContractEventTypeXdr extensions

/**
 * Converts XDR ContractEventType to DataLake EventType.
 */
internal fun ContractEventTypeXdr.toEventType(): EventType {
    return when (this) {
        ContractEventTypeXdr.CONTRACT -> EventType.CONTRACT
        ContractEventTypeXdr.SYSTEM -> EventType.SYSTEM
        ContractEventTypeXdr.DIAGNOSTIC -> EventType.DIAGNOSTIC
    }
}

// ContractEventBodyXdr extensions

/**
 * Extracts topics from contract event body.
 */
internal fun ContractEventBodyXdr.getTopics(): List<SCValXdr> {
    return when (this) {
        is ContractEventBodyXdr.V0 -> value.topics
    }
}

/**
 * Extracts data from contract event body.
 */
internal fun ContractEventBodyXdr.getData(): SCValXdr {
    return when (this) {
        is ContractEventBodyXdr.V0 -> value.data
    }
}
