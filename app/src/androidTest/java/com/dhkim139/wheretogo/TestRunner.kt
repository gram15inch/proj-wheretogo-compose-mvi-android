package com.dhkim139.wheretogo

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import com.dhkim139.wheretogo.usecase.CheckPointScenarioTest
import com.dhkim139.wheretogo.usecase.CourseScenarioTest
import com.dhkim139.wheretogo.usecase.UserScenarioTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.runner.RunWith
import org.junit.runners.Suite

class TestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}


@RunWith(Suite::class)
@Suite.SuiteClasses(
    value = [
        CheckPointScenarioTest::class,
        CourseScenarioTest::class,
        UserScenarioTest::class
    ]
)
class AllTests