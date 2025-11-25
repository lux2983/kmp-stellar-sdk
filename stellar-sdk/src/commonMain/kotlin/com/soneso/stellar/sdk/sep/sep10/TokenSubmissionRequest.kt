// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep10

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for submitting a signed challenge to obtain a JWT token.
 *
 * After validating and signing a challenge transaction, the client submits
 * the signed transaction back to the authentication server to obtain a JWT token.
 *
 * HTTP Request:
 * ```
 * POST {WEB_AUTH_ENDPOINT}
 * Content-Type: application/json
 *
 * {
 *   "transaction": "base64_signed_challenge_xdr"
 * }
 * ```
 *
 * Example usage:
 * ```kotlin
 * val request = TokenSubmissionRequest(
 *     transaction = signedChallengeXdr
 * )
 * ```
 *
 * See also:
 * - [SEP-10 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0010.md)
 * - [TokenSubmissionResponse] for the server response
 *
 * @property transaction Base64-encoded signed challenge transaction XDR
 */
@Serializable
internal data class TokenSubmissionRequest(
    @SerialName("transaction")
    val transaction: String
)
