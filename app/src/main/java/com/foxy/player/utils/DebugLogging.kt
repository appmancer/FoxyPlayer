package com.foxy.player.utils

/**
 * Centralized debug logging utility for development and testing.
 * Handles both System.out (visible in unit tests) and Android Log (visible in device logs).
 */
object DebugLogging {
    
    /**
     * Log a debug message using both System.out and Android Log.
     * System.out ensures visibility in unit tests, Android Log ensures visibility on device.
     */
    fun log(tag: String, message: String) {
        // Always use System.out for unit test visibility
        System.out.println("🔍 $tag: $message")
        
        // Try Android logging (fails gracefully in unit tests)
        try {
            android.util.Log.d(tag, "🔍 $message")
        } catch (e: Exception) {
            // Android logging not available in unit tests - this is expected
        }
    }
    
    /**
     * Log an error message using both System.out and Android Log.
     */
    fun logError(tag: String, message: String, throwable: Throwable? = null) {
        // Always use System.out for unit test visibility
        System.out.println("🚨 $tag: $message")
        throwable?.let { System.out.println("🚨 $tag: ${it.message}") }
        
        // Try Android logging (fails gracefully in unit tests)
        try {
            if (throwable != null) {
                android.util.Log.e(tag, "🚨 $message", throwable)
            } else {
                android.util.Log.e(tag, "🚨 $message")
            }
        } catch (e: Exception) {
            // Android logging not available in unit tests - this is expected
        }
    }
}