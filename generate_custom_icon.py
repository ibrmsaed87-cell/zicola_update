import os
from PIL import Image

res_dir = 'app/src/main/res'
icon_path = 'iconapp.png'
img = Image.open(icon_path).convert("RGBA")

# Legacy sizes
sizes = {
    'mdpi': 48,
    'hdpi': 72,
    'xhdpi': 96,
    'xxhdpi': 144,
    'xxxhdpi': 192
}

for density, size in sizes.items():
    folder = os.path.join(res_dir, f'mipmap-{density}')
    os.makedirs(folder, exist_ok=True)
    
    # Resize legacy icons
    resized = img.resize((size, size), Image.Resampling.LANCZOS)
    
    # Save as png
    resized.save(os.path.join(folder, 'ic_launcher.png'), 'PNG')
    resized.save(os.path.join(folder, 'ic_launcher_round.png'), 'PNG')

# Adaptive Icon Foreground (put in drawable-nodpi)
# Canvas is 432x432. The user requested: "لا تصغّر التصميم أكثر من اللازم داخل Safe Zone"
# Standard safe zone is 288, but to prevent it from looking tiny, we'll use 320.
fg_size = 432
safe_size = 320

fg_canvas = Image.new('RGBA', (fg_size, fg_size), (0, 0, 0, 0))
resized_safe = img.resize((safe_size, safe_size), Image.Resampling.LANCZOS)
offset = (fg_size - safe_size) // 2
fg_canvas.paste(resized_safe, (offset, offset), mask=resized_safe)

drawable_nodpi = os.path.join(res_dir, 'drawable-nodpi')
os.makedirs(drawable_nodpi, exist_ok=True)
fg_canvas.save(os.path.join(drawable_nodpi, 'ic_launcher_foreground.png'), 'PNG')

print("Icons generated successfully.")
