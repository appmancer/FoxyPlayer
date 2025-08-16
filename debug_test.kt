import com.foxy.player.authentication.AuthRepository
import com.google.gson.Gson

fun main() {
    val authRepository = AuthRepository("https://api.pcloud.com")
    val mockSuccessJson = authRepository.getMockSuccessResponse()
    
    println("Mock JSON:")
    println(mockSuccessJson)
    println()
    
    val result = authRepository.parseAuthResponse(mockSuccessJson)
    println("Parse result success: ${result.isSuccess}")
    
    if (result.isFailure) {
        println("Error: ${result.exceptionOrNull()?.message}")
        result.exceptionOrNull()?.printStackTrace()
    } else {
        println("Success: ${result.getOrNull()}")
    }
}
