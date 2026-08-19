package com.spinel.zicola.zicola.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spinel.zicola.zicola.data.CommentsRepository
import com.spinel.zicola.zicola.data.PreferencesManager
import com.spinel.zicola.zicola.model.Comment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CommentsUiState(
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = false,
    val loadError: String? = null,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val submitError: String? = null
)

class CommentsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CommentsRepository()
    private val preferencesManager = PreferencesManager(application)

    private val _uiState = MutableStateFlow(CommentsUiState())
    val uiState: StateFlow<CommentsUiState> = _uiState.asStateFlow()

    fun loadComments(bookId: String) {
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
    }

    fun submitComment(bookId: String, displayName: String, commentText: String) {
        if (_uiState.value.isSubmitting) return

        val trimmedName = displayName.trim()
        val trimmedComment = commentText.trim()

        if (trimmedName.isEmpty() || trimmedComment.isEmpty()) {
            _uiState.update { it.copy(submitError = "الرجاء إدخال الاسم والتعليق") }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, submitError = null, submitSuccess = false) }

        viewModelScope.launch {
            try {
                val deviceId = preferencesManager.getOrCreateDeviceId()
                val result = repository.createComment(bookId, deviceId, trimmedName, trimmedComment)
                
                result.onSuccess { response ->
                    if (response.status == "success") {
                        _uiState.update { 
                            it.copy(
                                isSubmitting = false,
                                submitSuccess = true,
                                submitError = null
                            ) 
                        }
                    } else {
                        _uiState.update { 
                            it.copy(
                                isSubmitting = false,
                                submitError = response.message ?: "حدث خطأ أثناء الإرسال"
                            ) 
                        }
                    }
                }.onFailure {
                    _uiState.update { 
                        it.copy(
                            isSubmitting = false,
                            submitError = "فشل الاتصال بالخادم"
                        ) 
                    }
                }
            } catch (e: Exception) {
                 _uiState.update { 
                    it.copy(
                        isSubmitting = false,
                        submitError = "حدث خطأ غير متوقع"
                    ) 
                }
            }
        }
    }

    fun clearSubmitStatus() {
        _uiState.update { it.copy(submitSuccess = false, submitError = null) }
    }
}
