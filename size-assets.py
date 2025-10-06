import os
import tkinter as tk
from tkinter import filedialog, messagebox
from PIL import Image

APP_TITLE = "Gerador de Chunks 2D (12 combinações)"
REQ_SIZE = (1024, 1024)
OUT_SMALL = (100, 100)  # resolução opcional de saída
def load_image(path):
    img = Image.open(path).convert("RGBA")
    if img.size != REQ_SIZE:
        # Ajuste automático para 1024x1024 mantendo simples (resize)
        img = img.resize(REQ_SIZE, Image.LANCZOS)
    return img

def split_quadrants(img):
    """Retorna dicionário com quadrantes: q1=TL, q2=TR, q3=BL, q4=BR."""
    w, h = img.size
    hw, hh = w // 2, h // 2
    q1 = img.crop((0,     0,     hw, hh))  # top-left
    q2 = img.crop((hw,    0,     w,  hh))  # top-right
    q3 = img.crop((0,     hh,    hw, h ))  # bottom-left
    q4 = img.crop((hw,    hh,    w,  h ))  # bottom-right
    return {"q1": q1, "q2": q2, "q3": q3, "q4": q4}

def compose(q1, q2, q3, q4):
    """Monta imagem 1024x1024 a partir dos quatro quadrantes (q1 TL, q2 TR, q3 BL, q4 BR)."""
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

def generate():
    img_a_path = entry_img_a.get().strip()
    img_b_path = entry_img_b.get().strip()
    name_a = safe_name(entry_name_a.get() or "imgA")
    name_b = safe_name(entry_name_b.get() or "imgB")
    out_dir = entry_out.get().strip()

    if not os.path.isfile(img_a_path) or not os.path.isfile(img_b_path):
        messagebox.showerror("Erro", "Selecione os arquivos de imagem A e B.")
        return
    if not out_dir:
        messagebox.showerror("Erro", "Selecione a pasta de saída.")
        return
    os.makedirs(out_dir, exist_ok=True)

    try:
        img_a = load_image(img_a_path)
        img_b = load_image(img_b_path)
    except Exception as e:
        messagebox.showerror("Erro ao abrir imagem", str(e))
        return

    qa = split_quadrants(img_a)  # q1,q2,q3,q4 de A
    qb = split_quadrants(img_b)  # q1,q2,q3,q4 de B

    outputs = []

    # 1) left-A-right-B  (esquerda A: q1,q3 | direita B: q2,q4)
    out = compose(qa["q1"], qb["q2"], qa["q3"], qb["q4"])
    fname = f"left-{name_a}-right-{name_b}.png"
    outputs.append((out, fname))

    # 2) left-B-right-A
    out = compose(qb["q1"], qa["q2"], qb["q3"], qa["q4"])
    fname = f"left-{name_b}-right-{name_a}.png"
    outputs.append((out, fname))

    # 3) top-A-bottom-B  (topo A: q1,q2 | baixo B: q3,q4)
    out = compose(qa["q1"], qa["q2"], qb["q3"], qb["q4"])
    fname = f"top-{name_a}-bottom-{name_b}.png"
    outputs.append((out, fname))

    # 4) top-B-bottom-A
    out = compose(qb["q1"], qb["q2"], qa["q3"], qa["q4"])
    fname = f"top-{name_b}-bottom-{name_a}.png"
    outputs.append((out, fname))

    # 5-12) threeparts-{A}-{oddpos}-{B} e o inverso (A <-> B)
    label_to_q = {
        "topleft": "q1",
        "topright": "q2",
        "bottomleft": "q3",
        "bottomright": "q4",
    }

    def build_threeparts(major_name, minor_name, q_major, q_minor, odd_label):
        """
        Cria imagem 3/4 major + 1/4 minor.
        odd_label é um de: 'topleft', 'topright', 'bottomleft', 'bottomright'.
        """
        odd_q = label_to_q[odd_label]  # mapeia para 'q1'..'q4'

        parts = {
            "q1": q_major["q1"],
            "q2": q_major["q2"],
            "q3": q_major["q3"],
            "q4": q_major["q4"],
        }
        # Substitui apenas o quadrante 'ímpar' pelo da outra imagem
        parts[odd_q] = q_minor[odd_q]

        out_img = compose(parts["q1"], parts["q2"], parts["q3"], parts["q4"])
        fname = f"threeparts-{major_name}-{odd_label}-{minor_name}.png"
        return out_img, fname

    # threeparts-A-oddpos-B (4 imagens)
    for oddpos in ["topleft", "topright", "bottomleft", "bottomright"]:
        outputs.append(build_threeparts(name_a, name_b, qa, qb, oddpos))

    # threeparts-B-oddpos-A (4 imagens)
    for oddpos in ["topleft", "topright", "bottomleft", "bottomright"]:
        outputs.append(build_threeparts(name_b, name_a, qb, qa, oddpos))

    # Salvar
    saved = []
    for img, fname in outputs:
        out_img = img
        if downscale_var.get():
            # Para texturas gerais, LANCZOS costuma ficar mais suave.
            # Se for pixel-art e quiser bordas “quadradas”, use Image.NEAREST.
            out_img = img.resize(OUT_SMALL, Image.LANCZOS)
        out_path = os.path.join(out_dir, fname if not downscale_var.get()
        else f"100x100-{fname}")
        out_img.save(out_path, format="PNG")

    messagebox.showinfo("Pronto!", f"{len(saved)} imagens geradas em:\n{out_dir}")

def browse_img(entry):
    path = filedialog.askopenfilename(
        title="Selecione uma imagem 1024x1024",
        filetypes=[("Imagens", "*.png;*.jpg;*.jpeg;*.webp;*.bmp")]
    )
    if path:
        entry.delete(0, tk.END)
        entry.insert(0, path)

def browse_out():
    path = filedialog.askdirectory(title="Selecione a pasta de saída")
    if path:
        entry_out.delete(0, tk.END)
        entry_out.insert(0, path)

# --- UI ---
root = tk.Tk()
root.title(APP_TITLE)
root.resizable(False, False)

pad = 6

frm = tk.Frame(root)
frm.pack(padx=10, pady=10)
downscale_var = tk.BooleanVar(value=False)

# Imagem A
tk.Label(frm, text="Imagem A:").grid(row=0, column=0, sticky="w", padx=pad, pady=pad)
entry_img_a = tk.Entry(frm, width=55)
entry_img_a.grid(row=0, column=1, padx=pad, pady=pad)
tk.Button(frm, text="Escolher...", command=lambda: browse_img(entry_img_a)).grid(row=0, column=2, padx=pad, pady=pad)

tk.Label(frm, text="Nome A (ex.: terra):").grid(row=1, column=0, sticky="w", padx=pad, pady=pad)
entry_name_a = tk.Entry(frm, width=20)
entry_name_a.grid(row=1, column=1, sticky="w", padx=pad, pady=pad)

# Imagem B
tk.Label(frm, text="Imagem B:").grid(row=2, column=0, sticky="w", padx=pad, pady=pad)
entry_img_b = tk.Entry(frm, width=55)
entry_img_b.grid(row=2, column=1, padx=pad, pady=pad)
tk.Button(frm, text="Escolher...", command=lambda: browse_img(entry_img_b)).grid(row=2, column=2, padx=pad, pady=pad)

tk.Label(frm, text="Nome B (ex.: agua):").grid(row=3, column=0, sticky="w", padx=pad, pady=pad)
entry_name_b = tk.Entry(frm, width=20)
entry_name_b.grid(row=3, column=1, sticky="w", padx=pad, pady=pad)

# Saída
tk.Label(frm, text="Pasta de saída:").grid(row=4, column=0, sticky="w", padx=pad, pady=pad)
entry_out = tk.Entry(frm, width=55)
entry_out.grid(row=4, column=1, padx=pad, pady=pad)
tk.Button(frm, text="Escolher...", command=browse_out).grid(row=4, column=2, padx=pad, pady=pad)

# Botão principal
btn = tk.Button(frm, text="Gerar 12 imagens", width=20, command=generate)
btn.grid(row=5, column=0, columnspan=3, pady=(pad*2, pad))
# Checkbox para reduzir as saídas para 100x100
chk = tk.Checkbutton(frm, text="Gerar em 100×100 (downscale na hora de salvar)",
                     variable=downscale_var)
chk.grid(row=6, column=0, columnspan=3, sticky="w")

# Dica de nomes gerados
hint = tk.Label(frm, fg="#555",
                text=(
                    "Padrões gerados:\n"
                    "  • left-<A>-right-<B>.png / left-<B>-right-<A>.png\n"
                    "  • top-<A>-bottom-<B>.png / top-<B>-bottom-<A>.png\n"
                    "  • threeparts-<A>-topleft-<B>.png / ...-topright-... / ...-bottomleft-... / ...-bottomright-...\n"
                    "  • (e as 4 inversas trocando A↔B)\n"
                )
                )
hint.grid(row=7, column=0, columnspan=3, sticky="w", padx=pad, pady=(0, pad))

root.mainloop()
