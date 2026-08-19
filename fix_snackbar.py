import re

with open('app/src/main/java/com/spinel/zicola/zicola/ui/screens/ReaderScreen.kt', 'r', encoding='utf-8') as f:
    code = f.read()

# find and remove the badly placed SnackbarHost
bad_snackbar = """        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 64.dp)
        )"""

if bad_snackbar in code:
    code = code.replace(bad_snackbar, "")

    # Place it correctly inside the Box.
    # We can look for the last AnimatedVisibility (for controls) closing brackets:
    target = """                    )
                )
            }
        }
    }"""
    replacement = """                    )
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 64.dp)
        )
    }"""
    code = code.replace(target, replacement)

    with open('app/src/main/java/com/spinel/zicola/zicola/ui/screens/ReaderScreen.kt', 'w', encoding='utf-8') as f:
        f.write(code)
    print("Fixed SnackbarHost position.")
else:
    print("Couldn't find the bad SnackbarHost string.")
