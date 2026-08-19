package com.spinel.zicola.zicola.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spinel.zicola.zicola.data.BookRepository
import com.spinel.zicola.zicola.data.PreferencesManager
import com.spinel.zicola.zicola.model.BookWithProgress
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BookRepository(application)
    private val preferencesManager = PreferencesManager(application)

    val booksWithProgress: StateFlow<List<BookWithProgress>> = combine(
        combine(
            preferencesManager.getBookProgress("zikola"),
            preferencesManager.getBookProgress("amarita"),
            preferencesManager.getBookProgress("wadi")
        ) { zikolaProg, amaritaProg, wadiProg -> listOf(zikolaProg, amaritaProg, wadiProg) },
        combine(
            preferencesManager.getBookBlockIndex("zikola"),
            preferencesManager.getBookBlockIndex("amarita"),
            preferencesManager.getBookBlockIndex("wadi")
        ) { zikolaBlock, amaritaBlock, wadiBlock -> listOf(zikolaBlock, amaritaBlock, wadiBlock) }
    ) { progressList, blockList ->
        repository.books.map { book ->
            val prog = when (book.id) {
                "zikola" -> progressList[0]
                "amarita" -> progressList[1]
                "wadi" -> progressList[2]
                else -> 0f
            }
            val lastBlock = when (book.id) {
                "zikola" -> blockList[0]
                "amarita" -> blockList[1]
                "wadi" -> blockList[2]
                else -> 0
            }
            BookWithProgress(book, prog, lastBlock)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = repository.books.map { BookWithProgress(it, 0f, 0) }
    )
}
