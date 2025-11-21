// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep10.exceptions

/**
 * Generic challenge validation exception for errors that don't fit specific validation categories.
 *
 * This exception is used for validation failures that don't have a specific exception class,
 * such as:
 * - Invalid XDR parsing
 * - Missing operations
 * - Other structural issues with the challenge transaction
 *
 * For specific validation failures, use the specialized exception classes:
 * - InvalidSequenceNumberException
 * - InvalidMemoTypeException
 * - InvalidOperationTypeException
 * - etc.
 *
 * @param message Description of the validation failure
 */
class GenericChallengeValidationException(message: String) : ChallengeValidationException(message)
