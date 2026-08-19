package com.spinel.zicola.zicola.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spinel.zicola.zicola.data.BookRepository
import com.spinel.zicola.zicola.data.PreferencesManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ReaderViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BookRepository(application)
    private val preferencesManager = PreferencesManager(application)

    val fontSize = preferencesManager.fontSizeFlow.stateIn(viewModelScope, SharingStarted.Eagerly, 20)
    val lineSpacing = preferencesManager.lineSpacingFlow.stateIn(viewModelScope, SharingStarted.Eagerly, 1.8f)
    val theme = preferencesManager.themeFlow.stateIn(viewModelScope, SharingStarted.Eagerly, "SEPIA")

    private val _blocks = MutableStateFlow<Map<Int, String>>(emptyMap())
    val blocks: StateFlow<Map<Int, String>> = _blocks.asStateFlow()

    private val _loadingBlocks = MutableStateFlow<Set<Int>>(emptySet())
    val loadingBlocks: StateFlow<Set<Int>> = _loadingBlocks.asStateFlow()

    private val _errorBlocks = MutableStateFlow<Set<Int>>(emptySet())
    val errorBlocks: StateFlow<Set<Int>> = _errorBlocks.asStateFlow()

    private var currentBookId: String? = null
    
    private val _totalBlocks = MutableStateFlow(0)
    val totalBlocks = _totalBlocks.asStateFlow()

    private val _initialScroll = MutableStateFlow<Pair<Int, Int>?>(null)
    val initialScroll = _initialScroll.asStateFlow()

    private var saveJob: Job? = null

    private val _currentBlockIndex = MutableStateFlow(0)
    val currentBlockIndex = _currentBlockIndex.asStateFlow()

    private val _currentBook = MutableStateFlow<com.spinel.zicola.zicola.model.Book?>(null)
    val currentBook = _currentBook.asStateFlow()


    private val _bookmarkedChapter = MutableStateFlow(-1)
    val bookmarkedChapter = _bookmarkedChapter.asStateFlow()

    private val _bookmarkedOffset = MutableStateFlow(-1)
    val bookmarkedOffset = _bookmarkedOffset.asStateFlow()
    private val _nextBook = MutableStateFlow<com.spinel.zicola.zicola.model.Book?>(null)
    val nextBook = _nextBook.asStateFlow()

    fun initBook(bookId: String, requestedChapter: Int) {
        if (currentBookId == bookId && requestedChapter == -1) return
        currentBookId = bookId
        
        val book = repository.getBook(bookId)
        _currentBook.value = book
        _totalBlocks.value = book?.totalBlocks ?: 0
        
        val nextBookId = when (bookId) {
            "zikola" -> "amarita"
            "amarita" -> "wadi"
            else -> null
        }
        _nextBook.value = nextBookId?.let { repository.getBook(it) }

        viewModelScope.launch {
            _bookmarkedChapter.value = preferencesManager.getBookmarkChapter(bookId).first()
            _bookmarkedOffset.value = preferencesManager.getBookmarkOffset(bookId).first()
        }

        
        viewModelScope.launch {
            val blockIndex = if (requestedChapter >= 0) {
                requestedChapter
            } else {
                preferencesManager.getBookBlockIndex(bookId).first()
            }
            
            val offset = if (requestedChapter >= 0) {
                0
            } else {
                preferencesManager.getBookScrollOffset(bookId).first()
            }
            
            _currentBlockIndex.value = blockIndex
            _initialScroll.value = Pair(blockIndex, offset)
            
            loadBlock(bookId, blockIndex)
            // Preload next
            if (blockIndex < _totalBlocks.value - 1) loadBlock(bookId, blockIndex + 1)
        }
    }
    
    fun goToNextChapter() {
        val next = _currentBlockIndex.value + 1
        if (next < _totalBlocks.value) {
            _currentBlockIndex.value = next
            _initialScroll.value = Pair(next, 0)
            currentBookId?.let { loadBlock(it, next) }
        }
    }

    fun loadBlock(bookId: String, blockIndex: Int) {
        if (_blocks.value.containsKey(blockIndex) || _loadingBlocks.value.contains(blockIndex)) return
        
        _loadingBlocks.update { it + blockIndex }
        _errorBlocks.update { it - blockIndex }
        
        viewModelScope.launch {
            val result = repository.getBlockContent(bookId, blockIndex)
            _loadingBlocks.update { it - blockIndex }
            
            result.onSuccess { content ->
                _blocks.update { it + (blockIndex to content) }
            }.onFailure {
                _errorBlocks.update { it + blockIndex }
            }
        }
    }

    fun saveProgress(blockIndex: Int, offset: Int) {
        val bookId = currentBookId ?: return
        val total = _totalBlocks.value
        if (total == 0) return
        
        val progress = blockIndex.toFloat() / total.toFloat()

        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(500) // Debounce
            preferencesManager.saveBookProgress(bookId, blockIndex, offset, progress)
        }
    }
    
    fun markAsFinished() {
        val bookId = currentBookId ?: return
        viewModelScope.launch {
            preferencesManager.saveBookProgress(bookId, _totalBlocks.value - 1, 0, 1.0f)
        }
    }

    fun updateFontSize(size: Int) {
        viewModelScope.launch { preferencesManager.saveFontSize(size) }
    }
    fun updateLineSpacing(spacing: Float) {
        viewModelScope.launch { preferencesManager.saveLineSpacing(spacing) }
    }

    fun saveBookmark(offset: Int) {
        val bookId = currentBookId ?: return
        val chapter = _currentBlockIndex.value
        viewModelScope.launch {
            preferencesManager.saveBookmark(bookId, chapter, offset)
            _bookmarkedChapter.value = chapter
            _bookmarkedOffset.value = offset
        }
    }


    fun jumpToBookmark(chapter: Int, offset: Int) {
        if (chapter < _totalBlocks.value && chapter >= 0) {
            _currentBlockIndex.value = chapter
            _initialScroll.value = Pair(chapter, offset)
            currentBookId?.let { loadBlock(it, chapter) }
        }
    }

    fun updateTheme(themeName: String) {
        viewModelScope.launch { preferencesManager.saveTheme(themeName) }
    }
}
