param(
    [int]$Port = 5500
)

$root = Join-Path $PSScriptRoot "src\main\resources\static"
$resolvedRoot = (Resolve-Path $root).Path
$listener = New-Object System.Net.HttpListener
$prefix = "http://localhost:$Port/"
$listener.Prefixes.Add($prefix)

function Get-ContentType {
    param([string]$Path)

    switch ([System.IO.Path]::GetExtension($Path).ToLowerInvariant()) {
        ".html" { return "text/html; charset=utf-8" }
        ".css"  { return "text/css; charset=utf-8" }
        ".js"   { return "application/javascript; charset=utf-8" }
        ".json" { return "application/json; charset=utf-8" }
        ".png"  { return "image/png" }
        ".jpg"  { return "image/jpeg" }
        ".jpeg" { return "image/jpeg" }
        ".gif"  { return "image/gif" }
        ".svg"  { return "image/svg+xml" }
        ".ico"  { return "image/x-icon" }
        default { return "application/octet-stream" }
    }
}

function Write-Response {
    param(
        [System.Net.HttpListenerResponse]$Response,
        [int]$StatusCode,
        [string]$ContentType,
        [byte[]]$Body
    )

    $Response.StatusCode = $StatusCode
    $Response.ContentType = $ContentType
    $Response.ContentLength64 = $Body.Length
    $Response.OutputStream.Write($Body, 0, $Body.Length)
    $Response.OutputStream.Close()
}

try {
    $listener.Start()
    Write-Host "Frontend server running at $prefix"
    Write-Host "Serving files from $resolvedRoot"
    Write-Host "Backend API expected at http://localhost:8080"

    while ($listener.IsListening) {
        $context = $listener.GetContext()
        $response = $context.Response

        try {
            $relativePath = [System.Uri]::UnescapeDataString($context.Request.Url.AbsolutePath.TrimStart("/"))
            if ([string]::IsNullOrWhiteSpace($relativePath)) {
                $relativePath = "index.html"
            }

            $normalizedPath = $relativePath.Replace("/", "\")
            $candidate = Join-Path $resolvedRoot $normalizedPath

            if (Test-Path $candidate -PathType Container) {
                $candidate = Join-Path $candidate "index.html"
            }

            if (-not (Test-Path $candidate -PathType Leaf)) {
                $body = [System.Text.Encoding]::UTF8.GetBytes("Not Found")
                Write-Response -Response $response -StatusCode 404 -ContentType "text/plain; charset=utf-8" -Body $body
                continue
            }

            $resolvedFile = (Resolve-Path $candidate).Path
            if (-not $resolvedFile.StartsWith($resolvedRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
                $body = [System.Text.Encoding]::UTF8.GetBytes("Forbidden")
                Write-Response -Response $response -StatusCode 403 -ContentType "text/plain; charset=utf-8" -Body $body
                continue
            }

            $bytes = [System.IO.File]::ReadAllBytes($resolvedFile)
            $contentType = Get-ContentType -Path $resolvedFile
            Write-Response -Response $response -StatusCode 200 -ContentType $contentType -Body $bytes
        }
        catch {
            $body = [System.Text.Encoding]::UTF8.GetBytes("Internal Server Error")
            Write-Response -Response $response -StatusCode 500 -ContentType "text/plain; charset=utf-8" -Body $body
        }
    }
}
finally {
    if ($listener.IsListening) {
        $listener.Stop()
    }
    $listener.Close()
}
