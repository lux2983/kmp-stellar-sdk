// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep01

/**
 * Currency documentation from the stellar.toml CURRENCIES list.
 *
 * Contains information about assets/currencies supported by the organization.
 * One set of fields for each currency supported. Applicable fields should be completed
 * and any that don't apply should be excluded.
 *
 * See [SEP-0001 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0001.md)
 */
data class Currency(
    /**
     * Token code.
     */
    val code: String? = null,

    /**
     * A pattern with ? as a single character wildcard. Allows a CURRENCIES entry to apply to
     * multiple assets that share the same info. An example is futures, where the only difference
     * between issues is the date of the contract. E.g. CORN???????? to match codes such as CORN20180604.
     */
    val codeTemplate: String? = null,

    /**
     * Token issuer Stellar public key.
     */
    val issuer: String? = null,

    /**
     * Contract ID of the token contract. The token must be compatible with the SEP-41 Token Interface
     * to be defined here. Required for tokens that are not Stellar Assets. Omitted if the token is a Stellar Asset.
     */
    val contract: String? = null,

    /**
     * Status of token. One of live, dead, test, or private. Allows issuer to mark whether token is
     * dead/for testing/for private use or is live and should be listed in live exchanges.
     */
    val status: String? = null,

    /**
     * Preference for number of decimals to show when a client displays currency balance.
     */
    val displayDecimals: Int? = null,

    /**
     * A short name for the token.
     */
    val name: String? = null,

    /**
     * Description of token and what it represents.
     */
    val desc: String? = null,

    /**
     * Conditions on token.
     */
    val conditions: String? = null,

    /**
     * URL to a PNG image on a transparent background representing token.
     */
    val image: String? = null,

    /**
     * Fixed number of tokens, if the number of tokens issued will never change.
     */
    val fixedNumber: Long? = null,

    /**
     * Max number of tokens, if there will never be more than maxNumber tokens.
     */
    val maxNumber: Long? = null,

    /**
     * The number of tokens is dilutable at the issuer's discretion.
     */
    val isUnlimited: Boolean? = null,

    /**
     * true if token can be redeemed for underlying asset, otherwise false.
     */
    val isAssetAnchored: Boolean? = null,

    /**
     * Type of asset anchored. Can be fiat, crypto, nft, stock, bond, commodity, realestate, or other.
     */
    val anchorAssetType: String? = null,

    /**
     * If anchored token, code/symbol for asset that token is anchored to.
     * E.g. USD, BTC, SBUX, Address of real-estate investment property.
     */
    val anchorAsset: String? = null,

    /**
     * URL to attestation or other proof, evidence, or verification of reserves,
     * such as third-party audits.
     */
    val attestationOfReserve: String? = null,

    /**
     * If anchored token, these are instructions to redeem the underlying asset from tokens.
     */
    val redemptionInstructions: String? = null,

    /**
     * If this is an anchored crypto token, list of one or more public addresses that hold the
     * assets for which you are issuing tokens.
     */
    val collateralAddresses: List<String>? = null,

    /**
     * Messages stating that funds in the collateralAddresses list are reserved to back the issued asset.
     */
    val collateralAddressMessages: List<String>? = null,

    /**
     * These prove you control the collateralAddresses. For each address you list, sign the entry in
     * collateralAddressMessages with the address's private key and add the resulting string to this
     * list as a base64-encoded raw signature.
     */
    val collateralAddressSignatures: List<String>? = null,

    /**
     * Indicates whether or not this is a SEP-0008 regulated asset. If missing, false is assumed.
     */
    val regulated: Boolean? = null,

    /**
     * URL of a SEP-0008 compliant approval service that signs validated transactions.
     */
    val approvalServer: String? = null,

    /**
     * A human readable string that explains the issuer's requirements for approving transactions.
     */
    val approvalCriteria: String? = null,

    /**
     * Alternately, stellar.toml can link out to a separate TOML file for each currency by specifying
     * toml="https://DOMAIN/.well-known/CURRENCY.toml" as the currency's only field.
     * In this case only this field is filled. To load the currency data, you can use
     * StellarToml.currencyFromUrl(toml).
     */
    val toml: String? = null
)
