package com.spinel.zicola.zicola.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.spinel.zicola.zicola.model.BookWithProgress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaptersScreen(
    bookWithProgress: BookWithProgress,
    onBackClick: () -> Unit,
    onChapterClick: (Int) -> Unit
) {
    val book = bookWithProgress.book
    val lastReadBlockIndex = bookWithProgress.lastReadBlockIndex
    val progress = bookWithProgress.progress

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            text = "فصول ${book.title}",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        ) 
                    },
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
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(book.totalBlocks) { index ->
                    val isCurrent = progress > 0f && index == lastReadBlockIndex
                    val isCompleted = progress > 0f && index < lastReadBlockIndex
                    
                    val chapterMeta = book.chapters.getOrNull(index)
                    val chapterTitle = chapterMeta?.title ?: "الفصل ${index + 1}"
                    val chapterExcerpt = chapterMeta?.excerpt ?: "اضغط لقراءة هذا الفصل..."

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChapterClick(index) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "الفصل ${index + 1} : $chapterTitle",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = chapterExcerpt,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.fillMaxWidth(),
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                if (isCurrent) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "توقفت هنا",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Start,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                            
                            if (isCompleted) {
                                Spacer(modifier = Modifier.width(16.dp))
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "مكتمل",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
