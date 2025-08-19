import com.foxy.player.authentication.AuthRepository
import com.foxy.player.authentication.AuthenticatedApiClient
import com.foxy.player.authentication.UserInfo
import com.foxy.player.music.MusicDiscoveryService

fun main() {
    println("🎵 Testing Real pCloud Integration")
    println("=" * 50)
    
    // Read real credentials
    val credentials = java.io.File("pcloud.txt").readLines()
    val email = credentials[2].trim()
    val password = credentials[3].trim()
    
    println("📧 Email: $email")
    println("🔐 Password: ${password.take(3)}...")
    
    try {
        // Setup authentication with real pCloud API
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        
        // Authenticate with real credentials
        println("\n🔐 Authenticating with pCloud...")
        val authResult = authRepository.authenticate(email, password)
        
        if (authResult.isSuccess) {
            val authResponse = authResult.getOrNull()!!
            println("✅ Authentication successful!")
            println("🎫 Auth Token: ${authResponse.authToken.take(10)}...")
            
            // Save auth state
            authRepository.saveAuthenticationState(authResponse.authToken, UserInfo(email))
            
            // Create authenticated API client and music service
            val authenticatedApiClient = AuthenticatedApiClient(authRepository)
            val musicService = MusicDiscoveryService(authenticatedApiClient)
            
            // Test 1: List root folders
            println("\n📁 Testing Root Folder Listing...")
            val rootFolders = musicService.listPCloudFoldersWithAPI("/")
            if (rootFolders.isSuccess) {
                val response = rootFolders.getOrNull()!!
                println("✅ Root folders found:")
                response.folderListing.folders.forEach { folder ->
                    println("  📂 $folder")
                }
                response.folderListing.files.take(5).forEach { file ->
                    println("  📄 $file")
                }
            } else {
                println("❌ Failed to list root folders: ${rootFolders.exceptionOrNull()?.message}")
            }
            
            // Test 2: Search for audio files
            println("\n🎵 Testing Audio File Discovery...")
            val audioFiles = musicService.listAudioFiles("/")
            if (audioFiles.isSuccess) {
                val response = audioFiles.getOrNull()!!
                println("✅ Audio files found:")
                response.audioFiles.take(10).forEach { file ->
                    println("  🎶 $file")
                }
                println("📊 Total audio files: ${response.audioFiles.size}")
            } else {
                println("❌ Failed to find audio files: ${audioFiles.exceptionOrNull()?.message}")
            }
            
            // Test 3: Test recursive traversal
            println("\n🔄 Testing Recursive Directory Traversal...")
            val recursiveResult = musicService.listPCloudFoldersRecursively("/")
            if (recursiveResult.isSuccess) {
                val response = recursiveResult.getOrNull()!!
                println("✅ Recursive traversal completed:")
                println("📊 Directories traversed: ${response.totalDirectoriesTraversed}")
                println("📂 All folders found:")
                response.allFolders.take(10).forEach { folder ->
                    println("  📁 $folder")
                }
            }
            
            // Test 4: Test pagination
            println("\n📄 Testing Pagination...")
            val paginatedResult = musicService.listAudioFilesWithPagination("/", null, 5)
            if (paginatedResult.isSuccess) {
                val response = paginatedResult.getOrNull()!!
                println("✅ First page:")
                response.audioFiles.forEach { file ->
                    println("  🎵 $file")
                }
                println("📄 Has next page: ${response.hasNextPage}")
                if (response.hasNextPage) {
                    println("🎫 Next page token: ${response.nextPageToken}")
                }
            }
            
            // Test 5: Test caching
            println("\n💾 Testing Caching System...")
            val cachedResult1 = musicService.listAudioFilesWithCache("/")
            val cachedResult2 = musicService.listAudioFilesWithCache("/")
            
            if (cachedResult1.isSuccess && cachedResult2.isSuccess) {
                val response1 = cachedResult1.getOrNull()!!
                val response2 = cachedResult2.getOrNull()!!
                
                println("✅ First call - From cache: ${response1.servedFromCache}")
                println("✅ Second call - From cache: ${response2.servedFromCache}")
                println("📊 Total API calls: ${response2.totalApiCallsMade}")
            }
            
            // Test 6: Test error handling
            println("\n🚨 Testing Error Handling...")
            val errorTests = listOf(
                "Network timeout" to musicService.listAudioFilesWithErrorHandling("/", networkTimeout = true),
                "HTTP 404 error" to musicService.listAudioFilesWithErrorHandling("/nonexistent", httpError = 404),
                "Auth error" to musicService.listAudioFilesWithErrorHandling("/", authError = true),
                "Retry scenario" to musicService.listAudioFilesWithErrorHandling("/", retryScenario = true),
                "Cached fallback" to musicService.listAudioFilesWithErrorHandling("/", useCachedFallback = true)
            )
            
            errorTests.forEach { (testName, result) ->
                if (result.isSuccess) {
                    val response = result.getOrNull()!!
                    println("✅ $testName: ${response.errorMessage}")
                } else {
                    println("❌ $testName failed")
                }
            }
            
            println("\n🎉 All tests completed!")
            
        } else {
            println("❌ Authentication failed: ${authResult.exceptionOrNull()?.message}")
        }
        
    } catch (e: Exception) {
        println("💥 Error during testing: ${e.message}")
        e.printStackTrace()
    }
}