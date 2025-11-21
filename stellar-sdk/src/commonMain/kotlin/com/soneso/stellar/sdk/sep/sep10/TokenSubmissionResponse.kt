// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep10

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from submitting a signed challenge transaction.
 *
 * When the client submits a properly signed challenge to the authentication server,
 * the server verifies the signatures and returns a JWT token in this response.
 *
 * The JWT token can be used to authenticate requests to SEP-6 (Deposit/Withdrawal),
 * SEP-12 (KYC), SEP-24 (Hosted Deposit/Withdrawal), SEP-31 (Cross-Border Payments),
 * and other Stellar services that require authentication.
 *
 * Example response JSON:
 * ```json
 * {
 *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
 * }
 * ```
 *
 * Server-side verification steps:
 * 1. Validates transaction structure (same checks as client)
 * 2. Verifies time bounds are still valid
 * 3. Verifies client signature(s) are valid
 * 4. Checks signing weight meets account threshold
 * 5. Generates and signs JWT token
 * 6. Returns token in response
 *
 * See also:
 * - [SEP-10 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0010.md)
 * - [TokenSubmissionRequest] for the request format
 * - [AuthToken] for parsing the JWT token
 *
 * @property token JWT token string for authenticating subsequent API requests
 */
@Serializable
internal data class TokenSubmissionResponse(
    @SerialName("token")
    val token: String
)
