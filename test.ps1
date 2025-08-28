param([string]$imagePath)

# 强制 UTF8 输出
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
# 设置 STA 线程
[System.Threading.Thread]::CurrentThread.ApartmentState = "STA"

# 加载 WinRT 支持
Add-Type -AssemblyName System.Runtime.WindowsRuntime

# 打开图片
$imageFile = Get-Item $imagePath
$storageFile = [Windows.Storage.StorageFile]::GetFileFromPathAsync($imageFile.FullName).GetAwaiter().GetResult()
$stream = $storageFile.OpenAsync([Windows.Storage.FileAccessMode]::Read).GetAwaiter().GetResult()

$decoder = [Windows.Graphics.Imaging.BitmapDecoder]::CreateAsync($stream).GetAwaiter().GetResult()
$softwareBitmap = $decoder.GetSoftwareBitmapAsync().GetAwaiter().GetResult()

# 创建 OCR 引擎（英文）
$ocrEngine = [Windows.Media.Ocr.OcrEngine]::TryCreateFromLanguage((New-Object Windows.Globalization.Language "en"))
$result = $ocrEngine.RecognizeAsync($softwareBitmap).GetAwaiter().GetResult()

# 输出每行文字及矩形
foreach ($line in $result.Lines) {
    $words = $line.Words
    if ($words.Count -eq 0) { continue }

    $minX = ($words | ForEach-Object { $_.BoundingRect.X } | Measure-Object -Minimum).Minimum
    $minY = ($words | ForEach-Object { $_.BoundingRect.Y } | Measure-Object -Minimum).Minimum
    $maxX = ($words | ForEach-Object { $_.BoundingRect.X + $_.BoundingRect.Width } | Measure-Object -Maximum).Maximum
    $maxY = ($words | ForEach-Object { $_.BoundingRect.Y + $_.BoundingRect.Height } | Measure-Object -Maximum).Maximum

    $width = $maxX - $minX
    $height = $maxY - $minY

    Write-Output ("{0}|{1}|{2}|{3}|{4}" -f $line.Text, $minX, $minY, $width, $height)
}
