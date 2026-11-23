package com.example.bursaryaid.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bursaryaid.model.AppNotification
import com.example.bursaryaid.repository.AppNotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val notifications: List<AppNotification> = emptyList(),
    val isLoading: Boolean = false
)

class NotificationViewModel(
    private val repository: AppNotificationRepository? = null,
    private val userId: Long = 0L
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        if (repository == null || userId == 0L) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getNotificationsByUserId(userId).collect { list ->
                _uiState.update { it.copy(notifications = list, isLoading = false) }
            }
        }
    }

    fun markAsRead(notificationId: Long) {
        val current = _uiState.value.notifications
        val updated = current.map {
            if (it.notificationId == notificationId) it.copy(isRead = true) else it
        }
        _uiState.update { it.copy(notifications = updated) }
    }
}
