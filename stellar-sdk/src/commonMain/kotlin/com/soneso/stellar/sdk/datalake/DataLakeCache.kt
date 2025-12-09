package com.soneso.stellar.sdk.datalake

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Cache interface for storing decompressed ledger batch data.
 *
 * Implementations can use in-memory storage, disk caching, or other strategies
 * to avoid redundant downloads and decompression of ledger batches.
 *
 * Usage:
 * ```kotlin
 * val cache = InMemoryDataLakeCache(maxEntries = 50)
 * val client = DataLakeClient.mainnet(cache = cache)
 * ```
 */
interface DataLakeCache {
    /**
     * Retrieve cached decompressed data for a ledger batch.
     *
     * @param batchStartSequence The start ledger sequence of the batch (uint32)
     * @return Decompressed XDR data if cached, null if not found
     */
    suspend fun get(batchStartSequence: UInt): ByteArray?

    /**
     * Store decompressed data for a ledger batch.
     *
     * @param batchStartSequence The start ledger sequence of the batch (uint32)
     * @param data Decompressed XDR data
     */
    suspend fun put(batchStartSequence: UInt, data: ByteArray)

    /**
     * Remove a cached entry.
     *
     * @param batchStartSequence The start ledger sequence of the batch (uint32)
     * @return true if entry was removed, false if not found
     */
    suspend fun remove(batchStartSequence: UInt): Boolean

    /**
     * Clear all cached entries.
     */
    suspend fun clear()
}

/**
 * In-memory LRU cache for ledger batch data.
 *
 * Uses a LinkedHashMap with access-order mode to implement least-recently-used eviction.
 * When the cache reaches maxEntries, the oldest (least recently accessed) entry is removed.
 *
 * Thread-safe for concurrent access via coroutine Mutex.
 *
 * Note: Each cache entry stores decompressed XDR data which can be 1-2 MB per batch.
 * A cache with 100 entries may use 100-200 MB of memory.
 *
 * @property maxEntries Maximum number of ledger batches to cache (default 100)
 * @throws IllegalArgumentException if maxEntries is not positive
 */
class InMemoryDataLakeCache(
    private val maxEntries: Int = 100
) : DataLakeCache {

    init {
        require(maxEntries > 0) { "maxEntries must be positive, got $maxEntries" }
    }

    private val mutex = Mutex()

    // LinkedHashMap with access-order mode (true) for LRU behavior
    private val cache = object : LinkedHashMap<UInt, ByteArray>(maxEntries, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<UInt, ByteArray>?): Boolean {
            return size > maxEntries
        }
    }

    /**
     * Retrieve cached decompressed data for a ledger batch.
     *
     * @param batchStartSequence The start ledger sequence of the batch (uint32)
     * @return Decompressed XDR data if cached, null if not found
     */
    override suspend fun get(batchStartSequence: UInt): ByteArray? = mutex.withLock {
        cache[batchStartSequence]?.copyOf() // Defensive copy
    }

    /**
     * Store decompressed data for a ledger batch.
     *
     * If the cache is full, the least recently used entry is automatically evicted.
     *
     * @param batchStartSequence The start ledger sequence of the batch (uint32)
     * @param data Decompressed XDR data
     */
    override suspend fun put(batchStartSequence: UInt, data: ByteArray) = mutex.withLock {
        cache[batchStartSequence] = data.copyOf() // Defensive copy
    }

    /**
     * Remove a cached entry.
     *
     * @param batchStartSequence The start ledger sequence of the batch (uint32)
     * @return true if entry was removed, false if not found
     */
    override suspend fun remove(batchStartSequence: UInt): Boolean = mutex.withLock {
        cache.remove(batchStartSequence) != null
    }

    /**
     * Clear all cached entries.
     */
    override suspend fun clear() = mutex.withLock {
        cache.clear()
    }

    /**
     * Get the current number of cached entries.
     *
     * @return Number of entries currently in the cache
     */
    suspend fun size(): Int = mutex.withLock {
        cache.size
    }
}
