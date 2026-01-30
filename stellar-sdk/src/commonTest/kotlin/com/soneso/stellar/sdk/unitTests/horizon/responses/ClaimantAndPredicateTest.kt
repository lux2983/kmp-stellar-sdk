package com.soneso.stellar.sdk.unitTests.horizon.responses

import com.soneso.stellar.sdk.horizon.responses.Claimant
import com.soneso.stellar.sdk.horizon.responses.Predicate
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ClaimantAndPredicateTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testClaimantDeserialization() {
        val claimantJson = """
        {
            "destination": "GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7",
            "predicate": {"unconditional": true}
        }
        """.trimIndent()
        val claimant = json.decodeFromString<Claimant>(claimantJson)
        assertEquals("GAAZI4TCR3TY5OJHCTJC2A4QSY6CJWJH5IAJTGKIN2ER7LBNVKOCCWN7", claimant.destination)
        assertEquals(true, claimant.predicate.unconditional)
    }

    @Test
    fun testUnconditionalPredicate() {
        val predicateJson = """{"unconditional": true}"""
        val predicate = json.decodeFromString<Predicate>(predicateJson)
        assertEquals(true, predicate.unconditional)
        assertNull(predicate.absBefore)
        assertNull(predicate.relBefore)
        assertNull(predicate.and)
        assertNull(predicate.or)
        assertNull(predicate.not)
    }

    @Test
    fun testAbsBeforePredicate() {
        val predicateJson = """{"abs_before": "2021-12-31T00:00:00Z"}"""
        val predicate = json.decodeFromString<Predicate>(predicateJson)
        assertEquals("2021-12-31T00:00:00Z", predicate.absBefore)
        assertNull(predicate.unconditional)
    }

    @Test
    fun testRelBeforePredicate() {
        val predicateJson = """{"rel_before": "86400"}"""
        val predicate = json.decodeFromString<Predicate>(predicateJson)
        assertEquals("86400", predicate.relBefore)
    }

    @Test
    fun testAndPredicate() {
        val predicateJson = """
        {
            "and": [
                {"abs_before": "2021-12-31T00:00:00Z"},
                {"rel_before": "86400"}
            ]
        }
        """.trimIndent()
        val predicate = json.decodeFromString<Predicate>(predicateJson)
        assertEquals(2, predicate.and?.size)
        assertEquals("2021-12-31T00:00:00Z", predicate.and?.get(0)?.absBefore)
        assertEquals("86400", predicate.and?.get(1)?.relBefore)
    }

    @Test
    fun testOrPredicate() {
        val predicateJson = """
        {
            "or": [
                {"unconditional": true},
                {"abs_before": "2022-01-01T00:00:00Z"}
            ]
        }
        """.trimIndent()
        val predicate = json.decodeFromString<Predicate>(predicateJson)
        assertEquals(2, predicate.or?.size)
        assertEquals(true, predicate.or?.get(0)?.unconditional)
        assertEquals("2022-01-01T00:00:00Z", predicate.or?.get(1)?.absBefore)
    }

    @Test
    fun testNotPredicate() {
        val predicateJson = """
        {
            "not": {"abs_before": "2021-06-01T00:00:00Z"}
        }
        """.trimIndent()
        val predicate = json.decodeFromString<Predicate>(predicateJson)
        assertEquals("2021-06-01T00:00:00Z", predicate.not?.absBefore)
    }

    @Test
    fun testNestedPredicate() {
        val predicateJson = """
        {
            "and": [
                {"not": {"abs_before": "2021-06-01T00:00:00Z"}},
                {"or": [
                    {"abs_before": "2022-12-31T00:00:00Z"},
                    {"unconditional": true}
                ]}
            ]
        }
        """.trimIndent()
        val predicate = json.decodeFromString<Predicate>(predicateJson)
        assertEquals(2, predicate.and?.size)
        val notPred = predicate.and?.get(0)?.not
        assertEquals("2021-06-01T00:00:00Z", notPred?.absBefore)
        val orPred = predicate.and?.get(1)?.or
        assertEquals(2, orPred?.size)
    }

    @Test
    fun testPredicateDataClassEquality() {
        val p1 = Predicate(unconditional = true)
        val p2 = Predicate(unconditional = true)
        assertEquals(p1, p2)
        assertEquals(p1.hashCode(), p2.hashCode())
    }

    @Test
    fun testClaimantDataClassEquality() {
        val c1 = Claimant("GTEST", Predicate(unconditional = true))
        val c2 = Claimant("GTEST", Predicate(unconditional = true))
        assertEquals(c1, c2)
    }
}
