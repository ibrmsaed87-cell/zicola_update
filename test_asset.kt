import java.io.File

fun main() {
    val f = File("app/src/main/assets/covers/zikola.png")
    println("Exists: ${f.exists()}")
    println("Length: ${f.length()}")
}
