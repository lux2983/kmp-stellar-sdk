//
//  OZRelayerClient.kt
//  Stellar SDK Kotlin Multiplatform
//
//  Created by Claude on 27.01.26.
//  Copyright © 2026 Soneso. All rights reserved.
//

package com.soneso.stellar.sdk.smartaccount.oz
import com.soneso.stellar.sdk.smartaccount.core.*

import com.soneso.stellar.sdk.xdr.HostFunctionXdr
import com.soneso.stellar.sdk.xdr.SorobanAuthorizationEntryXdr
import com.soneso.stellar.sdk.xdr.TransactionEnvelopeXdr
import com.soneso.stellar.sdk.xdr.toXdrBase64
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Response from the relayer service.
 *
 * The relayer wraps transactions with fee bumps and submits them to Stellar,
 * enabling gasless onboarding for users with empty wallets.
 *
 * Known error codes:
 * - INVALID_PARAMS: Request parameters are invalid
 * - INVALID_XDR: XDR encoding is malformed
 * - POOL_CAPACITY: Relayer pool is at capacity
 * - SIMULATION_FAILED: Transaction simulation failed
 * - ONCHAIN_FAILED: Transaction failed on-chain
 * - INVALID_TIME_BOUNDS: Transaction time bounds are invalid
 * - FEE_LIMIT_EXCEEDED: Transaction fee exceeds relayer limit
 * - UNAUTHORIZED: Request is not authorized
 * - TIMEOUT: Request timed out (client-side)
 *
 * @property success Indicates whether the transaction was successfully submitted
 * @property hash The transaction hash if submission succeeded
 * @property status The transaction status (e.g., "PENDING", "SUCCESS", "ERROR")
 * @property error Error message if the request failed
 * @property errorCode Error code if the request failed
 */
@Serializable
data class RelayerResponse(
    val success: Boolean,
    val hash: String? = null,
    val status: String? = null,
    val error: String? = null,
    val errorCode: String? = null
)

/**
 * Client for submitting transactions to an OpenZeppelin Smart Account relayer.
 *
 * The relayer provides fee sponsoring by wrapping user transactions with fee bumps,
 * enabling gasless onboarding and transactions for users with empty wallets.
 *
 * Two submission modes are supported:
 *
 * 1. **Host Function + Auth Entries**: Submit transaction components separately
 *    for the relayer to construct and wrap the full transaction.
 *
 * 2. **Signed Transaction XDR**: Submit a complete, signed transaction envelope
 *    for the relayer to wrap and submit.
 *
 * Example usage:
 * ```kotlin
 * val relayer = OZRelayerClient(relayerUrl = "https://relayer.example.com")
 *
 * // Mode 1: Submit host function and auth entries
 * val hostFunction = // ... HostFunctionXdr
 * val authEntries = // ... List<SorobanAuthorizationEntryXdr>
 * val response = relayer.send(hostFunction, authEntries)
 *
 * // Mode 2: Submit signed transaction XDR
 * val txEnvelope = // ... TransactionEnvelopeXdr
 * val response = relayer.sendXdr(txEnvelope)
 *
 * if (response.success) {
 *     println("Transaction hash: ${response.hash ?: "unknown"}")
 * } else {
 *     println("Error: ${response.error ?: "unknown"}")
 * }
 * ```
 *
 * @property relayerUrl The relayer endpoint URL
 * @property timeoutMs Request timeout in milliseconds (default: 6 minutes)
 * @property httpClient Optional custom HTTP client for testing
 */
class OZRelayerClient(
    private val relayerUrl: String,
    private val timeoutMs: Long = SmartAccountConstants.DEFAULT_RELAYER_TIMEOUT_MS,
    private val httpClient: HttpClient? = null
) {
    /**
     * JSON configuration for encoding/decoding requests and responses.
     */
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
    }

    // MARK: - Mode 1: Host Function + Auth Entries

    /**
     * Submits a transaction using host function and authorization entries.
     *
     * The relayer will construct a full transaction from these components,
     * wrap it with a fee bump, and submit it to the Stellar network.
     *
     * @param hostFunction The host function to execute
     * @param authEntries Authorization entries for the transaction
     * @return The relayer response with transaction hash or error
     * @throws TransactionException.SubmissionFailed if the request fails
     * @throws TransactionException.Timeout if the request times out
     */
    suspend fun send(
        hostFunction: HostFunctionXdr,
        authEntries: List<SorobanAuthorizationEntryXdr>
    ): RelayerResponse {
        // Encode host function to base64
        val funcBase64 = try {
            hostFunction.toXdrBase64()
        } catch (e: Exception) {
            throw TransactionException.submissionFailed(
                "Failed to encode host function to XDR",
                e
            )
        }

        // Encode auth entries to base64
        val authBase64Array = try {
            authEntries.map { it.toXdrBase64() }
        } catch (e: Exception) {
            throw TransactionException.submissionFailed(
                "Failed to encode auth entry to XDR",
                e
            )
        }

        // Build request payload
        val payload = mapOf(
            "func" to funcBase64,
            "auth" to authBase64Array
        )

        return performRequest(payload)
    }

    // MARK: - Mode 2: Signed Transaction XDR

    /**
     * Submits a complete signed transaction envelope.
     *
     * The relayer will wrap this transaction with a fee bump and submit it
     * to the Stellar network.
     *
     * @param transactionEnvelope TransactionEnvelope XDR to submit
     * @return The relayer response with transaction hash or error
     * @throws TransactionException.SubmissionFailed if the request fails
     * @throws TransactionException.Timeout if the request times out
     */
    suspend fun sendXdr(transactionEnvelope: TransactionEnvelopeXdr): RelayerResponse {
        // Encode transaction envelope to base64
        val xdrBase64 = try {
            transactionEnvelope.toXdrBase64()
        } catch (e: Exception) {
            throw TransactionException.submissionFailed(
                "Failed to encode transaction envelope to XDR",
                e
            )
        }

        // Build request payload
        val payload = mapOf(
            "xdr" to xdrBase64
        )

        return performRequest(payload)
    }

    // MARK: - Private Methods

    /**
     * Performs the HTTP request to the relayer.
     *
     * @param payload The JSON payload to send
     * @return The parsed relayer response
     * @throws TransactionException.SubmissionFailed if the request fails
     * @throws TransactionException.Timeout if the request times out
     */
    private suspend fun performRequest(payload: Map<String, Any>): RelayerResponse {
        return withHttpClient { client ->
            val response = try {
                client.post(relayerUrl) {
                    contentType(ContentType.Application.Json)
                    setBody(payload)
                    timeout {
                        requestTimeoutMillis = timeoutMs
                        connectTimeoutMillis = timeoutMs
                        socketTimeoutMillis = timeoutMs
                    }
                }
            } catch (e: HttpRequestTimeoutException) {
                throw TransactionException.timeout(
                    "Request timed out after ${timeoutMs}ms",
                    e
                )
            } catch (e: Exception) {
                throw TransactionException.submissionFailed(
                    "Network request failed: ${e.message}",
                    e
                )
            }

            // Handle non-200 status codes
            if (response.status.value != 200) {
                val errorMessage = try {
                    response.bodyAsText()
                } catch (e: Exception) {
                    response.status.description
                }

                throw TransactionException.submissionFailed(
                    "Relayer returned HTTP ${response.status.value}: $errorMessage"
                )
            }

            // Decode response
            val relayerResponse = try {
                val body = response.bodyAsText()
                json.decodeFromString<RelayerResponse>(body)
            } catch (e: Exception) {
                throw TransactionException.submissionFailed(
                    "Failed to decode relayer response",
                    e
                )
            }

            // Warn if success=true but hash is missing
            if (relayerResponse.success && relayerResponse.hash == null) {
                println("Warning: Relayer returned success=true but hash is null")
            }

            relayerResponse
        }
    }

    /**
     * Executes a block with an HTTP client, managing lifecycle properly.
     */
    private suspend fun <T> withHttpClient(block: suspend (HttpClient) -> T): T {
        val client = httpClient ?: HttpClient {
            install(ContentNegotiation) {
                json(json)
            }
            install(HttpTimeout) {
                connectTimeoutMillis = timeoutMs
                requestTimeoutMillis = timeoutMs
            }
        }

        return try {
            block(client)
        } finally {
            if (httpClient == null) {
                client.close()
            }
        }
    }
}
