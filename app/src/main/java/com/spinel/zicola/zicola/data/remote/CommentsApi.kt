package com.spinel.zicola.zicola.data.remote

import com.spinel.zicola.zicola.model.CreateCommentResponse
import com.spinel.zicola.zicola.model.GetCommentsResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CommentsApi {
    @GET("comments.php")
    suspend fun getComments(
        @Query("book_id") bookId: String
    ): GetCommentsResponse

    @FormUrlEncoded
    @POST("comment-create.php")
    suspend fun createComment(
        @Field("book_id") bookId: String,
        @Field("device_id") deviceId: String,
        @Field("display_name") displayName: String,
        @Field("comment_text") commentText: String
    ): CreateCommentResponse

    companion object {
        const val BASE_URL = "https://spinel.info/zicola/public/"
    }
}
