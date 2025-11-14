// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep01

/**
 * Organization documentation from the stellar.toml DOCUMENTATION section.
 *
 * Contains identifying information about the organization publishing the stellar.toml file,
 * including legal name, contact details, and verification attestations.
 *
 * See [SEP-0001 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0001.md)
 */
data class Documentation(
    /**
     * Legal name of the organization.
     */
    val orgName: String? = null,

    /**
     * (May not apply) DBA of the organization.
     */
    val orgDba: String? = null,

    /**
     * The organization's official URL. The stellar.toml must be hosted on the same domain.
     */
    val orgUrl: String? = null,

    /**
     * A URL to a PNG image of the organization's logo on a transparent background.
     */
    val orgLogo: String? = null,

    /**
     * Short description of the organization.
     */
    val orgDescription: String? = null,

    /**
     * Physical address for the organization.
     */
    val orgPhysicalAddress: String? = null,

    /**
     * URL on the same domain as the orgUrl that contains an image or PDF official document
     * attesting to the physical address. It must list the orgName or orgDba as the party at
     * the address. Only documents from an official third party are acceptable.
     * E.g. a utility bill, mail from a financial institution, or business license.
     */
    val orgPhysicalAddressAttestation: String? = null,

    /**
     * The organization's phone number in E.164 format, e.g. +14155552671.
     */
    val orgPhoneNumber: String? = null,

    /**
     * URL on the same domain as the orgUrl that contains an image or PDF of a phone bill
     * showing both the phone number and the organization's name.
     */
    val orgPhoneNumberAttestation: String? = null,

    /**
     * A Keybase account name for the organization. Should contain proof of ownership of any
     * public online accounts you list here, including the organization's domain.
     */
    val orgKeybase: String? = null,

    /**
     * The organization's Twitter account.
     */
    val orgTwitter: String? = null,

    /**
     * The organization's Github account.
     */
    val orgGithub: String? = null,

    /**
     * An email where clients can contact the organization. Must be hosted at the orgUrl domain.
     */
    val orgOfficialEmail: String? = null,

    /**
     * An email that users can use to request support regarding the organization's Stellar
     * assets or applications.
     */
    val orgSupportEmail: String? = null,

    /**
     * Name of the authority or agency that licensed the organization, if applicable.
     */
    val orgLicensingAuthority: String? = null,

    /**
     * Type of financial or other license the organization holds, if applicable.
     */
    val orgLicenseType: String? = null,

    /**
     * Official license number of the organization, if applicable.
     */
    val orgLicenseNumber: String? = null
)
