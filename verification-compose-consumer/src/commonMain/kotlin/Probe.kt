package org.koin.verification.compose

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject

@Composable
fun probeKoinCompose() {
    koinInject<Any>()
}
