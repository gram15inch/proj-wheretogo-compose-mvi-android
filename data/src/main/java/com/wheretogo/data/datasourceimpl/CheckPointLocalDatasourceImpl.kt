package com.wheretogo.data.datasourceimpl

import com.wheretogo.data.datasource.CheckPointLocalDatasource
import com.wheretogo.data.datasourceimpl.database.CheckPointDatabase
import com.wheretogo.data.model.checkpoint.LocalCheckPoint
import javax.inject.Inject

class CheckPointLocalDatasourceImpl @Inject constructor(
    private val checkPointDatabase: CheckPointDatabase
) : CheckPointLocalDatasource {
    private val checkPointDao by lazy { checkPointDatabase.checkPointDao() }
    override suspend fun getCheckPointGroup(checkPointIdGroup: List<String>): List<LocalCheckPoint> {
        return checkPointDao.selectByGroup(checkPointIdGroup)
    }

    override suspend fun getCheckPoint(checkPointId: String): LocalCheckPoint? {
        return checkPointDao.select(checkPointId)
    }

    override suspend fun setCheckPoint(checkPointGroup: List<LocalCheckPoint>) {
        checkPointDao.insert(checkPointGroup)
    }

    override suspend fun replaceCheckpointByCourse(courseId: String, checkPointGroup: List<LocalCheckPoint>){
        val newIdGroup= checkPointGroup.map { it.checkPointId }
        val staleIdGroup= checkPointDao.selectByCourseId(courseId)
            .filter{ it.checkPointId !in newIdGroup }
            .map { it.checkPointId }
        checkPointDao.deleteByGroup(staleIdGroup)
        checkPointDao.insert(checkPointGroup)
    }

    override suspend fun removeCheckPoint(checkPointId: String) {
        checkPointDao.delete(checkPointId)
    }

    override suspend fun updateCheckPoint(checkPointId: String, caption: String) {
        checkPointDao.update(checkPointId, caption)
    }

}