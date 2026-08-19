package com.spinel.zicola.zicola.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Comment(
    @Json(name = "id") val id: Int,
    @Json(name = "book_id") val bookId: String,
    @Json(name = "display_name") val displayName: String,
    @Json(name = "comment_text") val commentText: String,
    @Json(name = "created_at") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class GetCommentsResponse(
    @Json(name = "status") val status: String,
    @Json(name = "count") val count: Int,
    @Json(name = "comments") val comments: List<Comment>
)

@JsonClass(generateAdapter = true)
data class CreateCommentResponse(
    @Json(name = "status") val status: String,
    @Json(name = "message") val message: String? = null
)
