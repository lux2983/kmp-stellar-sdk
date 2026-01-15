// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep45

import kotlinx.serialization.Serializable

/**
 * Response from submitting signed authorization entries.
 *
 * When the client submits properly signed authorization entries to the authentication
 * server, the server verifies the signatures and returns either a JWT token on success
 * or an error message on failure.
 *
 * The JWT token can be used to authenticate requests to SEP-6 (Deposit/Withdrawal),
 * SEP-12 (KYC), SEP-24 (Hosted Deposit/Withdrawal), SEP-31 (Cross-Border Payments),
 * and other Stellar services that require authentication.
 *
 * Example success response JSON:
 * ```json
 * {
 *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
 * }
 * ```
 *
 * Example error response JSON:
 * ```json
 * {
 *   "error": "Invalid authorization entries"
 * }
 * ```
 *
 * Server-side verification steps:
 * 1. Decodes authorization entries from base64 XDR
 * 2. Validates entry structure and arguments
 * 3. Simulates the transaction to verify contract acceptance
 * 4. Verifies client signature(s) are valid
 * 5. Generates and signs JWT token
 * 6. Returns token in response
 *
 * See also:
 * - [SEP-45 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0045.md)
 * - [Sep45AuthToken] for parsing the JWT token
 *
 * @property token JWT token string for authenticating subsequent API requests (null on error)
 * @property error Error message if token submission failed (null on success)
 */
@Serializable
data class Sep45TokenResponse(
    val token: String? = null,
    val error: String? = null
)
