import re

with open('app/src/main/java/com/spinel/zicola/zicola/ui/viewmodel/CommentsViewModel.kt', 'r', encoding='utf-8') as f:
    code = f.read()

# Update CommentsUiState
ui_state_old = """data class CommentsUiState(
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = false,
    val loadError: String? = null,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val submitError: String? = null
)"""

ui_state_new = """data class CommentsUiState(
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = false,
    val loadError: String? = null,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val submitError: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasPrevious: Boolean = false,
    val hasNext: Boolean = false
)"""
code = code.replace(ui_state_old, ui_state_new)

# Update loadComments
load_comments_old = """    fun loadComments(bookId: String) {
        _uiState.update { it.copy(isLoading = true, loadError = null) }
        
        viewModelScope.launch {
            val result = repository.getComments(bookId)
            result.onSuccess { response ->
                if (response.status == "success") {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            comments = response.comments,
                            loadError = null
                        ) 
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            loadError = "حدث خطأ غير متوقع"
                        ) 
                    }
                }
            }.onFailure {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        loadError = "فشل الاتصال بالخادم"
                    ) 
                }
            }
        }
    }"""

load_comments_new = """    fun loadComments(bookId: String, page: Int = 1) {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, loadError = null) }
        
        viewModelScope.launch {
            val result = repository.getComments(bookId, page)
            result.onSuccess { response ->
                if (response.status == "success") {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            comments = response.comments,
                            loadError = null,
                            currentPage = response.pagination?.page ?: 1,
                            totalPages = response.pagination?.totalPages ?: 1,
                            hasPrevious = response.pagination?.hasPrevious ?: false,
                            hasNext = response.pagination?.hasNext ?: false
                        ) 
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            loadError = "حدث خطأ غير متوقع"
                        ) 
                    }
                }
            }.onFailure {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        loadError = "فشل الاتصال بالخادم"
                    ) 
                }
            }
        }
    }

    fun nextPage(bookId: String) {
        val state = _uiState.value
        if (state.hasNext && !state.isLoading && state.currentPage < state.totalPages) {
            loadComments(bookId, state.currentPage + 1)
        }
    }

    fun previousPage(bookId: String) {
        val state = _uiState.value
        if (state.hasPrevious && !state.isLoading && state.currentPage > 1) {
            loadComments(bookId, state.currentPage - 1)
        }
    }"""

code = code.replace(load_comments_old, load_comments_new)

with open('app/src/main/java/com/spinel/zicola/zicola/ui/viewmodel/CommentsViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(code)
print("CommentsViewModel patched")
