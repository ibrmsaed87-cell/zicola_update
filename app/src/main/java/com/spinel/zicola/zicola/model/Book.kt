package com.spinel.zicola.zicola.model

data class ChapterMetadata(
    val bookId: String,
    val chapterNumber: Int,
    val title: String,
    val excerpt: String,
    val assetFile: String
)

data class Book(
    val id: String,
    val title: String,
    val partNumber: String,
    val author: String,
    val description: String,
    val coverAssetPath: String,
    val totalBlocks: Int,
    val chapters: List<ChapterMetadata> = emptyList()
)

data class BookWithProgress(
    val book: Book,
    val progress: Float,
    val lastReadBlockIndex: Int = 0
)
