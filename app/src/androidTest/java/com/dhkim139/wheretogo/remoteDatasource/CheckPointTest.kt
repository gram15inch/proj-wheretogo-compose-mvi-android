package com.dhkim139.wheretogo.remoteDatasource

import android.util.Log
import com.wheretogo.data.datasourceimpl.CheckPointRemoteDatasourceImpl
import com.wheretogo.data.model.checkpoint.RemoteCheckPoint
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotEquals
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
    fun get_set_remove_should_work_correctly(): Unit = runBlocking {
        val inputCp1 = RemoteCheckPoint(checkPointId = "TestCp1", userId = "TestCp-User1")
        checkPointRemoteDatasourceImpl.removeCheckPoint(inputCp1.checkPointId)
        checkPointRemoteDatasourceImpl.getCheckPoint(inputCp1.checkPointId).empty()

        checkPointRemoteDatasourceImpl.setCheckPoint(inputCp1)
        checkPointRemoteDatasourceImpl.getCheckPoint(inputCp1.checkPointId).assertEquals(inputCp1)

        checkPointRemoteDatasourceImpl.removeCheckPoint(inputCp1.checkPointId)
        checkPointRemoteDatasourceImpl.getCheckPoint(inputCp1.checkPointId).empty()
    }


    private fun RemoteCheckPoint?.empty() {
        Log.d(tag, "cp: $this")
        assertEquals(null, this)
    }

    private fun RemoteCheckPoint?.assertEquals(input: RemoteCheckPoint) {
        Log.d(tag, "cp: $this")
        assertNotEquals(null, this)
        assertEquals(input, this)
    }
}