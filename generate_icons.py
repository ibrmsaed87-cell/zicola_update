import os
from PIL import Image, ImageDraw

def create_circular_mask(size):
    mask = Image.new("L", size, 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse((0, 0, size[0], size[1]), fill=255)
    return mask

def make_round(img):
    mask = create_circular_mask(img.size)
    result = img.copy()
    result.putalpha(mask)
    return result

def main():
    src_path = "iconapp.png"
    if not os.path.exists(src_path):
        print(f"Error: {src_path} not found!")
        return

    img = Image.open(src_path).convert("RGBA")
    
    # We want to use the image as a circle for round icons and foregrounds.
    round_img = make_round(img)
    
    # Legacy sizes
    sizes = {
        "mdpi": (48, 108),
        "hdpi": (72, 162),
        "xhdpi": (96, 216),
        "xxhdpi": (144, 324),
        "xxxhdpi": (192, 432)
    }
    
    base_dir = "app/src/main/res"
    
    for density, (legacy_size, adaptive_size) in sizes.items():
        res_dir = os.path.join(base_dir, f"mipmap-{density}")
        os.makedirs(res_dir, exist_ok=True)
        
        # 1. Legacy Square
        sq_img = img.resize((legacy_size, legacy_size), Image.Resampling.LANCZOS)
        sq_img.save(os.path.join(res_dir, "ic_launcher.png"))
        
        # 2. Legacy Round
        rnd_img = round_img.resize((legacy_size, legacy_size), Image.Resampling.LANCZOS)
        rnd_img.save(os.path.join(res_dir, "ic_launcher_round.png"))
        
        # 3. Adaptive Foreground
        # Safe zone is 72/108 = 2/3 of the adaptive size
        safe_size = int(adaptive_size * (72.0 / 108.0))
        fg_img = Image.new("RGBA", (adaptive_size, adaptive_size), (0,0,0,0))
        
        # Resize round_img to safe_size
        content = round_img.resize((safe_size, safe_size), Image.Resampling.LANCZOS)
        
        # Paste into center
        offset = (adaptive_size - safe_size) // 2
        fg_img.paste(content, (offset, offset), content)
        
        fg_img.save(os.path.join(res_dir, "ic_launcher_foreground.png"))
        
    print("Icons generated successfully.")

if __name__ == "__main__":
    main()
