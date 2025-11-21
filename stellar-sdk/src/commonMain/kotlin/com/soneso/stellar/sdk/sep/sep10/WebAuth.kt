// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep10

import com.soneso.stellar.sdk.AbstractTransaction
import com.soneso.stellar.sdk.KeyPair
import com.soneso.stellar.sdk.MuxedAccount
import com.soneso.stellar.sdk.Network
import com.soneso.stellar.sdk.StrKey
import com.soneso.stellar.sdk.Transaction
import com.soneso.stellar.sdk.DecoratedSignature
import com.soneso.stellar.sdk.sep.sep01.StellarToml
import com.soneso.stellar.sdk.sep.sep10.exceptions.*
import com.soneso.stellar.sdk.xdr.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.ExperimentalTime

/**
 * Functional interface for delegating client domain transaction signing to external services.
 *
 * This enables integration with enterprise security solutions that cannot export private keys:
 * - Hardware Security Modules (HSMs) - Secure cryptographic key storage and operations
 * - Custodial services - Fireblocks, Ledger Vault, BitGo, Copper, AWS KMS, Google Cloud KMS
 * - Mobile secure enclaves - iOS Keychain with Secure Enclave, Android Keystore System
 * - Multi-Party Computation (MPC) systems - Distributed key management and signing
 *
 * Use Cases:
 * - Enterprise wallets that store keys in HSMs for regulatory compliance
 * - Custodial services where keys are managed by third-party providers
 * - Mobile applications using platform-specific secure key storage
 * - Organizations requiring hardware-backed key security
 * - Multi-signature setups with distributed key management
 *
 * Security Considerations:
 * - The delegate receives the full transaction XDR (not just a hash) for transparency
 * - External service can verify transaction contents before signing
 * - Signature includes hint (last 4 bytes of public key) for key identification
 * - Network passphrase is embedded in transaction hash to prevent replay attacks
 * - Only use with trusted signing services that validate transaction safety
 *
 * Example - HSM integration:
 * ```kotlin
 * val signingDelegate = ClientDomainSigningDelegate { transactionXdr ->
 *     // Send to HSM for signing
 *     val response = hsmClient.signTransaction(transactionXdr)
 *     response.decoratedSignatureXdr
 * }
 *
 * val token = webAuth.jwtToken(
 *     clientAccountId = userAccountId,
 *     signers = listOf(userKeyPair),
 *     clientDomain = "wallet.company.com",
 *     clientDomainSigningDelegate = signingDelegate
 * )
 * ```
 *
 * Example - AWS KMS integration:
 * ```kotlin
 * val signingDelegate = ClientDomainSigningDelegate { transactionXdr ->
 *     val tx = AbstractTransaction.fromEnvelopeXdr(transactionXdr, network)
 *     val txHash = tx.hash()
 *
 *     // Sign with AWS KMS
 *     val kmsResponse = kmsClient.sign(SignRequest.builder()
 *         .keyId(keyId)
 *         .message(SdkBytes.fromByteArray(txHash))
 *         .signingAlgorithm(SigningAlgorithmSpec.ECDSA_SHA_256)
 *         .build())
 *
 *     // Build decorated signature
 *     val publicKey = getPublicKeyFromKMS(keyId)
 *     val hint = publicKey.takeLast(4).toByteArray()
 *     val signature = kmsResponse.signature().asByteArray()
 *
 *     val decoratedSig = DecoratedSignature(hint, signature)
 *     val writer = XdrWriter()
 *     decoratedSig.toXdr().encode(writer)
 *     Base64.encode(writer.toByteArray())
 * }
 * ```
 *
 * Example - Mobile Secure Enclave (iOS):
 * ```kotlin
 * val signingDelegate = ClientDomainSigningDelegate { transactionXdr ->
 *     // Sign using iOS Keychain with Secure Enclave
 *     secureEnclaveManager.signTransaction(transactionXdr)
 * }
 * ```
 *
 * @param transactionXdr The base64-encoded transaction envelope XDR to sign
 * @return The decorated signature in base64-encoded XDR format
 * @throws Exception If signing fails (network error, HSM unavailable, permission denied, etc.)
 *
 * @see <a href="https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0010.md">SEP-10 Specification</a>
 */
fun interface ClientDomainSigningDelegate {
    /**
     * Signs a transaction using an external signing service.
     *
     * @param transactionXdr The base64-encoded transaction envelope XDR
     * @return Base64-encoded DecoratedSignature XDR
     */
    suspend fun signTransaction(transactionXdr: String): String
}

/**
 * SEP-10 Web Authentication client.
 *
 * Provides secure authentication for Stellar wallets and applications using a
 * challenge-response protocol with transaction signing. This implements the client
 * side of the SEP-10 specification for authenticating with Stellar services.
 *
 * SEP-10 Authentication Flow:
 * 1. Client requests a challenge transaction from the authentication server
 * 2. Server generates a transaction with specific security requirements and signs it
 * 3. Client validates the challenge transaction (13 security checks)
 * 4. Client signs the validated transaction with their keypair(s)
 * 5. Client submits the signed transaction back to the server
 * 6. Server verifies the signatures and returns a JWT token
 * 7. Client uses the JWT token to authenticate subsequent API requests
 *
 * The JWT token can be used to authenticate requests to SEP-6 (Deposit/Withdrawal),
 * SEP-12 (KYC), SEP-24 (Hosted Deposit/Withdrawal), SEP-31 (Cross-Border Payments),
 * and other Stellar services that require authentication.
 *
 * Usage Patterns:
 *
 * **High-Level API (Recommended for most use cases):**
 * ```kotlin
 * // Initialize from domain's stellar.toml
 * val webAuth = WebAuth.fromDomain("example.com", Network.PUBLIC)
 *
 * // One-line authentication
 * val authToken = webAuth.jwtToken(
 *     clientAccountId = userKeyPair.getAccountId(),
 *     signers = listOf(userKeyPair)
 * )
 *
 * // Use token in API requests
 * httpClient.get("https://example.com/api/account") {
 *     header("Authorization", "Bearer ${authToken.token}")
 * }
 * ```
 *
 * **Low-Level API (For custom flows or debugging):**
 * ```kotlin
 * val webAuth = WebAuth.fromDomain("example.com", Network.PUBLIC)
 *
 * // Step 1: Request challenge
 * val challenge = webAuth.getChallenge(clientAccountId = accountId)
 *
 * // Step 2: Validate challenge (critical security step)
 * webAuth.validateChallenge(
 *     challengeXdr = challenge.transaction,
 *     clientAccountId = accountId
 * )
 *
 * // Step 3: Sign challenge
 * val signedChallenge = webAuth.signTransaction(
 *     challengeXdr = challenge.transaction,
 *     signers = listOf(userKeyPair)
 * )
 *
 * // Step 4: Submit and get token
 * val authToken = webAuth.sendSignedChallenge(signedChallenge)
 * ```
 *
 * **Multi-Signature Accounts:**
 * ```kotlin
 * val webAuth = WebAuth.fromDomain("example.com", Network.PUBLIC)
 *
 * // Provide all required signers for a multi-sig account
 * val authToken = webAuth.jwtToken(
 *     clientAccountId = multiSigAccountId,
 *     signers = listOf(signer1KeyPair, signer2KeyPair, signer3KeyPair)
 * )
 * ```
 *
 * **Account with Memo (Sub-accounts):**
 * ```kotlin
 * // For custodial services with sub-accounts identified by memo
 * val authToken = webAuth.jwtToken(
 *     clientAccountId = custodialAccountId,
 *     signers = listOf(userKeyPair),
 *     memo = 12345  // User's memo ID
 * )
 * ```
 *
 * **Muxed Account (Modern Sub-accounts):**
 * ```kotlin
 * // For muxed accounts (M... addresses)
 * val authToken = webAuth.jwtToken(
 *     clientAccountId = "M...",  // Muxed account address
 *     signers = listOf(userKeyPair)
 * )
 * ```
 *
 * **Client Domain Verification (Local Signing):**
 * ```kotlin
 * // When your application wants to prove domain ownership to the server
 * val authToken = webAuth.jwtToken(
 *     clientAccountId = userAccountId,
 *     signers = listOf(userKeyPair),
 *     clientDomain = "wallet.mycompany.com",
 *     clientDomainKeyPair = clientDomainSigningKey
 * )
 * ```
 *
 * **Client Domain Verification (External Signing - HSM/Custody):**
 * ```kotlin
 * // When client domain key is in HSM or custody service
 * val signingDelegate = ClientDomainSigningDelegate { transactionXdr ->
 *     // Delegate to HSM or custody service
 *     hsmService.signTransaction(transactionXdr)
 * }
 *
 * val authToken = webAuth.jwtToken(
 *     clientAccountId = userAccountId,
 *     signers = listOf(userKeyPair),
 *     clientDomain = "wallet.mycompany.com",
 *     clientDomainSigningDelegate = signingDelegate
 * )
 * ```
 *
 * Security Considerations:
 * - Always validate the challenge before signing (done automatically in jwtToken())
 * - Verify HTTPS is used for all communication with the authentication endpoint
 * - Verify the server's signing key matches the stellar.toml SIGNING_KEY
 * - Never skip validation checks (all 13 checks are required)
 * - Store JWT tokens securely (encrypted storage, not in logs)
 * - Check token expiration before use
 * - Use [fromDomain] to automatically discover and verify server configuration
 *
 * Time Bounds and Grace Period:
 * - Challenge transactions have time bounds to prevent replay attacks
 * - The grace period (default 300 seconds / 5 minutes) accounts for:
 *   - Network latency between client and server
 *   - Clock skew between client and server
 *   - Time for user to review and sign the challenge
 * - Challenges are typically valid for 15 minutes
 * - If validation fails due to time bounds, request a fresh challenge
 *
 * See also:
 * - [SEP-10 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0010.md)
 * - [fromDomain] for automatic configuration from stellar.toml
 * - [jwtToken] for the complete high-level authentication flow
 * - [validateChallenge] for the 13 security validation checks
 *
 * @property authEndpoint The authentication endpoint URL (from stellar.toml WEB_AUTH_ENDPOINT)
 * @property network The Stellar network (Network.PUBLIC or Network.TESTNET)
 * @property serverSigningKey The server's public signing key (from stellar.toml SIGNING_KEY)
 * @property serverHomeDomain The home domain of the server (used in challenge validation)
 * @property gracePeriodSeconds Grace period for time bounds validation (default: 300 seconds)
 * @property clientDomainSigningDelegate Optional delegate for external client domain signing
 */
class WebAuth(
    val authEndpoint: String,
    val network: Network,
    val serverSigningKey: String,
    val serverHomeDomain: String,
    val gracePeriodSeconds: Int = 300,
    private val httpClient: HttpClient? = null,
    private val httpRequestHeaders: Map<String, String>? = null,
    val clientDomainSigningDelegate: ClientDomainSigningDelegate? = null
) {

    /**
     * JSON configuration for parsing server responses.
     */
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Lazy-initialized HTTP client for WebAuth requests.
     * Uses default configuration similar to HorizonServer.
     */
    private val client: HttpClient by lazy {
        httpClient ?: HttpClient {
            install(ContentNegotiation) {
                json(this@WebAuth.json)
            }
            install(HttpTimeout) {
                connectTimeoutMillis = 10_000
                requestTimeoutMillis = 30_000
                socketTimeoutMillis = 30_000
            }
            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 3)
                exponentialDelay()
            }
            install(DefaultRequest) {
                header("X-Client-Name", "kmp-stellar-sdk")
                header("X-Client-Version", com.soneso.stellar.sdk.Util.getSdkVersion())
            }
        }
    }

    companion object {
        /**
         * Creates a WebAuth instance by discovering configuration from a domain's stellar.toml.
         *
         * This is the recommended way to initialize WebAuth. It automatically fetches the
         * stellar.toml file from the specified domain and extracts the required configuration:
         * - WEB_AUTH_ENDPOINT: The authentication endpoint URL
         * - SIGNING_KEY: The server's public signing key for validating challenges
         *
         * The stellar.toml file is fetched from: https://{domain}/.well-known/stellar.toml
         *
         * This method ensures you're using the correct, up-to-date configuration published
         * by the service, reducing configuration errors and improving security.
         *
         * Example:
         * ```kotlin
         * // Initialize from testnet anchor
         * val webAuth = WebAuth.fromDomain(
         *     domain = "testanchor.stellar.org",
         *     network = Network.TESTNET
         * )
         *
         * // Authenticate a user
         * val token = webAuth.jwtToken(accountId, signers)
         * ```
         *
         * Example with error handling:
         * ```kotlin
         * try {
         *     val webAuth = WebAuth.fromDomain("example.com", Network.PUBLIC)
         *     // Use webAuth for authentication
         * } catch (e: ChallengeRequestException) {
         *     println("Failed to load stellar.toml or missing required fields: ${e.message}")
         * }
         * ```
         *
         * @param domain The domain name (without protocol). E.g., "example.com"
         * @param network The Stellar network (Network.PUBLIC or Network.TESTNET)
         * @param httpClient Optional custom HTTP client for testing or proxy configuration
         * @param httpRequestHeaders Optional custom HTTP headers to include in requests
         * @param clientDomainSigningDelegate Optional delegate for external client domain signing
         * @return WebAuth instance configured from the domain's stellar.toml
         * @throws ChallengeRequestException If stellar.toml is missing, invalid, or lacks required fields
         */
        suspend fun fromDomain(
            domain: String,
            network: Network,
            httpClient: HttpClient? = null,
            httpRequestHeaders: Map<String, String>? = null,
            clientDomainSigningDelegate: ClientDomainSigningDelegate? = null
        ): WebAuth {
            try {
                // Fetch and parse stellar.toml
                val stellarToml = StellarToml.fromDomain(
                    domain = domain,
                    httpClient = httpClient,
                    httpRequestHeaders = httpRequestHeaders
                )

                // Extract required fields
                val authEndpoint = stellarToml.generalInformation.webAuthEndpoint
                    ?: throw ChallengeRequestException(
                        "WEB_AUTH_ENDPOINT not found in stellar.toml for domain: $domain. " +
                                "The domain does not appear to support SEP-10 Web Authentication."
                    )

                val signingKey = stellarToml.generalInformation.signingKey
                    ?: throw ChallengeRequestException(
                        "SIGNING_KEY not found in stellar.toml for domain: $domain. " +
                                "The server signing key is required for SEP-10 authentication."
                    )

                return WebAuth(
                    authEndpoint = authEndpoint,
                    network = network,
                    serverSigningKey = signingKey,
                    serverHomeDomain = domain,
                    httpClient = httpClient,
                    httpRequestHeaders = httpRequestHeaders,
                    clientDomainSigningDelegate = clientDomainSigningDelegate
                )
            } catch (e: ChallengeRequestException) {
                // Re-throw our own exceptions
                throw e
            } catch (e: Exception) {
                // Wrap other exceptions
                throw ChallengeRequestException(
                    "Failed to initialize WebAuth from domain $domain: ${e.message}",
                    cause = e
                )
            }
        }
    }

    /**
     * Performs complete SEP-10 authentication flow.
     *
     * This is the high-level API that handles the entire challenge-response flow:
     * 1. Requests challenge from server
     * 2. Validates challenge transaction (13 security checks)
     * 3. Signs challenge with provided keypairs
     * 4. Submits signed challenge to server
     * 5. Returns JWT authentication token
     *
     * This method is recommended for most use cases as it handles all the complexity
     * of the SEP-10 protocol and performs all required security validations.
     *
     * Example - Basic authentication:
     * ```kotlin
     * val webAuth = WebAuth.fromDomain("example.com", Network.PUBLIC)
     * val userKeyPair = KeyPair.fromSecretSeed("S...")
     *
     * val authToken = webAuth.jwtToken(
     *     clientAccountId = userKeyPair.getAccountId(),
     *     signers = listOf(userKeyPair)
     * )
     *
     * println("Token: ${authToken.token}")
     * println("Expires: ${authToken.exp}")
     * ```
     *
     * Example - Multi-signature account:
     * ```kotlin
     * val authToken = webAuth.jwtToken(
     *     clientAccountId = "GACCOUNT...",
     *     signers = listOf(signer1, signer2, signer3)  // All required signers
     * )
     * ```
     *
     * Example - Account with memo:
     * ```kotlin
     * // For custodial services using memos for sub-accounts
     * val authToken = webAuth.jwtToken(
     *     clientAccountId = custodialAccountId,
     *     signers = listOf(userKeyPair),
     *     memo = 12345  // Sub-account identifier
     * )
     * ```
     *
     * Example - Client domain verification (local signing):
     * ```kotlin
     * val authToken = webAuth.jwtToken(
     *     clientAccountId = userAccountId,
     *     signers = listOf(userKeyPair),
     *     clientDomain = "wallet.mycompany.com",
     *     clientDomainKeyPair = clientDomainSigningKey
     * )
     * ```
     *
     * Example - Client domain verification (HSM/external signing):
     * ```kotlin
     * val authToken = webAuth.jwtToken(
     *     clientAccountId = userAccountId,
     *     signers = listOf(userKeyPair),
     *     clientDomain = "wallet.mycompany.com",
     *     clientDomainSigningDelegate = hsmSigningDelegate
     * )
     * ```
     *
     * @param clientAccountId Stellar account ID to authenticate (G... or M... address)
     * @param signers List of keypairs to sign the challenge (must include all required signers)
     * @param memo Optional ID memo for sub-account identification (used with G... addresses)
     * @param homeDomain Optional home domain for multi-domain authentication servers
     * @param clientDomain Optional client domain for domain verification
     * @param clientDomainKeyPair Optional keypair for local client domain signing (from client domain's stellar.toml SIGNING_KEY)
     * @param clientDomainSigningDelegate Optional delegate for external client domain signing (HSM, custody, etc.)
     * @return AuthToken containing JWT token and parsed claims
     * @throws ChallengeRequestException If challenge request fails
     * @throws com.soneso.stellar.sdk.sep.sep10.exceptions.ChallengeValidationException If challenge validation fails
     * @throws TokenSubmissionException If token submission fails
     * @throws IllegalArgumentException If signers list is empty or both clientDomainKeyPair and clientDomainSigningDelegate are provided
     */
    suspend fun jwtToken(
        clientAccountId: String,
        signers: List<KeyPair>,
        memo: Long? = null,
        homeDomain: String? = null,
        clientDomain: String? = null,
        clientDomainKeyPair: KeyPair? = null,
        clientDomainSigningDelegate: ClientDomainSigningDelegate? = null
    ): AuthToken {
        // Validate signers
        if (signers.isEmpty()) {
            throw IllegalArgumentException(
                "Signers list cannot be empty. At least one keypair is required to sign the challenge."
            )
        }

        // Validate client domain signing parameters
        if (clientDomainKeyPair != null && clientDomainSigningDelegate != null) {
            throw IllegalArgumentException(
                "Cannot specify both clientDomainKeyPair and clientDomainSigningDelegate. " +
                        "Use clientDomainKeyPair for local signing or clientDomainSigningDelegate for external signing."
            )
        }

        if (clientDomain != null && clientDomainKeyPair == null && clientDomainSigningDelegate == null) {
            throw IllegalArgumentException(
                "When clientDomain is specified, either clientDomainKeyPair or " +
                        "clientDomainSigningDelegate must be provided for signing."
            )
        }

        // Use instance-level delegate if not provided as parameter
        val effectiveDelegate = clientDomainSigningDelegate ?: this.clientDomainSigningDelegate

        // Step 1: Request challenge from server
        val challenge = getChallenge(
            clientAccountId = clientAccountId,
            memo = memo,
            homeDomain = homeDomain,
            clientDomain = clientDomain
        )

        // Step 2: Validate challenge transaction (13 security checks)
        // Extract clientDomainAccountId from client domain's stellar.toml if provided
        val clientDomainAccountId = if (clientDomain != null && (clientDomainKeyPair != null || effectiveDelegate != null)) {
            try {
                val clientToml = StellarToml.fromDomain(
                    domain = clientDomain,
                    httpClient = httpClient,
                    httpRequestHeaders = httpRequestHeaders
                )
                clientToml.generalInformation.signingKey
                    ?: throw GenericChallengeValidationException(
                        "SIGNING_KEY not found in stellar.toml for client domain: $clientDomain"
                    )
            } catch (e: Exception) {
                throw GenericChallengeValidationException(
                    "Failed to load stellar.toml for client domain $clientDomain: ${e.message}"
                )
            }
        } else {
            null
        }

        validateChallenge(
            challengeXdr = challenge.transaction,
            clientAccountId = clientAccountId,
            clientDomainAccountId = clientDomainAccountId,
            expectedMemo = memo
        )

        // Step 3: Sign challenge with all signers (including client domain keypair/delegate if provided)
        val signedChallengeXdr = signTransaction(
            challengeXdr = challenge.transaction,
            signers = signers,
            clientDomainKeyPair = clientDomainKeyPair,
            clientDomainSigningDelegate = effectiveDelegate
        )

        // Step 4: Submit signed challenge and get JWT token
        val authToken = sendSignedChallenge(signedChallengeXdr)

        // Step 5: Return the authentication token
        return authToken
    }

    /**
     * Requests a challenge transaction from the authentication server.
     *
     * This is the first step of the SEP-10 authentication flow. The server generates
     * a challenge transaction specifically for the client account and returns it as
     * base64-encoded XDR.
     *
     * The challenge transaction:
     * - Has sequence number 0 (cannot be submitted to network)
     * - Contains ManageData operations with authentication metadata
     * - Is already signed by the server
     * - Has time bounds to prevent replay attacks
     * - Includes the home domain in the first operation
     *
     * HTTP Request:
     * ```
     * GET {authEndpoint}?account={clientAccountId}[&memo={memo}][&home_domain={homeDomain}][&client_domain={clientDomain}]
     * ```
     *
     * Example - Basic challenge request:
     * ```kotlin
     * val challenge = webAuth.getChallenge(clientAccountId = "GACCOUNT...")
     * println("Challenge XDR: ${challenge.transaction}")
     * ```
     *
     * Example - With memo for sub-account:
     * ```kotlin
     * val challenge = webAuth.getChallenge(
     *     clientAccountId = custodialAccountId,
     *     memo = 12345
     * )
     * ```
     *
     * Example - With client domain:
     * ```kotlin
     * val challenge = webAuth.getChallenge(
     *     clientAccountId = "GACCOUNT...",
     *     clientDomain = "wallet.mycompany.com"
     * )
     * ```
     *
     * Security note: Always validate the returned challenge with [validateChallenge]
     * before signing it. Never sign an unvalidated challenge.
     *
     * @param clientAccountId Stellar account ID to authenticate (G... or M... address)
     * @param memo Optional ID memo for sub-account identification
     * @param homeDomain Optional home domain for multi-domain servers
     * @param clientDomain Optional client domain for domain verification
     * @return ChallengeResponse containing challenge transaction XDR
     * @throws ChallengeRequestException If the request fails or returns an error
     * @throws NoMemoForMuxedAccountsException If memo is provided with muxed account
     */
    suspend fun getChallenge(
        clientAccountId: String,
        memo: Long? = null,
        homeDomain: String? = null,
        clientDomain: String? = null
    ): ChallengeResponse {
        // 1. Validate inputs
        validateChallengeRequest(clientAccountId, memo)

        // 2. Build URL with query parameters
        val url = buildChallengeUrl(clientAccountId, memo, homeDomain, clientDomain)

        // 3. Make HTTP GET request
        val response: HttpResponse = try {
            client.get(url) {
                // Add custom headers if provided
                httpRequestHeaders?.forEach { (key, value) ->
                    header(key, value)
                }
            }
        } catch (e: Exception) {
            throw ChallengeRequestException(
                statusCode = 0,
                errorMessage = "Network error: ${e.message}",
                cause = e
            )
        }

        // 4. Handle HTTP status codes
        return when (response.status.value) {
            200 -> {
                // Parse successful response
                val bodyText = try {
                    response.bodyAsText()
                } catch (e: Exception) {
                    throw ChallengeRequestException(
                        statusCode = 200,
                        errorMessage = "Failed to read response body",
                        cause = e
                    )
                }

                val challengeResponse = try {
                    json.decodeFromString<ChallengeResponse>(bodyText)
                } catch (e: Exception) {
                    throw ChallengeRequestException(
                        statusCode = 200,
                        errorMessage = "Failed to parse JSON response: ${e.message}",
                        cause = e
                    )
                }

                // Validate response has transaction field
                if (challengeResponse.transaction.isBlank()) {
                    throw ChallengeRequestException(
                        statusCode = 200,
                        errorMessage = "Response missing required 'transaction' field"
                    )
                }

                challengeResponse
            }
            400 -> {
                val errorBody = try { response.bodyAsText() } catch (e: Exception) { "Unable to read error" }
                throw ChallengeRequestException(
                    statusCode = 400,
                    errorMessage = "Bad request: $errorBody"
                )
            }
            401 -> {
                val errorBody = try { response.bodyAsText() } catch (e: Exception) { "Unauthorized" }
                throw ChallengeRequestException(
                    statusCode = 401,
                    errorMessage = errorBody
                )
            }
            403 -> {
                val errorBody = try { response.bodyAsText() } catch (e: Exception) { "Forbidden" }
                throw ChallengeRequestException(
                    statusCode = 403,
                    errorMessage = errorBody
                )
            }
            404 -> {
                throw ChallengeRequestException(
                    statusCode = 404,
                    errorMessage = "WebAuth endpoint not found at: $authEndpoint"
                )
            }
            else -> {
                val errorBody = try { response.bodyAsText() } catch (e: Exception) { "" }
                throw ChallengeRequestException(
                    statusCode = response.status.value,
                    errorMessage = "Server error (${response.status.value}): ${response.status.description}" +
                        if (errorBody.isNotEmpty()) " - $errorBody" else ""
                )
            }
        }
    }

    /**
     * Validates input parameters for challenge request.
     *
     * @throws IllegalArgumentException If clientAccountId format is invalid
     * @throws NoMemoForMuxedAccountsException If memo is provided with muxed account
     */
    private fun validateChallengeRequest(clientAccountId: String, memo: Long?) {
        // Validate account ID format (G... or M...)
        val isValidAccount = StrKey.isValidEd25519PublicKey(clientAccountId)
        val isValidMuxedAccount = StrKey.isValidMed25519PublicKey(clientAccountId)

        if (!isValidAccount && !isValidMuxedAccount) {
            throw IllegalArgumentException(
                "Invalid clientAccountId: must be a valid Stellar account (G...) or muxed account (M...). " +
                "Received: ${clientAccountId.take(10)}..."
            )
        }

        // Check for memo with muxed account
        if (isValidMuxedAccount && memo != null) {
            throw NoMemoForMuxedAccountsException()
        }
    }

    /**
     * Builds the challenge request URL with query parameters.
     *
     * @return Complete URL with encoded query parameters
     */
    private fun buildChallengeUrl(
        clientAccountId: String,
        memo: Long?,
        homeDomain: String?,
        clientDomain: String?
    ): String {
        // Use Ktor's URL builder for proper encoding
        return URLBuilder(authEndpoint).apply {
            parameters.append("account", clientAccountId)
            memo?.let { parameters.append("memo", it.toString()) }
            homeDomain?.let { parameters.append("home_domain", it) }
            clientDomain?.let { parameters.append("client_domain", it) }
        }.buildString()
    }

    /**
     * Validates a challenge transaction according to SEP-10 security requirements.
     *
     * This is the MOST CRITICAL security step in SEP-10 authentication. This method
     * performs 13 required validation checks to ensure the challenge is safe to sign.
     *
     * Validation Checks:
     * 1. Transaction envelope type must be ENVELOPE_TYPE_TX
     * 2. Sequence number must be exactly 0
     * 3. Memo type, if present, must be MEMO_ID
     * 4. Memo value must match expected memo (if provided)
     * 5. Transaction cannot have both memo and muxed account
     * 6. All operations must be ManageData type
     * 7. First operation source must be client account
     * 8. First operation key must be "{serverHomeDomain} auth"
     * 9. Client domain operation source must match (if present)
     * 10. Web auth domain value must match endpoint host (if present)
     * 11. Time bounds must be set and current time must be within bounds
     * 12. Transaction must have exactly 1 signature (server's)
     * 13. Server signature must be valid
     *
     * Why validation is critical:
     * - Prevents man-in-the-middle attacks
     * - Prevents transaction replay attacks
     * - Ensures challenge cannot perform destructive operations
     * - Verifies server authenticity
     * - Protects against domain confusion attacks
     *
     * Example - Validate before signing:
     * ```kotlin
     * val challenge = webAuth.getChallenge(clientAccountId)
     *
     * try {
     *     webAuth.validateChallenge(
     *         challengeXdr = challenge.transaction,
     *         clientAccountId = clientAccountId
     *     )
     *     // Challenge is valid, safe to sign
     *     val signed = webAuth.signTransaction(challenge.transaction, signers)
     * } catch (e: InvalidSignatureException) {
     *     // CRITICAL: Server signature invalid, possible MITM attack
     *     throw SecurityException("Server signature invalid - DO NOT sign")
     * } catch (e: ChallengeValidationException) {
     *     // Other validation failure
     *     println("Challenge validation failed: ${e.message}")
     * }
     * ```
     *
     * @param challengeXdr Base64-encoded challenge transaction XDR
     * @param clientAccountId Expected client account ID (must match first operation source)
     * @param clientDomainAccountId Optional expected client domain account ID (if using client domain)
     * @param expectedMemo Optional expected memo value (must match transaction memo if provided)
     * @throws com.soneso.stellar.sdk.sep.sep10.exceptions.ChallengeValidationException If any validation check fails
     */
    @OptIn(ExperimentalEncodingApi::class)
    suspend fun validateChallenge(
        challengeXdr: String,
        clientAccountId: String,
        clientDomainAccountId: String? = null,
        expectedMemo: Long? = null
    ) {
        // Parse the envelope XDR
        val envelopeXdr = try {
            val bytes = Base64.decode(challengeXdr)
            val reader = XdrReader(bytes)
            TransactionEnvelopeXdr.decode(reader)
        } catch (e: Exception) {
            throw GenericChallengeValidationException("Invalid transaction XDR: ${e.message}")
        }

        // Check #1: Transaction type must be ENVELOPE_TYPE_TX
        if (envelopeXdr !is TransactionEnvelopeXdr.V1) {
            throw GenericChallengeValidationException(
                "Invalid transaction envelope type. Expected ENVELOPE_TYPE_TX, got ${envelopeXdr.discriminant}"
            )
        }

        val envelope = envelopeXdr.value
        val transaction = envelope.tx

        // Check #2: Sequence number must be exactly 0
        if (transaction.seqNum.value.value != 0L) {
            throw InvalidSequenceNumberException(transaction.seqNum.value.value)
        }

        // Extract memo if present
        val memo = transaction.memo
        val memoId = when (memo) {
            is MemoXdr.Id -> memo.value.value.toLong()
            else -> null
        }

        // Check #3: Memo type, if present, must be MEMO_ID
        if (memo !is MemoXdr.Void && memo !is MemoXdr.Id) {
            throw InvalidMemoTypeException(
                "Invalid memo type. Expected MEMO_NONE or MEMO_ID, got ${memo.discriminant}"
            )
        }

        // Check #4: Memo value must match expected memo (if provided)
        if (expectedMemo != null) {
            if (memoId == null) {
                throw InvalidMemoValueException(expectedMemo, null)
            }
            if (memoId != expectedMemo) {
                throw InvalidMemoValueException(expectedMemo, memoId)
            }
        }

        // Check #5: Transaction cannot have both memo and muxed account
        if (memoId != null && StrKey.isValidMed25519PublicKey(clientAccountId)) {
            throw MemoWithMuxedAccountException(clientAccountId, memoId)
        }

        // Validate operations (checks #6-10)
        validateOperations(transaction.operations, clientAccountId, clientDomainAccountId)

        // Check #11: Time bounds validation
        validateTimeBounds(transaction.cond)

        // Check #12: Signature count must be exactly 1
        val signatures = envelope.signatures
        if (signatures.size != 1) {
            throw InvalidSignatureCountException(signatures.size)
        }

        // Check #13: Server signature must be valid
        validateServerSignature(challengeXdr, signatures[0])
    }

    /**
     * Validates all operations in the challenge transaction.
     *
     * Performs checks #6-10:
     * - All operations must be ManageData type
     * - First operation source must match client account
     * - First operation key must be "{serverHomeDomain} auth"
     * - Client domain operation source must match (if present)
     * - Web auth domain value must match endpoint host (if present)
     */
    private fun validateOperations(
        operations: List<OperationXdr>,
        clientAccountId: String,
        clientDomainAccountId: String?
    ) {
        if (operations.isEmpty()) {
            throw GenericChallengeValidationException("Transaction must have at least one operation")
        }

        operations.forEachIndexed { index, operation ->
            // Check #6: All operations must be ManageData type
            val manageDataOp = when (operation.body) {
                is OperationBodyXdr.ManageDataOp -> operation.body.value
                else -> throw InvalidOperationTypeException(operation.body.discriminant.toString(), index)
            }

            // Get operation source account
            val operationSource = operation.sourceAccount
                ?: throw InvalidSourceAccountException("Expected source account", null)

            val operationAccountId = MuxedAccount.fromXdr(operationSource).accountId

            if (index == 0) {
                // Check #7: First operation source must be client account
                val normalizedClientAccountId = if (StrKey.isValidMed25519PublicKey(clientAccountId)) {
                    // For muxed accounts, extract the underlying G... account
                    MuxedAccount(clientAccountId).accountId
                } else {
                    clientAccountId
                }

                if (operationAccountId != normalizedClientAccountId) {
                    throw InvalidSourceAccountException(normalizedClientAccountId, operationAccountId)
                }

                // Check #8: First operation key must be "{serverHomeDomain} auth"
                val dataName = manageDataOp.dataName.value
                val expectedDataName = "$serverHomeDomain auth"
                if (dataName != expectedDataName) {
                    throw InvalidHomeDomainException(serverHomeDomain, dataName)
                }
            } else {
                // For operations after the first, validate based on data name
                val dataName = manageDataOp.dataName.value

                when (dataName) {
                    "client_domain" -> {
                        // Check #9: Client domain operation source must match
                        if (clientDomainAccountId != null) {
                            if (operationAccountId != clientDomainAccountId) {
                                throw InvalidClientDomainSourceException(clientDomainAccountId, operationAccountId)
                            }
                        }
                    }
                    "web_auth_domain" -> {
                        // Check #10: Web auth domain value must match endpoint host
                        val dataValue = manageDataOp.dataValue
                            ?: throw InvalidWebAuthDomainException(authEndpoint, "")

                        val webAuthDomain = dataValue.value.decodeToString()
                        val endpointHost = try {
                            Url(authEndpoint).host
                        } catch (e: Exception) {
                            throw InvalidWebAuthDomainException(authEndpoint, webAuthDomain)
                        }

                        if (webAuthDomain != endpointHost) {
                            throw InvalidWebAuthDomainException(endpointHost, webAuthDomain)
                        }
                    }
                    else -> {
                        // Other operations must have server as source
                        if (operationAccountId != serverSigningKey) {
                            throw InvalidSourceAccountException(serverSigningKey, operationAccountId)
                        }
                    }
                }
            }
        }
    }

    /**
     * Validates time bounds of the challenge transaction.
     *
     * Performs check #11:
     * - Time bounds must be set
     * - Current time must be within bounds (with grace period)
     */
    @OptIn(ExperimentalTime::class)
    private fun validateTimeBounds(preconditions: PreconditionsXdr) {
        val currentTime = kotlin.time.Clock.System.now().toEpochMilliseconds() / 1000

        // Extract time bounds from preconditions
        val timeBounds = when (preconditions) {
            is PreconditionsXdr.TimeBounds -> preconditions.value
            is PreconditionsXdr.V2 -> preconditions.value.timeBounds
                ?: throw InvalidTimeBoundsException(null, null, currentTime, gracePeriodSeconds)
            is PreconditionsXdr.Void -> throw InvalidTimeBoundsException(null, null, currentTime, gracePeriodSeconds)
        }

        val minTime = timeBounds.minTime.value.value.toLong()
        val maxTime = timeBounds.maxTime.value.value.toLong()

        // Check if current time is within grace period
        if (currentTime < minTime - gracePeriodSeconds || currentTime > maxTime + gracePeriodSeconds) {
            throw InvalidTimeBoundsException(minTime, maxTime, currentTime, gracePeriodSeconds)
        }
    }

    /**
     * Validates the server's signature on the challenge transaction.
     *
     * Performs check #13:
     * - Server signature must be valid
     */
    private suspend fun validateServerSignature(
        challengeXdr: String,
        signature: DecoratedSignatureXdr
    ) {
        // Parse transaction to get hash
        val transaction = try {
            AbstractTransaction.fromEnvelopeXdr(challengeXdr, network)
        } catch (e: Exception) {
            throw InvalidSignatureException(serverSigningKey)
        }

        // Get transaction hash
        val transactionHash = transaction.hash()

        // Verify signature
        val serverKeyPair = try {
            KeyPair.fromAccountId(serverSigningKey)
        } catch (e: Exception) {
            throw InvalidSignatureException(serverSigningKey)
        }

        val isValid = try {
            serverKeyPair.verify(transactionHash, signature.signature.value)
        } catch (e: Exception) {
            throw InvalidSignatureException(serverSigningKey)
        }

        if (!isValid) {
            throw InvalidSignatureException(serverSigningKey)
        }
    }

    /**
     * Signs a challenge transaction with provided keypairs.
     *
     * Takes a validated challenge transaction and adds signatures from the provided
     * keypairs. The server's original signature is preserved, and new signatures are
     * appended.
     *
     * For multi-signature accounts, all required signers must be provided to meet
     * the account's signing threshold.
     *
     * The signing process:
     * 1. Parse challenge XDR to transaction envelope
     * 2. Compute transaction hash for the network
     * 3. Preserve existing signatures (server's signature)
     * 4. Sign transaction hash with each provided keypair
     * 5. Optionally sign with client domain keypair or delegate
     * 6. Append new signatures to the envelope
     * 7. Return updated envelope as base64 XDR
     *
     * Example - Single signature:
     * ```kotlin
     * val signedChallenge = webAuth.signTransaction(
     *     challengeXdr = challenge.transaction,
     *     signers = listOf(userKeyPair)
     * )
     * ```
     *
     * Example - Multi-signature account:
     * ```kotlin
     * val signedChallenge = webAuth.signTransaction(
     *     challengeXdr = challenge.transaction,
     *     signers = listOf(signer1, signer2, signer3)
     * )
     * ```
     *
     * Example - With client domain (local signing):
     * ```kotlin
     * val signedChallenge = webAuth.signTransaction(
     *     challengeXdr = challenge.transaction,
     *     signers = listOf(userKeyPair),
     *     clientDomainKeyPair = clientDomainKeyPair
     * )
     * ```
     *
     * Example - With client domain (external signing):
     * ```kotlin
     * val signedChallenge = webAuth.signTransaction(
     *     challengeXdr = challenge.transaction,
     *     signers = listOf(userKeyPair),
     *     clientDomainSigningDelegate = hsmDelegate
     * )
     * ```
     *
     * Security warning: Only sign validated challenges. Always call [validateChallenge]
     * before calling this method.
     *
     * @param challengeXdr Base64-encoded challenge transaction XDR
     * @param signers List of keypairs to sign with (must have private keys)
     * @param clientDomainKeyPair Optional keypair for local client domain signing
     * @param clientDomainSigningDelegate Optional delegate for external client domain signing
     * @return Base64-encoded signed transaction XDR
     * @throws IllegalArgumentException If parsing fails or transaction is invalid, or both signing methods provided
     * @throws GenericChallengeValidationException If transaction type is invalid
     */
    @OptIn(ExperimentalEncodingApi::class)
    suspend fun signTransaction(
        challengeXdr: String,
        signers: List<KeyPair>,
        clientDomainKeyPair: KeyPair? = null,
        clientDomainSigningDelegate: ClientDomainSigningDelegate? = null
    ): String {
        // Validate that both signing methods are not provided
        if (clientDomainKeyPair != null && clientDomainSigningDelegate != null) {
            throw IllegalArgumentException(
                "Cannot specify both clientDomainKeyPair and clientDomainSigningDelegate. " +
                        "Use one or the other for client domain signing."
            )
        }

        // Parse transaction from base64 XDR
        val transaction = try {
            AbstractTransaction.fromEnvelopeXdr(challengeXdr, network)
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Failed to parse challenge transaction: ${e.message}",
                e
            )
        }

        // Must be standard Transaction (not FeeBumpTransaction)
        if (transaction !is Transaction) {
            throw GenericChallengeValidationException(
                "Challenge must be a standard Transaction, not FeeBumpTransaction"
            )
        }

        // Validate envelope type - must be V1 (ENVELOPE_TYPE_TX)
        if (transaction.envelopeType != EnvelopeTypeXdr.ENVELOPE_TYPE_TX) {
            throw GenericChallengeValidationException(
                "Invalid transaction envelope type: ${transaction.envelopeType}. " +
                "SEP-10 challenges must use ENVELOPE_TYPE_TX (V1)"
            )
        }

        // Calculate transaction hash for signing
        val txHash = transaction.hash()

        // Sign with each provided keypair and add signatures
        // Note: transaction.signatures already contains the server signature
        // We append client signatures to preserve the server signature
        for (signer in signers) {
            val decoratedSignature = signer.signDecorated(txHash)
            transaction.signatures.add(decoratedSignature)
        }

        // Add client domain signature if provided
        if (clientDomainKeyPair != null) {
            val clientDomainSignature = clientDomainKeyPair.signDecorated(txHash)
            transaction.signatures.add(clientDomainSignature)
        } else if (clientDomainSigningDelegate != null) {
            // Delegate signing to external service
            val transactionXdr = transaction.toEnvelopeXdrBase64()
            val decoratedSignatureXdr = clientDomainSigningDelegate.signTransaction(transactionXdr)

            // Parse and add the decorated signature
            try {
                val sigBytes = Base64.decode(decoratedSignatureXdr)
                val reader = XdrReader(sigBytes)
                val decoratedSigXdr = DecoratedSignatureXdr.decode(reader)
                val decoratedSignature = DecoratedSignature.fromXdr(decoratedSigXdr)
                transaction.signatures.add(decoratedSignature)
            } catch (e: Exception) {
                throw IllegalArgumentException(
                    "Failed to parse decorated signature from delegate: ${e.message}",
                    e
                )
            }
        }

        // Convert back to base64 XDR
        return transaction.toEnvelopeXdrBase64()
    }

    /**
     * Submits a signed challenge transaction to obtain JWT token.
     *
     * This is the final step of the SEP-10 authentication flow. The signed challenge
     * is sent to the server, which verifies the signatures and returns a JWT token.
     *
     * HTTP Request:
     * ```
     * POST {authEndpoint}
     * Content-Type: application/json
     *
     * {
     *   "transaction": "base64_signed_challenge_xdr"
     * }
     * ```
     *
     * Server verification:
     * 1. Validates transaction structure (same checks as client)
     * 2. Verifies time bounds are still valid
     * 3. Verifies client signature(s) are valid
     * 4. Checks signing weight meets account threshold
     * 5. Generates and signs JWT token
     * 6. Returns token in response
     *
     * Example - Submit signed challenge:
     * ```kotlin
     * val authToken = webAuth.sendSignedChallenge(signedChallengeXdr)
     *
     * println("Token: ${authToken.token}")
     * println("Account: ${authToken.account}")
     * println("Expires: ${authToken.exp}")
     *
     * // Use token in API requests
     * httpClient.get("https://example.com/api/account") {
     *     header("Authorization", "Bearer ${authToken.token}")
     * }
     * ```
     *
     * Example - Handle submission errors:
     * ```kotlin
     * try {
     *     val authToken = webAuth.sendSignedChallenge(signedChallenge)
     * } catch (e: TokenSubmissionException) {
     *     when {
     *         e.message?.contains("401") == true -> {
     *             // Signature verification failed
     *             println("Invalid signatures or insufficient signing weight")
     *         }
     *         e.message?.contains("400") == true -> {
     *             // Invalid transaction
     *             println("Malformed transaction or expired challenge")
     *         }
     *     }
     * }
     * ```
     *
     * @param signedChallengeXdr Base64-encoded signed challenge XDR
     * @return AuthToken containing JWT token and parsed claims
     * @throws TokenSubmissionException If submission fails or server returns an error
     */
    suspend fun sendSignedChallenge(
        signedChallengeXdr: String
    ): AuthToken {
        // 1. Validate input
        if (signedChallengeXdr.isBlank()) {
            throw IllegalArgumentException("Signed challenge XDR cannot be empty")
        }

        // 2. Create request body
        val requestBody = TokenSubmissionRequest(transaction = signedChallengeXdr)

        // 3. Make HTTP POST request
        val response: HttpResponse = try {
            client.post(authEndpoint) {
                contentType(ContentType.Application.Json)
                // Add custom headers if provided
                httpRequestHeaders?.forEach { (key, value) ->
                    header(key, value)
                }
                setBody(requestBody)
            }
        } catch (e: Exception) {
            throw TokenSubmissionException(
                message = "Network error during token submission: ${e.message}",
                cause = e
            )
        }

        // 4. Handle HTTP response
        return when (response.status.value) {
            200 -> {
                // Parse successful response
                val bodyText = try {
                    response.bodyAsText()
                } catch (e: Exception) {
                    throw TokenSubmissionException(
                        message = "Failed to read response body: ${e.message}",
                        cause = e
                    )
                }

                val tokenResponse = try {
                    json.decodeFromString<TokenSubmissionResponse>(bodyText)
                } catch (e: Exception) {
                    throw TokenSubmissionException(
                        message = "Failed to parse JSON response: ${e.message}",
                        cause = e
                    )
                }

                // Validate response has token field
                if (tokenResponse.token.isBlank()) {
                    throw TokenSubmissionException(
                        message = "Response missing required 'token' field"
                    )
                }

                // Parse and return AuthToken
                try {
                    AuthToken.parse(tokenResponse.token)
                } catch (e: Exception) {
                    throw TokenSubmissionException(
                        message = "Failed to parse JWT token: ${e.message}",
                        cause = e
                    )
                }
            }
            400 -> {
                val errorBody = try { response.bodyAsText() } catch (e: Exception) { "Bad Request" }
                throw TokenSubmissionException(
                    message = "Bad request (400): Invalid transaction format - $errorBody"
                )
            }
            401 -> {
                val errorBody = try { response.bodyAsText() } catch (e: Exception) { "Unauthorized" }
                throw TokenSubmissionException(
                    message = "Unauthorized (401): Signature verification failed - $errorBody"
                )
            }
            403 -> {
                val errorBody = try { response.bodyAsText() } catch (e: Exception) { "Forbidden" }
                throw TokenSubmissionException(
                    message = "Forbidden (403): Not authorized - $errorBody"
                )
            }
            404 -> {
                throw TokenSubmissionException(
                    message = "Not Found (404): WebAuth endpoint not found at: $authEndpoint"
                )
            }
            in 500..599 -> {
                val errorBody = try { response.bodyAsText() } catch (e: Exception) { "" }
                throw TokenSubmissionException(
                    message = "Server error (${response.status.value}): ${response.status.description}" +
                        if (errorBody.isNotEmpty()) " - $errorBody" else ""
                )
            }
            else -> {
                val errorBody = try { response.bodyAsText() } catch (e: Exception) { "" }
                throw TokenSubmissionException(
                    message = "Unexpected response (${response.status.value}): ${response.status.description}" +
                        if (errorBody.isNotEmpty()) " - $errorBody" else ""
                )
            }
        }
    }
}
