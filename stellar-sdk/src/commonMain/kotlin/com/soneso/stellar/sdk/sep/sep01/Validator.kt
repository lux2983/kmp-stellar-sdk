// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep01

/**
 * Validator information from the stellar.toml VALIDATORS list.
 *
 * Contains one set of fields for each validator node the organization runs.
 * Combined with the steps outlined in SEP-20, this allows organizations to declare
 * their validator nodes and let others know the location of any public archives they maintain.
 *
 * See [SEP-0001 Specification](https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0001.md)
 */
data class Validator(
    /**
     * A name for display in stellar-core configs that conforms to ^[a-z0-9-]{2,16}$.
     */
    val alias: String? = null,

    /**
     * A human-readable name for display in quorum explorers and other interfaces.
     */
    val displayName: String? = null,

    /**
     * The Stellar account associated with the node.
     */
    val publicKey: String? = null,

    /**
     * The IP:port or domain:port peers can use to connect to the node.
     */
    val host: String? = null,

    /**
     * The location of the history archive published by this validator.
     */
    val history: String? = null
)
