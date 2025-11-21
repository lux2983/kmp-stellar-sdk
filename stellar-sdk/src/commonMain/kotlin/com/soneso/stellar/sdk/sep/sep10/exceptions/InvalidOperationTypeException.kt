// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep10.exceptions

/**
 * Exception thrown when the challenge transaction contains operations other than ManageData.
 *
 * SEP-10 Security Requirement: ALL operations in a challenge transaction MUST be
 * of type ManageData (also called Manage Data).
 *
 * This requirement is critical for security because:
 * - ManageData operations only set account data entries (harmless metadata)
 * - They cannot transfer funds, create trustlines, or modify account settings
 * - They cannot be used to damage the account if the challenge is intercepted
 *
 * Attack scenarios prevented:
 * If other operation types were allowed, a malicious server could include
 * operations like:
 * - Payment (stealing funds)
 * - SetOptions (changing account signers or thresholds)
 * - ChangeTrust (adding unwanted trustlines)
 *
 * When you sign a challenge transaction, you're only agreeing to set harmless
 * metadata on your account, not to perform any actual blockchain operations.
 *
 * @param operationType The invalid operation type found (e.g., "Payment", "SetOptions")
 * @param operationIndex The index of the invalid operation in the transaction
 */
class InvalidOperationTypeException(operationType: String, operationIndex: Int) :
    ChallengeValidationException(
        "Challenge transaction must only contain ManageData operations, " +
                "but operation at index $operationIndex is of type: $operationType. " +
                "This is a security requirement to prevent destructive operations."
    )
