package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface YapProjectDao {
    @Query("SELECT * FROM yap_projects ORDER BY lastModified DESC")
    fun getAllProjects(): Flow<List<YapProjectEntity>>

    @Query("SELECT * FROM yap_projects WHERE isFavorite = 1 ORDER BY lastModified DESC")
    fun getFavoriteProjects(): Flow<List<YapProjectEntity>>

    @Query("SELECT * FROM yap_projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Long): YapProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: YapProjectEntity): Long

    @Update
    suspend fun updateProject(project: YapProjectEntity)

    @Delete
    suspend fun deleteProject(project: YapProjectEntity)

    @Query("DELETE FROM yap_projects WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE yap_projects SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long)
}
