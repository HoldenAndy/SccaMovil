package com.proyecto.scca.core.notifications

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import javax.inject.Inject
import javax.inject.Singleton

enum class NotificationKind {
    INFO,
    WARNING,
    CRITICAL,
    SUCCESS,
}

data class AppNotification(
    val id: String,
    val kind: NotificationKind,
    val title: String,
    val body: String?,
    val route: String?,
    val createdAtMs: Long,
    val read: Boolean,
)

@Singleton
class NotificationCenter
    @Inject
    constructor() {
        private val _items = MutableStateFlow<List<AppNotification>>(emptyList())
        val items: StateFlow<List<AppNotification>> = _items.asStateFlow()

        fun push(
            kind: NotificationKind,
            title: String,
            body: String? = null,
            route: String? = null,
        ) {
            val now = Clock.System.now().toEpochMilliseconds()
            val item =
                AppNotification(
                    id = "$now-${title.hashCode()}",
                    kind = kind,
                    title = title,
                    body = body,
                    route = route,
                    createdAtMs = now,
                    read = false,
                )
            _items.value = (listOf(item) + _items.value).take(MAX_ITEMS)
        }

        fun markAllRead() {
            _items.value = _items.value.map { it.copy(read = true) }
        }

        fun markRead(id: String) {
            _items.value = _items.value.map { item -> if (item.id == id) item.copy(read = true) else item }
        }

        fun dismiss(id: String) {
            _items.value = _items.value.filterNot { it.id == id }
        }

        private companion object {
            const val MAX_ITEMS = 50
        }
    }
