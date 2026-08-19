import re

with open('app/src/main/java/com/spinel/zicola/zicola/ui/screens/BookDetailsScreen.kt', 'r', encoding='utf-8') as f:
    code = f.read()

# Add imports
imports_to_add = """
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.items
import com.spinel.zicola.zicola.ui.viewmodel.CommentsViewModel
"""
if "import androidx.lifecycle.viewmodel.compose.viewModel" not in code:
    code = code.replace("import androidx.compose.runtime.Composable", "import androidx.compose.runtime.Composable" + imports_to_add)

# Signature modification
if "commentsViewModel: CommentsViewModel = viewModel()" not in code:
    code = code.replace("    onContinueClick: () -> Unit\n) {", "    onContinueClick: () -> Unit,\n    commentsViewModel: CommentsViewModel = viewModel()\n) {")

# State & LaunchedEffect
state_and_effect = """
    val book = bookWithProgress.book
    val progress = bookWithProgress.progress

    val commentsState by commentsViewModel.uiState.collectAsState()

    LaunchedEffect(book.id) {
        commentsViewModel.loadComments(book.id)
    }
"""
if "val commentsState by commentsViewModel.uiState.collectAsState()" not in code:
    code = code.replace("    val book = bookWithProgress.book\n    val progress = bookWithProgress.progress", state_and_effect.strip())


# Replace Mock comments section
mock_comments = """
            // Using mock comments for now, but added an empty state example commented out
            /*
            item {
                EmptyCommentsState()
            }
            */

            items(3) { index ->
                CommentCard(
                    name = "قارئ ${index + 1}",
                    date = "منذ ${index + 1} أيام",
                    comment = "تعليق تجريبي لتجربة التصميم. الرواية رائعة جداً وممتعة! أسلوب الكاتب مشوق."
                )
            }
"""

real_comments = """
            when {
                commentsState.isLoading -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                commentsState.loadError != null -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "تعذر تحميل التعليقات",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                commentsState.comments.isEmpty() -> {
                    item {
                        EmptyCommentsState()
                    }
                }
                else -> {
                    items(commentsState.comments) { comment ->
                        CommentCard(
                            name = comment.displayName,
                            date = comment.createdAt,
                            comment = comment.commentText
                        )
                    }
                }
            }
"""

if "commentsState.isLoading" not in code:
    code = code.replace(mock_comments.strip(), real_comments.strip())


# Change EmptyCommentsState message to exact required one: "لا توجد تعليقات حتى الآن" (optional, current is "لا توجد تعليقات بعد")
# Let's fix that too
code = code.replace('"لا توجد تعليقات بعد"', '"لا توجد تعليقات حتى الآن"')
# Removing the prompt to add a comment since the user asked not to add add comment section. The current EmptyCommentsState says "كن أول من يشارك رأيه" we'll remove it.
empty_state_old = """
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "كن أول من يشارك رأيه",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
"""
code = code.replace(empty_state_old.strip(), "")

with open('app/src/main/java/com/spinel/zicola/zicola/ui/screens/BookDetailsScreen.kt', 'w', encoding='utf-8') as f:
    f.write(code)

print("Patching BookDetailsScreen complete.")
