package com.example.afinal.data.local

import androidx.room.*

/**
 * Data Access Object (DAO) for the "users" table.
 * Defines all database operations for users. Room automatically generates the implementation.
 * All methods are suspend functions, meaning they must be called from a coroutine or 
 * another suspend function to avoid blocking the main thread.
 */
@Dao
interface UserDao {
    /**
     * Insert a single user into the users table.
     * Conflict Strategy: ABORT (Terminates insertion and throws exception if primary key duplicates).
     * Usage: Used during registration to ensure unique usernames.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity)

    /**
     * Query a user by username.
     * @param username The target username.
     * @return UserEntity if found, null otherwise.
     */
    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUser(username: String): UserEntity?

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)
}
