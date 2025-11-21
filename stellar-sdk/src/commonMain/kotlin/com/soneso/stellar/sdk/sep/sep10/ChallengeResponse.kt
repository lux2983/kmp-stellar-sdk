// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep10

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from a SEP-10 challenge request.
 *
 * The server returns this response when a client requests a challenge transaction
 * for authentication. The response contains a base64-encoded XDR transaction
 * envelope that the client must validate and sign.
 *
 * The challenge transaction is a specially-constructed Stellar transaction that:
 * - Has sequence number 0 (cannot be submitted to network)
 * - Contains only ManageData operations (harmless metadata)
 * - Is already signed by the server
 * - Has time bounds to prevent replay attacks
 * - Proves the server's identity through its signature
 *
 * Workflow:
 * 1. Client requests challenge via GET to WEB_AUTH_ENDPOINT
 * 2. Server returns ChallengeResponse with transaction XDR
 * 3. Client validates the challenge (critical security step)
 * 4. Client signs the validated challenge
 * 5. Client submits signed challenge back to server
 * 6. Server returns JWT token
 *
 * Example response JSON:
 * ```json
 * {
 *   "transaction": "AAAAAgAAAADR...base64...==",
 *   "network_passphrase": "Test SDF Network ; September 2015"
 * }
 * ```
 *
 * Security considerations:
 * - Always validate the challenge before signing (use WebAuth.validateChallenge())
 * - Verify the server signature matches the stellar.toml SIGNING_KEY
 * - Check time bounds are reasonable
 * - Ensure all operations are ManageData type
 *
 * See also:
 * - [SEP-10 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0010.md)
 * - [com.soneso.stellar.sdk.sep.sep10.WebAuth.validateChallenge] for validation
 *
 * @property transaction Base64-encoded XDR transaction envelope containing the challenge
 * @property networkPassphrase Optional network passphrase for additional verification
 */
@Serializable
data class ChallengeResponse(
    @SerialName("transaction")
    val transaction: String,

    @SerialName("network_passphrase")
    val networkPassphrase: String? = null
)
