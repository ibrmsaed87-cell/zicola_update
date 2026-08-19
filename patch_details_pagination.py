import re

with open('app/src/main/java/com/spinel/zicola/zicola/ui/screens/BookDetailsScreen.kt', 'r', encoding='utf-8') as f:
    code = f.read()

# 1. Remove AddCommentSection from its current position
add_comment_item = """
            item {
                AddCommentSection(
                    name = nameInput,
                    onNameChange = { nameInput = it },
                    comment = commentInput,
                    onCommentChange = { commentInput = it },
                    isSubmitting = commentsState.isSubmitting,
                    onSubmit = {
                        commentsViewModel.submitComment(book.id, nameInput, commentInput)
                    }
                )
            }
"""
code = code.replace(add_comment_item, "")

# 2. Add pagination and AddCommentSection after the `when` block for comments
pagination_and_add_comment = """
            }

            if (commentsState.totalPages > 1 && !commentsState.isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { commentsViewModel.previousPage(book.id) },
                            enabled = commentsState.hasPrevious
                        ) {
                            Text("السابق")
                        }
                        
                        Text(
                            text = "صفحة ${commentsState.currentPage} من ${commentsState.totalPages}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        TextButton(
                            onClick = { commentsViewModel.nextPage(book.id) },
                            enabled = commentsState.hasNext
                        ) {
                            Text("التالي")
                        }
                    }
                }
            }

            item {
                AddCommentSection(
                    name = nameInput,
                    onNameChange = { nameInput = it },
                    comment = commentInput,
                    onCommentChange = { commentInput = it },
                    isSubmitting = commentsState.isSubmitting,
                    onSubmit = {
                        commentsViewModel.submitComment(book.id, nameInput, commentInput)
                    }
                )
            }
        }
    }
}"""

# We replace the end of the when block and the end of LazyColumn/Scaffold
old_end = """
            }
        }
    }
}"""

code = code.replace(old_end, pagination_and_add_comment)

with open('app/src/main/java/com/spinel/zicola/zicola/ui/screens/BookDetailsScreen.kt', 'w', encoding='utf-8') as f:
    f.write(code)
print("BookDetailsScreen patched")
