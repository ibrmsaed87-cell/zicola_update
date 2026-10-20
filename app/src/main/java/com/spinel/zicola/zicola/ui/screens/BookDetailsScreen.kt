package com.spinel.zicola.zicola.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.items
import com.spinel.zicola.zicola.ui.viewmodel.CommentsViewModel

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.spinel.zicola.zicola.model.Book
import com.spinel.zicola.zicola.model.BookWithProgress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailsScreen(
    bookWithProgress: BookWithProgress,
    onBackClick: () -> Unit,
    onChaptersClick: () -> Unit,
    onContinueClick: () -> Unit,
    commentsViewModel: CommentsViewModel = viewModel()
) {
val book = bookWithProgress.book
    val progress = bookWithProgress.progress

    val commentsState by commentsViewModel.uiState.collectAsState()

    LaunchedEffect(book.id) {
        commentsViewModel.loadComments(book.id)
    }


    var nameInput by remember { mutableStateOf("") }
    var commentInput by remember { mutableStateOf("") }
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "عودة"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .aspectRatio(0.65f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = book.coverAssetPath,
                        contentDescription = "غلاف ${book.title}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${book.partNumber} • تأليف: ${book.author}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            item {
                Text(
                    text = book.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (progress > 0f) {
                        Text(
                            text = "اكتمل ${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                    
                    Button(
                        onClick = onChaptersClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "عرض الفصول",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    if (progress > 0f) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onContinueClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "متابعة القراءة",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Text(
                    text = "تعليقات القراء",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }


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
}

@Composable
fun EmptyCommentsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "لا توجد تعليقات حتى الآن",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
    }
}

@Composable
fun CommentCard(name: String, date: String, comment: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = comment,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 28.sp
            )
        }
    }
}


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
