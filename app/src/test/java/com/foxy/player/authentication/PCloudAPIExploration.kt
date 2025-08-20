package com.foxy.player.authentication

import com.google.gson.Gson
import com.google.gson.JsonParser
import java.io.IOException
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Test

class PCloudAPIExploration {

    private val httpClient = OkHttpClient()
    private val gson = Gson()

    @Test
    fun `explore pCloud API responses for different scenarios`() {
        println("🔍 Exploring pCloud API Authentication responses...")
        println("=".repeat(60))

        val scenarios = listOf(
            TestScenario("Invalid credentials", "fake@test.com", "wrongpassword"),
            TestScenario("Invalid email format", "notanemail", "somepassword"),
            TestScenario("Empty credentials", "", ""),
            TestScenario("SQL injection attempt", "'; DROP TABLE users; --", "password")
        )

        scenarios.forEach { scenario ->
            exploreScenario(scenario)
            Thread.sleep(1000) // Be respectful to the API
        }
    }

    private fun exploreScenario(scenario: TestScenario) {
        println("\n📋 Testing: ${scenario.description}")
        println("-".repeat(40))
        println("Username: '${scenario.username}'")
        println("Password: '${scenario.password}'")

        try {
            val requestBody = FormBody.Builder()
                .add("username", scenario.username)
                .add("password", scenario.password).add("getauth", "1")
                .build()

            val request = Request.Builder()
                .url("https://eapi.pcloud.com/userinfo")
                .post(requestBody)
                .build()

            println("🚀 Making request to: ${request.url}")
            val response = httpClient.newCall(request).execute()

            println("📊 HTTP Status: ${response.code}")
            println("✅ Success: ${response.isSuccessful}")

            val responseBody = response.body?.string() ?: ""
            println("📄 Raw Response (${responseBody.length} chars): $responseBody")

            // Try to parse and format JSON
            try {
                val jsonElement = JsonParser.parseString(responseBody)
                val prettyJson = gson.toJson(jsonElement)
                println("🎨 Formatted JSON:")
                println(prettyJson)
            } catch (e: Exception) {
                println("⚠️  Response is not valid JSON: ${e.message}")
            }

            // Show relevant headers
            println("📋 Key Response Headers:")
            listOf("content-type", "content-length", "server", "set-cookie").forEach { headerName ->
                response.header(headerName)?.let { value ->
                    println("  $headerName: $value")
                }
            }
        } catch (e: IOException) {
            println("❌ Network Error: ${e.message}")
            e.printStackTrace()
        } catch (e: Exception) {
            println("❌ Unexpected Error: ${e.message}")
            e.printStackTrace()
        }

        println("=".repeat(60))
    }

    data class TestScenario(
        val description: String,
        val username: String,
        val password: String
    )
}
