//
//  OZTransactionOperations.kt
//  Stellar SDK Kotlin Multiplatform
//
//  Created by Claude on 27.01.26.
//  Copyright © 2026 Soneso. All rights reserved.
//

package com.soneso.stellar.sdk.smartaccount.oz
import com.soneso.stellar.sdk.smartaccount.core.*

import com.soneso.stellar.sdk.Address
import com.soneso.stellar.sdk.InvokeHostFunctionOperation
import com.soneso.stellar.sdk.KeyPair
import com.soneso.stellar.sdk.Memo
import com.soneso.stellar.sdk.MemoNone
import com.soneso.stellar.sdk.Network
import com.soneso.stellar.sdk.Transaction
import com.soneso.stellar.sdk.TransactionBuilder
import com.soneso.stellar.sdk.rpc.responses.GetTransactionStatus
import com.soneso.stellar.sdk.xdr.HostFunctionXdr
import com.soneso.stellar.sdk.xdr.Int64Xdr
import com.soneso.stellar.sdk.xdr.InvokeContractArgsXdr
import com.soneso.stellar.sdk.xdr.SCAddressXdr
import com.soneso.stellar.sdk.xdr.SCSymbolXdr
import com.soneso.stellar.sdk.xdr.SCValXdr
import com.soneso.stellar.sdk.xdr.SorobanAuthorizationEntryXdr
import com.soneso.stellar.sdk.xdr.SorobanCredentialsXdr
import com.soneso.stellar.sdk.xdr.Uint64Xdr
import com.soneso.stellar.sdk.xdr.Uint32Xdr
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.delay

/**
 * Result of a transaction submission and polling operation.
 *
 * Contains the outcome of a transaction after it has been submitted to the network
 * and potentially confirmed on-chain. Use this to determine if a transaction succeeded
 * and retrieve its hash and ledger number.
 *
 * Example:
 * ```kotlin
 * val result = txOps.transfer(
 *     tokenContract = "CBCD...",
 *     recipient = "GA7Q...",
 *     amount = 10.0
 * )
 *
 * if (result.success) {
 *     println("Transaction succeeded! Hash: ${result.hash ?: "unknown"}")
 *     println("Confirmed in ledger: ${result.ledger ?: 0}")
 * } else {
 *     println("Transaction failed: ${result.error ?: "unknown error"}")
 * }
 * ```
 *
 * @property success Whether the transaction was successful
 * @property hash The transaction hash if submission succeeded
 * @property ledger The ledger number where the transaction was confirmed
 * @property error Error message if the transaction failed
 */
data class TransactionResult(
    val success: Boolean,
    val hash: String? = null,
    val ledger: UInt? = null,
    val error: String? = null
)

/**
 * Transaction operations for OpenZeppelin Smart Accounts.
 *
 * Provides high-level transaction building, signing, and submission capabilities
 * for smart account operations. Handles:
 *
 * - Token transfers with automatic stroops conversion
 * - Transaction simulation and fee estimation
 * - Authorization entry signing with WebAuthn
 * - Relayer submission for fee sponsoring
 * - Transaction polling and confirmation
 * - Testnet wallet funding via Friendbot
 *
 * This class works in tandem with OZSmartAccountKit and should be accessed via
 * the kit instance rather than instantiated directly.
 *
 * Example usage:
 * ```kotlin
 * val kit = OZSmartAccountKit.create(config)
 * val txOps = OZTransactionOperations(kit)
 *
 * // Transfer tokens
 * val result = txOps.transfer(
 *     tokenContract = nativeTokenAddress,
 *     recipient = "GA7Q...",
 *     amount = 100.0
 * )
 * println("Transfer ${if (result.success) "succeeded" else "failed"}")
 *
 * // Fund testnet wallet
 * val fundedAmount = txOps.fundWallet(nativeTokenContract = nativeTokenAddress)
 * println("Funded with $fundedAmount XLM")
 * ```
 */
class OZTransactionOperations internal constructor(
    private val kit: OZSmartAccountKit
) {
    private val httpClient = HttpClient()

    // MARK: - Token Transfer

    /**
     * Transfers tokens from the smart account to a recipient.
     *
     * Builds and submits a token transfer transaction from the connected smart account
     * to the specified recipient. The amount is automatically converted from XLM to stroops.
     *
     * Flow:
     * 1. Validates inputs (addresses, amount, not sending to self)
     * 2. Converts amount to stroops (1 XLM = 10,000,000 stroops)
     * 3. Builds contract invocation for token transfer
     * 4. Simulates transaction to get auth entries
     * 5. Signs auth entries with passkey (requires user interaction)
     * 6. Re-simulates with signed auth entries
     * 7. Submits via relayer (if configured) or RPC
     * 8. Polls for confirmation
     *
     * IMPORTANT: This method requires WebAuthn interaction to sign auth entries.
     * The user will be prompted for biometric authentication.
     *
     * @param tokenContract The token contract address (C-address)
     * @param recipient The recipient address (G-address for accounts, C-address for contracts)
     * @param amount The amount to transfer in XLM (will be converted to stroops)
     * @return TransactionResult indicating success or failure
     * @throws SmartAccountException if validation fails, simulation fails, or submission fails
     *
     * Example:
     * ```kotlin
     * val result = txOps.transfer(
     *     tokenContract = "CBCD1234...",
     *     recipient = "GA7QYNF7...",
     *     amount = 10.5
     * )
     *
     * if (result.success) {
     *     println("Transferred 10.5 XLM. Hash: ${result.hash ?: ""}")
     * } else {
     *     println("Transfer failed: ${result.error ?: ""}")
     * }
     * ```
     */
    suspend fun transfer(
        tokenContract: String,
        recipient: String,
        amount: Double
    ): TransactionResult {
        // STEP 1: Validate inputs
        val (_, contractId) = kit.requireConnected()

        // Validate token contract address (must be C-address)
        if (!tokenContract.startsWith("C") || tokenContract.length != 56) {
            throw ValidationException.invalidAddress(
                "Token contract must be a valid C-address, got: $tokenContract"
            )
        }

        // Validate recipient address (G or C)
        if ((!recipient.startsWith("G") && !recipient.startsWith("C")) || recipient.length != 56) {
            throw ValidationException.invalidAddress(
                "Recipient must be a valid G-address or C-address, got: $recipient"
            )
        }

        // Validate amount
        if (amount <= 0) {
            throw ValidationException.invalidInput(
                "amount",
                "Amount must be greater than zero, got: $amount"
            )
        }

        // Prevent self-transfer
        if (recipient == contractId) {
            throw ValidationException.invalidInput(
                "recipient",
                "Cannot transfer to self"
            )
        }

        // STEP 2: Convert amount to stroops
        val stroops = SmartAccountSharedUtils.amountToStroops(amount)

        // STEP 3: Build host function for token transfer
        // Contract call: token.transfer(from: smartAccount, to: recipient, amount: stroops)
        val fromAddress = Address(contractId).toSCAddress()
        val toAddress = if (recipient.startsWith("G")) {
            val keyPair = KeyPair.fromAccountId(recipient)
            SCAddressXdr.AccountId(keyPair.getXdrAccountId())
        } else {
            Address(recipient).toSCAddress()
        }

        val amountScVal = SmartAccountSharedUtils.stroopsToI128ScVal(stroops)

        val functionArgs = listOf(
            SCValXdr.Address(fromAddress),
            SCValXdr.Address(toAddress),
            amountScVal
        )

        val invokeArgs = InvokeContractArgsXdr(
            contractAddress = Address(tokenContract).toSCAddress(),
            functionName = SCSymbolXdr("transfer"),
            args = functionArgs
        )

        val hostFunction = HostFunctionXdr.InvokeContract(invokeArgs)

        // STEP 4-8: Submit the transaction (will handle simulation, signing, and polling)
        return submit(hostFunction = hostFunction, auth = emptyList())
    }

    // MARK: - Auth Entry Signing

    /**
     * Signs authorization entries matching the connected contract.
     *
     * Iterates through all auth entries and signs those with address credentials
     * matching the connected smart account contract. The signature is added to the
     * entry's signature map using the specified signer.
     *
     * @param authEntries The authorization entries to sign
     * @param signer The smart account signer to use for signing
     * @param signature The signature object (WebAuthn, Ed25519, or Policy)
     * @param expirationLedger Optional ledger number at which signatures expire (defaults to current + buffer)
     * @return Array of signed authorization entries
     * @throws SmartAccountException if signing fails
     *
     * Example:
     * ```kotlin
     * val webAuthnSig = WebAuthnSignature(
     *     authenticatorData = authData,
     *     clientData = clientData,
     *     signature = signature
     * )
     *
     * val signedEntries = txOps.signAuthEntries(
     *     authEntries = unsignedEntries,
     *     signer = externalSigner,
     *     signature = webAuthnSig,
     *     expirationLedger = currentLedger + 100u
     * )
     * ```
     */
    suspend fun signAuthEntries(
        authEntries: List<SorobanAuthorizationEntryXdr>,
        signer: SmartAccountSigner,
        signature: SmartAccountSignature,
        expirationLedger: UInt? = null
    ): List<SorobanAuthorizationEntryXdr> {
        val (_, contractId) = kit.requireConnected()

        // Determine expiration ledger
        val expiration = expirationLedger ?: run {
            // Fetch latest ledger and add buffer
            val latestLedger = kit.sorobanServer.getLatestLedger()
            latestLedger.sequence.toUInt() + SmartAccountConstants.AUTH_ENTRY_EXPIRATION_BUFFER.toUInt()
        }

        // Sign all matching auth entries
        return authEntries.map { entry ->
            // Check if this entry's credentials match our contract
            val credentials = (entry.credentials as? SorobanCredentialsXdr.Address)?.value
                ?: return@map entry // Not an address credential, skip

            // Check if the address matches our contract
            val entryAddress = SmartAccountSharedUtils.extractAddressString(credentials.address)
            if (entryAddress == contractId) {
                // This entry is for our smart account - sign it
                SmartAccountAuth.signAuthEntry(
                    entry = entry,
                    signer = signer,
                    signature = signature,
                    expirationLedger = expiration,
                    networkPassphrase = kit.config.networkPassphrase
                )
            } else {
                // Not our entry, pass through unchanged
                entry
            }
        }
    }

    // MARK: - Transaction Submission

    /**
     * Submits a host function with full Soroban authorization flow.
     *
     * Performs the complete transaction lifecycle: simulation, auth entry extraction,
     * WebAuthn signing, re-simulation with signed auth, and submission. This method
     * handles the critical authorization step that allows state-changing operations
     * to succeed on-chain.
     *
     * Flow:
     * 1. Require connected wallet (credential ID + contract ID)
     * 2. Get deployer account from kit
     * 3. Build transaction with host function and provided auth (may be empty)
     * 4. Simulate transaction to discover required auth entries
     * 5. Extract auth entries from simulation result
     * 6. For each auth entry matching our contract:
     *    a. Set signature expiration ledger
     *    b. Build auth payload hash
     *    c. Sign with WebAuthn passkey (triggers biometric prompt)
     *    d. Normalize signature to low-S compact format
     *    e. Build WebAuthn signature ScVal
     *    f. Construct signer key from stored credential
     *    g. Build signature map entry with double XDR-encoded signature
     *    h. Set credential signature on entry
     * 7. Update transaction with signed auth entries
     * 8. Re-simulate to get correct resource fees
     * 9. Assemble transaction from re-simulation
     * 10. Sign envelope with deployer keypair
     * 11. Determine submission mode (relayer vs RPC)
     * 12. Submit and poll for confirmation
     *
     * IMPORTANT: This method requires WebAuthn interaction to sign auth entries.
     * The user will be prompted for biometric authentication for each auth entry
     * that matches the connected smart account contract.
     *
     * @param hostFunction The host function to execute
     * @param auth Authorization entries for the transaction (typically empty; simulation provides them)
     * @return TransactionResult indicating success or failure
     * @throws SmartAccountException if submission, simulation, signing, or polling fails
     */
    suspend fun submit(
        hostFunction: HostFunctionXdr,
        auth: List<SorobanAuthorizationEntryXdr>
    ): TransactionResult {
        // STEP 1: Require connected wallet
        val (credentialId, contractId) = kit.requireConnected()

        // STEP 2: Get deployer account
        val deployer = kit.getDeployer()

        val deployerAccount = kit.sorobanServer.getAccount(deployer.getAccountId())

        // STEP 3: Build transaction with host function and provided auth
        val operation = InvokeHostFunctionOperation(hostFunction, auth)

        val transaction = TransactionBuilder(deployerAccount, Network(kit.config.networkPassphrase))
            .setBaseFee(100)
            .addOperation(operation)
            .addMemo(MemoNone)
            .setTimeout(300)
            .build()

        // STEP 4: Simulate transaction
        val simulation = kit.sorobanServer.simulateTransaction(transaction)

        // STEP 5: Check for simulation errors
        if (simulation.error != null) {
            throw TransactionException.simulationFailed("Simulation error: ${simulation.error}")
        }

        // STEP 6: Extract auth entries from simulation
        val simulatedAuthEntries = simulation.results?.firstOrNull()?.parseAuth() ?: emptyList()

        // STEP 7-8: Sign auth entries matching our contract
        val signedAuthEntries = if (simulatedAuthEntries.isNotEmpty()) {
            // Get latest ledger ONCE before the signing loop
            val latestLedger = kit.sorobanServer.getLatestLedger()
            val expiration = latestLedger.sequence.toUInt() + SmartAccountConstants.AUTH_ENTRY_EXPIRATION_BUFFER.toUInt()

            val signed = mutableListOf<SorobanAuthorizationEntryXdr>()

            for (entry in simulatedAuthEntries) {
                // Check if this entry has address credentials
                val addressCreds = (entry.credentials as? SorobanCredentialsXdr.Address)?.value
                if (addressCreds == null) {
                    // Not an address credential (e.g., sourceAccount), pass through unchanged
                    signed.add(entry)
                    continue
                }

                // Check if the address matches our contract
                val entryAddress = SmartAccountSharedUtils.extractAddressString(addressCreds.address)
                if (entryAddress != contractId) {
                    // Not our contract's entry, pass through unchanged
                    signed.add(entry)
                    continue
                }

                // This entry matches our smart account contract -- sign it

                // (a) Build the auth payload hash for WebAuthn signing
                val payloadHash = SmartAccountAuth.buildAuthPayloadHash(
                    entry = entry,
                    expirationLedger = expiration,
                    networkPassphrase = kit.config.networkPassphrase
                )

                // (b) Require WebAuthn provider
                // TODO: WebAuthn provider needs to be added to OZSmartAccountKit or config
                val webauthnProvider = kit.config.webauthnProvider
                    ?: throw ValidationException.invalidInput(
                        "webauthnProvider",
                        "WebAuthn provider is required for signing auth entries but is not configured"
                    )

                // (c) Authenticate with passkey (triggers biometric prompt)
                val authResult = webauthnProvider.authenticate(payloadHash)

                // (d) Normalize DER signature to compact format with low-S
                val compactSig = SmartAccountUtils.normalizeSignature(authResult.signature)

                // (e) Build WebAuthn signature
                val webAuthnSig = WebAuthnSignature(
                    authenticatorData = authResult.authenticatorData,
                    clientData = authResult.clientDataJSON,
                    signature = compactSig
                )

                // (f) Reconstruct keyData from stored credential
                val storage = kit.getStorage()
                val stored = storage.get(credentialId)
                    ?: throw CredentialException.notFound(credentialId)

                val publicKey = stored.publicKey
                val credIdBytes = SmartAccountSharedUtils.base64urlDecode(credentialId)
                    ?: throw CredentialException.invalid(
                        "Failed to decode credentialId from Base64URL: $credentialId"
                    )

                val keyData = publicKey + credIdBytes

                // (g) Build external signer
                val signer = ExternalSigner(
                    verifierAddress = kit.config.webauthnVerifierAddress,
                    keyData = keyData
                )

                // (h) Sign the auth entry
                val signedEntry = SmartAccountAuth.signAuthEntry(
                    entry = entry,
                    signer = signer,
                    signature = webAuthnSig,
                    expirationLedger = expiration,
                    networkPassphrase = kit.config.networkPassphrase
                )

                signed.add(signedEntry)
            }

            signed
        } else {
            emptyList()
        }

        // Emit transaction signed event
        kit.events.emit(
            SmartAccountEvent.TransactionSigned(
                contractId = contractId,
                credentialId = if (signedAuthEntries.isNotEmpty()) credentialId else null
            )
        )

        // STEP 9: Rebuild transaction with signed auth entries
        val signedOperation = InvokeHostFunctionOperation(hostFunction, signedAuthEntries)
        val signedTransaction = TransactionBuilder(deployerAccount, Network(kit.config.networkPassphrase))
            .setBaseFee(100)
            .addOperation(signedOperation)
            .addMemo(MemoNone)
            .setTimeout(300)
            .build()

        // STEP 10: Re-simulate with signed auth entries to get correct resource fees
        val reSimulation = kit.sorobanServer.simulateTransaction(signedTransaction)

        if (reSimulation.error != null) {
            throw TransactionException.simulationFailed("Re-simulation error: ${reSimulation.error}")
        }

        // STEP 11: Assemble transaction from re-simulation
        val transactionData = reSimulation.parseTransactionData()
            ?: throw TransactionException.submissionFailed(
                "Failed to get transaction data from re-simulation"
            )

        val minResourceFee = reSimulation.minResourceFee
            ?: throw TransactionException.submissionFailed(
                "Failed to get min resource fee from re-simulation"
            )

        // Rebuild transaction with Soroban data and resource fee
        val finalTransaction = TransactionBuilder(deployerAccount, Network(kit.config.networkPassphrase))
            .setBaseFee(100 + minResourceFee)
            .addOperation(signedOperation)
            .addMemo(MemoNone)
            .setTimeout(300)
            .setSorobanData(transactionData)
            .build()

        // STEP 12: Sign envelope with deployer keypair
        finalTransaction.sign(deployer)

        // STEP 13: Determine submission method using SIGNED auth entries (not original input)
        return if (kit.relayerClient != null) {
            val useMode2 = shouldUseRelayerMode2(signedAuthEntries)

            if (useMode2) {
                // Mode 2: Submit signed transaction XDR
                val txXdr = finalTransaction.toEnvelopeXdr()
                val relayerResponse = kit.relayerClient.sendXdr(txXdr)

                // Emit transaction submitted event
                if (relayerResponse.hash != null) {
                    kit.events.emit(
                        SmartAccountEvent.TransactionSubmitted(
                            hash = relayerResponse.hash,
                            success = relayerResponse.success
                        )
                    )
                }

                if (relayerResponse.success && relayerResponse.hash != null) {
                    pollForConfirmation(relayerResponse.hash)
                } else {
                    TransactionResult(
                        success = false,
                        error = relayerResponse.error ?: "Relayer submission failed"
                    )
                }
            } else {
                // Mode 1: Submit host function and signed auth entries
                val relayerResponse = kit.relayerClient.send(hostFunction, signedAuthEntries)

                // Emit transaction submitted event
                if (relayerResponse.hash != null) {
                    kit.events.emit(
                        SmartAccountEvent.TransactionSubmitted(
                            hash = relayerResponse.hash,
                            success = relayerResponse.success
                        )
                    )
                }

                if (relayerResponse.success && relayerResponse.hash != null) {
                    pollForConfirmation(relayerResponse.hash)
                } else {
                    TransactionResult(
                        success = false,
                        error = relayerResponse.error ?: "Relayer submission failed"
                    )
                }
            }
        } else {
            // No relayer - submit via RPC
            val sendResult = kit.sorobanServer.sendTransaction(finalTransaction)

            val hash = sendResult.hash
                ?: throw TransactionException.submissionFailed(
                    "Failed to get transaction hash from send result: ${sendResult.errorResultXdr ?: "unknown error"}"
                )

            // Emit transaction submitted event
            kit.events.emit(
                SmartAccountEvent.TransactionSubmitted(
                    hash = hash,
                    success = true
                )
            )

            pollForConfirmation(hash)
        }
    }

    // MARK: - Pre-Assembled Transaction Submission

    /**
     * Submits a pre-assembled transaction (already simulated, signed auth entries set, resource fees applied).
     *
     * This is used by multiSignerTransfer which handles its own simulation and auth signing flow.
     * The transaction only needs the deployer envelope signature and submission.
     *
     * @param transaction The assembled transaction ready for submission
     * @return TransactionResult with submission outcome
     * @throws SmartAccountException if submission fails
     */
    internal suspend fun submitAssembledTransaction(transaction: Transaction): TransactionResult {
        val deployer = kit.getDeployer()
        transaction.sign(deployer)

        // Determine submission method
        val authEntries = transaction.operations
            .filterIsInstance<InvokeHostFunctionOperation>()
            .flatMap { it.auth }

        return if (kit.relayerClient != null) {
            // Always use Mode 2 for pre-assembled transactions
            val txXdr = transaction.toEnvelopeXdr()
            val relayerResponse = kit.relayerClient.sendXdr(txXdr)

            // Emit transaction submitted event
            if (relayerResponse.hash != null) {
                kit.events.emit(
                    SmartAccountEvent.TransactionSubmitted(
                        hash = relayerResponse.hash,
                        success = relayerResponse.success
                    )
                )
            }

            if (relayerResponse.success && relayerResponse.hash != null) {
                pollForConfirmation(relayerResponse.hash)
            } else {
                TransactionResult(
                    success = false,
                    error = relayerResponse.error ?: "Relayer submission failed"
                )
            }
        } else {
            // No relayer - submit via RPC
            val sendResult = kit.sorobanServer.sendTransaction(transaction)

            val hash = sendResult.hash
                ?: throw TransactionException.submissionFailed(
                    "Failed to get transaction hash from send result: ${sendResult.errorResultXdr ?: "unknown error"}"
                )

            // Emit transaction submitted event
            kit.events.emit(
                SmartAccountEvent.TransactionSubmitted(
                    hash = hash,
                    success = true
                )
            )

            pollForConfirmation(hash)
        }
    }

    // MARK: - Testnet Wallet Funding

    /**
     * Funds the smart account wallet using Friendbot (testnet only).
     *
     * Creates a temporary keypair, funds it via Friendbot, then transfers the balance
     * (minus reserve) to the smart account contract. This enables testing without
     * requiring pre-funded wallets.
     *
     * Flow:
     * 1. Generate random temporary keypair
     * 2. Fund temp account via Friendbot HTTP GET
     * 3. Wait briefly for funding to confirm
     * 4. Query temp account balance via native token contract simulation
     * 5. Calculate transfer amount (balance - reserve)
     * 6. Build transfer from temp to smart account
     * 7. Simulate, sign with temp keypair, submit via RPC
     * 8. Return funded amount in XLM
     *
     * IMPORTANT: Only works on testnet. Do not use on mainnet.
     *
     * @param nativeTokenContract The native token (XLM) contract address (C-address)
     * @return The amount funded in XLM
     * @throws SmartAccountException if funding fails at any step
     *
     * Example:
     * ```kotlin
     * val fundedAmount = txOps.fundWallet(
     *     nativeTokenContract = "CBCD1234..."
     * )
     * println("Funded smart account with $fundedAmount XLM")
     * ```
     */
    suspend fun fundWallet(nativeTokenContract: String): Double {
        val (_, contractId) = kit.requireConnected()

        // Validate native token contract address
        if (!nativeTokenContract.startsWith("C") || nativeTokenContract.length != 56) {
            throw ValidationException.invalidAddress(
                "Native token contract must be a valid C-address, got: $nativeTokenContract"
            )
        }

        // STEP 1: Create temporary keypair
        val tempKeypair = KeyPair.random()

        // STEP 2: Fund via Friendbot
        val friendbotUrl = "${SmartAccountConstants.FRIENDBOT_URL}?addr=${tempKeypair.getAccountId()}"

        val response = httpClient.get(friendbotUrl)
        if (!response.status.isSuccess()) {
            throw TransactionException.submissionFailed("Friendbot funding failed")
        }

        // STEP 3: Wait for funding to confirm (2 seconds)
        delay(2000)

        // STEP 4: Get temp account
        val tempAccount = kit.sorobanServer.getAccount(tempKeypair.getAccountId())

        // STEP 5: Calculate transfer amount
        // Reserve for account minimum balance
        val reserveStroops = SmartAccountConstants.FRIENDBOT_RESERVE_XLM * SmartAccountConstants.STROOPS_PER_XLM

        // Query temp account balance via contract simulation
        val balanceArgs = listOf(
            SCValXdr.Address(SCAddressXdr.AccountId(tempKeypair.getXdrAccountId()))
        )
        val balanceInvokeArgs = InvokeContractArgsXdr(
            contractAddress = Address(nativeTokenContract).toSCAddress(),
            functionName = SCSymbolXdr("balance"),
            args = balanceArgs
        )
        val balanceHostFunction = HostFunctionXdr.InvokeContract(balanceInvokeArgs)
        val balanceResult = SmartAccountSharedUtils.simulateAndExtractResult(
            hostFunction = balanceHostFunction,
            kit = kit
        )

        // Parse I128 result to Int64 stroops
        val i128Parts = (balanceResult as? SCValXdr.I128)?.value
            ?: throw TransactionException.submissionFailed("Failed to query temp account balance")

        // For typical Friendbot amounts (10,000 XLM), the hi part is zero and lo fits in Long
        val balanceStroops = i128Parts.lo.value.toLong()

        if (balanceStroops <= reserveStroops) {
            throw TransactionException.submissionFailed("Insufficient balance after Friendbot funding")
        }

        val transferStroops = balanceStroops - reserveStroops

        // STEP 6: Build transfer from temp account to smart account
        val fromAddress = SCAddressXdr.AccountId(tempKeypair.getXdrAccountId())
        val toAddress = Address(contractId).toSCAddress()
        val amountScVal = SmartAccountSharedUtils.stroopsToI128ScVal(transferStroops)

        val functionArgs = listOf(
            SCValXdr.Address(fromAddress),
            SCValXdr.Address(toAddress),
            amountScVal
        )

        val invokeArgs = InvokeContractArgsXdr(
            contractAddress = Address(nativeTokenContract).toSCAddress(),
            functionName = SCSymbolXdr("transfer"),
            args = functionArgs
        )

        val hostFunction = HostFunctionXdr.InvokeContract(invokeArgs)
        val operation = InvokeHostFunctionOperation(hostFunction, emptyList())

        // STEP 7: Simulate
        val transaction = TransactionBuilder(tempAccount, Network(kit.config.networkPassphrase))
            .setBaseFee(100)
            .addOperation(operation)
            .addMemo(MemoNone)
            .setTimeout(300)
            .build()

        val simulation = kit.sorobanServer.simulateTransaction(transaction)

        if (simulation.error != null) {
            throw TransactionException.simulationFailed("Failed to simulate funding transfer")
        }

        // Extract auth entries from simulation
        val decodedAuth = simulation.results?.firstOrNull()?.parseAuth() ?: emptyList()

        // Assemble transaction from simulation
        val transactionData = simulation.parseTransactionData()
            ?: throw TransactionException.submissionFailed(
                "Failed to get transaction data from simulation"
            )

        val minResourceFee = simulation.minResourceFee
            ?: throw TransactionException.submissionFailed(
                "Failed to get min resource fee from simulation"
            )

        // Rebuild transaction with auth entries and Soroban data
        val finalOperation = InvokeHostFunctionOperation(hostFunction, decodedAuth)
        val finalTransaction = TransactionBuilder(tempAccount, Network(kit.config.networkPassphrase))
            .setBaseFee(100 + minResourceFee)
            .addOperation(finalOperation)
            .addMemo(MemoNone)
            .setTimeout(300)
            .setSorobanData(transactionData)
            .build()

        // Sign with temp keypair
        finalTransaction.sign(tempKeypair)

        // Submit via RPC
        val sendResult = kit.sorobanServer.sendTransaction(finalTransaction)

        val hash = sendResult.hash
            ?: throw TransactionException.submissionFailed("Failed to send funding transaction: ${sendResult.errorResultXdr ?: "unknown error"}")

        // Poll for confirmation
        val result = pollForConfirmation(hash)

        if (!result.success) {
            throw TransactionException.submissionFailed(
                "Funding transaction failed: ${result.error ?: "unknown error"}"
            )
        }

        // STEP 8: Return funded amount in XLM
        return transferStroops.toDouble() / SmartAccountConstants.STROOPS_PER_XLM
    }

    // MARK: - Private Helpers

    /**
     * Determines if relayer Mode 2 should be used based on auth entries.
     *
     * Mode 2 (signed transaction XDR) is required when any auth entry has
     * source_account credentials rather than address credentials.
     *
     * @param authEntries The authorization entries to check
     * @return True if Mode 2 should be used, false for Mode 1
     */
    private fun shouldUseRelayerMode2(authEntries: List<SorobanAuthorizationEntryXdr>): Boolean {
        return authEntries.any { entry ->
            // Check if credentials is SourceAccount type (Void in KMP SDK)
            entry.credentials is SorobanCredentialsXdr.Void
        }
    }

    /**
     * Polls for transaction confirmation.
     *
     * Repeatedly checks the transaction status on Soroban RPC until it is confirmed,
     * fails, or times out. Uses exponential backoff between attempts.
     *
     * @param hash The transaction hash to poll
     * @return TransactionResult indicating success or failure
     * @throws SmartAccountException if polling times out
     */
    private suspend fun pollForConfirmation(hash: String): TransactionResult {
        val maxAttempts = 10
        val sleepDurationMs = 2000L

        repeat(maxAttempts) { attempt ->
            val txStatus = kit.sorobanServer.getTransaction(hash)

            when (txStatus.status) {
                GetTransactionStatus.SUCCESS -> return TransactionResult(
                    success = true,
                    hash = hash,
                    ledger = txStatus.latestLedger?.toUInt()
                )

                GetTransactionStatus.FAILED -> {
                    val errorMessage = txStatus.resultXdr ?: "Transaction failed on-chain"
                    return TransactionResult(
                        success = false,
                        hash = hash,
                        ledger = txStatus.latestLedger?.toUInt(),
                        error = errorMessage
                    )
                }

                GetTransactionStatus.NOT_FOUND -> {
                    // Transaction not yet confirmed, retry
                    if (attempt < maxAttempts - 1) {
                        delay(sleepDurationMs)
                    } else {
                        return TransactionResult(
                            success = false,
                            hash = hash,
                            error = "Transaction timed out after $maxAttempts attempts"
                        )
                    }
                }
            }
        }

        // Should not reach here, but for safety
        throw TransactionException.timeout("Transaction polling timed out after $maxAttempts attempts")
    }
}
