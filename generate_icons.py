from PIL import Image, ImageDraw
import os

# Paths
source_icon_path = "iconapp.png"
res_dir = "app/src/main/res"

if not os.path.exists(source_icon_path):
    print(f"Error: {source_icon_path} not found.")
    exit(1)

try:
    img = Image.open(source_icon_path).convert("RGBA")
except Exception as e:
    print(f"Error opening image: {e}")
    exit(1)

# Ensure square
w, h = img.size
if w != h:
    size = max(w, h)
    new_img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    new_img.paste(img, ((size - w) // 2, (size - h) // 2))
    img = new_img

densities = {
    "mdpi": {"legacy": 48, "adaptive": 108, "safe": 72},
    "hdpi": {"legacy": 72, "adaptive": 162, "safe": 108},
    "xhdpi": {"legacy": 96, "adaptive": 216, "safe": 144},
    "xxhdpi": {"legacy": 144, "adaptive": 324, "safe": 216},
    "xxxhdpi": {"legacy": 192, "adaptive": 432, "safe": 288},
}

# Generate a circular mask for round icons
def make_round(im):
    mask = Image.new('L', im.size, 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse((0, 0) + im.size, fill=255)
    round_im = Image.new('RGBA', im.size)
    round_im.paste(im, (0, 0), mask=mask)
    return round_im

# Create directories and generate icons
for density, sizes in densities.items():
    density_dir = os.path.join(res_dir, f"mipmap-{density}")
    os.makedirs(density_dir, exist_ok=True)
    
    # 1. Legacy Icon
    legacy_size = sizes["legacy"]
    legacy_icon = img.resize((legacy_size, legacy_size), Image.Resampling.LANCZOS)
    legacy_icon.save(os.path.join(density_dir, "ic_launcher.png"))
    
    # 2. Round Icon
    round_icon = make_round(legacy_icon)
    round_icon.save(os.path.join(density_dir, "ic_launcher_round.png"))
    
    # 3. Adaptive Foreground
    adaptive_size = sizes["adaptive"]
    safe_size = sizes["safe"]
    padding = (adaptive_size - safe_size) // 2
    
    foreground = Image.new("RGBA", (adaptive_size, adaptive_size), (0, 0, 0, 0))
    safe_img = img.resize((safe_size, safe_size), Image.Resampling.LANCZOS)
    foreground.paste(safe_img, (padding, padding))
    foreground.save(os.path.join(density_dir, "ic_launcher_foreground.png"))

print("Icons generated successfully.")
