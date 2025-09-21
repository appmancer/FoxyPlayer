import org.junit.Test
import org.junit.Assert.*
import com.foxy.player.authentication.AuthRepository
import com.foxy.player.authentication.UserInfo
import com.foxy.player.authentication.network.AuthenticatedApiClient

class DebugLoggingTest {
    @Test
    fun debug_logging_content() {
        val authRepository = AuthRepository()
        val apiClient = AuthenticatedApiClient(authRepository)
        
        // Set up auth
        authRepository.saveAuthenticationState("test_token", UserInfo("test@example.com"))
        
        // Get initial logs
        val initialLogs = apiClient.getThrottlingLogs()
        println("Initial throttling logs size: ${initialLogs.size}")
        println("Initial throttling logs content: $initialLogs")
        
        // Make request
        apiClient.makeThrottledRequest("/test")
        
        // Get updated logs
        val updatedLogs = apiClient.getThrottlingLogs()
        println("Updated throttling logs size: ${updatedLogs.size}")
        println("Updated throttling logs content: $updatedLogs")
        
        // Test assertion
        println("Size comparison: ${updatedLogs.size} > ${initialLogs.size} = ${updatedLogs.size > initialLogs.size}")
        
        if (updatedLogs.isNotEmpty()) {
            val lastLog = updatedLogs.last()
            println("Last log contains 'throttle': ${lastLog.contains("throttle")}")
            println("Last log contains 'delay': ${lastLog.contains("delay")}")
        }
    }
}
