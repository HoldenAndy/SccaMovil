package com.proyecto.scca.presentation.navigation

import androidx.lifecycle.ViewModel
import com.proyecto.scca.core.notifications.AppNotification
import com.proyecto.scca.core.notifications.NotificationCenter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ChromeViewModel
    @Inject
    constructor(
        private val notificationCenter: NotificationCenter,
    ) : ViewModel() {
        val notifications: StateFlow<List<AppNotification>> = notificationCenter.items

        fun markAllRead() {
            notificationCenter.markAllRead()
        }

        fun markRead(id: String) {
            notificationCenter.markRead(id)
        }

        fun dismiss(id: String) {
            notificationCenter.dismiss(id)
        }
    }
