import re

with open('app/src/main/java/com/spinel/zicola/zicola/model/CommentModels.kt', 'r', encoding='utf-8') as f:
    code = f.read()

pagination_model = """
@JsonClass(generateAdapter = true)
data class Pagination(
    @Json(name = "page") val page: Int,
    @Json(name = "per_page") val perPage: Int,
    @Json(name = "total") val total: Int,
    @Json(name = "total_pages") val totalPages: Int,
    @Json(name = "has_previous") val hasPrevious: Boolean,
    @Json(name = "has_next") val hasNext: Boolean
)
"""

if "data class Pagination" not in code:
    code = code.replace("@JsonClass(generateAdapter = true)\ndata class GetCommentsResponse", pagination_model + "\n@JsonClass(generateAdapter = true)\ndata class GetCommentsResponse")

# Replace GetCommentsResponse
get_comments_old = """@JsonClass(generateAdapter = true)
data class GetCommentsResponse(
    @Json(name = "status") val status: String,
    @Json(name = "count") val count: Int,
    @Json(name = "comments") val comments: List<Comment>
)"""

get_comments_new = """@JsonClass(generateAdapter = true)
data class GetCommentsResponse(
    @Json(name = "status") val status: String,
    @Json(name = "pagination") val pagination: Pagination? = null,
    @Json(name = "comments") val comments: List<Comment>
)"""

code = code.replace(get_comments_old, get_comments_new)

with open('app/src/main/java/com/spinel/zicola/zicola/model/CommentModels.kt', 'w', encoding='utf-8') as f:
    f.write(code)
print("CommentModels patched")
