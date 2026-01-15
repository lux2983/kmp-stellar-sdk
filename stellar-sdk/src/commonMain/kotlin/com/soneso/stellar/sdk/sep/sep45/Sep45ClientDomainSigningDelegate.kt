// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep45

/**
 * Delegate for signing a single authorization entry with client domain credentials.
 *
 * SEP-45 supports client domain verification, where a client domain's signing key
 * signs one of the authorization entries to prove the client is authorized by that domain.
 * This is useful for wallet applications that want to prove their association with
 * a specific domain (e.g., "wallet.example.com").
 *
 * This delegate enables integration with:
 * - **Wallet backend servers**: Remote signing services that hold custody of domain keys
 * - **Hardware Security Modules (HSMs)**: Enterprise key management systems
 * - **Cloud KMS services**: AWS KMS, Google Cloud KMS, Azure Key Vault
 * - **Multi-Party Computation systems**: Distributed key signing
 *
 * ## Design Note
 *
 * This interface uses String-based API (base64 XDR) instead of object-based API
 * because remote signing services communicate via HTTP with base64-encoded XDR.
 * This eliminates unnecessary encode/decode cycles in the common case where
 * the signing happens on a remote server.
 *
 * ## Implementation Requirements
 *
 * The implementation should:
 * 1. Decode the base64 XDR to [com.soneso.stellar.sdk.xdr.SorobanAuthorizationEntryXdr] (if needed for inspection)
 * 2. Set `signatureExpirationLedger` on `credentials.addressCredentials`
 * 3. Sign the entry using the client domain's private key
 * 4. Encode back to base64 XDR
 *
 * ## Example: Remote Signing Server
 *
 * ```kotlin
 * val delegate = Sep45ClientDomainSigningDelegate { entryXdr ->
 *     // POST to remote signing server
 *     val response = httpClient.post("https://signing.example.com/sign") {
 *         contentType(ContentType.Application.Json)
 *         setBody("""{"entry": "$entryXdr", "network": "testnet"}""")
 *     }
 *     val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
 *     json["signed_entry"]?.jsonPrimitive?.content
 *         ?: throw Exception("Signing failed: ${response.bodyAsText()}")
 * }
 *
 * val token = webAuth.jwtToken(
 *     clientAccountId = contractId,
 *     signers = listOf(clientSigner),
 *     clientDomain = "wallet.example.com",
 *     clientDomainSigningDelegate = delegate
 * )
 * ```
 *
 * ## Example: Local Signing with KeyPair
 *
 * ```kotlin
 * val clientDomainKeyPair = KeyPair.fromSecretSeed("S...")
 *
 * val delegate = Sep45ClientDomainSigningDelegate { entryXdr ->
 *     // Decode XDR
 *     val entry = SorobanAuthorizationEntryXdr.fromXdrBase64(entryXdr)
 *
 *     // Sign using Auth helper
 *     val signedEntry = Auth.authorizeEntry(
 *         entry = entry,
 *         signer = clientDomainKeyPair,
 *         validUntilLedgerSeq = expirationLedger,
 *         network = Network.TESTNET
 *     )
 *
 *     // Encode back to base64
 *     signedEntry.toXdrBase64()
 * }
 * ```
 *
 * ## Security Considerations
 *
 * - The signing key should be kept secure and never transmitted
 * - Use HTTPS for remote signing servers
 * - Consider using bearer tokens or mTLS for authentication
 * - Validate the entry before signing (check contract address, function name)
 *
 * @see WebAuthForContracts.jwtToken for usage with client domain authentication
 * @see com.soneso.stellar.sdk.Auth for signing authorization entries
 */
fun interface Sep45ClientDomainSigningDelegate {

    /**
     * Signs a single authorization entry with the client domain's credentials.
     *
     * This method is called by [WebAuthForContracts] when a client domain is specified
     * and a signing delegate is provided (instead of a local keypair).
     *
     * The implementation receives an unsigned authorization entry for the client domain
     * and must return the same entry with a valid signature added.
     *
     * @param entryXdr Base64-encoded [com.soneso.stellar.sdk.xdr.SorobanAuthorizationEntryXdr] XDR
     *                 representing the unsigned authorization entry for the client domain
     * @return Base64-encoded [com.soneso.stellar.sdk.xdr.SorobanAuthorizationEntryXdr] XDR
     *         with the entry signed by the client domain's key
     * @throws Exception if signing fails (e.g., network error, invalid entry, key not found)
     */
    suspend fun signEntry(entryXdr: String): String
}
