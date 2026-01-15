// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep45

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Response from a SEP-45 challenge request.
 *
 * The server returns this response when a client requests a challenge for
 * contract account authentication. The response contains a base64-encoded XDR
 * array of SorobanAuthorizationEntry objects that the client must validate and sign.
 *
 * Unlike SEP-10 which uses transaction XDR, SEP-45 uses authorization entries
 * that contain invocations of the `web_auth_verify` function on the server's
 * web auth contract.
 *
 * Workflow:
 * 1. Client requests challenge via GET to WEB_AUTH_FOR_CONTRACTS_ENDPOINT
 * 2. Server returns Sep45ChallengeResponse with authorization entries XDR
 * 3. Client validates the challenge (critical security step)
 * 4. Client signs the validated authorization entries
 * 5. Client submits signed entries back to server
 * 6. Server returns JWT token
 *
 * Example response JSON:
 * ```json
 * {
 *   "authorization_entries": "AAAAAQAAAA...base64...==",
 *   "network_passphrase": "Test SDF Network ; September 2015"
 * }
 * ```
 *
 * Note: Some servers may return camelCase field names (`authorizationEntries`,
 * `networkPassphrase`). Use [fromJson] for robust parsing that handles both formats.
 *
 * Security considerations:
 * - Always validate the challenge before signing (use WebAuthForContracts.validateChallenge())
 * - Verify the server signature matches the stellar.toml SIGNING_KEY
 * - Ensure contract address matches WEB_AUTH_CONTRACT_ID from stellar.toml
 * - Verify function name is `web_auth_verify`
 *
 * See also:
 * - [SEP-45 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0045.md)
 *
 * @property authorizationEntries Base64-encoded XDR array of SorobanAuthorizationEntry objects
 * @property networkPassphrase Optional network passphrase for additional verification
 */
@Serializable
data class Sep45ChallengeResponse(
    @SerialName("authorization_entries")
    val authorizationEntries: String? = null,

    @SerialName("network_passphrase")
    val networkPassphrase: String? = null
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        /**
         * Parses a JSON string into a Sep45ChallengeResponse.
         *
         * This method handles both snake_case and camelCase field names for
         * compatibility with different server implementations:
         * - `authorization_entries` or `authorizationEntries`
         * - `network_passphrase` or `networkPassphrase`
         *
         * @param jsonString The JSON response string from the server
         * @return Parsed Sep45ChallengeResponse
         * @throws kotlinx.serialization.SerializationException if the JSON is malformed
         */
        fun fromJson(jsonString: String): Sep45ChallengeResponse {
            val jsonObject = json.parseToJsonElement(jsonString).jsonObject

            // Try snake_case first, then camelCase
            val authEntries = jsonObject["authorization_entries"]?.jsonPrimitive?.content
                ?: jsonObject["authorizationEntries"]?.jsonPrimitive?.content

            val networkPassphrase = jsonObject["network_passphrase"]?.jsonPrimitive?.content
                ?: jsonObject["networkPassphrase"]?.jsonPrimitive?.content

            return Sep45ChallengeResponse(
                authorizationEntries = authEntries,
                networkPassphrase = networkPassphrase
            )
        }
    }
}
