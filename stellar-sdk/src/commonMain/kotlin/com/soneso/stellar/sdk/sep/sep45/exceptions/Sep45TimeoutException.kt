// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep45.exceptions

/**
 * Exception thrown when a SEP-45 operation times out.
 *
 * This can occur when:
 * - Network request to the authentication server takes too long
 * - Server is overloaded and not responding within timeout period
 * - Connection is slow or unstable
 * - DNS resolution takes too long
 *
 * Unlike other SEP-45 exceptions, a timeout does not necessarily indicate
 * an authentication failure. The operation may have succeeded on the server
 * but the response was not received in time.
 *
 * Recommendations:
 * - Implement retry logic with exponential backoff
 * - Consider increasing timeout for slow networks
 * - Check network connectivity before retrying
 * - For token submission, verify token status before resubmitting
 *
 * Example - Handle timeout with retry:
 * ```kotlin
 * var retryCount = 0
 * val maxRetries = 3
 *
 * while (retryCount < maxRetries) {
 *     try {
 *         val token = webAuth.jwtToken(contractAccountId, signers)
 *         break
 *     } catch (e: Sep45TimeoutException) {
 *         retryCount++
 *         if (retryCount >= maxRetries) {
 *             throw e
 *         }
 *         delay(1000L * retryCount) // Exponential backoff
 *     }
 * }
 * ```
 *
 * @param message Description of the timeout
 */
class Sep45TimeoutException(message: String) : Sep45Exception(message)
