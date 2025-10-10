import os
import tkinter as tk
from tkinter import filedialog, messagebox
from PIL import Image
import numpy as np

# ==============================================================================
# --- CONFIGURAÇÕES GLOBAIS ---
# ==============================================================================
APP_TITLE = "Gerador e Corretor de Chunks 2D"
REQ_SIZE = (1024, 1024)
OUT_SMALL = (100, 100)
CORNER_RADIUS = 300

# --- Parâmetros da prainha/ondulação
SHORE_W = 26
WAVE_AMP = 24
WAVE_PERIODS = 6

# --- Parâmetros de CORREÇÃO DO GLITCH (do segundo script) ---
DIAMOND_RADIUS_RATIO = 0.12  # Raio do losango central (12% da dimensão da imagem)
COLOR_TOLERANCE = 20         # Tolerância para cores similares
BLACK_THRESHOLD = 50         # Limite para considerar uma cor como "preta"


# ==============================================================================
# --- LÓGICA DE CORREÇÃO DO GLITCH CENTRAL (INTEGRADA DO SCRIPT 2) ---
# ==============================================================================
def is_similar_color(color1_rgba, color2_rgba, tolerance):
    """Verifica se duas cores RGBA são similares nos canais RGB."""
    # Compara apenas os 3 primeiros canais (R, G, B)
    return all(abs(c1 - c2) <= tolerance for c1, c2 in zip(color1_rgba[:3], color2_rgba[:3]))

def is_black(color_rgba, threshold):
    """Verifica se uma cor RGBA é considerada preta nos canais RGB."""
    # Compara apenas os 3 primeiros canais (R, G, B)
    return all(c <= threshold for c in color_rgba[:3])

# ==============================================================================
# --- LÓGICA DE CORREÇÃO DO GLITCH CENTRAL (INTEGRADA DO SCRIPT 2) ---
# ==============================================================================
def is_similar_color(color1_rgba, color2_rgba, tolerance):
    """Verifica se duas cores RGBA são similares nos canais RGB."""
    # CORREÇÃO 1: Converte para int() antes da subtração para evitar overflow
    return all(abs(int(c1) - int(c2)) <= tolerance for c1, c2 in zip(color1_rgba[:3], color2_rgba[:3]))

def is_black(color_rgba, threshold):
    """Verifica se uma cor RGBA é considerada preta nos canais RGB."""
    return all(c <= threshold for c in color_rgba[:3])

def fix_central_glitch(img_to_fix: Image.Image) -> Image.Image:
    """
    Recebe uma imagem PIL (RGBA), corrige a falha central em um losango e
    retorna a imagem PIL corrigida.
    """
    # CORREÇÃO 2: Adiciona .copy() para garantir que o array seja editável
    data = np.asarray(img_to_fix).copy()
    height, width, _ = data.shape
    center_x, center_y = width // 2, height // 2

    # 1. Calcular o raio do losango em pixels
    diamond_radius = int(min(height, width) * DIAMOND_RADIUS_RATIO)
    if diamond_radius < 5:
        return img_to_fix

    # 2. Encontrar a cor predominante no losango (ignorando a linha preta)
    start_x = max(0, center_x - diamond_radius)
    end_x = min(width, center_x + diamond_radius + 1)
    start_y = max(0, center_y - diamond_radius)
    end_y = min(height, center_y + diamond_radius + 1)

    colors_in_roi = []
    for y in range(start_y, end_y):
        for x in range(start_x, end_x):
            if abs(x - center_x) + abs(y - center_y) <= diamond_radius:
                pixel_color = tuple(data[y, x])
                if not is_black(pixel_color, BLACK_THRESHOLD):
                    colors_in_roi.append(pixel_color)

    if not colors_in_roi:
        return img_to_fix

    # Agrupa cores similares para encontrar a predominante
    color_buckets = {}
    for color in colors_in_roi:
        found_bucket = False
        for bucket_key in color_buckets:
            if is_similar_color(color, bucket_key, COLOR_TOLERANCE):
                color_buckets[bucket_key].append(color)
                found_bucket = True
                break
        if not found_bucket:
            color_buckets[color] = [color]

    # Calcula a cor média do maior grupo
    if not color_buckets:
        return img_to_fix # Retorna original se não achar cores

    dominant_color_list = max(color_buckets.values(), key=len)
    avg_r = np.mean([c[0] for c in dominant_color_list])
    avg_g = np.mean([c[1] for c in dominant_color_list])
    avg_b = np.mean([c[2] for c in dominant_color_list])
    dominant_color = (int(avg_r), int(avg_g), int(avg_b), 255)

    # 3. Corrigir pixels dentro do losango
    for y in range(start_y, end_y):
        for x in range(start_x, end_x):
            if abs(x - center_x) + abs(y - center_y) <= diamond_radius:
                current_pixel_color = tuple(data[y, x])
                is_dom = is_similar_color(current_pixel_color, dominant_color, COLOR_TOLERANCE)
                is_blk = is_black(current_pixel_color, BLACK_THRESHOLD)
                if not is_dom and not is_blk:
                    data[y, x] = dominant_color

    # 4. Retorna a imagem corrigida
    return Image.fromarray(data, mode="RGBA")


# ==============================================================================
# --- LÓGICA DE GERAÇÃO DE IMAGENS (DO SCRIPT 1) ---
# ==============================================================================
def load_image(path):
    img = Image.open(path).convert("RGBA")
    if img.size != REQ_SIZE:
        img = img.resize(REQ_SIZE, Image.LANCZOS)
    return img

def split_quadrants(img):
    w, h = img.size
    hw, hh = w // 2, h // 2
    q1 = img.crop((0,     0,     hw, hh))  # top-left
    q2 = img.crop((hw,    0,     w,  hh))  # top-right
    q3 = img.crop((0,     hh,    hw, h ))  # bottom-left
    q4 = img.crop((hw,    hh,    w,  h ))  # bottom-right
    return {"q1": q1, "q2": q2, "q3": q3, "q4": q4}

def compose(q1, q2, q3, q4):
    w, h = REQ_SIZE
    hw, hh = w // 2, h // 2
    out = Image.new("RGBA", REQ_SIZE)
    out.paste(q1, (0,   0))
    out.paste(q2, (hw,  0))
    out.paste(q3, (0,  hh))
    out.paste(q4, (hw, hh))
    return out

def safe_name(s):
    return "".join(c if c.isalnum() or c in "-_." else "-" for c in s.strip().lower())

def _rng_from_names(a_name: str, b_name: str) -> np.random.RandomState:
    seed = (hash(a_name) ^ (hash(b_name) << 1)) & 0x7FFFFFFF
    return np.random.RandomState(seed)

def _periodic_curve(n: int, periods: int, amp: float, rng: np.random.RandomState) -> np.ndarray:
    xs = np.linspace(0, 2*np.pi*periods, n, endpoint=False)
    y = np.zeros(n, dtype=np.float32)
    harmonics = [(1.0, 1.0), (2.0, 0.6), (3.0, 0.35)]
    rng.shuffle(harmonics)
    for k, w in harmonics[:3]:
        phase = rng.uniform(0, 2*np.pi)
        y += w * np.sin(k*xs + phase)
    y /= np.max(np.abs(y)) + 1e-6
    return (y * amp).astype(np.int32)

def blend_wavy_lr(img_left: Image.Image, img_right: Image.Image, a_name: str, b_name: str) -> Image.Image:
    w, h = REQ_SIZE
    hw = w // 2
    A = np.asarray(img_left)
    B = np.asarray(img_right)
    rng = _rng_from_names(a_name, b_name)
    offset = _periodic_curve(h, WAVE_PERIODS, WAVE_AMP, rng)
    shore_half = SHORE_W // 2
    out = np.empty_like(A)
    shore_color = (0, 0, 0, 255)
    for y in range(h):
        xb = hw + int(offset[y])
        xs0 = max(0, xb - shore_half)
        xs1 = min(w, xb + shore_half + (SHORE_W % 2))
        if xs0 > 0: out[y, :xs0] = A[y, :xs0]
        if xs1 < w: out[y, xs1:] = B[y, xs1:]
        out[y, xs0:xs1] = shore_color
    return Image.fromarray(out, mode="RGBA")

def blend_wavy_tb(img_top: Image.Image, img_bottom: Image.Image, a_name: str, b_name: str) -> Image.Image:
    w, h = REQ_SIZE
    hh = h // 2
    A = np.asarray(img_top)
    B = np.asarray(img_bottom)
    rng = _rng_from_names(a_name, b_name)
    offset = _periodic_curve(w, WAVE_PERIODS, WAVE_AMP, rng)
    shore_half = SHORE_W // 2
    out = np.empty_like(A)
    shore_color = (0, 0, 0, 255)
    for x in range(w):
        yb = hh + int(offset[x])
        ys0 = max(0, yb - shore_half)
        ys1 = min(h, yb + shore_half + (SHORE_W % 2))
        if ys0 > 0: out[:ys0, x] = A[:ys0, x]
        if ys1 < h: out[ys1:, x] = B[ys1:, x]
        out[ys0:ys1, x] = shore_color
    return Image.fromarray(out, mode="RGBA")

def blend_wavy_cross(img_comp, major_img, minor_img, major_name, minor_name, odd_label):
    w, h = REQ_SIZE
    hw, hh = w // 2, h // 2
    out = np.asarray(img_comp).copy()
    shore_v_color = (0, 0, 0, 255)
    shore_h_color = (0, 0, 0, 255)
    rng_v = _rng_from_names(major_name, minor_name)
    rng_h = _rng_from_names(minor_name, major_name)
    off_v = _periodic_curve(h, WAVE_PERIODS, WAVE_AMP, rng_v)
    off_h = _periodic_curve(w, WAVE_PERIODS, WAVE_AMP, rng_h)
    shore_half = SHORE_W // 2
    odd_is_left = odd_label in ("topleft","bottomleft")
    odd_is_top  = odd_label in ("topleft","topright")
    OFFSET = CORNER_RADIUS - SHORE_W // 2
    if odd_is_top and odd_is_left: x_c, y_c = hw - OFFSET, hh - OFFSET
    elif odd_is_top and not odd_is_left: x_c, y_c = hw + OFFSET, hh - OFFSET
    elif (not odd_is_top) and odd_is_left: x_c, y_c = hw - OFFSET, hh + OFFSET
    else: x_c, y_c = hw + OFFSET, hh + OFFSET
    PAD = max(4, SHORE_W // 2)
    MA, MI = np.asarray(major_img), np.asarray(minor_img)
    y_start, y_end = (0, hh) if odd_is_top else (hh, h)
    y_range = range(y_start, y_c) if odd_is_top else range(y_c, y_end)
    for y in y_range:
        xb = hw + int(off_v[y])
        xs0, xs1 = max(0, xb - shore_half), min(w, xb + shore_half + (SHORE_W % 2))
        xl0, xl1 = max(0, xs0 - PAD), xs0
        if xl1 > xl0: out[y, xl0:xl1] = MI[y, xl0:xl1] if odd_is_left else MA[y, xl0:xl1]
        out[y, xs0:xs1] = shore_v_color
        xr0, xr1 = xs1, min(w, xs1 + PAD)
        if xr1 > xr0: out[y, xr0:xr1] = MA[y, xr0:xr1] if odd_is_left else MI[y, xr0:xr1]
    x_start, x_end = (0, hw) if odd_is_left else (hw, w)
    x_range = range(x_start, x_c) if odd_is_left else range(x_c, x_end)
    for x in x_range:
        yb = hh + int(off_h[x])
        ys0, ys1 = max(0, yb - shore_half), min(h, yb + shore_half + (SHORE_W % 2))
        yt0, yt1 = max(0, ys0 - PAD), ys0
        if yt1 > yt0: out[yt0:yt1, x] = MI[yt0:yt1, x] if odd_is_top else MA[yt0:yt1, x]
        out[ys0:ys1, x] = shore_h_color
        yb0, yb1 = ys1, min(h, ys1 + PAD)
        if yb1 > yb0: out[yb0:yb1, x] = MA[yb0:yb1, x] if odd_is_top else MI[yb0:yb1, x]
    R, th = CORNER_RADIUS, SHORE_W
    x0, x1 = max(0, x_c - (R + th)), min(w, x_c + (R + th))
    y0, y1 = max(0, y_c - (R + th)), min(h, y_c + (R + th))
    if x1 > x0 and y1 > y0:
        yy, xx = np.ogrid[y0:y1, x0:x1]
        dist = np.sqrt((xx - x_c)**2 + (yy - y_c)**2)
        if odd_is_top and odd_is_left: sector = (xx >= x_c) & (yy >= y_c)
        elif odd_is_top and not odd_is_left: sector = (xx <= x_c) & (yy >= y_c)
        elif (not odd_is_top) and odd_is_left: sector = (xx >= x_c) & (yy <= y_c)
        else: sector = (xx <= x_c) & (yy <= y_c)
        area = sector & (dist <= (R + th))
        ring, inside_minor, outside_major = area & (np.abs(dist - R) <= th//2), area & (dist < (R - th//2)), area & (dist > (R + th//2))
        sub = out[y0:y1, x0:x1]
        sub[inside_minor] = MI[y0:y1, x0:x1][inside_minor]
        sub[outside_major] = MA[y0:y1, x0:x1][outside_major]
        sub[ring] = (0, 0, 0, 255)
        out[y0:y1, x0:x1] = sub
    return Image.fromarray(out, mode="RGBA")

# ==============================================================================
# --- PIPELINE PRINCIPAL (COM AS 12 SAÍDAS) ---
# ==============================================================================
def generate():
    img_a_path = entry_img_a.get().strip()
    img_b_path = entry_img_b.get().strip()
    if not os.path.isfile(img_a_path) or not os.path.isfile(img_b_path):
        messagebox.showerror("Erro", "Selecione os arquivos de imagem A e B.")
        return
    name_a = safe_name(os.path.splitext(os.path.basename(img_a_path))[0])
    name_b = safe_name(os.path.splitext(os.path.basename(img_b_path))[0])
    out_dir = os.path.dirname(img_a_path)
    os.makedirs(out_dir, exist_ok=True)

    try:
        img_a = load_image(img_a_path)
        img_b = load_image(img_b_path)
    except Exception as e:
        messagebox.showerror("Erro ao abrir imagem", str(e))
        return

    qa = split_quadrants(img_a)
    qb = split_quadrants(img_b)
    outputs = []

    # 1) left-A-right-B
    outputs.append((blend_wavy_lr(img_a, img_b, name_a, name_b), f"left-{name_a}-right-{name_b}.png"))
    # 2) left-B-right-A
    outputs.append((blend_wavy_lr(img_b, img_a, name_b, name_a), f"left-{name_b}-right-{name_a}.png"))
    # 3) top-A-bottom-B
    outputs.append((blend_wavy_tb(img_a, img_b, name_a, name_b), f"top-{name_a}-bottom-{name_b}.png"))
    # 4) top-B-bottom-A
    outputs.append((blend_wavy_tb(img_b, img_a, name_b, name_a), f"top-{name_b}-bottom-{name_a}.png"))

    label_to_q = {"topleft": "q1", "topright": "q2", "bottomleft": "q3", "bottomright": "q4"}

    def build_threeparts(major_name, minor_name, q_major, q_minor, odd_label):
        parts = {k: v for k, v in q_major.items()}
        parts[label_to_q[odd_label]] = q_minor[label_to_q[odd_label]]
        base = compose(parts["q1"], parts["q2"], parts["q3"], parts["q4"])

        major_img = img_a if major_name == name_a else img_b
        minor_img = img_b if major_name == name_a else img_a

        # Gera a imagem com a falha
        out_img_glitched = blend_wavy_cross(base, major_img, minor_img, major_name, minor_name, odd_label)

        # >>>>>>>>>>>>>>>>> A MÁGICA ACONTECE AQUI <<<<<<<<<<<<<<<<<
        # Aplica a correção do glitch central imediatamente
        out_img_fixed = fix_central_glitch(out_img_glitched)
        # >>>>>>>>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<<<

        fname = f"threeparts-{major_name}-{odd_label}-{minor_name}.png"
        return out_img_fixed, fname

    # 5-8) threeparts-A-oddpos-B (com correção automática)
    for oddpos in ["topleft", "topright", "bottomleft", "bottomright"]:
        outputs.append(build_threeparts(name_a, name_b, qa, qb, oddpos))

    # 9-12) threeparts-B-oddpos-A (com correção automática)
    for oddpos in ["topleft", "topright", "bottomleft", "bottomright"]:
        outputs.append(build_threeparts(name_b, name_a, qb, qa, oddpos))

    # Salvar
    saved_count = 0
    for img, fname in outputs:
        out_path = os.path.join(out_dir, fname)
        img_to_save = img
        if downscale_var.get():
            img_to_save = img.resize(OUT_SMALL, Image.LANCZOS)
            out_path = os.path.join(out_dir, f"100x100-{fname}")
        img_to_save.save(out_path, format="PNG")
        saved_count += 1

    messagebox.showinfo("Pronto!", f"{saved_count} imagens geradas e corrigidas em:\n{out_dir}")


# ==============================================================================
# --- INTERFACE GRÁFICA (UI) ---
# ==============================================================================
def browse_img(entry):
    path = filedialog.askopenfilename(
        title="Selecione uma imagem 1024x1024",
        filetypes=[("Imagens", "*.png;*.jpg;*.jpeg;*.webp;*.bmp")]
    )
    if path:
        entry.delete(0, tk.END)
        entry.insert(0, path)

if __name__ == "__main__":
    root = tk.Tk()
    root.title(APP_TITLE)
    root.resizable(False, False)
    pad = 6
    frm = tk.Frame(root)
    frm.pack(padx=10, pady=10)
    downscale_var = tk.BooleanVar(value=False)

    tk.Label(frm, text="Imagem A:").grid(row=0, column=0, sticky="w", padx=pad, pady=pad)
    entry_img_a = tk.Entry(frm, width=55)
    entry_img_a.grid(row=0, column=1, padx=pad, pady=pad)
    tk.Button(frm, text="Escolher...", command=lambda: browse_img(entry_img_a)).grid(row=0, column=2, padx=pad, pady=pad)

    tk.Label(frm, text="Imagem B:").grid(row=2, column=0, sticky="w", padx=pad, pady=pad)
    entry_img_b = tk.Entry(frm, width=55)
    entry_img_b.grid(row=2, column=1, padx=pad, pady=pad)
    tk.Button(frm, text="Escolher...", command=lambda: browse_img(entry_img_b)).grid(row=2, column=2, padx=pad, pady=pad)

    btn = tk.Button(frm, text="Gerar 12 imagens", width=20, command=generate)
    btn.grid(row=5, column=0, columnspan=3, pady=(pad*2, pad))

    chk = tk.Checkbutton(frm, text="Gerar em 100×100 (downscale)", variable=downscale_var)
    chk.grid(row=6, column=0, columnspan=3, sticky="w")

    hint = tk.Label(frm, fg="#555", justify="left",
                    text="Gera 12 combinações com 'prainha' ondulada.\nAs 8 imagens do tipo 'threeparts' são corrigidas automaticamente.")
    hint.grid(row=7, column=0, columnspan=3, sticky="w", padx=pad, pady=(0, pad))

    root.mainloop()