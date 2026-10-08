package org.koin.review

import org.koin.core.context.GlobalContext
import kotlin.test.*

class PlatformContextLifecycleTest {
    @Test fun stopClearsTheApplicationReference() {
        GlobalContext.stopKoin()
        val application = GlobalContext.startKoin { }
        try {
            assertSame(application, GlobalContext.getKoinApplicationOrNull())
            GlobalContext.stopKoin()
            assertNull(GlobalContext.getOrNull())
            assertNull(GlobalContext.getKoinApplicationOrNull())
            assertNotSame(application, GlobalContext.startKoin { })
        } finally {
            GlobalContext.stopKoin()
        }
    }
}
