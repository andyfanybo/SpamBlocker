package com.spamblocker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 黑名单号码 DAO
 */
@Dao
interface BlockedNumberDao {

    /** 获取所有黑名单号码 (Flow 实时监听) */
    @Query("SELECT * FROM blocked_numbers ORDER BY addedAt DESC")
    fun getAllBlockedNumbers(): Flow<List<BlockedNumber>>

    /** 根据号码查找 */
    @Query("SELECT * FROM blocked_numbers WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun findByPhoneNumber(phoneNumber: String): BlockedNumber?

    /** 检查号码是否在黑名单中 */
    @Query("SELECT COUNT(*) FROM blocked_numbers WHERE phoneNumber = :phoneNumber")
    suspend fun isBlocked(phoneNumber: String): Int

    /** 检查来电是否应被拦截 */
    @Query("SELECT blockCalls FROM blocked_numbers WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun shouldBlockCall(phoneNumber: String): Boolean?

    /** 检查短信是否应被拦截 */
    @Query("SELECT blockSms FROM blocked_numbers WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun shouldBlockSms(phoneNumber: String): Boolean?

    /** 添加号码到黑名单 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(blockedNumber: BlockedNumber)

    /** 批量添加 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(numbers: List<BlockedNumber>)

    /** 更新号码信息 */
    @Update
    suspend fun update(blockedNumber: BlockedNumber)

    /** 从黑名单移除 */
    @Delete
    suspend fun delete(blockedNumber: BlockedNumber)

    /** 根据号码移除 */
    @Query("DELETE FROM blocked_numbers WHERE phoneNumber = :phoneNumber")
    suspend fun deleteByPhoneNumber(phoneNumber: String)

    /** 获取黑名单数量 */
    @Query("SELECT COUNT(*) FROM blocked_numbers")
    suspend fun getCount(): Int

    /** 模糊搜索 */
    @Query("SELECT * FROM blocked_numbers WHERE phoneNumber LIKE '%' || :keyword || '%' OR label LIKE '%' || :keyword || '%' ORDER BY addedAt DESC")
    fun search(keyword: String): Flow<List<BlockedNumber>>
}
