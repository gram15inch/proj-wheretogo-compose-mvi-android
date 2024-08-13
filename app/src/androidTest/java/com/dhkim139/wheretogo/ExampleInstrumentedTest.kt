package com.dhkim139.wheretogo





import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase.assertEquals


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
import org.junit.jupiter.api.Test


class ExampleInstrumentedTest {


    @Test
    fun useAppContext() {
        // Context of the app under test.
       val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.dhkim139.wheretogo", appContext.packageName)

    }
}