package com.spamblocker.util

/**
 * 电话号码工具类：规范化、匹配不同格式的号码
 */
object PhoneNumberUtils {

    /**
     * 提取电话号码中的数字部分，去除空格、横线、括号等
     */
    fun normalize(phoneNumber: String?): String {
        if (phoneNumber.isNullOrBlank()) return ""
        return phoneNumber.replace(Regex("[^+0-9]"), "")
    }

    /**
     * 比较两个号码是否匹配（忽略格式差异）
     * 支持完整匹配、后缀匹配（如 +86 前缀和无前缀）
     */
    fun matches(num1: String?, num2: String?): Boolean {
        val n1 = normalize(num1)
        val n2 = normalize(num2)
        if (n1.isEmpty() || n2.isEmpty()) return false

        // 完全一致
        if (n1 == n2) return true

        // 后缀匹配：较短的号码是较长号码的后缀
        // 例如 "13800138000" 和 "+8613800138000" 后11位一致
        return when {
            n1.length > n2.length -> n1.endsWith(n2)
            n2.length > n1.length -> n2.endsWith(n1)
            else -> false
        }
    }

    /**
     * 格式化号码显示 (如 138-0000-0000)
     */
    fun formatForDisplay(phoneNumber: String): String {
        val digits = phoneNumber.replace(Regex("[^0-9]"), "")
        return when {
            digits.length == 11 -> "${digits.substring(0, 3)}-${digits.substring(3, 7)}-${digits.substring(7)}"
            digits.length == 12 -> "${digits.substring(0, 3)}-${digits.substring(3, 7)}-${digits.substring(7)}"
            digits.length >= 8 -> {
                val last4 = digits.takeLast(4)
                val middle = digits.dropLast(4).takeLast(4)
                val front = digits.dropLast(8)
                if (front.isNotEmpty()) "$front-$middle-$last4" else "$middle-$last4"
            }
            else -> phoneNumber
        }
    }
}
