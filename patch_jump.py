import re

# 1. ReaderViewModel.kt
with open('app/src/main/java/com/spinel/zicola/zicola/ui/viewmodel/ReaderViewModel.kt', 'r', encoding='utf-8') as f:
    vm_code = f.read()

jump_func = """
    fun jumpToBookmark(chapter: Int, offset: Int) {
        if (chapter < _totalBlocks.value && chapter >= 0) {
            _currentBlockIndex.value = chapter
            _initialScroll.value = Pair(chapter, offset)
            currentBookId?.let { loadBlock(it, chapter) }
        }
    }
"""

if "fun jumpToBookmark" not in vm_code:
    vm_code = vm_code.replace("    fun updateTheme(themeName: String) {", jump_func + "\n    fun updateTheme(themeName: String) {")
    with open('app/src/main/java/com/spinel/zicola/zicola/ui/viewmodel/ReaderViewModel.kt', 'w', encoding='utf-8') as f:
        f.write(vm_code)

# 2. ReaderScreen.kt
with open('app/src/main/java/com/spinel/zicola/zicola/ui/screens/ReaderScreen.kt', 'r', encoding='utf-8') as f:
    screen_code = f.read()

var_add = """
    val bookmarkedChapter by viewModel.bookmarkedChapter.collectAsStateWithLifecycle()
    val bookmarkedOffset by viewModel.bookmarkedOffset.collectAsStateWithLifecycle()
"""
screen_code = screen_code.replace("    val bookmarkedChapter by viewModel.bookmarkedChapter.collectAsStateWithLifecycle()", var_add.strip())

ui_add = """
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
"""

target = """                        }
                    }
                }"""
replacement = """                        }
                    }
                }

""" + ui_add.strip() + "\n"

# we want to insert after the Theme Row
# let's be more specific to avoid replacing wrong block
theme_block = """                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = themeOpt.background,
                                    selectedLabelColor = themeOpt.text
                                )
                            )
                        }
                    }
                }"""
theme_replacement = theme_block + "\n\n" + ui_add

screen_code = screen_code.replace(theme_block, theme_replacement)

with open('app/src/main/java/com/spinel/zicola/zicola/ui/screens/ReaderScreen.kt', 'w', encoding='utf-8') as f:
    f.write(screen_code)

print("Patching complete.")
