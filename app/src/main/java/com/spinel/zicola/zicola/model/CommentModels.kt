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
data class Pagination(
    @Json(name = "page") val page: Int,
    @Json(name = "per_page") val perPage: Int,
    @Json(name = "total") val total: Int,
    @Json(name = "total_pages") val totalPages: Int,
    @Json(name = "has_previous") val hasPrevious: Boolean,
    @Json(name = "has_next") val hasNext: Boolean
)

@JsonClass(generateAdapter = true)
data class GetCommentsResponse(
    @Json(name = "status") val status: String,
    @Json(name = "pagination") val pagination: Pagination? = null,
    @Json(name = "comments") val comments: List<Comment>
)

@JsonClass(generateAdapter = true)
data class CreateCommentResponse(
    @Json(name = "status") val status: String,
    @Json(name = "message") val message: String? = null
)
