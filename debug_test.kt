import java.io.File

fun main() {
    // Check the same logic as the test
    val musicDomainFiles = listOf(
        "./app/src/main/java/com/foxy/player/music/network/PCloudDiscovery.kt",
        "./app/src/main/java/com/foxy/player/music/business/MusicMetadataExtraction.kt",
        "./app/src/main/java/com/foxy/player/music/utils/MusicLibraryUtils.kt",
        "./app/src/main/java/com/foxy/player/music/MusicLibrary.kt"
    ).map { File(it) }.filter { it.exists() }

    println("Files exist: ${musicDomainFiles.isNotEmpty()}")
    println("Files found: ${musicDomainFiles.map { it.name }}")

    var hasThreadSleep = false
    var hasCoroutineDelay = false
    
    for (file in musicDomainFiles) {
        val content = file.readText()
        println("Checking file: ${file.name}")
        
        if (content.contains("Thread.sleep")) {
            hasThreadSleep = true
            println("  Found Thread.sleep")
        }
        if (content.contains("delay(") && content.contains("suspend")) {
            hasCoroutineDelay = true
            println("  Found delay() and suspend")
        }
    }

    println("hasThreadSleep: $hasThreadSleep")
    println("hasCoroutineDelay: $hasCoroutineDelay")
}