import com.foxy.player.authentication.network.AuthenticatedApiClient

fun main() {
    val client = AuthenticatedApiClient("https://eapi.pcloud.com", "test-token")
    
    println("Testing throttling logic:")
    
    val result1 = client.makeThrottledRequest("test")
    println("Request 1 throttle: ${result1.getOrNull()?.throttleDelayMs}")
    
    val result2 = client.makeThrottledRequest("test")
    println("Request 2 throttle: ${result2.getOrNull()?.throttleDelayMs}")
    
    val result3 = client.makeThrottledRequest("test")
    println("Request 3 throttle: ${result3.getOrNull()?.throttleDelayMs}")
}