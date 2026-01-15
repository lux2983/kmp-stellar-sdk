// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep45.exceptions

/**
 * Exception thrown when the network passphrase doesn't match the expected network.
 *
 * SEP-45 Security Requirement: If the challenge response includes a network_passphrase,
 * it MUST match the network passphrase of the network being used for authentication.
 *
 * This check prevents cross-network attacks where:
 * - A testnet challenge is used on mainnet (or vice versa)
 * - Authorization entries signed for one network are replayed on another
 * - Users accidentally authenticate on the wrong network
 *
 * Network passphrases:
 * - Public (mainnet): "Public Global Stellar Network ; September 2015"
 * - Testnet: "Test SDF Network ; September 2015"
 *
 * The network passphrase is critical because:
 * - Signatures are bound to a specific network via the network ID
 * - Cross-network replay attacks would be possible without this check
 * - Different networks may have different security postures
 *
 * Note: The network_passphrase in the response is optional. If not provided,
 * clients should assume the network matches their configuration. However,
 * when provided, it MUST be validated.
 *
 * Example - Handle network mismatch:
 * ```kotlin
 * try {
 *     val webAuth = WebAuthForContracts.fromDomain("example.com", Network.PUBLIC)
 *     val token = webAuth.jwtToken(contractId, signers)
 * } catch (e: Sep45InvalidNetworkPassphraseException) {
 *     logger.error("Network passphrase mismatch!")
 *     logger.error("Expected: ${e.expected}")
 *     logger.error("Actual: ${e.actual}")
 *     // Verify you're connecting to the correct network
 * }
 * ```
 *
 * @property expected The expected network passphrase
 * @property actual The actual network passphrase from the server response
 */
class Sep45InvalidNetworkPassphraseException(
    val expected: String,
    val actual: String
) : Sep45ChallengeValidationException(
    "Network passphrase mismatch. Expected: '$expected', but server returned: '$actual'"
)
