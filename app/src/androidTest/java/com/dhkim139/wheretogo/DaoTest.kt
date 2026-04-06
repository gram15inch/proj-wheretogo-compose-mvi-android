import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.wheretogo.data.datasourceimpl.database.CheckPointDao
import com.wheretogo.data.datasourceimpl.database.CheckPointDatabase
import com.wheretogo.data.model.checkpoint.CheckPointGroupMeta
import com.wheretogo.data.model.checkpoint.LocalCheckPoint
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class CheckPointDaoTest {

    private lateinit var db: CheckPointDatabase
    private lateinit var dao: CheckPointDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CheckPointDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.checkPointDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun upsertAndGetCheckPoint() = runTest {
        val meta = CheckPointGroupMeta(groupId = "cs1", cachedAt = 1L)
        val cp1 = LocalCheckPoint(checkPointId = "cp1", courseId = "cs1", caption = "내용")
        val cp2 = LocalCheckPoint(checkPointId = "cp2", courseId = "cs1", caption = "내용")
        // 체크포인트,메타 각각 삽입
        dao.upsert(listOf(cp1,cp2))
        dao.upsertMeta(meta)

        // 그룹 조회
        val group = dao.getCheckPointGroup("cs1")
        assertEquals(meta, group?.meta)
        assertEquals(listOf(cp1, cp2), group?.items)

        // 개별 조회
        val items = dao.select(listOf(cp1.checkPointId,cp2.checkPointId))
        assertEquals(listOf(cp1,cp2), items)

        val itemMeta = dao.selectMeta(meta.groupId)
        assertEquals(meta, itemMeta)
    }


    @Test
    fun deleteAndGetCheckPoint() = runTest {
        val meta = CheckPointGroupMeta(groupId = "cs1", cachedAt = 1L)
        val cp1 = LocalCheckPoint(checkPointId = "cp1", courseId = "cs1", caption = "내용")
        val cp2 = LocalCheckPoint(checkPointId = "cp2", courseId = "cs1", caption = "내용")
        dao.upsert(listOf(cp1,cp2))
        dao.upsertMeta(meta)

        // 그룹 삭제
        dao.deleteGroup(meta.groupId)
        dao.deleteMeta(meta.groupId)

        // 그룹 조회
        val group = dao.getCheckPointGroup(meta.groupId)
        assertEquals(null, group)

        // 개별 조회
        val items = dao.select(listOf(cp1.checkPointId,cp2.checkPointId))
        assertEquals(emptyList<LocalCheckPoint>(), items)

        val itemMeta = dao.selectMeta(meta.groupId)
        assertEquals(null, itemMeta)
    }
}