package com.spinel.zicola.zicola.data

import android.content.Context
import com.spinel.zicola.zicola.model.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BookRepository(private val context: Context) {

    val books = listOf(
        Book(
            id = "zikola",
            title = "أرض زيكولا",
            partNumber = "الجزء الأول",
            author = "عمرو عبد الحميد",
            description = "هل جربت أن تتعامل بعملة مختلفة عن العملات الورقية.. ليست معدنية وليست ذهبية.. الثروة هنا من نوع آخر.. لن تدفع مالاً لتأخذ.. بل ستدفع من ذكائك.",
            coverAssetPath = "file:///android_asset/covers/zikola.png",
            totalBlocks = 20,
            chapters = ChapterDataProvider.zikolaChapters
        ),
        Book(
            id = "amarita",
            title = "أماريتا",
            partNumber = "الجزء الثاني",
            author = "عمرو عبد الحميد",
            description = "لم أرَ من قبل خوف وجوه أهل زيكولا مثلما كنت أراه في تلك اللحظات أسفل أنوار المشاعل.",
            coverAssetPath = "file:///android_asset/covers/amarita.png",
            totalBlocks = 36,
            chapters = ChapterDataProvider.amaritaChapters
        ),
        Book(
            id = "wadi",
            title = "وادي الذئاب المنسية",
            partNumber = "الجزء الثالث",
            author = "عمرو عبد الحميد",
            description = "في أرض زيكولا حيث تباع الأرواح وتُشترى.. لا مكان للضعفاء.. هنا وادي الذئاب المنسية.",
            coverAssetPath = "file:///android_asset/covers/wadi.png",
            totalBlocks = 30,
            chapters = ChapterDataProvider.wadiChapters
        )
    )

    fun getBook(id: String): Book? = books.find { it.id == id }

    suspend fun getBlockContent(bookId: String, blockIndex: Int): Result<String> = withContext(Dispatchers.IO) {
        try {
            val files = context.assets.list("books/$bookId") ?: emptyArray()
            
            // Numeric sort for files like block_01.txt, block_02.txt
            val sortedFiles = files.filter { it.endsWith(".txt") }.sortedBy { fileName ->
                Regex("\\d+").find(fileName)?.value?.toInt() ?: 0
            }

            if (blockIndex in sortedFiles.indices) {
                val fileName = sortedFiles[blockIndex]
                val inputStream = context.assets.open("books/$bookId/$fileName")
                val content = inputStream.bufferedReader().use { it.readText() }
                Result.success(content)
            } else {
                Result.failure(Exception("Block not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getActualBlockCount(bookId: String): Int {
        return try {
            val files = context.assets.list("books/$bookId") ?: emptyArray()
            files.count { it.endsWith(".txt") }
        } catch (e: Exception) {
            0
        }
    }
}
