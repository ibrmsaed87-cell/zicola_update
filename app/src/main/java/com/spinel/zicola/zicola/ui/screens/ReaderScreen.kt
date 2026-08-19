package com.spinel.zicola.zicola.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.filled.Menu
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*


import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spinel.zicola.zicola.model.Book
import com.spinel.zicola.zicola.ui.theme.DarkBackground
import com.spinel.zicola.zicola.ui.theme.DarkCharcoal
import com.spinel.zicola.zicola.ui.theme.OffWhite
import com.spinel.zicola.zicola.ui.theme.SepiaBackground
import com.spinel.zicola.zicola.ui.theme.SepiaText
import com.spinel.zicola.zicola.ui.theme.Charcoal
import com.spinel.zicola.zicola.ui.viewmodel.ReaderViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

enum class ReaderTheme(val background: Color, val text: Color, val label: String) {
    LIGHT(OffWhite, Charcoal, "فاتح"),
    SEPIA(SepiaBackground, SepiaText, "دافئ"),
    DARK(DarkBackground, DarkCharcoal, "داكن")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    bookId: String,
    bookTitle: String,
    chapterIndex: Int,
    viewModel: ReaderViewModel,
    onBackClick: () -> Unit,
    onChaptersClick: () -> Unit,
    onNextBookClick: (String) -> Unit
) {
    LaunchedEffect(bookId, chapterIndex) {
        viewModel.initBook(bookId, chapterIndex)
    }

    val fontSize by viewModel.fontSize.collectAsStateWithLifecycle()
    val lineSpacing by viewModel.lineSpacing.collectAsStateWithLifecycle()
    val themeName by viewModel.theme.collectAsStateWithLifecycle()
    val currentTheme = ReaderTheme.values().find { it.name == themeName } ?: ReaderTheme.SEPIA
    
    val blocks by viewModel.blocks.collectAsStateWithLifecycle()
    val loadingBlocks by viewModel.loadingBlocks.collectAsStateWithLifecycle()
    val errorBlocks by viewModel.errorBlocks.collectAsStateWithLifecycle()
    val totalBlocks by viewModel.totalBlocks.collectAsStateWithLifecycle()
    val currentBlockIndex by viewModel.currentBlockIndex.collectAsStateWithLifecycle()
    val initialScroll by viewModel.initialScroll.collectAsStateWithLifecycle()
    val currentBook by viewModel.currentBook.collectAsStateWithLifecycle()

    var showControls by remember { mutableStateOf(true) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    

    val snackbarHostState = remember { SnackbarHostState() }
val bookmarkedChapter by viewModel.bookmarkedChapter.collectAsStateWithLifecycle()
    val bookmarkedOffset by viewModel.bookmarkedOffset.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(initialScroll) {
        initialScroll?.let { (index, offset) ->
            if (index == currentBlockIndex) {
                listState.scrollToItem(0, offset)
            }
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { 
            listState.firstVisibleItemScrollOffset 
        }
        .distinctUntilChanged()
        .collect { offset ->
            viewModel.saveProgress(currentBlockIndex, offset)
        }
    }

    var textItemSize by remember { mutableIntStateOf(0) }
    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableFloatStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()

    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Int>>(emptyList()) }
    var currentResultIndex by remember { mutableIntStateOf(-1) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val searchFocusRequester = remember { FocusRequester() }

    LaunchedEffect(currentBlockIndex, bookId) {
        isSearching = false
        searchQuery = ""
        searchResults = emptyList()
        currentResultIndex = -1
    }

    LaunchedEffect(searchQuery, currentBlockIndex) {
        if (searchQuery.isNotBlank()) {
            val text = blocks[currentBlockIndex] ?: ""
            val matches = mutableListOf<Int>()
            var index = text.indexOf(searchQuery, ignoreCase = true)
            while (index >= 0) {
                matches.add(index)
                index = text.indexOf(searchQuery, index + 1, ignoreCase = true)
            }
            searchResults = matches
            currentResultIndex = if (matches.isNotEmpty()) 0 else -1
        } else {
            searchResults = emptyList()
            currentResultIndex = -1
        }
    }

    LaunchedEffect(currentResultIndex) {
        if (currentResultIndex >= 0 && currentResultIndex < searchResults.size && textLayoutResult != null) {
            val matchIndex = searchResults[currentResultIndex]
            val line = textLayoutResult!!.getLineForOffset(matchIndex)
            val yPos = textLayoutResult!!.getLineTop(line)
            
            coroutineScope.launch {
                listState.animateScrollToItem(0, yPos.toInt())
            }
        }
    }


    val chapterProgress by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleHeight = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
            if (textItemSize <= visibleHeight || visibleHeight <= 0) {
                1f
            } else {
                val maxScroll = (textItemSize - visibleHeight).toFloat()
                val currentScroll = if (listState.firstVisibleItemIndex == 0) {
                    listState.firstVisibleItemScrollOffset.toFloat()
                } else {
                    maxScroll
                }
                (currentScroll / maxScroll).coerceIn(0f, 1f)
            }
        }
    }

    LaunchedEffect(chapterProgress) {
        if (!isDraggingSlider) {
            sliderValue = chapterProgress
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
            modifier = Modifier.fillMaxSize(),
            color = currentTheme.background
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        showControls = !showControls
                    },
                contentPadding = PaddingValues(top = 80.dp, bottom = 64.dp, start = 24.dp, end = 24.dp)
            ) {
                item {
                    val content = blocks[currentBlockIndex]
                    val isError = errorBlocks.contains(currentBlockIndex)
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 800.dp)
                            .onGloballyPositioned { coordinates ->
                                textItemSize = coordinates.size.height
                            }
                    ) {
                        if (content != null) {
                            val annotatedContent = remember(content, searchQuery, searchResults, currentResultIndex, currentTheme) {
                                if (searchQuery.isBlank() || searchResults.isEmpty()) return@remember AnnotatedString(content)
                                
                                buildAnnotatedString {
                                    append(content)
                                    val highlightStyle = SpanStyle(background = Color.Yellow.copy(alpha = 0.4f), color = Color.Black)
                                    val currentHighlightStyle = SpanStyle(background = Color.Yellow, color = Color.Black)
                                    
                                    searchResults.forEachIndexed { index, matchIndex ->
                                        val style = if (index == currentResultIndex) currentHighlightStyle else highlightStyle
                                        addStyle(style, matchIndex, matchIndex + searchQuery.length)
                                    }
                                }
                            }
                            
                            Text(
                                text = annotatedContent,
                                fontSize = fontSize.sp,
                                lineHeight = fontSize.sp * lineSpacing,
                                color = currentTheme.text,
                                textAlign = TextAlign.Start,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 32.dp),
                                onTextLayout = { result ->
                                    textLayoutResult = result
                                }
                            )
                        } else if (isError) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("تعذر تحميل هذا الجزء", color = currentTheme.text)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { viewModel.loadBlock(bookId, currentBlockIndex) }) {
                                    Text("إعادة المحاولة")
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = currentTheme.text.copy(alpha = 0.5f))
                            }
                        }
                    }
                }

                item {
                    if (blocks[currentBlockIndex] != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (currentBlockIndex < totalBlocks - 1) {
                                Button(
                                    onClick = { viewModel.goToNextChapter() },
                                    modifier = Modifier
                                        .fillMaxWidth(0.8f)
                                        .height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = currentTheme.text.copy(alpha = 0.1f),
                                        contentColor = currentTheme.text
                                    )
                                ) {
                                    Text("الفصل التالي", style = MaterialTheme.typography.titleMedium)
                                }
                            } else {
                                Text(
                                    text = "تمت الرواية",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = currentTheme.text
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                val nextBook = viewModel.nextBook.collectAsStateWithLifecycle().value
                                
                                if (nextBook != null) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth(0.9f)
                                            .padding(vertical = 16.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = currentTheme.text.copy(alpha = 0.05f)
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            AsyncImage(
                                                model = nextBook.coverAssetPath,
                                                contentDescription = nextBook.title,
                                                modifier = Modifier
                                                    .width(120.dp)
                                                    .aspectRatio(0.65f)
                                                    .clip(RoundedCornerShape(8.dp))
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = nextBook.title,
                                                style = MaterialTheme.typography.titleLarge,
                                                color = currentTheme.text,
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Button(
                                                onClick = { onNextBookClick(nextBook.id) },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(50.dp),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary,
                                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                                )
                                            ) {
                                                Text("انتقل للجزء التالي", style = MaterialTheme.typography.titleMedium)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                
                                Button(
                                    onClick = onChaptersClick,
                                    modifier = Modifier
                                        .fillMaxWidth(0.8f)
                                        .height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = currentTheme.text.copy(alpha = 0.1f),
                                        contentColor = currentTheme.text
                                    )
                                ) {
                                    Text("العودة إلى الفصول", style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showControls,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            val chapterTitle = currentBook?.chapters?.getOrNull(currentBlockIndex)?.title
            if (isSearching) {
                TopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("ابحث في الفصل...", color = currentTheme.text.copy(alpha = 0.5f)) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = currentTheme.text,
                                unfocusedTextColor = currentTheme.text,
                                cursorColor = currentTheme.text
                            ),
                            modifier = Modifier.fillMaxWidth().focusRequester(searchFocusRequester),
                            singleLine = true
                        )
                        LaunchedEffect(Unit) {
                            searchFocusRequester.requestFocus()
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSearching = false
                            searchQuery = ""
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "إغلاق البحث",
                                tint = currentTheme.text
                            )
                        }
                    },
                    actions = {
                        if (searchQuery.isNotBlank()) {
                            if (searchResults.isEmpty()) {
                                Text("لا توجد نتائج", color = currentTheme.text.copy(alpha = 0.7f), modifier = Modifier.padding(end = 8.dp))
                            } else {
                                Text(
                                    "${currentResultIndex + 1} / ${searchResults.size}",
                                    color = currentTheme.text.copy(alpha = 0.7f)
                                )
                                IconButton(onClick = {
                                    if (currentResultIndex > 0) currentResultIndex-- else currentResultIndex = searchResults.size - 1
                                }) {
                                    Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "السابق", tint = currentTheme.text)
                                }
                                IconButton(onClick = {
                                    if (currentResultIndex < searchResults.size - 1) currentResultIndex++ else currentResultIndex = 0
                                }) {
                                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "التالي", tint = currentTheme.text)
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = currentTheme.background.copy(alpha = 0.95f)
                    )
                )
            } else {
                TopAppBar(
                    title = { 
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = bookTitle, 
                                color = currentTheme.text, 
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Start
                            )
                            val subtitle = if (chapterTitle != null) "الفصل ${currentBlockIndex + 1} — $chapterTitle" else "الفصل ${currentBlockIndex + 1}"
                            Text(
                                text = subtitle, 
                                color = currentTheme.text.copy(alpha = 0.7f), 
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                textAlign = TextAlign.Start
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "عودة",
                                tint = currentTheme.text
                            )
                        }
                    },
                    actions = {

                        IconButton(onClick = { 
                            viewModel.saveBookmark(listState.firstVisibleItemScrollOffset)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("تم حفظ العلامة المرجعية")
                            }
                        }) {
                            Icon(
                                imageVector = if (bookmarkedChapter != -1) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "علامة مرجعية",
                                tint = currentTheme.text
                            )
                        }
                        IconButton(onClick = { isSearching = true }) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "بحث",
                                tint = currentTheme.text
                            )
                        }
                        IconButton(onClick = { showSettingsSheet = true }) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "إعدادات القراءة",
                                tint = currentTheme.text
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = currentTheme.background.copy(alpha = 0.95f)
                    )
                )
            }
        }

        AnimatedVisibility(
            visible = showControls,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                color = currentTheme.background.copy(alpha = 0.95f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Slider(
                    value = sliderValue,
                    onValueChange = { newValue ->
                        sliderValue = newValue
                        isDraggingSlider = true
                        
                        val layoutInfo = listState.layoutInfo
                        val visibleHeight = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
                        if (textItemSize > visibleHeight && visibleHeight > 0) {
                            val maxScroll = textItemSize - visibleHeight
                            val targetScroll = (newValue * maxScroll).toInt()
                            coroutineScope.launch {
                                listState.scrollToItem(0, targetScroll)
                            }
                        }
                    },
                    onValueChangeFinished = {
                        isDraggingSlider = false
                        if (listState.firstVisibleItemIndex == 0) {
                            viewModel.saveProgress(currentBlockIndex, listState.firstVisibleItemScrollOffset)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = currentTheme.text.copy(alpha = 0.2f)
                    )
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 64.dp)
        )
    }




    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "إعدادات القراءة",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("حجم الخط", style = MaterialTheme.typography.titleMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(onClick = { if (fontSize > 14) viewModel.updateFontSize(fontSize - 2) }) {
                            Text("A-", fontSize = 16.sp)
                        }
                        Text("$fontSize", style = MaterialTheme.typography.bodyLarge)
                        OutlinedButton(onClick = { if (fontSize < 32) viewModel.updateFontSize(fontSize + 2) }) {
                            Text("A+", fontSize = 16.sp)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("تباعد الأسطر", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = lineSpacing == 1.4f,
                            onClick = { viewModel.updateLineSpacing(1.4f) },
                            label = { Text("ضيق") }
                        )
                        FilterChip(
                            selected = lineSpacing == 1.8f,
                            onClick = { viewModel.updateLineSpacing(1.8f) },
                            label = { Text("متوسط") }
                        )
                        FilterChip(
                            selected = lineSpacing == 2.2f,
                            onClick = { viewModel.updateLineSpacing(2.2f) },
                            label = { Text("واسع") }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("المظهر", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReaderTheme.values().forEach { themeOpt ->
                            FilterChip(
                                selected = currentTheme == themeOpt,
                                onClick = { viewModel.updateTheme(themeOpt.name) },
                                label = { Text(themeOpt.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = themeOpt.background,
                                    selectedLabelColor = themeOpt.text
                                )
                            )
                        }
                    }
                }


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("العلامة المرجعية", style = MaterialTheme.typography.titleMedium)
                    Button(
                        onClick = {
                            if (bookmarkedChapter == -1) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("لا توجد علامة محفوظة")
                                }
                            } else {
                                if (currentBlockIndex == bookmarkedChapter) {
                                    coroutineScope.launch {
                                        listState.scrollToItem(0, bookmarkedOffset)
                                    }
                                } else {
                                    viewModel.jumpToBookmark(bookmarkedChapter, bookmarkedOffset)
                                }
                                showSettingsSheet = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("الانتقال إلى العلامة المحفوظة")
                    }
                }

            }
        }
    }
    }
}
