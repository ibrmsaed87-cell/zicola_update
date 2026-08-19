import re

with open('app/src/main/java/com/spinel/zicola/zicola/data/CommentsRepository.kt', 'r', encoding='utf-8') as f:
    code = f.read()

code = code.replace(
    "suspend fun getComments(bookId: String): Result<GetCommentsResponse> {",
    "suspend fun getComments(bookId: String, page: Int): Result<GetCommentsResponse> {"
).replace(
    "val response = api.getComments(bookId)",
    "val response = api.getComments(bookId, page)"
)

with open('app/src/main/java/com/spinel/zicola/zicola/data/CommentsRepository.kt', 'w', encoding='utf-8') as f:
    f.write(code)
print("CommentsRepository patched")
