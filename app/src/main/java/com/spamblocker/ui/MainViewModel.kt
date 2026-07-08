package com.spamblocker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spamblocker.data.AppDatabase
import com.spamblocker.data.BlockedNumber
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 主 ViewModel：管理黑名单数据
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).blockedNumberDao()

    /** 所有黑名单号码 */
    val blockedNumbers: StateFlow<List<BlockedNumber>> = dao.getAllBlockedNumbers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 搜索关键词 */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /** 搜索结果 */
    val searchResults: StateFlow<List<BlockedNumber>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                dao.getAllBlockedNumbers()
            } else {
                dao.search(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 拦截总数 */
    val blockedCount: StateFlow<Int> = blockedNumbers
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** 添加号码 */
    fun addNumber(phoneNumber: String, label: String, blockCalls: Boolean = true, blockSms: Boolean = true) {
        viewModelScope.launch {
            val normalized = com.spamblocker.util.PhoneNumberUtils.normalize(phoneNumber)
            if (normalized.isNotBlank()) {
                dao.insert(
                    BlockedNumber(
                        phoneNumber = normalized,
                        label = label,
                        blockCalls = blockCalls,
                        blockSms = blockSms
                    )
                )
            }
        }
    }

    /** 移除号码 */
    fun removeNumber(blockedNumber: BlockedNumber) {
        viewModelScope.launch {
            dao.delete(blockedNumber)
        }
    }

    /** 根据号码移除 */
    fun removeByPhoneNumber(phoneNumber: String) {
        viewModelScope.launch {
            dao.deleteByPhoneNumber(com.spamblocker.util.PhoneNumberUtils.normalize(phoneNumber))
        }
    }

    /** 更新号码信息 */
    fun updateNumber(blockedNumber: BlockedNumber) {
        viewModelScope.launch {
            dao.update(blockedNumber)
        }
    }

    /** 切换来电拦截 */
    fun toggleCallBlock(blockedNumber: BlockedNumber) {
        viewModelScope.launch {
            dao.update(blockedNumber.copy(blockCalls = !blockedNumber.blockCalls))
        }
    }

    /** 切换短信拦截 */
    fun toggleSmsBlock(blockedNumber: BlockedNumber) {
        viewModelScope.launch {
            dao.update(blockedNumber.copy(blockSms = !blockedNumber.blockSms))
        }
    }

    /** 更新搜索关键词 */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    /** 检查号码是否已存在 */
    suspend fun isNumberBlocked(phoneNumber: String): Boolean {
        return dao.isBlocked(com.spamblocker.util.PhoneNumberUtils.normalize(phoneNumber)) > 0
    }
}
