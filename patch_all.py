import re

# 1. PreferencesManager.kt
with open('app/src/main/java/com/spinel/zicola/zicola/data/PreferencesManager.kt', 'r', encoding='utf-8') as f:
    pm_code = f.read()

bookmark_funcs = """
    fun getBookmarkChapter(bookId: String): Flow<Int> = context.dataStore.data.map { it[intPreferencesKey("${bookId}_bookmark_chapter")] ?: -1 }
    fun getBookmarkOffset(bookId: String): Flow<Int> = context.dataStore.data.map { it[intPreferencesKey("${bookId}_bookmark_offset")] ?: -1 }

    suspend fun saveBookmark(bookId: String, chapterIndex: Int, offset: Int) {
        context.dataStore.edit {
            it[intPreferencesKey("${bookId}_bookmark_chapter")] = chapterIndex
            it[intPreferencesKey("${bookId}_bookmark_offset")] = offset
        }
    }
"""

if "fun getBookmarkChapter" not in pm_code:
    pm_code = pm_code.replace("    companion object {", bookmark_funcs + "\n    companion object {")
    with open('app/src/main/java/com/spinel/zicola/zicola/data/PreferencesManager.kt', 'w', encoding='utf-8') as f:
        f.write(pm_code)


# 2. ReaderViewModel.kt
with open('app/src/main/java/com/spinel/zicola/zicola/ui/viewmodel/ReaderViewModel.kt', 'r', encoding='utf-8') as f:
    vm_code = f.read()

bookmark_vars = """
    private val _bookmarkedChapter = MutableStateFlow(-1)
    val bookmarkedChapter = _bookmarkedChapter.asStateFlow()

    private val _bookmarkedOffset = MutableStateFlow(-1)
    val bookmarkedOffset = _bookmarkedOffset.asStateFlow()
"""

if "_bookmarkedChapter" not in vm_code:
    vm_code = vm_code.replace("    private val _nextBook = MutableStateFlow<com.spinel.zicola.zicola.model.Book?>(null)", bookmark_vars + "    private val _nextBook = MutableStateFlow<com.spinel.zicola.zicola.model.Book?>(null)")

init_vars = """
        viewModelScope.launch {
            _bookmarkedChapter.value = preferencesManager.getBookmarkChapter(bookId).first()
            _bookmarkedOffset.value = preferencesManager.getBookmarkOffset(bookId).first()
        }
"""
if "preferencesManager.getBookmarkChapter" not in vm_code:
    vm_code = vm_code.replace("        _nextBook.value = nextBookId?.let { repository.getBook(it) }", "        _nextBook.value = nextBookId?.let { repository.getBook(it) }\n" + init_vars)

bookmark_func = """
    fun saveBookmark(offset: Int) {
        val bookId = currentBookId ?: return
        val chapter = _currentBlockIndex.value
        viewModelScope.launch {
            preferencesManager.saveBookmark(bookId, chapter, offset)
            _bookmarkedChapter.value = chapter
            _bookmarkedOffset.value = offset
        }
    }
"""
if "fun saveBookmark" not in vm_code:
    vm_code = vm_code.replace("    fun updateTheme(themeName: String) {", bookmark_func + "\n    fun updateTheme(themeName: String) {")

with open('app/src/main/java/com/spinel/zicola/zicola/ui/viewmodel/ReaderViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(vm_code)


# 3. ReaderScreen.kt
with open('app/src/main/java/com/spinel/zicola/zicola/ui/screens/ReaderScreen.kt', 'r', encoding='utf-8') as f:
    screen_code = f.read()

imports = """
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
"""
if "import androidx.compose.material.icons.filled.Bookmark" not in screen_code:
    screen_code = screen_code.replace("import androidx.compose.material.icons.filled.Search", imports + "import androidx.compose.material.icons.filled.Search")

vars_to_add = """
    val snackbarHostState = remember { SnackbarHostState() }
    val bookmarkedChapter by viewModel.bookmarkedChapter.collectAsStateWithLifecycle()
"""
if "val snackbarHostState" not in screen_code:
    screen_code = screen_code.replace("    val listState = rememberLazyListState()", vars_to_add + "    val listState = rememberLazyListState()")

snackbar_host = """
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 64.dp)
        )
"""
if "hostState = snackbarHostState" not in screen_code:
    screen_code = screen_code.replace("    if (showSettingsSheet) {", snackbar_host + "\n    if (showSettingsSheet) {")

bookmark_action = """
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
"""
if "Icons.Filled.Bookmark" not in screen_code:
    # There's a specific block inside TopAppBar actions for not searching state
    # We want to add the bookmark icon before the search icon
    screen_code = screen_code.replace(
        "                        IconButton(onClick = { isSearching = true }) {", 
        bookmark_action + "                        IconButton(onClick = { isSearching = true }) {"
    )

with open('app/src/main/java/com/spinel/zicola/zicola/ui/screens/ReaderScreen.kt', 'w', encoding='utf-8') as f:
    f.write(screen_code)

print("Patching complete.")
