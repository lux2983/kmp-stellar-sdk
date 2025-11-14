// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep01

/**
 * Point of contact documentation from the stellar.toml PRINCIPALS list.
 *
 * Contains identifying information for the primary point of contact or principal of the organization.
 *
 * See [SEP-0001 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0001.md)
 */
data class PointOfContact(
    /**
     * Full legal name.
     */
    val name: String? = null,

    /**
     * Business email address for the principal.
     */
    val email: String? = null,

    /**
     * Personal Keybase account. Should include proof of ownership for other online accounts,
     * as well as the organization's domain.
     */
    val keybase: String? = null,

    /**
     * Personal Telegram account.
     */
    val telegram: String? = null,

    /**
     * Personal Twitter account.
     */
    val twitter: String? = null,

    /**
     * Personal Github account.
     */
    val github: String? = null,

    /**
     * SHA-256 hash of a photo of the principal's government-issued photo ID.
     */
    val idPhotoHash: String? = null,

    /**
     * SHA-256 hash of a verification photo of principal. Should be well-lit and contain:
     * principal holding ID card and signed, dated, hand-written message stating
     * "I, $name, am a principal of $orgName, a Stellar token issuer with address $issuerAddress."
     */
    val verificationPhotoHash: String? = null
)
