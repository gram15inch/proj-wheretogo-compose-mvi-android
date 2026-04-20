import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.wheretogo.data.datasource.CheckPointLocalDatasource
import com.wheretogo.data.datasourceimpl.CheckPointLocalDatasourceImpl
import com.wheretogo.data.datasourceimpl.database.CheckPointDatabase
import com.wheretogo.data.model.checkpoint.LocalCheckPoint
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class CheckPointTest {

    private lateinit var dataSource: CheckPointLocalDatasource
    private lateinit var db: CheckPointDatabase

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CheckPointDatabase::class.java
        ).allowMainThreadQueries().build()

        dataSource = CheckPointLocalDatasourceImpl(db)
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun saveAndGetTest() = runTest {
        val courseId = "cs1"
        val saveItems = listOf(
            LocalCheckPoint(checkPointId = "cp1", courseId = courseId),
            LocalCheckPoint(checkPointId = "cp2", courseId = courseId)
        )
        // 데이터 없음

        // 데이터 저장후 특정 ID로 조회
        val save= dataSource.saveCheckPoints(saveItems)
        val items = dataSource.getCheckPoints(saveItems.map { it.checkPointId }).getOrThrow()

        // 저장 성공 및 조회 데이터가 일치
        assertTrue(save.isSuccess)
        assertEquals(items[0].checkPointId, saveItems[0].checkPointId)
        assertEquals(items[1].checkPointId, saveItems[1].checkPointId)
    }

    @Test
    fun getClusterTest() = runTest {
        val courseId = "cs1"
        val saveItems = listOf(
            LocalCheckPoint(checkPointId = "cp1", courseId = courseId),
            LocalCheckPoint(checkPointId = "cp2", courseId = courseId)
        )

        // 조회할 데이터 생성
        dataSource.replaceCluster(courseId, saveItems).getOrThrow()

        // 그룹 ID로 조회
        val cluster = dataSource.getCluster(courseId).getOrThrow()

        // 조회 데이터 일치
        assertEquals(courseId, cluster?.meta?.groupId)
        assertEquals(saveItems[0].caption, cluster?.items[0]?.caption)
        assertEquals(saveItems[1].caption, cluster?.items[1]?.caption)
    }

    @Test
    fun replaceByGroupTest() = runTest {
        val courseId = "cs1"
        val oldItems = listOf(LocalCheckPoint(checkPointId = "old", courseId = courseId))
        val newItems = listOf(LocalCheckPoint(checkPointId = "new", courseId = courseId))

        // 교체할 데이터 생성
        dataSource.replaceCluster(courseId, oldItems)
        val oldCluster = dataSource.getCluster(courseId).getOrThrow()

        // 새로운 데이터로 교체
        dataSource.replaceCluster(courseId, newItems).getOrThrow()

        // 교체된 데이터 일치 및 캐시시간 불일치
        val cluster = dataSource.getCluster(courseId).getOrThrow()
        assertEquals(1, newItems.size)
        assertEquals(newItems[0].checkPointId, cluster?.items[0]!!.checkPointId) // 여기 에러
        assertNotEquals(cluster.meta.cachedAt, oldCluster?.meta!!.cachedAt)
    }

    @Test
    fun deleteByIdTest() = runTest {
        // 삭제할 데이터 생성
        val courseId ="cs1"
        val deleteItems = listOf(LocalCheckPoint("no-del",courseId), LocalCheckPoint("del"))
        dataSource.replaceCluster(courseId, deleteItems).getOrThrow()

        // 데이터 삭제
        dataSource.deleteCheckPoints(listOf("del")).getOrThrow()

        // 삭제되지 않은 데이터만 남음
        val items = dataSource.getCheckPoints(deleteItems.map { it.checkPointId }).getOrThrow()
        assertEquals(1,items.size)
        assertEquals("no-del",items[0].checkPointId)

        val cluster = dataSource.getCluster(courseId).getOrThrow()
        assertEquals(1,cluster?.items?.size)
        assertEquals("no-del",cluster?.items[0]?.checkPointId)
    }
}