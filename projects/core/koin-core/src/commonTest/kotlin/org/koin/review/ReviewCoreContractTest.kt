package org.koin.review

import org.koin.dsl.*
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.core.scope.ScopeCallback
import kotlin.test.*

class ReviewCoreContractTest {
    @Test fun throwingDefinitionOnCloseCanRetryWithoutLosingTheScopeSource() {
        var attempts = 0
        var callbacks = 0
        lateinit var closingScope: Scope
        val source = Any()
        val app = koinApplication { modules(module {
            scope(named("definition-retry")) {
                scoped { Value() }.onClose {
                    assertTrue(closingScope.closed)
                    assertFailsWith<org.koin.core.error.ClosedScopeException> { closingScope.get<Value>() }
                    closingScope.close() // Reentrant cleanup must not invoke onClose twice.
                    if (++attempts == 1) error("definition onClose")
                }
            }
        }) }
        try {
            closingScope = app.koin.createScope("definition-retry", named("definition-retry"), source)
            val value = closingScope.get<Value>()
            closingScope.registerCallback(object : ScopeCallback {
                override fun onScopeClose(scope: Scope) { callbacks++ }
            })
            assertFailsWith<IllegalStateException> { closingScope.close() }
            assertFalse(closingScope.closed)
            assertSame(closingScope, app.koin.getScopeOrNull("definition-retry"))
            assertSame(source, closingScope.getSource<Any>())
            assertSame(value, closingScope.get<Value>())
            closingScope.close()
            closingScope.close()
            assertTrue(closingScope.closed)
            assertNull(app.koin.getScopeOrNull("definition-retry"))
            assertNull(closingScope.getSource<Any>())
            assertEquals(2, attempts)
            assertEquals(1, callbacks)
        } finally { app.close() }
    }

    @Test fun throwingScopeCallbackCanRetryWithoutLeavingClosingGateSet() {
        var callbacks = 0
        var disposed = 0
        val app = koinApplication { modules(module {
            scope(named("retry")) { scoped { Value() }.onClose { disposed++ } }
        }) }
        try {
            val scope = app.koin.createScope("retry", named("retry"))
            val value = scope.get<Value>()
            scope.registerCallback(object : ScopeCallback {
                override fun onScopeClose(scope: Scope) {
                    assertSame(value, scope.get<Value>())
                    if (++callbacks == 1) error("host callback")
                }
            })
            assertFailsWith<IllegalStateException> { scope.close() }
            assertFalse(scope.closed)
            scope.close(); assertTrue(scope.closed)
            assertEquals(2, callbacks); assertEquals(1, disposed)
        } finally { app.close() }
    }

    class Value(val text: String = "default")

    @Test fun singleFactoryParametersScopeCloseAndUnloadKeepTheirContracts() {
        var scopeClosed = 0
        val definitions = module {
            single(named("single")) { Value() }
            factory(named("factory")) { Value() }
            factory(named("parameter")) { (text: String) -> Value(text) }
            scope(named("screen")) { scoped { Value() }.onClose { scopeClosed++ } }
        }
        val app = koinApplication { modules(definitions) }
        val koin = app.koin
        try {
            assertSame(koin.get<Value>(named("single")), koin.get<Value>(named("single")))
            assertNotSame(koin.get<Value>(named("factory")), koin.get<Value>(named("factory")))
            assertEquals("provided", koin.get<Value>(named("parameter")) { parametersOf("provided") }.text)
            val first = koin.createScope("first", named("screen"))
            val second = koin.createScope("second", named("screen"))
            assertSame(first.get<Value>(), first.get<Value>())
            assertNotSame(first.get<Value>(), second.get<Value>())
            val firstValue = first.get<Value>()
            var callbacks = 0
            first.registerCallback(object : ScopeCallback {
                override fun onScopeClose(scope: Scope) {
                    callbacks++
                    assertSame(firstValue, scope.get<Value>())
                    scope.close()
                }
            })
            first.close(); first.close()
            assertEquals(1, callbacks)
            assertEquals(1, scopeClosed)
            assertNull(koin.getScopeOrNull("first"))
            second.close(); assertEquals(2, scopeClosed)
            koin.unloadModules(listOf(definitions))
            assertNull(koin.getOrNull<Value>(named("single")))
        } finally { app.close() }
    }
}
