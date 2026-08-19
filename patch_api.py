import re

with open('app/src/main/java/com/spinel/zicola/zicola/data/remote/CommentsApi.kt', 'r', encoding='utf-8') as f:
    code = f.read()

code = code.replace(
    "@Query(\"book_id\") bookId: String\n    ): GetCommentsResponse",
    "@Query(\"book_id\") bookId: String,\n        @Query(\"page\") page: Int\n    ): GetCommentsResponse"
)

with open('app/src/main/java/com/spinel/zicola/zicola/data/remote/CommentsApi.kt', 'w', encoding='utf-8') as f:
    f.write(code)
print("CommentsApi patched")
