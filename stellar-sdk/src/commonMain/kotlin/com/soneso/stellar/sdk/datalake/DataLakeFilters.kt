package com.soneso.stellar.sdk.datalake

data class TransactionFilter(
    val contractId: String? = null,
    val sourceAccount: String? = null,
    val operationType: String? = null,
    val includeFailedTransactions: Boolean = false
) {
    companion object {
        fun byContract(contractId: String) = TransactionFilter(contractId = contractId)
        fun byAccount(account: String) = TransactionFilter(sourceAccount = account)
        fun all() = TransactionFilter()
    }
}

data class EventFilter(
    val contractId: String? = null,
    val topics: List<String>? = null,
    val eventType: EventType? = null
) {
    companion object {
        fun byContract(contractId: String) = EventFilter(contractId = contractId)
        fun byTopic(topic: String) = EventFilter(topics = listOf(topic))
        fun all() = EventFilter()
    }
}

enum class EventType {
    CONTRACT,
    SYSTEM,
    DIAGNOSTIC
}
