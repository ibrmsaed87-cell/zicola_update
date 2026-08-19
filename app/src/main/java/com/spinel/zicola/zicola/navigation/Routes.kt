package com.spinel.zicola.zicola.navigation

sealed class Route(val route: String) {
    object Home : Route("home")
    object Settings : Route("settings")
    object BookDetails : Route("book_details/{bookId}") {
        fun createRoute(bookId: String) = "book_details/$bookId"
    }
    object Chapters : Route("chapters/{bookId}") {
        fun createRoute(bookId: String) = "chapters/$bookId"
    }
    object Reader : Route("reader/{bookId}/{chapterIndex}") {
        fun createRoute(bookId: String, chapterIndex: Int) = "reader/$bookId/$chapterIndex"
    }
}
