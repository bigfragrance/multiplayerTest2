import sys
import os
import warnings
import numpy as np
import cv2
import easyocr

# 屏蔽 pin_memory 的提示
warnings.filterwarnings("ignore", category=UserWarning, message=".*pin_memory.*")

def imread_any_path(path):
    """
    兼容中文/特殊路径读取图片。cv2.imread 在 Windows 中文路径上可能失败，
    这里用 np.fromfile + cv2.imdecode 方案。
    """
    if not os.path.exists(path):
        raise FileNotFoundError(f"文件不存在: {path}")
    data = np.fromfile(path, dtype=np.uint8)
    img = cv2.imdecode(data, cv2.IMREAD_COLOR)
    if img is None:
        raise ValueError(f"无法读取图片: {path}")
    return img

def sanitize_text(s: str) -> str:
    # TSV 输出，避免制表符/换行破坏格式
    return str(s).replace("\t", " ").replace("\r", " ").replace("\n", " ")

def to_float(x) -> float:
    # 统一转为 Python float，避免 numpy 标量
    try:
        return float(x)
    except Exception:
        return float(np.asarray(x).item())

def flatten_bbox(bbox):
    """
    bbox 形如 [[x0,y0],[x1,y1],[x2,y2],[x3,y3]]
    扁平化为 [x0,y0,x1,y1,x2,y2,x3,y3]，并全部转 float
    """
    flat = []
    for pt in bbox:
        x, y = pt
        flat.append(to_float(x))
        flat.append(to_float(y))
    return flat

def main():
    if len(sys.argv) < 2:
        print("用法: python ocr_cli_tsv.py <image_path>", file=sys.stderr)
        sys.exit(2)

    image_path = sys.argv[1]

    # 先尝试读取，确保路径/文件有效
    _ = imread_any_path(image_path)

    # 语言：你之前用的是 'zh-cn'，这里保持一致；如有需要可改为 'ch_sim'
    reader = easyocr.Reader(['en', 'zh-cn'], gpu=False, verbose=False)

    # 直接传入路径给 easyocr（内部会再读一次，不影响）
    results = reader.readtext(image_path)  # 每项: (bbox, text, prob)

    # 输出为 TSV：x0\ty0\tx1\ty1\tx2\ty2\tx3\ty3\tprob\ttext
    # 注意：不要打印其它无关内容到 stdout（避免干扰 Java 解析）
    out_lines = []
    for (bbox, text, prob) in results:
        flat = flatten_bbox(bbox)  # 8 个 float
        p = to_float(prob)
        t = sanitize_text(text)
        # 组装一行
        row = flat + [p, t]
        # 前 9 列是数字，最后一列是文本
        line = "\t".join([("{:.6f}".format(v) if i < 9 else str(v))
                          for i, v in enumerate(row)])
        out_lines.append(line)

    # 逐行输出
    if out_lines:
        sys.stdout.write("\n".join(out_lines))
    # 无结果时输出空（Java 可自行判断）
    # 不要额外 print() 其它信息

if __name__ == "__main__":
    main()