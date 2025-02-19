package com.dhkim139.wheretogo.feature

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until


fun pickAccount(name: String) {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    device.waitForIdle()
    device.wait(Until.hasObject(By.text(name)), 5000)
    device.findObject(By.text(name))?.click()
}