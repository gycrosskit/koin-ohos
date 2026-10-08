package org.koin.review

import org.koin.mp.KoinPlatformTools
import org.koin.core.context.MutableGlobalContext
import kotlin.test.*

class PlatformStackTraceTest {
    @Test fun stopClearsTheApplicationReference() {
        val context = MutableGlobalContext()
        val application = context.startKoin { }
        assertSame(application, context.getKoinApplicationOrNull())
        context.stopKoin()
        assertNull(context.getOrNull())
        assertNull(context.getKoinApplicationOrNull())
        assertNotSame(application, context.startKoin { })
        context.stopKoin()
    }

    private fun originalFailure(): Nothing = error("original native cause")
    @Test fun stackTraceBelongsToTheOriginalException() {
        val original = runCatching { originalFailure() }.exceptionOrNull() as Exception
        val text = KoinPlatformTools.getStackTrace(original)
        assertTrue(text.contains("original native cause"))
        assertTrue(text.contains("originalFailure"), text)
    }
}
