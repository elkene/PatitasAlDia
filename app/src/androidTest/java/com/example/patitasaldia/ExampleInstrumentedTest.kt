package com.example.patitasaldia

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Prueba instrumentada de ejemplo que se ejecuta en un dispositivo Android.
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun usaElContextoDeLaApp() {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.patitasaldia", contexto.packageName)
    }
}
