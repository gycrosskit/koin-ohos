package org.koin.resolution

import org.koin.Simple
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.instance.ResolutionContext
import org.koin.core.logger.Level
import org.koin.core.qualifier.named
import org.koin.core.resolution.ResolutionExtension
import org.koin.core.scope.Scope
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.to

@OptIn(KoinInternalApi::class)
class ResolutionExtensionTest {

    @Test
    fun add_extend_resolution(){
        val resolutionExtension = object : ResolutionExtension {
            override val name: String = "hello-null-extension"
            override fun resolve(
                scope: Scope,
                instanceContext: ResolutionContext
            ): Any? {
                return null
            }
        }

        val koin = koinApplication{
            koin.resolver.addResolutionExtension(resolutionExtension)
        }.koin

        assertEquals(1,koin.resolver.extendedResolution.size)
    }

    @Test
    fun extend_resolution_test(){
        val resolutionExtension = object : ResolutionExtension {
            val instanceMap = mapOf<KClass<*>, Any>(
                Simple.ComponentA::class to Simple.ComponentA()
            )

            override val name: String = "hello-extension"
            override fun resolve(
                scope: Scope,
                instanceContext: ResolutionContext
            ): Any? {
                return instanceMap[instanceContext.clazz]
            }
        }

        val koin = koinApplication{
            printLogger(Level.DEBUG)
            koin.resolver.addResolutionExtension(resolutionExtension)
            modules(module {
                single { Simple.ComponentB(get())}
            })
        }.koin

        assertEquals(resolutionExtension.instanceMap[Simple.ComponentA::class], koin.get<Simple.ComponentB>().a)
        assertSame(resolutionExtension.instanceMap[Simple.ComponentA::class], koin.get<Simple.ComponentA>())
        assertNull(koin.getOrNull<String>())
        assertEquals(1,koin.instanceRegistry.instances.values.size)
    }

    @Test
    fun parent_definitions_precede_extension_fallback() {
        var extensionCalls = 0
        val resolutionExtension = object : ResolutionExtension {
            override val name = "parent-fallback"
            override fun resolve(scope: Scope, instanceContext: ResolutionContext): Any? {
                extensionCalls++
                return if (instanceContext.clazz == String::class) "extension" else null
            }
        }
        val app = koinApplication {
            koin.resolver.addResolutionExtension(resolutionExtension)
            modules(module {
                single { "registered" }
                scope(named("child")) { }
            })
        }
        try {
            val child = app.koin.createScope("child", named("child"))
            assertEquals("registered", child.get<String>())
            assertEquals(0, extensionCalls)
            assertEquals("extension", child.get<String>(named("missing")))
            assertEquals(1, extensionCalls)
        } finally {
            app.close()
        }
    }
}
