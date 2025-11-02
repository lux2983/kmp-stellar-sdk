package com.soneso.stellar.sdk.rpc.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request for JSON-RPC method getEvents.
 *
 * Fetches a filtered list of events emitted by a given ledger range.
 *
 * @property startLedger Ledger sequence number to start fetching events from (inclusive).
 *                       Required when pagination.cursor is null. Must be omitted when cursor is provided.
 * @property endLedger Ledger sequence number to end fetching events (inclusive). Optional.
 *                     Must be greater than startLedger if both are provided.
 *                     Must be omitted when cursor is provided.
 * @property filters List of event filters to match against. Events matching any filter will be included.
 *                   Maximum 5 filters allowed. Can be empty to match all events.
 * @property pagination Optional pagination configuration for limiting and controlling result sets.
 *
 * @see <a href="https://developers.stellar.org/docs/data/rpc/api-reference/methods/getEvents">getEvents documentation</a>
 */
@Serializable
data class GetEventsRequest(
    val startLedger: Long? = null,
    val endLedger: Long? = null,
    val filters: List<EventFilter>,
    val pagination: Pagination? = null
) {
    init {
        // Validate ledger parameters
        if (pagination?.cursor == null) {
            require(startLedger != null && startLedger > 0) {
                "startLedger must be provided and positive when cursor is not used"
            }
        } else {
            require(startLedger == null) {
                "startLedger must be omitted when cursor is provided"
            }
            require(endLedger == null) {
                "endLedger must be omitted when cursor is provided"
            }
        }

        endLedger?.let { end ->
            startLedger?.let { start ->
                require(end > start) { "endLedger must be greater than startLedger" }
            }
        }

        require(filters.size <= 5) { "filters must not exceed 5 items" }

        pagination?.let {
            it.limit?.let { limit ->
                require(limit > 0) { "pagination.limit must be positive" }
                require(limit <= 10000) { "pagination.limit must not exceed 10000" }
            }
        }
    }

    /**
     * Pagination options for controlling the number of results returned.
     *
     * @property cursor Continuation token from a previous response for fetching the next page.
     *                  When provided, startLedger and endLedger must be omitted.
     * @property limit Maximum number of events to return (default 100, max 10000).
     */
    @Serializable
    data class Pagination(
        val cursor: String? = null,
        val limit: Long? = null
    )

    /**
     * Filter configuration for event matching.
     *
     * @property type Type of events to match (contract or system). If omitted, matches all event types
     *                (system, contract, and diagnostic).
     * @property contractIds List of contract IDs to filter by. Maximum 5 contract IDs allowed.
     *                       If null or empty, matches events from any contract.
     * @property topics List of topic filters. Each inner list represents ONE topic filter containing segments.
     *                  Multiple topic filters are OR'd together - an event matches if it satisfies ANY filter.
     *                  Topic values must be base64-encoded XDR SCVal strings or wildcards.
     *                  Maximum 5 topic filters per request.
     *                  Each topic filter can have 1-4 segments (plus optional trailing wildcard).
     *
     * **Topic Filter Matching Logic**:
     * - Each topic filter is an array of segment matchers
     * - Each position in the array matches that exact position in the event's topic array
     * - Without trailing "**", the event must have exactly the same number of topics as the filter
     * - With trailing "**", the event must have at least as many topics as the filter (minus the "**")
     *
     * Wildcards:
     * - "*" matches exactly one topic segment at that position
     * - "**" matches zero or more remaining segments (only allowed as the last segment)
     *
     * Example topics structure:
     * ```kotlin
     * // Create base64-encoded XDR topics
     * val counterTopic = Scv.toSymbol("COUNTER").toXdrBase64()
     * val incrementTopic = Scv.toSymbol("increment").toXdrBase64()
     *
     * val topics = listOf(
     *   // Filter 1: Match events with exactly 2 topics where topic[0]="COUNTER" AND topic[1]="increment"
     *   listOf(counterTopic, incrementTopic),
     *
     *   // Filter 2: Match events with 1+ topics where topic[0]="COUNTER" (any remaining topics)
     *   listOf(counterTopic, "**"),
     *
     *   // Filter 3: Match events with exactly 2 topics where topic[0]=any AND topic[1]="increment"
     *   listOf("*", incrementTopic)
     * )
     * // Events matching ANY of these filters will be included (OR logic)
     * ```
     *
     * **Common Mistakes**:
     * - `listOf(listOf(counterTopic))` - Only matches events with EXACTLY 1 topic equal to "COUNTER"
     * - `listOf(listOf(counterTopic, "**"))` - Matches events with 1+ topics where first is "COUNTER" ✓
     * - To OR values at same position, use multiple filters: `listOf(listOf(val1, "**"), listOf(val2, "**"))`
     *
     * Important: All non-wildcard values must be base64-encoded SCVal XDR strings.
     * Use Scv utility class to convert values: Scv.toSymbol("name").toXdrBase64()
     */
    @Serializable
    data class EventFilter(
        val type: EventFilterType? = null,
        val contractIds: List<String>? = null,
        val topics: List<List<String>>? = null
    ) {
        init {
            contractIds?.let {
                require(it.size <= 5) { "contractIds must not exceed 5 items" }
                it.forEach { contractId ->
                    require(contractId.isNotBlank()) { "contractIds must not contain blank entries" }
                }
            }

            topics?.let {
                require(it.size <= 5) { "topics must not exceed 5 topic filters" }
                it.forEach { topicFilter ->
                    require(topicFilter.isNotEmpty()) { "topic filter must not be empty" }

                    // Check for ** only at the end
                    val doubleWildcardIndex = topicFilter.indexOf("**")
                    if (doubleWildcardIndex != -1) {
                        require(doubleWildcardIndex == topicFilter.size - 1) {
                            "Wildcard '**' can only appear as the last segment"
                        }
                    }

                    // Max 4 segments (not counting trailing **)
                    val hasTrailingDoubleWildcard = topicFilter.lastOrNull() == "**"
                    val maxSegments = if (hasTrailingDoubleWildcard) topicFilter.size - 1 else topicFilter.size
                    require(maxSegments <= 4) {
                        "topic filter must not exceed 4 segments (excluding trailing '**')"
                    }
                }
            }
        }
    }

    /**
     * Type of event to filter.
     *
     * @property SYSTEM System events generated by the Stellar protocol.
     * @property CONTRACT Events emitted by smart contracts.
     *
     * Note: Diagnostic events are included by default when type is omitted.
     * They cannot be filtered exclusively.
     */
    @Serializable
    enum class EventFilterType {
        @SerialName("system")
        SYSTEM,

        @SerialName("contract")
        CONTRACT
    }
}
