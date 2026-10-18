package org.koin.core

import org.koin.Simple
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.component.getScopeId
import org.koin.core.logger.Level
import org.koin.core.qualifier.named
import org.koin.core.resolution.flatten
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

@OptIn(KoinInternalApi::class)
class CascadeScopeLinkTest {

    @Test
    fun single_parent_link_resolves_parent_before_root_fallback() {
        val app = koinApplication {
            modules(module {
                single { Simple.ComponentA() }
                single(named("root")) { Simple.ComponentA() }
                scope(named("parent")) { scoped { Simple.ComponentA() } }
                scope(named("child")) { }
            })
        }
        try {
            val koin = app.koin
            val parent = koin.createScope("parent", named("parent"))
            val child = koin.createScope("child", named("child"))
            child.unlink(koin.getScope("_root_"))
            child.linkTo(parent)
            assertSame(parent.get<Simple.ComponentA>(), child.get<Simple.ComponentA>())
            assertSame(koin.get<Simple.ComponentA>(named("root")), child.get<Simple.ComponentA>(named("root")))
            assertNull(child.getOrNull<String>())
        } finally {
            app.close()
        }
    }

    @Test
    fun `flatten scope set`(){
        val koin = koinApplication {
            modules(
                module {
                    scope<Simple.ComponentA> {
                    }
                    scope<Simple.ComponentB> {
                    }
                }
            )
        }.koin

        val a = Simple.ComponentA()
        val scopeA = koin.createScope<Simple.ComponentA>(a.getScopeId())

        val b = Simple.ComponentB(a)
        val scopeB = koin.createScope<Simple.ComponentA>(b.getScopeId())

        scopeB.linkTo(scopeA)

        val set = flatten(scopeB.linkedScopes.toList() + scopeB)
        assertEquals(setOf(scopeB,scopeA,koin.scopeRegistry.rootScope), set)
    }

    @Test
    fun `transverse scopes`(){
        val koin = koinApplication {
            modules(
                module {

                    scope<Simple.ComponentA> {
                        scoped { Simple.ComponentB(get()) }
                    }
                    scope<Simple.ComponentB> {
                        scoped { Simple.ComponentC(get()) }
                    }
                }
            )
            printLogger(Level.DEBUG)
        }.koin

        val a = Simple.ComponentA()
        val scopeA = koin.createScope<Simple.ComponentA>(a.getScopeId(), source = a)

        val b = Simple.ComponentB(a)
        val scopeB = koin.createScope<Simple.ComponentB>(b.getScopeId(), source = b)
        scopeB.linkTo(scopeA)

        val cFromScopeB = scopeB.get<Simple.ComponentC>()
        val bFromScopeB = scopeB.get<Simple.ComponentB>()
        val bFromScopeB2 = scopeB.get<Simple.ComponentB>()
        val bFromScopeA = scopeA.get<Simple.ComponentB>()
        assertEquals(b, cFromScopeB.b)
        assertEquals(b, bFromScopeB)
        assertEquals(b, bFromScopeB2)
        assertNotEquals(b, bFromScopeA)
        assertEquals(a, bFromScopeB.a)
        assertEquals(a, bFromScopeB2.a)
    }
}
