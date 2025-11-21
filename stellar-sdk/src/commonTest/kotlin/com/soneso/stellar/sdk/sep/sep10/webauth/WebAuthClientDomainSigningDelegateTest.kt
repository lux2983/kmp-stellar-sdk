// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep10.webauth

import com.soneso.stellar.sdk.*
import com.soneso.stellar.sdk.sep.sep10.ClientDomainSigningDelegate
import com.soneso.stellar.sdk.sep.sep10.WebAuth
import com.soneso.stellar.sdk.xdr.*
import kotlinx.coroutines.test.runTest
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.*
import kotlin.time.ExperimentalTime

/**
 * Tests for ClientDomainSigningDelegate functionality.
 *
 * Covers external signing delegation for enterprise use cases:
 * - HSMs (Hardware Security Modules)
 * - Custodial services (Fireblocks, AWS KMS, etc.)
 * - Mobile secure enclaves
 * - Multi-Party Computation (MPC) systems
 */
class WebAuthClientDomainSigningDelegateTest {

    private val testServerSecretSeed = "SDJHRQF4GCMIIKAAAQ6IHY42X73FQFLHUULAPSKKD4DFDM7UXWWCRHBE"
    private val testHomeDomain = "testanchor.stellar.org"
    private val testAuthEndpoint = "https://testanchor.stellar.org/auth"
    private val network = Network.TESTNET

    /**
     * Creates a valid challenge transaction XDR for signing tests.
     */
    @OptIn(ExperimentalTime::class)
    private suspend fun createValidChallengeXdr(
        clientKeyPair: KeyPair,
        serverSecretSeed: String = testServerSecretSeed,
        homeDomain: String = testHomeDomain
    ): String {
        val serverKeyPair = KeyPair.fromSecretSeed(serverSecretSeed)
        val sourceAccount = Account(serverKeyPair.getAccountId(), -1L)

        val currentTime = kotlin.time.Clock.System.now().toEpochMilliseconds() / 1000
        val builder = TransactionBuilder(sourceAccount, network)
            .setBaseFee(100)
            .addTimeBounds(
                TimeBounds(
                    minTime = currentTime - 300,
                    maxTime = currentTime + 600
                )
            )

        // First operation: home domain auth
        val authOp = ManageDataOperation(
            name = "$homeDomain auth",
            value = "test_value".encodeToByteArray()
        )
        authOp.sourceAccount = clientKeyPair.getAccountId()
        builder.addOperation(authOp)

        val transaction = builder.build()
        transaction.sign(serverKeyPair)

        return transaction.toEnvelopeXdrBase64()
    }

    @Test
    fun testSignTransactionWithDelegateSuccess() = runTest {
        val serverKeyPair = KeyPair.fromSecretSeed(testServerSecretSeed)
        val clientKeyPair = KeyPair.random()
        val clientDomainKeyPair = KeyPair.random()

        // Create valid challenge
        val challengeXdr = createValidChallengeXdr(clientKeyPair = clientKeyPair)

        // Mock signing delegate that simulates HSM/custody service
        val signingDelegate = ClientDomainSigningDelegate { transactionXdr ->
            // Parse transaction to get hash (like a real HSM would)
            val tx = AbstractTransaction.fromEnvelopeXdr(transactionXdr, network)
            val txHash = tx.hash()

            // Sign with client domain key
            val signature = clientDomainKeyPair.signDecorated(txHash)

            // Return decorated signature as base64 XDR
            val writer = XdrWriter()
            signature.toXdr().encode(writer)
            Base64.encode(writer.toByteArray())
        }

        val webAuth = WebAuth(
            authEndpoint = testAuthEndpoint,
            network = network,
            serverSigningKey = serverKeyPair.getAccountId(),
            serverHomeDomain = testHomeDomain
        )

        // Sign with user keypair and delegate
        val signedChallengeXdr = webAuth.signTransaction(
            challengeXdr = challengeXdr,
            signers = listOf(clientKeyPair),
            clientDomainSigningDelegate = signingDelegate
        )

        // Parse signed transaction
        val signedTx = AbstractTransaction.fromEnvelopeXdr(signedChallengeXdr, network) as Transaction

        // Verify: Should have 3 signatures (server + client + client domain)
        assertEquals(3, signedTx.signatures.size, "Should have 3 signatures (server + client + client domain)")

        // Verify server signature
        val txHash = signedTx.hash()
        assertTrue(
            serverKeyPair.verify(txHash, signedTx.signatures[0].signature),
            "Server signature should be valid"
        )

        // Verify client signature
        assertTrue(
            clientKeyPair.verify(txHash, signedTx.signatures[1].signature),
            "Client signature should be valid"
        )

        // Verify client domain signature (from delegate)
        assertTrue(
            clientDomainKeyPair.verify(txHash, signedTx.signatures[2].signature),
            "Client domain signature from delegate should be valid"
        )
    }

    @Test
    fun testBothClientDomainKeyPairAndDelegateProvided() = runTest {
        val serverKeyPair = KeyPair.fromSecretSeed(testServerSecretSeed)
        val clientKeyPair = KeyPair.random()
        val clientDomainKeyPair = KeyPair.random()

        val challengeXdr = createValidChallengeXdr(clientKeyPair = clientKeyPair)

        val signingDelegate = ClientDomainSigningDelegate { _ ->
            throw IllegalStateException("Should not be called")
        }

        val webAuth = WebAuth(
            authEndpoint = testAuthEndpoint,
            network = network,
            serverSigningKey = serverKeyPair.getAccountId(),
            serverHomeDomain = testHomeDomain
        )

        // Should throw when both are provided
        val exception = assertFailsWith<IllegalArgumentException> {
            webAuth.signTransaction(
                challengeXdr = challengeXdr,
                signers = listOf(clientKeyPair),
                clientDomainKeyPair = clientDomainKeyPair,
                clientDomainSigningDelegate = signingDelegate
            )
        }

        assertTrue(
            exception.message?.contains("Cannot specify both") == true,
            "Error message should explain mutual exclusivity"
        )
        assertTrue(
            exception.message?.contains("clientDomainKeyPair") == true,
            "Error message should mention clientDomainKeyPair"
        )
        assertTrue(
            exception.message?.contains("clientDomainSigningDelegate") == true,
            "Error message should mention clientDomainSigningDelegate"
        )
    }

    @Test
    fun testClientDomainRequiresSigningMethod() = runTest {
        val clientKeyPair = KeyPair.random()

        val webAuth = WebAuth(
            authEndpoint = testAuthEndpoint,
            network = network,
            serverSigningKey = KeyPair.fromSecretSeed(testServerSecretSeed).getAccountId(),
            serverHomeDomain = testHomeDomain
        )

        // Should throw when clientDomain provided without signing method
        val exception = assertFailsWith<IllegalArgumentException> {
            webAuth.jwtToken(
                clientAccountId = clientKeyPair.getAccountId(),
                signers = listOf(clientKeyPair),
                clientDomain = "wallet.example.com"
                // Missing: clientDomainKeyPair and clientDomainSigningDelegate
            )
        }

        assertTrue(
            exception.message?.contains("clientDomain") == true,
            "Error message should mention clientDomain"
        )
        assertTrue(
            exception.message?.contains("either clientDomainKeyPair or clientDomainSigningDelegate") == true,
            "Error message should explain requirement"
        )
    }

    @Test
    @OptIn(ExperimentalEncodingApi::class)
    fun testDelegateInvalidSignatureFormat() = runTest {
        val serverKeyPair = KeyPair.fromSecretSeed(testServerSecretSeed)
        val clientKeyPair = KeyPair.random()

        val challengeXdr = createValidChallengeXdr(clientKeyPair = clientKeyPair)

        // Delegate that returns invalid base64
        val invalidDelegate = ClientDomainSigningDelegate { _ ->
            "INVALID_BASE64_XDR!!!"
        }

        val webAuth = WebAuth(
            authEndpoint = testAuthEndpoint,
            network = network,
            serverSigningKey = serverKeyPair.getAccountId(),
            serverHomeDomain = testHomeDomain
        )

        // Should throw when delegate returns invalid data
        assertFailsWith<IllegalArgumentException> {
            webAuth.signTransaction(
                challengeXdr = challengeXdr,
                signers = listOf(clientKeyPair),
                clientDomainSigningDelegate = invalidDelegate
            )
        }
    }

    @Test
    @OptIn(ExperimentalEncodingApi::class)
    fun testDelegateMalformedXdr() = runTest {
        val serverKeyPair = KeyPair.fromSecretSeed(testServerSecretSeed)
        val clientKeyPair = KeyPair.random()

        val challengeXdr = createValidChallengeXdr(clientKeyPair = clientKeyPair)

        // Delegate that returns valid base64 but too short to be valid XDR
        val malformedDelegate = ClientDomainSigningDelegate { _ ->
            // Valid base64 but only 1 byte (too short for any XDR structure)
            Base64.encode(byteArrayOf(0x00))
        }

        val webAuth = WebAuth(
            authEndpoint = testAuthEndpoint,
            network = network,
            serverSigningKey = serverKeyPair.getAccountId(),
            serverHomeDomain = testHomeDomain
        )

        // Should throw when delegate returns malformed XDR
        assertFailsWith<IllegalArgumentException> {
            webAuth.signTransaction(
                challengeXdr = challengeXdr,
                signers = listOf(clientKeyPair),
                clientDomainSigningDelegate = malformedDelegate
            )
        }
    }

    @Test
    fun testDelegateWrongSignature() = runTest {
        val serverKeyPair = KeyPair.fromSecretSeed(testServerSecretSeed)
        val clientKeyPair = KeyPair.random()
        val wrongKeyPair = KeyPair.random() // Different key than expected

        val challengeXdr = createValidChallengeXdr(clientKeyPair = clientKeyPair)

        // Delegate that signs with wrong key
        val wrongSignatureDelegate = ClientDomainSigningDelegate { transactionXdr ->
            val tx = AbstractTransaction.fromEnvelopeXdr(transactionXdr, network)
            val txHash = tx.hash()

            // Sign with WRONG key
            val signature = wrongKeyPair.signDecorated(txHash)

            val writer = XdrWriter()
            signature.toXdr().encode(writer)
            Base64.encode(writer.toByteArray())
        }

        val webAuth = WebAuth(
            authEndpoint = testAuthEndpoint,
            network = network,
            serverSigningKey = serverKeyPair.getAccountId(),
            serverHomeDomain = testHomeDomain
        )

        // Should NOT throw - signature is valid format, just from different key
        // Server will reject during token submission
        val signedChallengeXdr = webAuth.signTransaction(
            challengeXdr = challengeXdr,
            signers = listOf(clientKeyPair),
            clientDomainSigningDelegate = wrongSignatureDelegate
        )

        val signedTx = AbstractTransaction.fromEnvelopeXdr(signedChallengeXdr, network) as Transaction

        // Should have all signatures (server + client + wrong client domain)
        assertEquals(3, signedTx.signatures.size)

        // Verify the wrong signature was added (but won't verify with correct key)
        val txHash = signedTx.hash()
        assertFalse(
            wrongKeyPair.verify(txHash, signedTx.signatures[2].signature).not(),
            "Signature should be from wrong key"
        )
    }

    @Test
    fun testMultipleSignersPlusDelegate() = runTest {
        val serverKeyPair = KeyPair.fromSecretSeed(testServerSecretSeed)
        val clientKeyPair = KeyPair.random()
        val signer2 = KeyPair.random()
        val signer3 = KeyPair.random()
        val clientDomainKeyPair = KeyPair.random()

        val challengeXdr = createValidChallengeXdr(clientKeyPair = clientKeyPair)

        val signingDelegate = ClientDomainSigningDelegate { transactionXdr ->
            val tx = AbstractTransaction.fromEnvelopeXdr(transactionXdr, network)
            val txHash = tx.hash()
            val signature = clientDomainKeyPair.signDecorated(txHash)

            val writer = XdrWriter()
            signature.toXdr().encode(writer)
            Base64.encode(writer.toByteArray())
        }

        val webAuth = WebAuth(
            authEndpoint = testAuthEndpoint,
            network = network,
            serverSigningKey = serverKeyPair.getAccountId(),
            serverHomeDomain = testHomeDomain
        )

        // Sign with 3 regular signers + delegate
        val signedChallengeXdr = webAuth.signTransaction(
            challengeXdr = challengeXdr,
            signers = listOf(clientKeyPair, signer2, signer3),
            clientDomainSigningDelegate = signingDelegate
        )

        val signedTx = AbstractTransaction.fromEnvelopeXdr(signedChallengeXdr, network) as Transaction

        // Verify: Should have 5 signatures (server + 3 clients + client domain)
        assertEquals(5, signedTx.signatures.size, "Should have 5 signatures")

        // Verify all signatures are valid
        val txHash = signedTx.hash()
        assertTrue(serverKeyPair.verify(txHash, signedTx.signatures[0].signature))
        assertTrue(clientKeyPair.verify(txHash, signedTx.signatures[1].signature))
        assertTrue(signer2.verify(txHash, signedTx.signatures[2].signature))
        assertTrue(signer3.verify(txHash, signedTx.signatures[3].signature))
        assertTrue(clientDomainKeyPair.verify(txHash, signedTx.signatures[4].signature))
    }

    @Test
    fun testDelegateExceptionHandling() = runTest {
        val serverKeyPair = KeyPair.fromSecretSeed(testServerSecretSeed)
        val clientKeyPair = KeyPair.random()

        val challengeXdr = createValidChallengeXdr(clientKeyPair = clientKeyPair)

        // Delegate that throws exception (simulating HSM unavailable)
        val failingDelegate = ClientDomainSigningDelegate { _ ->
            throw IllegalStateException("HSM unavailable")
        }

        val webAuth = WebAuth(
            authEndpoint = testAuthEndpoint,
            network = network,
            serverSigningKey = serverKeyPair.getAccountId(),
            serverHomeDomain = testHomeDomain
        )

        // Exception should propagate
        val exception = assertFailsWith<IllegalStateException> {
            webAuth.signTransaction(
                challengeXdr = challengeXdr,
                signers = listOf(clientKeyPair),
                clientDomainSigningDelegate = failingDelegate
            )
        }

        assertEquals("HSM unavailable", exception.message)
    }

    @Test
    fun testBackwardCompatibilityWithoutDelegate() = runTest {
        val serverKeyPair = KeyPair.fromSecretSeed(testServerSecretSeed)
        val clientKeyPair = KeyPair.random()

        val challengeXdr = createValidChallengeXdr(clientKeyPair = clientKeyPair)

        val webAuth = WebAuth(
            authEndpoint = testAuthEndpoint,
            network = network,
            serverSigningKey = serverKeyPair.getAccountId(),
            serverHomeDomain = testHomeDomain
            // No delegate
        )

        // Should work exactly as before - without delegate
        val signedChallengeXdr = webAuth.signTransaction(
            challengeXdr = challengeXdr,
            signers = listOf(clientKeyPair)
        )

        val signedTx = AbstractTransaction.fromEnvelopeXdr(signedChallengeXdr, network) as Transaction

        // Should have only 2 signatures (server + client)
        assertEquals(2, signedTx.signatures.size, "Should have 2 signatures (no client domain)")

        val txHash = signedTx.hash()
        assertTrue(serverKeyPair.verify(txHash, signedTx.signatures[0].signature))
        assertTrue(clientKeyPair.verify(txHash, signedTx.signatures[1].signature))
    }

    @Test
    fun testLocalKeyPairStillWorks() = runTest {
        val serverKeyPair = KeyPair.fromSecretSeed(testServerSecretSeed)
        val clientKeyPair = KeyPair.random()
        val clientDomainKeyPair = KeyPair.random()

        val challengeXdr = createValidChallengeXdr(clientKeyPair = clientKeyPair)

        val webAuth = WebAuth(
            authEndpoint = testAuthEndpoint,
            network = network,
            serverSigningKey = serverKeyPair.getAccountId(),
            serverHomeDomain = testHomeDomain
        )

        // Use local keypair (existing behavior)
        val signedChallengeXdr = webAuth.signTransaction(
            challengeXdr = challengeXdr,
            signers = listOf(clientKeyPair),
            clientDomainKeyPair = clientDomainKeyPair
        )

        val signedTx = AbstractTransaction.fromEnvelopeXdr(signedChallengeXdr, network) as Transaction

        // Should have 3 signatures (server + client + client domain)
        assertEquals(3, signedTx.signatures.size)

        val txHash = signedTx.hash()
        assertTrue(serverKeyPair.verify(txHash, signedTx.signatures[0].signature))
        assertTrue(clientKeyPair.verify(txHash, signedTx.signatures[1].signature))
        assertTrue(clientDomainKeyPair.verify(txHash, signedTx.signatures[2].signature))
    }

    @Test
    fun testDelegateReceivesFullTransactionXdr() = runTest {
        val serverKeyPair = KeyPair.fromSecretSeed(testServerSecretSeed)
        val clientKeyPair = KeyPair.random()
        val clientDomainKeyPair = KeyPair.random()

        val challengeXdr = createValidChallengeXdr(clientKeyPair = clientKeyPair)
        val originalTx = AbstractTransaction.fromEnvelopeXdr(challengeXdr, network)

        var receivedXdr: String? = null

        // Delegate that captures the transaction XDR it receives
        val capturingDelegate = ClientDomainSigningDelegate { transactionXdr ->
            receivedXdr = transactionXdr

            val tx = AbstractTransaction.fromEnvelopeXdr(transactionXdr, network)
            val txHash = tx.hash()
            val signature = clientDomainKeyPair.signDecorated(txHash)

            val writer = XdrWriter()
            signature.toXdr().encode(writer)
            Base64.encode(writer.toByteArray())
        }

        val webAuth = WebAuth(
            authEndpoint = testAuthEndpoint,
            network = network,
            serverSigningKey = serverKeyPair.getAccountId(),
            serverHomeDomain = testHomeDomain
        )

        webAuth.signTransaction(
            challengeXdr = challengeXdr,
            signers = listOf(clientKeyPair),
            clientDomainSigningDelegate = capturingDelegate
        )

        // Verify delegate received full transaction XDR
        assertNotNull(receivedXdr, "Delegate should have received transaction XDR")

        // Verify the XDR can be parsed and contains the signatures added so far
        val receivedTx = AbstractTransaction.fromEnvelopeXdr(receivedXdr!!, network) as Transaction

        // Should have server + client signatures (delegate is last)
        assertEquals(2, receivedTx.signatures.size, "Delegate should receive tx with server + client sigs")

        // Verify transaction hash matches original (same transaction, just more signatures)
        assertEquals(
            originalTx.hash().toList(),
            receivedTx.hash().toList(),
            "Transaction hash should be same"
        )
    }

    @Test
    fun testJwtTokenMutualExclusivityValidation() = runTest {
        val clientKeyPair = KeyPair.random()
        val clientDomainKeyPair = KeyPair.random()
        val signingDelegate = ClientDomainSigningDelegate { _ ->
            throw IllegalStateException("Should not be called")
        }

        val webAuth = WebAuth(
            authEndpoint = testAuthEndpoint,
            network = network,
            serverSigningKey = KeyPair.fromSecretSeed(testServerSecretSeed).getAccountId(),
            serverHomeDomain = testHomeDomain
        )

        // Should throw when both are provided to jwtToken
        val exception = assertFailsWith<IllegalArgumentException> {
            webAuth.jwtToken(
                clientAccountId = clientKeyPair.getAccountId(),
                signers = listOf(clientKeyPair),
                clientDomain = "wallet.example.com",
                clientDomainKeyPair = clientDomainKeyPair,
                clientDomainSigningDelegate = signingDelegate
            )
        }

        assertTrue(
            exception.message?.contains("Cannot specify both") == true,
            "jwtToken should validate mutual exclusivity"
        )
    }

    @Test
    fun testInstanceLevelDelegateInWebAuthConstructor() = runTest {
        val serverKeyPair = KeyPair.fromSecretSeed(testServerSecretSeed)
        val clientKeyPair = KeyPair.random()
        val clientDomainKeyPair = KeyPair.random()

        val challengeXdr = createValidChallengeXdr(clientKeyPair = clientKeyPair)

        // Create delegate
        val signingDelegate = ClientDomainSigningDelegate { transactionXdr ->
            val tx = AbstractTransaction.fromEnvelopeXdr(transactionXdr, network)
            val txHash = tx.hash()
            val signature = clientDomainKeyPair.signDecorated(txHash)

            val writer = XdrWriter()
            signature.toXdr().encode(writer)
            Base64.encode(writer.toByteArray())
        }

        // Pass delegate in constructor
        val webAuth = WebAuth(
            authEndpoint = testAuthEndpoint,
            network = network,
            serverSigningKey = serverKeyPair.getAccountId(),
            serverHomeDomain = testHomeDomain,
            clientDomainSigningDelegate = signingDelegate
        )

        // Sign transaction without passing delegate (should use instance-level delegate)
        val signedChallengeXdr = webAuth.signTransaction(
            challengeXdr = challengeXdr,
            signers = listOf(clientKeyPair)
            // Note: No clientDomainSigningDelegate parameter
        )

        val signedTx = AbstractTransaction.fromEnvelopeXdr(signedChallengeXdr, network) as Transaction

        // Should have 2 signatures (server + client)
        // Instance-level delegate is used only in jwtToken(), not in signTransaction()
        assertEquals(2, signedTx.signatures.size, "signTransaction() doesn't use instance delegate")
    }
}
