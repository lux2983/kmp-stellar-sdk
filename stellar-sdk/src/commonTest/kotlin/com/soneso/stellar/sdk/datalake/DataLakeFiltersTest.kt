package com.soneso.stellar.sdk.datalake

import kotlin.test.*

/**
 * Unit tests for Data Lake filter classes.
 *
 * Tests TransactionFilter and EventFilter factory methods and defaults.
 */
class DataLakeFiltersTest {

    /**
     * Test TransactionFilter.byContract factory method.
     */
    @Test
    fun testTransactionFilter_ByContract() {
        val contractId = "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABSC4"
        val filter = TransactionFilter.byContract(contractId)

        assertEquals(contractId, filter.contractId, "Should set contract ID")
        assertNull(filter.sourceAccount, "Source account should be null")
        assertNull(filter.operationType, "Operation type should be null")
        assertFalse(filter.includeFailedTransactions, "Should not include failed transactions by default")
    }

    /**
     * Test TransactionFilter.byAccount factory method.
     */
    @Test
    fun testTransactionFilter_ByAccount() {
        val accountId = "GBRPYHIL2CI3FNQ4BXLFMNDLFJUNPU2HY3ZMFSHONUCEOASW7QC7OX2H"
        val filter = TransactionFilter.byAccount(accountId)

        assertEquals(accountId, filter.sourceAccount, "Should set source account")
        assertNull(filter.contractId, "Contract ID should be null")
        assertNull(filter.operationType, "Operation type should be null")
        assertFalse(filter.includeFailedTransactions, "Should not include failed transactions by default")
    }

    /**
     * Test TransactionFilter.all factory method.
     */
    @Test
    fun testTransactionFilter_All() {
        val filter = TransactionFilter.all()

        assertNull(filter.contractId, "Contract ID should be null")
        assertNull(filter.sourceAccount, "Source account should be null")
        assertNull(filter.operationType, "Operation type should be null")
        assertFalse(filter.includeFailedTransactions, "Should not include failed transactions by default")
    }

    /**
     * Test TransactionFilter with custom parameters.
     */
    @Test
    fun testTransactionFilter_CustomParameters() {
        val filter = TransactionFilter(
            contractId = "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABSC4",
            sourceAccount = "GBRPYHIL2CI3FNQ4BXLFMNDLFJUNPU2HY3ZMFSHONUCEOASW7QC7OX2H",
            operationType = "PAYMENT",
            includeFailedTransactions = true
        )

        assertEquals("CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABSC4", filter.contractId)
        assertEquals("GBRPYHIL2CI3FNQ4BXLFMNDLFJUNPU2HY3ZMFSHONUCEOASW7QC7OX2H", filter.sourceAccount)
        assertEquals("PAYMENT", filter.operationType)
        assertTrue(filter.includeFailedTransactions, "Should include failed transactions when specified")
    }

    /**
     * Test TransactionFilter defaults.
     */
    @Test
    fun testTransactionFilter_Defaults() {
        val filter = TransactionFilter()

        assertNull(filter.contractId, "Contract ID should default to null")
        assertNull(filter.sourceAccount, "Source account should default to null")
        assertNull(filter.operationType, "Operation type should default to null")
        assertFalse(filter.includeFailedTransactions, "Should not include failed transactions by default")
    }

    /**
     * Test TransactionFilter with only operation type.
     */
    @Test
    fun testTransactionFilter_OnlyOperationType() {
        val filter = TransactionFilter(operationType = "INVOKE_HOST_FUNCTION")

        assertNull(filter.contractId, "Contract ID should be null")
        assertNull(filter.sourceAccount, "Source account should be null")
        assertEquals("INVOKE_HOST_FUNCTION", filter.operationType)
        assertFalse(filter.includeFailedTransactions, "Should not include failed transactions by default")
    }

    /**
     * Test TransactionFilter with includeFailedTransactions only.
     */
    @Test
    fun testTransactionFilter_IncludeFailedOnly() {
        val filter = TransactionFilter(includeFailedTransactions = true)

        assertNull(filter.contractId, "Contract ID should be null")
        assertNull(filter.sourceAccount, "Source account should be null")
        assertNull(filter.operationType, "Operation type should be null")
        assertTrue(filter.includeFailedTransactions, "Should include failed transactions")
    }

    /**
     * Test EventFilter.byContract factory method.
     */
    @Test
    fun testEventFilter_ByContract() {
        val contractId = "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABSC4"
        val filter = EventFilter.byContract(contractId)

        assertEquals(contractId, filter.contractId, "Should set contract ID")
        assertNull(filter.topics, "Topics should be null")
        assertNull(filter.eventType, "Event type should be null")
    }

    /**
     * Test EventFilter.byTopic factory method.
     */
    @Test
    fun testEventFilter_ByTopic() {
        val topic = "transfer"
        val filter = EventFilter.byTopic(topic)

        assertEquals(listOf(topic), filter.topics, "Should set topics as single-item list")
        assertNull(filter.contractId, "Contract ID should be null")
        assertNull(filter.eventType, "Event type should be null")
    }

    /**
     * Test EventFilter.all factory method.
     */
    @Test
    fun testEventFilter_All() {
        val filter = EventFilter.all()

        assertNull(filter.contractId, "Contract ID should be null")
        assertNull(filter.topics, "Topics should be null")
        assertNull(filter.eventType, "Event type should be null")
    }

    /**
     * Test EventFilter with custom parameters.
     */
    @Test
    fun testEventFilter_CustomParameters() {
        val filter = EventFilter(
            contractId = "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABSC4",
            topics = listOf("transfer", "mint"),
            eventType = EventType.CONTRACT
        )

        assertEquals("CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABSC4", filter.contractId)
        assertEquals(listOf("transfer", "mint"), filter.topics)
        assertEquals(EventType.CONTRACT, filter.eventType)
    }

    /**
     * Test EventFilter defaults.
     */
    @Test
    fun testEventFilter_Defaults() {
        val filter = EventFilter()

        assertNull(filter.contractId, "Contract ID should default to null")
        assertNull(filter.topics, "Topics should default to null")
        assertNull(filter.eventType, "Event type should default to null")
    }

    /**
     * Test EventFilter with multiple topics.
     */
    @Test
    fun testEventFilter_MultipleTopics() {
        val topics = listOf("transfer", "mint", "burn")
        val filter = EventFilter(topics = topics)

        assertEquals(topics, filter.topics)
        assertNull(filter.contractId, "Contract ID should be null")
        assertNull(filter.eventType, "Event type should be null")
    }

    /**
     * Test EventFilter with empty topics list.
     */
    @Test
    fun testEventFilter_EmptyTopics() {
        val filter = EventFilter(topics = emptyList())

        assertEquals(emptyList(), filter.topics, "Topics should be empty list")
        assertNull(filter.contractId, "Contract ID should be null")
        assertNull(filter.eventType, "Event type should be null")
    }

    /**
     * Test EventFilter with contract event type.
     */
    @Test
    fun testEventFilter_ContractEventType() {
        val filter = EventFilter(eventType = EventType.CONTRACT)

        assertEquals(EventType.CONTRACT, filter.eventType)
        assertNull(filter.contractId, "Contract ID should be null")
        assertNull(filter.topics, "Topics should be null")
    }

    /**
     * Test EventFilter with system event type.
     */
    @Test
    fun testEventFilter_SystemEventType() {
        val filter = EventFilter(eventType = EventType.SYSTEM)

        assertEquals(EventType.SYSTEM, filter.eventType)
        assertNull(filter.contractId, "Contract ID should be null")
        assertNull(filter.topics, "Topics should be null")
    }

    /**
     * Test EventFilter with diagnostic event type.
     */
    @Test
    fun testEventFilter_DiagnosticEventType() {
        val filter = EventFilter(eventType = EventType.DIAGNOSTIC)

        assertEquals(EventType.DIAGNOSTIC, filter.eventType)
        assertNull(filter.contractId, "Contract ID should be null")
        assertNull(filter.topics, "Topics should be null")
    }

    /**
     * Test EventFilter with all parameters combined.
     */
    @Test
    fun testEventFilter_AllParametersCombined() {
        val contractId = "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABSC4"
        val topics = listOf("transfer")
        val eventType = EventType.CONTRACT

        val filter = EventFilter(
            contractId = contractId,
            topics = topics,
            eventType = eventType
        )

        assertEquals(contractId, filter.contractId)
        assertEquals(topics, filter.topics)
        assertEquals(eventType, filter.eventType)
    }

    /**
     * Test EventType enum values.
     */
    @Test
    fun testEventType_EnumValues() {
        val eventTypes = EventType.entries

        assertEquals(3, eventTypes.size, "Should have 3 event types")
        assertTrue(eventTypes.contains(EventType.CONTRACT), "Should contain CONTRACT")
        assertTrue(eventTypes.contains(EventType.SYSTEM), "Should contain SYSTEM")
        assertTrue(eventTypes.contains(EventType.DIAGNOSTIC), "Should contain DIAGNOSTIC")
    }

    /**
     * Test TransactionFilter copy semantics.
     */
    @Test
    fun testTransactionFilter_CopySemantics() {
        val filter1 = TransactionFilter(
            contractId = "CONTRACT1",
            sourceAccount = "ACCOUNT1"
        )

        val filter2 = filter1.copy(contractId = "CONTRACT2")

        assertEquals("CONTRACT2", filter2.contractId)
        assertEquals("ACCOUNT1", filter2.sourceAccount)
        assertEquals("CONTRACT1", filter1.contractId, "Original should not be modified")
    }

    /**
     * Test EventFilter copy semantics.
     */
    @Test
    fun testEventFilter_CopySemantics() {
        val filter1 = EventFilter(
            contractId = "CONTRACT1",
            topics = listOf("topic1")
        )

        val filter2 = filter1.copy(contractId = "CONTRACT2")

        assertEquals("CONTRACT2", filter2.contractId)
        assertEquals(listOf("topic1"), filter2.topics)
        assertEquals("CONTRACT1", filter1.contractId, "Original should not be modified")
    }
}
