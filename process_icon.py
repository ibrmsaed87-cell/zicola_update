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
    
    # Remove old webp files to avoid compilation conflicts
    old_file = os.path.join(folder, 'ic_launcher.webp')
    old_round = os.path.join(folder, 'ic_launcher_round.webp')
    if os.path.exists(old_file): os.remove(old_file)
    if os.path.exists(old_round): os.remove(old_round)

# Adaptive Icon Foreground (put in drawable-nodpi)
# Canvas is 432x432 (xxxhdpi equivalent of 108dp). Safe zone is 288x288 (72dp)
fg_size = 432
safe_size = 288

fg_canvas = Image.new('RGBA', (fg_size, fg_size), (0, 0, 0, 0))
resized_safe = img.resize((safe_size, safe_size), Image.Resampling.LANCZOS)
offset = (fg_size - safe_size) // 2
fg_canvas.paste(resized_safe, (offset, offset), mask=resized_safe)

drawable_nodpi = os.path.join(res_dir, 'drawable-nodpi')
os.makedirs(drawable_nodpi, exist_ok=True)
fg_canvas.save(os.path.join(drawable_nodpi, 'ic_launcher_foreground.png'), 'PNG')

# Remove old xml foreground
old_xml = os.path.join(res_dir, 'drawable', 'ic_launcher_foreground.xml')
if os.path.exists(old_xml): os.remove(old_xml)

print("Icons generated successfully.")
