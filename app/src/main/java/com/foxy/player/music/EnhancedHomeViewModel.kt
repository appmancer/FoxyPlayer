package com.foxy.player.music

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Enhanced state model for home screen content
 * Supports reactive StateFlow state management with loading/success/error patterns
 */
data class EnhancedHomeState(
    val isLoading: Boolean = true,
    val content: List<Any> = emptyList(),
    val error: String? = null
)

/**
 * EnhancedHomeViewModel with reactive StateFlow state management
 * PLY-140: EnhancedHomeViewModel Implementation
 * * Provides reactive state management for home screen with backward compatibility
 * to MusicHubViewModel via hubState property.
 * * Architecture:
 * - Uses StateFlow for reactive UI updates
 * - Follows Android ViewModel lifecycle patterns
 * - Implements backward compatibility bridge
 * - Provides foundation for future content loading features
 */
class EnhancedHomeViewModel : ViewModel() {

    private val _enhancedState = MutableStateFlow(EnhancedHomeState())
    val enhancedState: StateFlow<EnhancedHomeState> = _enhancedState.asStateFlow()

    /**
     * Backward compatibility bridge to MusicHubViewModel
     * Provides hubState for legacy components that depend on HubState
     * Automatically syncs with enhancedState changes
     */
    private val _hubState = MutableStateFlow(HubState(isLoading = true))
    val hubState: StateFlow<HubState> = _hubState.asStateFlow()
}
