import com.foxy.player.music.AndroidBackgroundSyncService

fun main() {
    val serviceClass = AndroidBackgroundSyncService::class.java
    println("Available methods in AndroidBackgroundSyncService:")
    serviceClass.methods.forEach { method ->
        println("  - ${method.name} (${method.parameterTypes.joinToString { it.simpleName }})")
    }
}
