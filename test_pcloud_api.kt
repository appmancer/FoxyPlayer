#!/usr/bin/env kotlin

// Simple script to manually test pCloud API and see real responses
@file:DependsOn("com.squareup.okhttp3:okhttp:4.12.0")
@file:DependsOn("com.google.code.gson:gson:2.10.1")

import okhttp3.*
import com.google.gson.Gson
import com.google.gson.JsonParser
import java.io.IOException

fun main() {
    println("🔍 Testing pCloud API Authentication...")
    println("=" * 50)
    
    val client = OkHttpClient()
    val gson = Gson()
    
    // Test scenarios
    val scenarios = listOf(
        TestScenario("Invalid credentials", "fake@test.com", "wrongpassword"),
        TestScenario("Invalid email format", "notanemail", "somepassword"),
        TestScenario("Empty credentials", "", "")
    )
    
    scenarios.forEach { scenario ->
        println("\n📋 Testing: ${scenario.description}")
        println("-".repeat(30))
        
        try {
            val requestBody = FormBody.Builder()
                .add("username", scenario.username)
                .add("password", scenario.password)
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
            println("Raw Response: $responseBody")
            
            // Try to pretty print JSON
            try {
                val jsonElement = JsonParser.parseString(responseBody)
                val prettyJson = gson.toJson(jsonElement)
                println("Formatted JSON:")
                println(prettyJson)
            } catch (e: Exception) {
                println("Not valid JSON or parsing failed: ${e.message}")
            }
            
            println("Response Headers:")
            response.headers.forEach { (name, value) ->
                println("  $name: $value")
            }
            
        } catch (e: IOException) {
            println("❌ Network Error: ${e.message}")
        } catch (e: Exception) {
            println("❌ Unexpected Error: ${e.message}")
        }
        
        println("\n" + "=".repeat(50))
        Thread.sleep(1000) // Be nice to the API
    }
}

data class TestScenario(
    val description: String,
    val username: String, 
    val password: String
)

operator fun String.times(n: Int) = this.repeat(n)