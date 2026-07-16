[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$InputPath,

    [string]$TemplatePath = '',
    [string]$OutputDir = '',
    [string]$OutputName = 'Jiali Wang Motivationsschreiben.docx',
    [string]$Recipient = 'Hessisches Ministerium der Finanzen',
    [string]$City = 'Wiesbaden',
    [string]$DateText = '',
    [switch]$NoPdf
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
Add-Type -AssemblyName System.IO.Compression

$ScriptRoot = if ([string]::IsNullOrWhiteSpace($PSScriptRoot)) { Split-Path -Parent $MyInvocation.MyCommand.Path } else { $PSScriptRoot }
if ([string]::IsNullOrWhiteSpace($TemplatePath)) {
    $TemplatePath = Join-Path $ScriptRoot '..\Motivationsschreiben\Jiali Wang anschreiben.docx'
}
if ([string]::IsNullOrWhiteSpace($OutputDir)) {
    $OutputDir = Join-Path $ScriptRoot '..\output'
}

$W_NS = 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'
$XML_NS = 'http://www.w3.org/XML/1998/namespace'

function Resolve-FullPath([string]$Path) {
    $executionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($Path)
}

function Get-GermanDateText {
    $months = @{
        1 = 'Januar'; 2 = 'Februar'; 3 = ('M' + [char]0x00E4 + 'rz'); 4 = 'April'
        5 = 'Mai'; 6 = 'Juni'; 7 = 'Juli'; 8 = 'August'
        9 = 'September'; 10 = 'Oktober'; 11 = 'November'; 12 = 'Dezember'
    }
    $now = Get-Date
    '{0}. {1} {2}' -f $now.Day, $months[[int]$now.Month], $now.Year
}

function ConvertTo-Paragraphs([string]$RawText) {
    $normalized = $RawText -replace "`r`n", "`n" -replace "`r", "`n"
    $blocks = New-Object System.Collections.Generic.List[string]
    $current = New-Object System.Collections.Generic.List[string]

    foreach ($line in ($normalized -split "`n")) {
        $trimmed = $line.Trim()
        if ($trimmed.Length -eq 0) {
            if ($current.Count -gt 0) {
                $blocks.Add(($current -join ' '))
                $current.Clear()
            }
            continue
        }
        $current.Add($trimmed)
    }

    if ($current.Count -gt 0) {
        $blocks.Add(($current -join ' '))
    }

    $cleaned = New-Object System.Collections.Generic.List[string]
    foreach ($block in $blocks) {
        $text = $block.Trim()
        $text = [regex]::Replace($text, '^\*\*(.+)\*\*$', '$1')
        if ($text.Length -gt 0) {
            $cleaned.Add($text)
        }
    }
    return ,$cleaned.ToArray()
}

function Get-ClosingText {
    'Mit freundlichen Gr' + [char]0x00FC + [char]0x00DF + 'en'
}

function Get-NormalizedSubject([string]$Subject) {
    $normalized = $Subject.Trim()
    $prefix = 'Motivationsschreiben'
    if ($normalized.StartsWith($prefix, [System.StringComparison]::OrdinalIgnoreCase)) {
        $trimChars = [char[]]@(' ', '-', ':', [char]0x2013, [char]0x2014)
        $normalized = $normalized.Substring($prefix.Length).TrimStart($trimChars).Trim()
    }
    if ([string]::IsNullOrWhiteSpace($normalized)) {
        return 'Bewerbung'
    }
    return $normalized
}

function Read-Letter([string]$Path) {
    $raw = Get-Content -Raw -Encoding UTF8 -LiteralPath $Path
    $paragraphs = ConvertTo-Paragraphs $raw
    if ($paragraphs.Count -eq 0) {
        throw "Input file is empty: $Path"
    }

    $subject = 'Motivationsschreiben'
    $index = 0
    if ($paragraphs[0] -match '^(Motivationsschreiben|Bewerbung)\b') {
        $subject = Get-NormalizedSubject $paragraphs[0]
        $index = 1
    }

    $salutation = 'Sehr geehrte Damen und Herren,'
    if ($index -lt $paragraphs.Count -and $paragraphs[$index] -match '^Sehr geehrte') {
        $salutation = $paragraphs[$index]
        $index++
    }

    $closingIndex = -1
    $closingText = Get-ClosingText
    for ($i = $index; $i -lt $paragraphs.Count; $i++) {
        if ($paragraphs[$i].Trim() -eq $closingText -or $paragraphs[$i] -match '^Mit\s+freundlichen\s+Gr.{2}en$') {
            $closingIndex = $i
            break
        }
    }

    if ($closingIndex -lt 0) {
        $closingIndex = $paragraphs.Count
    }

    $body = @()
    if ($closingIndex -gt $index) {
        $body = $paragraphs[$index..($closingIndex - 1)]
    }
    if ($body.Count -eq 0) {
        throw "No body paragraphs found in input file: $Path"
    }

    $signature = 'Jiali Wang'
    if (($closingIndex + 1) -lt $paragraphs.Count) {
        $signature = $paragraphs[$closingIndex + 1]
    }

    [pscustomobject]@{
        Subject = $subject
        Salutation = $salutation
        Body = $body
        Closing = $closingText
        Signature = $signature
    }
}

function New-NamespaceManager([System.Xml.XmlDocument]$Doc) {
    $ns = New-Object System.Xml.XmlNamespaceManager($Doc.NameTable)
    $ns.AddNamespace('w', $script:W_NS)
    $ns.AddNamespace('wp', 'http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing')
    $ns.AddNamespace('a', 'http://schemas.openxmlformats.org/drawingml/2006/main')
    $ns.AddNamespace('v', 'urn:schemas-microsoft-com:vml')
    return ,$ns
}

function Read-DocXml([string]$DocxPath) {
    $zip = [System.IO.Compression.ZipFile]::OpenRead($DocxPath)
    try {
        $entry = $zip.GetEntry('word/document.xml')
        if ($null -eq $entry) {
            throw "word/document.xml not found in $DocxPath"
        }
        $reader = New-Object System.IO.StreamReader($entry.Open(), [System.Text.Encoding]::UTF8)
        $xmlText = $reader.ReadToEnd()
        $reader.Close()

        $doc = New-Object System.Xml.XmlDocument
        $doc.PreserveWhitespace = $true
        $doc.LoadXml($xmlText)
        return ,$doc
    }
    finally {
        $zip.Dispose()
    }
}

function Write-DocXml([string]$DocxPath, [System.Xml.XmlDocument]$Doc) {
    $settings = New-Object System.Xml.XmlWriterSettings
    $settings.Encoding = New-Object System.Text.UTF8Encoding($false)
    $settings.OmitXmlDeclaration = $false
    $settings.Indent = $false

    $memory = New-Object System.IO.MemoryStream
    $writer = [System.Xml.XmlWriter]::Create($memory, $settings)
    $Doc.Save($writer)
    $writer.Close()
    $bytes = $memory.ToArray()
    $memory.Dispose()

    $zip = [System.IO.Compression.ZipFile]::Open($DocxPath, [System.IO.Compression.ZipArchiveMode]::Update)
    try {
        $entry = $zip.GetEntry('word/document.xml')
        $entry.Delete()
        $newEntry = $zip.CreateEntry('word/document.xml', [System.IO.Compression.CompressionLevel]::Optimal)
        $stream = $newEntry.Open()
        $stream.Write($bytes, 0, $bytes.Length)
        $stream.Close()
    }
    finally {
        $zip.Dispose()
    }
}

function Get-FirstRunPr([System.Xml.XmlElement]$Paragraph, [System.Xml.XmlNamespaceManager]$Ns) {
    $runPr = $Paragraph.SelectSingleNode('./w:r[1]/w:rPr', $Ns)
    if ($null -eq $runPr) {
        $runPr = $Paragraph.SelectSingleNode('./w:pPr/w:rPr', $Ns)
    }
    if ($null -ne $runPr) {
        return $runPr.CloneNode($true)
    }
    return $null
}

function New-Paragraph(
    [System.Xml.XmlDocument]$Doc,
    [System.Xml.XmlElement]$TemplateParagraph,
    [System.Xml.XmlNode]$RunPr,
    [AllowNull()][string]$Text
) {
    $paragraph = $Doc.CreateElement('w', 'p', $script:W_NS)
    $pPr = $TemplateParagraph.SelectSingleNode('./w:pPr', $script:Ns)
    if ($null -ne $pPr) {
        [void]$paragraph.AppendChild($Doc.ImportNode($pPr, $true))
    }

    if ($null -ne $Text) {
        $run = $Doc.CreateElement('w', 'r', $script:W_NS)
        if ($null -ne $RunPr) {
            [void]$run.AppendChild($Doc.ImportNode($RunPr, $true))
        }

        $textNode = $Doc.CreateElement('w', 't', $script:W_NS)
        $spaceAttr = $Doc.CreateAttribute('xml', 'space', $script:XML_NS)
        $spaceAttr.Value = 'preserve'
        [void]$textNode.Attributes.Append($spaceAttr)
        $textNode.InnerText = $Text

        [void]$run.AppendChild($textNode)
        [void]$paragraph.AppendChild($run)
    }

    return ,$paragraph
}

function Set-DirectParagraphText(
    [System.Xml.XmlElement]$Paragraph,
    [System.Xml.XmlNamespaceManager]$Ns,
    [string]$Text
) {
    $texts = $Paragraph.SelectNodes('./w:r/w:t', $Ns)
    if ($texts.Count -eq 0) {
        throw 'Expected at least one direct text node in paragraph.'
    }

    $texts.Item(0).InnerText = $Text
    $spaceAttr = $texts.Item(0).Attributes.GetNamedItem('space', $script:XML_NS)
    if ($null -eq $spaceAttr) {
        $spaceAttr = $texts.Item(0).OwnerDocument.CreateAttribute('xml', 'space', $script:XML_NS)
        $spaceAttr.Value = 'preserve'
        [void]$texts.Item(0).Attributes.Append($spaceAttr)
    }

    for ($i = 1; $i -lt $texts.Count; $i++) {
        $texts.Item($i).InnerText = ''
    }
}

function Set-DateText(
    [System.Xml.XmlElement]$Paragraph,
    [System.Xml.XmlNamespaceManager]$Ns,
    [string]$Text
) {
    $dateTexts = $Paragraph.SelectNodes('.//w:txbxContent//w:t', $Ns)
    if ($dateTexts.Count -eq 0) {
        return
    }

    $parts = [regex]::Split($Text.Trim(), '\s+')
    if ($parts.Count -eq 3) {
        $values = @($parts[0], ' ', $parts[1], ' ', $parts[2])
    }
    else {
        $values = @($Text, '', '', '', '')
    }

    $half = [Math]::Floor($dateTexts.Count / 2)
    if ($half -lt 5) {
        $half = $dateTexts.Count
    }

    for ($offset = 0; $offset -lt $dateTexts.Count; $offset += $half) {
        for ($i = 0; $i -lt [Math]::Min(5, $dateTexts.Count - $offset); $i++) {
            $dateTexts.Item($offset + $i).InnerText = $values[$i]
        }
    }
}

function Set-ContactBoxWidth([System.Xml.XmlDocument]$Doc) {
    $ns = New-NamespaceManager $Doc
    $contactPara = $Doc.SelectNodes('//w:body/w:p', $ns).Item(1)
    if ($null -eq $contactPara) {
        return
    }

    $newLeftEmu = '4318000'
    $newWidthEmu = '2794000'
    $newWidthVml = '220pt'
    $newLeftVml = '340pt'

    $contactPara.SelectNodes('.//wp:positionH/wp:posOffset', $ns) | ForEach-Object { $_.InnerText = $newLeftEmu }
    $contactPara.SelectNodes('.//wp:extent', $ns) | ForEach-Object { $_.SetAttribute('cx', $newWidthEmu) }
    $contactPara.SelectNodes('.//a:xfrm/a:ext', $ns) | ForEach-Object { $_.SetAttribute('cx', $newWidthEmu) }
    $contactPara.SelectNodes('.//v:shape', $ns) | ForEach-Object {
        $style = $_.GetAttribute('style')
        $style = [regex]::Replace($style, 'margin-left:[^;]+;', "margin-left:$newLeftVml;")
        $style = [regex]::Replace($style, 'width:[^;]+;', "width:$newWidthVml;")
        $_.SetAttribute('style', $style)
    }
}

function Update-DocxLetter(
    [string]$DocxPath,
    [pscustomobject]$Letter,
    [string]$RecipientText,
    [string]$CityText,
    [string]$LetterDate
) {
    $doc = Read-DocXml $DocxPath
    $script:Ns = New-NamespaceManager $doc

    Set-ContactBoxWidth $doc

    $body = $doc.SelectSingleNode('//w:body', $script:Ns)
    $paragraphs = $body.SelectNodes('./w:p', $script:Ns)
    if ($paragraphs.Count -lt 26) {
        throw 'The template does not match the expected Motivationsschreiben structure.'
    }

    Set-DirectParagraphText $paragraphs.Item(3) $script:Ns $RecipientText
    Set-DirectParagraphText $paragraphs.Item(4) $script:Ns $CityText
    Set-DirectParagraphText $paragraphs.Item(7) $script:Ns $Letter.Subject
    Set-DateText $paragraphs.Item(7) $script:Ns $LetterDate

    $salutationRunPr = Get-FirstRunPr $paragraphs.Item(9) $script:Ns
    $bodyRunPr = Get-FirstRunPr $paragraphs.Item(11) $script:Ns
    $closingRunPr = Get-FirstRunPr $paragraphs.Item(19) $script:Ns
    $nameRunPr = Get-FirstRunPr $paragraphs.Item(25) $script:Ns

    $newItems = New-Object System.Collections.Generic.List[object]
    $newItems.Add([pscustomobject]@{ Template = $paragraphs.Item(8); RunPr = $null; Text = $null })
    $newItems.Add([pscustomobject]@{ Template = $paragraphs.Item(9); RunPr = $salutationRunPr; Text = $Letter.Salutation })
    $newItems.Add([pscustomobject]@{ Template = $paragraphs.Item(10); RunPr = $null; Text = $null })

    $bodyTemplate = $paragraphs.Item(11)
    $blankTemplate = $paragraphs.Item(12)
    foreach ($paragraph in $Letter.Body) {
        $newItems.Add([pscustomobject]@{ Template = $bodyTemplate; RunPr = $bodyRunPr; Text = $paragraph })
        $newItems.Add([pscustomobject]@{ Template = $blankTemplate; RunPr = $null; Text = $null })
    }

    $newItems.Add([pscustomobject]@{ Template = $paragraphs.Item(19); RunPr = $closingRunPr; Text = $Letter.Closing })
    $newItems.Add([pscustomobject]@{ Template = $paragraphs.Item(20); RunPr = $null; Text = $null })
    $newItems.Add([pscustomobject]@{ Template = $paragraphs.Item(21); RunPr = $null; Text = $null })
    $newItems.Add([pscustomobject]@{ Template = $paragraphs.Item(22); RunPr = $null; Text = $null })
    $newItems.Add([pscustomobject]@{ Template = $paragraphs.Item(25); RunPr = $nameRunPr; Text = $Letter.Signature })

    for ($i = $paragraphs.Count - 1; $i -ge 8; $i--) {
        [void]$body.RemoveChild($paragraphs.Item($i))
    }

    $insertBefore = $body.SelectSingleNode('./w:sectPr', $script:Ns)
    foreach ($item in $newItems) {
        $newParagraph = New-Paragraph $doc $item.Template $item.RunPr $item.Text
        [void]$body.InsertBefore($newParagraph, $insertBefore)
    }

    Write-DocXml $DocxPath $doc
}

function Convert-DocxToPdf([string]$DocxPath, [string]$PdfPath) {
    $word = $null
    $document = $null
    try {
        $word = New-Object -ComObject Word.Application
        $word.Visible = $false
        $word.DisplayAlerts = 0
        $document = $word.Documents.Open($DocxPath, $false, $false)
        $document.Activate() | Out-Null
        $document.Repaginate()
        $document.ExportAsFixedFormat(
            $PdfPath,
            17,
            $false,
            0,
            0,
            1,
            1,
            0,
            $true,
            $true,
            0,
            $true,
            $true,
            $true
        )
        return $true
    }
    catch {
        Write-Verbose "Word PDF export failed: $($_.Exception.Message)"
    }
    finally {
        if ($null -ne $document) {
            $document.Close($false) | Out-Null
        }
        if ($null -ne $word) {
            $word.Quit() | Out-Null
            [void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($word)
        }
    }

    $office = Get-Command soffice -ErrorAction SilentlyContinue
    if ($null -eq $office) {
        $office = Get-Command libreoffice -ErrorAction SilentlyContinue
    }
    if ($null -eq $office) {
        return $false
    }

    $pdfDir = Split-Path -Parent $PdfPath
    $process = Start-Process -FilePath $office.Source `
        -ArgumentList @('--headless', '--convert-to', 'pdf', '--outdir', $pdfDir, $DocxPath) `
        -WindowStyle Hidden -Wait -PassThru

    if ($process.ExitCode -ne 0) {
        return $false
    }

    $generatedPdf = Join-Path $pdfDir (([System.IO.Path]::GetFileNameWithoutExtension($DocxPath)) + '.pdf')
    if ((Resolve-FullPath $generatedPdf) -ne (Resolve-FullPath $PdfPath)) {
        Move-Item -LiteralPath $generatedPdf -Destination $PdfPath -Force
    }
    Test-Path -LiteralPath $PdfPath
}

$inputFullPath = Resolve-FullPath $InputPath
$templateFullPath = Resolve-FullPath $TemplatePath
$outputFullDir = Resolve-FullPath $OutputDir

if (-not (Test-Path -LiteralPath $inputFullPath)) {
    throw "Input file not found: $inputFullPath"
}
if (-not (Test-Path -LiteralPath $templateFullPath)) {
    throw "Template file not found: $templateFullPath"
}
if (-not (Test-Path -LiteralPath $outputFullDir)) {
    New-Item -ItemType Directory -Path $outputFullDir | Out-Null
}
if (-not $OutputName.EndsWith('.docx', [System.StringComparison]::OrdinalIgnoreCase)) {
    $OutputName = "$OutputName.docx"
}
if ([string]::IsNullOrWhiteSpace($DateText)) {
    $DateText = Get-GermanDateText
}

$outputDocx = Join-Path $outputFullDir $OutputName
$outputPdf = [System.IO.Path]::ChangeExtension($outputDocx, '.pdf')

$letter = Read-Letter $inputFullPath
Copy-Item -LiteralPath $templateFullPath -Destination $outputDocx -Force
Update-DocxLetter -DocxPath $outputDocx -Letter $letter -RecipientText $Recipient -CityText $City -LetterDate $DateText

Write-Output "DOCX: $outputDocx"

if (-not $NoPdf) {
    $converted = Convert-DocxToPdf -DocxPath (Resolve-FullPath $outputDocx) -PdfPath (Resolve-FullPath $outputPdf)
    if ($converted) {
        Write-Output "PDF:  $outputPdf"
    }
    else {
        Write-Warning "DOCX generated, but PDF export failed. Install Microsoft Word or LibreOffice, or run the script on a machine that has one of them."
    }
}
