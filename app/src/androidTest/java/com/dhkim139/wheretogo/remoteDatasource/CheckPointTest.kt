package com.dhkim139.wheretogo.remoteDatasource

import android.util.Log
import com.wheretogo.data.datasourceimpl.CheckPointRemoteDatasourceImpl
import com.wheretogo.data.model.checkpoint.RemoteCheckPoint
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class CheckPointTest {
    private val tag = "tst_checkpoint"

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var checkPointRemoteDatasourceImpl: CheckPointRemoteDatasourceImpl

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun setAndGetCheckPointTest(): Unit = runBlocking {
        val datasource = checkPointRemoteDatasourceImpl

        val cp1 = datasource.getCheckPoint("cp1")
        Log.d(tag, "$cp1")
        assertEquals(true, cp1?.imageName?.isNotEmpty())
        assertEquals(true, cp1?.titleComment?.isNotEmpty())
        assertNotEquals(0.0, cp1?.latLng?.latitude)
    }

    @Test
    fun removeCheckPointTest(): Unit = runBlocking {
        val datasource = checkPointRemoteDatasourceImpl
        val removeCheckPoint = RemoteCheckPoint(
            checkPointId = "cs_test1",
            userId = "uid1",
        )

        datasource.getCheckPoint(removeCheckPoint.checkPointId).empty()
        datasource.setCheckPoint(removeCheckPoint).success()

        datasource.getCheckPoint(removeCheckPoint.checkPointId).full()
        datasource.removeCheckPoint(removeCheckPoint.checkPointId).success()

        datasource.getCheckPoint(removeCheckPoint.checkPointId).empty()
    }


    private fun RemoteCheckPoint?.empty() {
        Log.d(tag, "cp: $this")
        assertEquals(null, this)
    }

    private fun RemoteCheckPoint?.full() {
        Log.d(tag, "cp: $this")
        assertNotEquals(null, this)
    }

    private fun Boolean.success() {
        Log.d(tag, "result: $this")
        assertTrue(this)
    }

    private fun Boolean.fail() {
        Log.d(tag, "result: $this")
        assertFalse(this)
    }

}