package com.spamblocker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 被拦截号码的实体类
 */
@Entity(tableName = "blocked_numbers")
data class BlockedNumber(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** 电话号码 */
    val phoneNumber: String,

    /** 备注/标签 (如: "骚扰电话", "诈骗", "广告") */
    val label: String = "",

    /** 是否拦截来电 */
    val blockCalls: Boolean = true,

    /** 是否拦截短信 */
    val blockSms: Boolean = true,

    /** 添加时间戳 */
    val addedAt: Long = System.currentTimeMillis()
)
