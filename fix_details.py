import re

with open('app/src/main/java/com/spinel/zicola/zicola/ui/screens/BookDetailsScreen.kt', 'r', encoding='utf-8') as f:
    lines = f.readlines()

# We need to drop lines 420 to 466 and close AddCommentSection properly.
# The original AddCommentSection ended with:
#                 }
#             }
#         }
#     }
# }

new_lines = lines[:419]
new_lines.append("        }\n    }\n}\n")

with open('app/src/main/java/com/spinel/zicola/zicola/ui/screens/BookDetailsScreen.kt', 'w', encoding='utf-8') as f:
    f.writelines(new_lines)
print("BookDetailsScreen fixed")
