package com.example.stormpilot.testing

import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.AssumptionViolatedException
import org.junit.Test
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.concurrent.TimeUnit

class SkipAfterTimeoutRuleTest : StormPilotUnitTest() {
    @Test
    fun evaluate_allowsFastStatementsToComplete() {
        var evaluated = false

        SkipAfterTimeoutRule(timeout = 100, unit = TimeUnit.MILLISECONDS)
            .apply(
                statement { evaluated = true },
                Description.createTestDescription(javaClass, "fast"),
            )
            .evaluate()

        assertTrue(evaluated)
    }

    @Test
    fun evaluate_convertsTimeoutIntoSkippedAssumption() {
        val wrapped = SkipAfterTimeoutRule(timeout = 10, unit = TimeUnit.MILLISECONDS)
            .apply(
                statement { Thread.sleep(1_000) },
                Description.createTestDescription(javaClass, "slow"),
            )

        assertThrows(AssumptionViolatedException::class.java) {
            wrapped.evaluate()
        }
    }

    @Test
    fun evaluate_propagatesAssertionFailuresFromWrappedTest() {
        val expected = AssertionError("boom")
        val wrapped = SkipAfterTimeoutRule(timeout = 100, unit = TimeUnit.MILLISECONDS)
            .apply(
                statement { throw expected },
                Description.createTestDescription(javaClass, "failure"),
            )

        val actual = assertThrows(AssertionError::class.java) {
            wrapped.evaluate()
        }

        assertSame(expected, actual)
    }

    private fun statement(block: () -> Unit): Statement {
        return object : Statement() {
            override fun evaluate() {
                block()
            }
        }
    }
}
