// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep01

/**
 * General information from the stellar.toml file as defined in SEP-0001.
 *
 * Contains service endpoints, network configuration, and account information
 * that organizations publish to declare their Stellar integration.
 *
 * See [SEP-0001 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0001.md)
 */
data class GeneralInformation(
    /**
     * The version of SEP-1 this stellar.toml adheres to. This helps parsers know which fields to expect.
     */
    val version: String? = null,

    /**
     * The passphrase for the specific Stellar network this infrastructure operates on.
     */
    val networkPassphrase: String? = null,

    /**
     * The endpoint for clients to resolve stellar addresses for users on your domain via SEP-2 Federation Protocol.
     */
    val federationServer: String? = null,

    /**
     * The endpoint used for SEP-3 Compliance Protocol.
     */
    val authServer: String? = null,

    /**
     * The server used for SEP-6 Anchor/Client interoperability.
     */
    val transferServer: String? = null,

    /**
     * The server used for SEP-24 Anchor/Client interoperability.
     */
    val transferServerSep24: String? = null,

    /**
     * The server used for SEP-12 Anchor/Client customer info transfer.
     */
    val kycServer: String? = null,

    /**
     * The endpoint used for SEP-10 Web Authentication.
     */
    val webAuthEndpoint: String? = null,

    /**
     * The signing key used for SEP-3 Compliance Protocol (deprecated) and SEP-10 Authentication Protocol.
     */
    val signingKey: String? = null,

    /**
     * Location of public-facing Horizon instance (if one is offered).
     */
    val horizonUrl: String? = null,

    /**
     * A list of Stellar accounts that are controlled by this domain.
     */
    val accounts: List<String> = emptyList(),

    /**
     * The signing key used for SEP-7 delegated signing.
     */
    val uriRequestSigningKey: String? = null,

    /**
     * The server used for receiving SEP-31 direct fiat-to-fiat payments.
     * Requires SEP-12 and hence a KYC_SERVER TOML attribute.
     */
    val directPaymentServer: String? = null,

    /**
     * The server used for receiving SEP-38 requests.
     */
    val anchorQuoteServer: String? = null,

    /**
     * The endpoint used for SEP-45 Web Authentication (contract-based auth).
     */
    val webAuthForContractsEndpoint: String? = null,

    /**
     * The web authentication contract ID for SEP-45 Web Authentication.
     */
    val webAuthContractId: String? = null
)
