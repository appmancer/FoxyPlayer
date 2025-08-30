import com.foxy.player.music.AndroidBackgroundSyncService

fun main() {
    val serviceClass = AndroidBackgroundSyncService::class.java
    println("Class name: ${serviceClass.name}")
    println("Methods:")
    serviceClass.methods.forEach { method ->
        println("  ${method.name}")
    }
    
    val hasScheduleMethod = serviceClass.methods.any { it.name == "schedulePeriodicSync" }
    val hasForegroundMethod = serviceClass.methods.any { it.name == "startForegroundSync" }
    
    println("Has schedulePeriodicSync: $hasScheduleMethod")
    println("Has startForegroundSync: $hasForegroundMethod")
}
