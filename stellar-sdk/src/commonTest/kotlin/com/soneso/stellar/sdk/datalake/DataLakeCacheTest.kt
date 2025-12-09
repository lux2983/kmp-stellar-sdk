package com.soneso.stellar.sdk.datalake

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DataLakeCacheTest {

    @Test
    fun testCacheGetReturnsNullForNonExistentKey() = runTest {
        val cache = InMemoryDataLakeCache()
        val result = cache.get(12345u)
        assertNull(result, "Cache should return null for non-existent key")
    }

    @Test
    fun testCachePutAndGetRoundtrip() = runTest {
        val cache = InMemoryDataLakeCache()
        val ledgerSequence = 12345u
        val data = byteArrayOf(1, 2, 3, 4, 5)

        cache.put(ledgerSequence, data)
        val retrieved = cache.get(ledgerSequence)

        assertNotNull(retrieved, "Cache should return stored data")
        assertTrue(data.contentEquals(retrieved), "Retrieved data should match stored data")
    }

    @Test
    fun testCacheLRUEviction() = runTest {
        val maxEntries = 5
        val cache = InMemoryDataLakeCache(maxEntries = maxEntries)

        // Add maxEntries + 2 entries
        for (i in 1..7) {
            cache.put(i.toUInt(), byteArrayOf(i.toByte()))
        }

        // First two entries should be evicted
        assertNull(cache.get(1u), "First entry should be evicted")
        assertNull(cache.get(2u), "Second entry should be evicted")

        // Last 5 entries should still be present
        for (i in 3..7) {
            val retrieved = cache.get(i.toUInt())
            assertNotNull(retrieved, "Entry $i should still be in cache")
            assertEquals(i.toByte(), retrieved[0], "Entry $i should have correct value")
        }

        // Verify cache size
        assertEquals(maxEntries, cache.size(), "Cache size should be maxEntries")
    }

    @Test
    fun testCacheLRUAccessOrder() = runTest {
        val maxEntries = 3
        val cache = InMemoryDataLakeCache(maxEntries = maxEntries)

        // Add 3 entries
        cache.put(1u, byteArrayOf(1))
        cache.put(2u, byteArrayOf(2))
        cache.put(3u, byteArrayOf(3))

        // Access entry 1 (moves it to most recently used)
        cache.get(1u)

        // Add a new entry (should evict entry 2, the least recently used)
        cache.put(4u, byteArrayOf(4))

        // Entry 2 should be evicted
        assertNull(cache.get(2u), "Entry 2 should be evicted as least recently used")

        // Entries 1, 3, 4 should still be present
        assertNotNull(cache.get(1u), "Entry 1 should still be in cache")
        assertNotNull(cache.get(3u), "Entry 3 should still be in cache")
        assertNotNull(cache.get(4u), "Entry 4 should still be in cache")
    }

    @Test
    fun testCacheClear() = runTest {
        val cache = InMemoryDataLakeCache()

        // Add some entries
        cache.put(1u, byteArrayOf(1))
        cache.put(2u, byteArrayOf(2))
        cache.put(3u, byteArrayOf(3))

        assertEquals(3, cache.size(), "Cache should have 3 entries")

        // Clear cache
        cache.clear()

        assertEquals(0, cache.size(), "Cache should be empty after clear")
        assertNull(cache.get(1u), "Entry should not exist after clear")
        assertNull(cache.get(2u), "Entry should not exist after clear")
        assertNull(cache.get(3u), "Entry should not exist after clear")
    }

    @Test
    fun testCacheWithCustomMaxEntries() = runTest {
        val customMaxEntries = 50
        val cache = InMemoryDataLakeCache(maxEntries = customMaxEntries)

        // Add customMaxEntries items
        for (i in 1..customMaxEntries) {
            cache.put(i.toUInt(), byteArrayOf(i.toByte()))
        }

        assertEquals(customMaxEntries, cache.size(), "Cache should have exactly maxEntries items")

        // Add one more item
        cache.put((customMaxEntries + 1).toUInt(), byteArrayOf((customMaxEntries + 1).toByte()))

        // First entry should be evicted
        assertNull(cache.get(1u), "First entry should be evicted")

        // Cache size should remain at maxEntries
        assertEquals(customMaxEntries, cache.size(), "Cache size should remain at maxEntries")
    }

    @Test
    fun testCacheConcurrentAccess() = runTest {
        val cache = InMemoryDataLakeCache(maxEntries = 100)
        val numCoroutines = 10
        val entriesPerCoroutine = 10

        // Launch multiple coroutines to write to cache
        val writeJobs = (1..numCoroutines).map { coroutineId ->
            async {
                for (i in 1..entriesPerCoroutine) {
                    val key = (coroutineId * 100 + i).toUInt()
                    val data = byteArrayOf(coroutineId.toByte(), i.toByte())
                    cache.put(key, data)
                }
            }
        }

        // Wait for all writes to complete
        writeJobs.awaitAll()

        // Launch multiple coroutines to read from cache
        val readJobs = (1..numCoroutines).map { coroutineId ->
            async {
                for (i in 1..entriesPerCoroutine) {
                    val key = (coroutineId * 100 + i).toUInt()
                    val data = cache.get(key)
                    assertNotNull(data, "Data should exist for key $key")
                    assertEquals(coroutineId.toByte(), data[0], "First byte should match coroutine ID")
                    assertEquals(i.toByte(), data[1], "Second byte should match iteration")
                }
            }
        }

        // Wait for all reads to complete
        readJobs.awaitAll()

        // Verify total cache size
        val expectedSize = numCoroutines * entriesPerCoroutine
        assertEquals(expectedSize, cache.size(), "Cache should contain all entries")
    }

    @Test
    fun testCacheDefensiveCopy() = runTest {
        val cache = InMemoryDataLakeCache()
        val ledgerSequence = 12345u
        val originalData = byteArrayOf(1, 2, 3, 4, 5)

        // Store data
        cache.put(ledgerSequence, originalData)

        // Modify original data
        originalData[0] = 99

        // Retrieve data
        val retrieved = cache.get(ledgerSequence)
        assertNotNull(retrieved, "Cache should return stored data")

        // Verify the cached data is not affected by the modification
        assertEquals(1, retrieved[0], "Cached data should not be affected by original data modification")

        // Modify retrieved data
        retrieved[0] = 88

        // Retrieve data again
        val retrievedAgain = cache.get(ledgerSequence)
        assertNotNull(retrievedAgain, "Cache should return stored data")

        // Verify the cached data is not affected by the modification
        assertEquals(1, retrievedAgain[0], "Cached data should not be affected by retrieved data modification")
    }

    @Test
    fun testCacheUpdateExistingEntry() = runTest {
        val cache = InMemoryDataLakeCache()
        val ledgerSequence = 12345u

        // Store initial data
        cache.put(ledgerSequence, byteArrayOf(1, 2, 3))

        // Update with new data
        cache.put(ledgerSequence, byteArrayOf(4, 5, 6))

        // Retrieve data
        val retrieved = cache.get(ledgerSequence)
        assertNotNull(retrieved, "Cache should return stored data")
        assertTrue(byteArrayOf(4, 5, 6).contentEquals(retrieved), "Retrieved data should be the updated data")

        // Cache size should still be 1
        assertEquals(1, cache.size(), "Cache size should still be 1 after update")
    }

    @Test
    fun testCacheWithZeroMaxEntriesThrowsException() = runTest {
        assertFailsWith<IllegalArgumentException>("Creating cache with maxEntries=0 should throw") {
            InMemoryDataLakeCache(maxEntries = 0)
        }
    }

    @Test
    fun testCacheWithNegativeMaxEntriesThrowsException() = runTest {
        assertFailsWith<IllegalArgumentException>("Creating cache with negative maxEntries should throw") {
            InMemoryDataLakeCache(maxEntries = -1)
        }
    }

    @Test
    fun testCacheRemove() = runTest {
        val cache = InMemoryDataLakeCache()
        val ledgerSequence = 12345u

        // Store data
        cache.put(ledgerSequence, byteArrayOf(1, 2, 3))
        assertNotNull(cache.get(ledgerSequence), "Entry should exist")

        // Remove entry
        val removed = cache.remove(ledgerSequence)
        assertTrue(removed, "Remove should return true for existing entry")
        assertNull(cache.get(ledgerSequence), "Entry should be removed")

        // Try to remove non-existent entry
        val removedAgain = cache.remove(ledgerSequence)
        assertFalse(removedAgain, "Remove should return false for non-existent entry")
    }

    @Test
    fun testCacheWithLargeData() = runTest {
        val cache = InMemoryDataLakeCache(maxEntries = 10)
        val ledgerSequence = 12345u

        // Create a large byte array (1 MB)
        val largeData = ByteArray(1024 * 1024) { it.toByte() }

        cache.put(ledgerSequence, largeData)
        val retrieved = cache.get(ledgerSequence)

        assertNotNull(retrieved, "Cache should return large data")
        assertEquals(largeData.size, retrieved.size, "Retrieved data size should match")
        assertTrue(largeData.contentEquals(retrieved), "Large data should be stored and retrieved correctly")
    }

    @Test
    fun testCacheMultipleRetrievalsUpdateAccessOrder() = runTest {
        val maxEntries = 3
        val cache = InMemoryDataLakeCache(maxEntries = maxEntries)

        // Add 3 entries
        cache.put(1u, byteArrayOf(1))
        cache.put(2u, byteArrayOf(2))
        cache.put(3u, byteArrayOf(3))

        // Access entry 1 multiple times
        cache.get(1u)
        cache.get(1u)
        cache.get(1u)

        // Add a new entry (should evict entry 2)
        cache.put(4u, byteArrayOf(4))

        // Entry 2 should be evicted
        assertNull(cache.get(2u), "Entry 2 should be evicted")

        // Entry 1 should still be present (frequently accessed)
        assertNotNull(cache.get(1u), "Entry 1 should still be in cache")
    }
}
