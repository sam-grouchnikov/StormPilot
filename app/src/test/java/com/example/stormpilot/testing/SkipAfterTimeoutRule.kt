package com.example.stormpilot.testing

import org.junit.AssumptionViolatedException
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class SkipAfterTimeoutRule(
    private val timeout: Long = 1,
    private val unit: TimeUnit = TimeUnit.MINUTES,
) : TestRule {
    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                val executor = Executors.newSingleThreadExecutor { runnable ->
                    Thread(
                        runnable,
                        "unit-test-${description.className}.${description.methodName}",
                    ).apply {
                        isDaemon = true
                    }
                }

                val future = executor.submit(Callable {
                    base.evaluate()
                    Unit
                })

                try {
                    future.get(timeout, unit)
                } catch (error: TimeoutException) {
                    future.cancel(true)
                    throw AssumptionViolatedException(
                        "Skipped because ${description.displayName} exceeded ${unit.toSeconds(timeout)} seconds.",
                    )
                } catch (error: ExecutionException) {
                    throw unwrapExecutionError(error)
                } finally {
                    executor.shutdownNow()
                }
            }
        }
    }

    private fun unwrapExecutionError(error: ExecutionException): Throwable {
        return error.cause ?: error
    }
}
