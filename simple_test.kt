import okhttp3.*
import com.google.gson.Gson
import com.google.gson.JsonParser
import java.io.IOException

fun main() {
    val client = OkHttpClient()
    val gson = Gson()
    
    println("🔍 Testing pCloud API with invalid credentials...")
    
    try {
        val requestBody = FormBody.Builder()
            .add("username", "fake@test.com")
            .add("password", "wrongpassword")
            .add("getauth", "1")
            .build()

        val request = Request.Builder()
            .url("https://eapi.pcloud.com/userinfo")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        
        println("HTTP Status: ${response.code}")
        println("Success: ${response.isSuccessful}")
        
        val responseBody = response.body?.string() ?: ""
        println("Response: $responseBody")
        
    } catch (e: Exception) {
        println("Error: ${e.message}")
        e.printStackTrace()
    }
}