import re

with open('app/src/main/java/com/spinel/zicola/zicola/ui/screens/BookDetailsScreen.kt', 'r', encoding='utf-8') as f:
    code = f.read()

# Add needed imports
imports_to_add = """
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
"""
if "import androidx.compose.runtime.mutableStateOf" not in code:
    code = code.replace("import androidx.compose.runtime.LaunchedEffect", "import androidx.compose.runtime.LaunchedEffect\n" + imports_to_add.strip())

# Add state variables to the screen
state_vars = """
    var nameInput by remember { mutableStateOf("") }
    var commentInput by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(commentsState.submitSuccess) {
        if (commentsState.submitSuccess) {
            snackbarHostState.showSnackbar("تم إرسال تعليقك وسيظهر بعد موافقة الإدارة")
            commentInput = ""
            commentsViewModel.clearSubmitStatus()
        }
    }

    LaunchedEffect(commentsState.submitError) {
        commentsState.submitError?.let {
            snackbarHostState.showSnackbar("تعذر إرسال التعليق، حاول مرة أخرى")
            commentsViewModel.clearSubmitStatus()
        }
    }
"""
if "val snackbarHostState = remember { SnackbarHostState() }" not in code:
    code = code.replace("    Scaffold(", state_vars + "\n    Scaffold(")

# Add snackbarHost to Scaffold
if "snackbarHost = { SnackbarHost(snackbarHostState) }" not in code:
    code = code.replace("        containerColor = MaterialTheme.colorScheme.background,", "        containerColor = MaterialTheme.colorScheme.background,\n        snackbarHost = { SnackbarHost(snackbarHostState) },")

# Add the Add Comment Section before the 'when' block (right after "تعليقات القراء")
add_comment_ui = """
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

if "AddCommentSection(" not in code:
    code = code.replace("""            item {
                Text(
                    text = "تعليقات القراء",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }""", """            item {
                Text(
                    text = "تعليقات القراء",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }\n""" + add_comment_ui)

# Append the AddCommentSection composable
add_comment_composable = """
@Composable
fun AddCommentSection(
    name: String,
    onNameChange: (String) -> Unit,
    comment: String,
    onCommentChange: (String) -> Unit,
    isSubmitting: Boolean,
    onSubmit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("الاسم") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                enabled = !isSubmitting,
                shape = RoundedCornerShape(12.dp)
            )
            
            OutlinedTextField(
                value = comment,
                onValueChange = onCommentChange,
                label = { Text("اكتب تعليقك") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                enabled = !isSubmitting,
                shape = RoundedCornerShape(12.dp)
            )
            
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isSubmitting && name.trim().isNotEmpty() && comment.trim().isNotEmpty(),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("جارٍ الإرسال...")
                } else {
                    Text("إرسال التعليق")
                }
            }
        }
    }
}
"""

if "fun AddCommentSection" not in code:
    code = code + "\n" + add_comment_composable

with open('app/src/main/java/com/spinel/zicola/zicola/ui/screens/BookDetailsScreen.kt', 'w', encoding='utf-8') as f:
    f.write(code)

print("Patching BookDetailsScreen (Phase 6) complete.")
