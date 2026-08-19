import re

with open('app/src/main/java/com/spinel/zicola/zicola/ui/screens/ReaderScreen.kt', 'r', encoding='utf-8') as f:
    code = f.read()

# Add imports
imports = """
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
"""
code = code.replace('import androidx.compose.runtime.launch', 'import androidx.compose.runtime.launch\n' + imports)
if 'import androidx.compose.material.icons.filled.Search' not in code:
    code = code.replace('import androidx.compose.runtime.*', 'import androidx.compose.runtime.*\n' + imports)

# Add State variables
state_vars = """
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
"""
code = code.replace('val coroutineScope = rememberCoroutineScope()', 'val coroutineScope = rememberCoroutineScope()\n' + state_vars)


# Replace Text Rendering
old_text_render = """
                        if (content != null) {
                            Text(
                                text = content,
                                fontSize = fontSize.sp,
                                lineHeight = fontSize.sp * lineSpacing,
                                color = currentTheme.text,
                                textAlign = TextAlign.Start,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 32.dp)
                            )
                        }"""

new_text_render = """
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
                        }"""
code = code.replace(old_text_render.strip(), new_text_render.strip())


# Replace TopAppBar
old_top_app_bar = """
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
            )"""

new_top_app_bar = """
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
            }"""
code = code.replace(old_top_app_bar.strip(), new_top_app_bar.strip())

with open('app/src/main/java/com/spinel/zicola/zicola/ui/screens/ReaderScreen.kt', 'w', encoding='utf-8') as f:
    f.write(code)

