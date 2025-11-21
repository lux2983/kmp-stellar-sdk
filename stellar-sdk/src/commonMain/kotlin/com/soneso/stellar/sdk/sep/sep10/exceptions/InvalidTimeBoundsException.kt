// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep10.exceptions

/**
 * Exception thrown when the challenge transaction's time bounds are invalid.
 *
 * SEP-10 Security Requirement: The challenge transaction MUST have time bounds set,
 * and the current time must be within those bounds (with a configurable grace period).
 *
 * Time bounds validation prevents:
 * - Replay attacks using expired challenges
 * - Use of challenges that are too far in the future
 * - Indefinite validity of challenge transactions
 *
 * Standard validation rules:
 * - minTime must be in the past (accounting for clock skew)
 * - maxTime must be in the future (accounting for clock skew)
 * - The grace period (default: 300 seconds / 5 minutes) allows for:
 *   - Network latency
 *   - Clock differences between client and server
 *   - User time to review and sign
 *
 * Typical time bounds:
 * - Server sets minTime to current time
 * - Server sets maxTime to current time + 15 minutes
 * - Client validates within 5-minute grace period on each side
 *
 * Attack scenario prevented:
 * Without time bounds, an attacker who intercepts a signed challenge could
 * replay it at any point in the future to impersonate the user.
 *
 * @param minTime The minimum time bound (Unix timestamp)
 * @param maxTime The maximum time bound (Unix timestamp)
 * @param currentTime The current time when validation was performed
 * @param gracePeriodSeconds The grace period used for validation
 */
class InvalidTimeBoundsException(
    minTime: Long?,
    maxTime: Long?,
    currentTime: Long,
    gracePeriodSeconds: Int
) : ChallengeValidationException(
    buildTimeBoundsMessage(minTime, maxTime, currentTime, gracePeriodSeconds)
)

private fun buildTimeBoundsMessage(
    minTime: Long?,
    maxTime: Long?,
    currentTime: Long,
    gracePeriodSeconds: Int
): String {
    return when {
        minTime == null || maxTime == null ->
            "Challenge transaction must have time bounds set. " +
                    "Found minTime: $minTime, maxTime: $maxTime"

        currentTime < (minTime - gracePeriodSeconds) ->
            "Challenge transaction is not yet valid. " +
                    "Current time: $currentTime, minTime: $minTime, grace period: ${gracePeriodSeconds}s"

        currentTime > (maxTime + gracePeriodSeconds) ->
            "Challenge transaction has expired. " +
                    "Current time: $currentTime, maxTime: $maxTime, grace period: ${gracePeriodSeconds}s"

        else ->
            "Challenge transaction time bounds validation failed. " +
                    "minTime: $minTime, maxTime: $maxTime, current: $currentTime"
    }
}
