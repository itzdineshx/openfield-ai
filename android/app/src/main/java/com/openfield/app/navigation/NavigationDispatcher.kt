package com.openfield.app.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Global dispatcher to route external events (e.g. system intents, shortcuts, share targets)
 * into the main Compose navigation graph.
 */
object NavigationDispatcher {
    private val _targetRoute = MutableStateFlow<String?>(null)
    val targetRoute: StateFlow<String?> = _targetRoute.asStateFlow()

    fun navigateTo(route: String) {
        _targetRoute.value = route
    }

    fun clear() {
        _targetRoute.value = null
    }
}
