package com.spinel.zicola.zicola.data

import com.spinel.zicola.zicola.data.remote.CommentsApi
import com.spinel.zicola.zicola.data.remote.RetrofitClient
import com.spinel.zicola.zicola.model.CreateCommentResponse
import com.spinel.zicola.zicola.model.GetCommentsResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CommentsRepository(private val api: CommentsApi = RetrofitClient.commentsApi) {
    
    suspend fun getComments(bookId: String, page: Int): Result<GetCommentsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getComments(bookId, page)
                Result.success(response)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun createComment(
        bookId: String,
        deviceId: String,
        displayName: String,
        commentText: String
    ): Result<CreateCommentResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.createComment(bookId, deviceId, displayName, commentText)
                Result.success(response)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
