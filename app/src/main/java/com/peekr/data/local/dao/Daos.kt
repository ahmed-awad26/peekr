package com.peekr.data.local.dao

import androidx.room.*
import com.peekr.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE platformId = :platformId ORDER BY timestamp DESC")
    fun getPostsByPlatform(platformId: String): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    @Update
    suspend fun updatePost(post: PostEntity)

    @Query("UPDATE posts SET isRead = 1 WHERE id = :postId")
    suspend fun markAsRead(postId: Long)

    @Query("DELETE FROM posts WHERE timestamp < :before")
    suspend fun deleteOldPosts(before: Long)

    @Query("SELECT COUNT(*) FROM posts WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    // للويدجيز - sync (مش Flow)
    @Query("SELECT * FROM posts ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatestPostsSync(limit: Int): List<PostEntity>

    @Query("SELECT * FROM posts WHERE platformId = :platformId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatestPostsByPlatformSync(platformId: String, limit: Int): List<PostEntity>

    @Query("SELECT * FROM posts WHERE sourceId = :sourceId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatestPostsBySourceSync(sourceId: String, limit: Int): List<PostEntity>
}
