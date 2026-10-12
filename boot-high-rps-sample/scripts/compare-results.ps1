param(
    [Parameter(Mandatory = $true)]
    [string]$BaselineStats,
    
    [Parameter(Mandatory = $true)]
    [string]$HighRpsStats,
    
    [Parameter(Mandatory = $false)]
    [string]$BaselineDiagnostics = "",
    
    [Parameter(Mandatory = $false)]
    [string]$HighRpsDiagnostics = ""
)

if (-not (Test-Path $BaselineStats)) {
    Write-Error "Baseline stats file not found: $BaselineStats"
    exit 1
}

if (-not (Test-Path $HighRpsStats)) {
    Write-Error "High RPS stats file not found: $HighRpsStats"
    exit 1
}

$baseline = Get-Content $BaselineStats | ConvertFrom-Json
$highRps = Get-Content $HighRpsStats | ConvertFrom-Json

function Get-Stat($statsObj, [string]$path) {
    if ($null -eq $statsObj) { return "N/A" }
    $curr = $statsObj
    foreach ($part in $path.Split(".")) {
        if ($null -ne $curr -and $null -ne $curr.$part) {
            $curr = $curr.$part
        } else {
            return "N/A"
        }
    }
    if ($null -eq $curr) { return "N/A" }
    return $curr
}

Write-Host "## Performance Comparison"
Write-Host ""
Write-Host "### Per-Endpoint Statistics"
Write-Host ""

$endpoints = @("author_register", "post_create", "post_read", "comment_create", "comment_read", "tag_read", "post_update", "post_delete", "comment_update", "comment_delete")

foreach ($endpoint in $endpoints) {
    $bEp = if ($null -ne $baseline.contents) { $baseline.contents.$endpoint } else { $null }
    $hEp = if ($null -ne $highRps.contents) { $highRps.contents.$endpoint } else { $null }
    
    if ($null -eq $bEp -and $null -eq $hEp) { continue }
    
    Write-Host "#### Endpoint: $endpoint"
    Write-Host "| Metric | Baseline | High RPS | Delta |"
    Write-Host "|---|---|---|---|"
    
    $bStats = if ($null -ne $bEp) { $bEp.stats } else { $null }
    $hStats = if ($null -ne $hEp) { $hEp.stats } else { $null }
    
    $bRps = Get-Stat $bStats "meanNumberOfRequestsPerSecond.total"
    $hRps = Get-Stat $hStats "meanNumberOfRequestsPerSecond.total"
    $rpsDelta = if ($bRps -ne "N/A" -and $hRps -ne "N/A") { [math]::Round($hRps - $bRps, 2) } else { "N/A" }
    
    $b50 = Get-Stat $bStats "percentiles1.total"
    $h50 = Get-Stat $hStats "percentiles1.total"
    $d50 = if ($b50 -ne "N/A" -and $h50 -ne "N/A") { $h50 - $b50 } else { "N/A" }

    $b75 = Get-Stat $bStats "percentiles2.total"
    $h75 = Get-Stat $hStats "percentiles2.total"
    $d75 = if ($b75 -ne "N/A" -and $h75 -ne "N/A") { $h75 - $b75 } else { "N/A" }

    $b95 = Get-Stat $bStats "percentiles3.total"
    $h95 = Get-Stat $hStats "percentiles3.total"
    $d95 = if ($b95 -ne "N/A" -and $h95 -ne "N/A") { $h95 - $b95 } else { "N/A" }
    
    $b99 = Get-Stat $bStats "percentiles4.total"
    $h99 = Get-Stat $hStats "percentiles4.total"
    $d99 = if ($b99 -ne "N/A" -and $h99 -ne "N/A") { $h99 - $b99 } else { "N/A" }

    $bMax = Get-Stat $bStats "maxResponseTime.total"
    $hMax = Get-Stat $hStats "maxResponseTime.total"
    $dMax = if ($bMax -ne "N/A" -and $hMax -ne "N/A") { $hMax - $bMax } else { "N/A" }

    $bOk = Get-Stat $bStats "numberOfRequests.ok"
    $hOk = Get-Stat $hStats "numberOfRequests.ok"
    $bKo = Get-Stat $bStats "numberOfRequests.ko"
    $hKo = Get-Stat $hStats "numberOfRequests.ko"

    $bTotal = Get-Stat $bStats "numberOfRequests.total"
    $hTotal = Get-Stat $hStats "numberOfRequests.total"
    
    $bErrRate = if ($bTotal -ne "N/A" -and $bTotal -gt 0) { [math]::Round(($bKo / $bTotal) * 100, 2) } else { "N/A" }
    $hErrRate = if ($hTotal -ne "N/A" -and $hTotal -gt 0) { [math]::Round(($hKo / $hTotal) * 100, 2) } else { "N/A" }
    $errRateDelta = if ($bErrRate -ne "N/A" -and $hErrRate -ne "N/A") { [math]::Round($hErrRate - $bErrRate, 2) } else { "N/A" }

    $dOk = if ($bOk -ne "N/A" -and $hOk -ne "N/A") { $hOk - $bOk } else { "N/A" }
    $dKo = if ($bKo -ne "N/A" -and $hKo -ne "N/A") { $hKo - $bKo } else { "N/A" }

    Write-Host "| Throughput (RPS) | $bRps | $hRps | $rpsDelta |"
    Write-Host "| Response Time p50 (ms) | $b50 | $h50 | $d50 |"
    Write-Host "| Response Time p75 (ms) | $b75 | $h75 | $d75 |"
    Write-Host "| Response Time p95 (ms) | $b95 | $h95 | $d95 |"
    Write-Host "| Response Time p99 (ms) | $b99 | $h99 | $d99 |"
    Write-Host "| Max Response Time (ms) | $bMax | $hMax | $dMax |"
    Write-Host "| Successful Requests | $bOk | $hOk | $dOk |"
    Write-Host "| Failed Requests | $bKo | $hKo | $dKo |"
    Write-Host "| Error Rate (%) | $bErrRate | $hErrRate | $errRateDelta |"
    Write-Host ""
}

Write-Host "### Global (Aggregate) Statistics (Complementary)"
Write-Host ""
Write-Host "| Metric | Baseline | High RPS | Delta |"
Write-Host "|---|---|---|---|"
$gbStats = $baseline.stats
$ghStats = $highRps.stats
$gbRps = Get-Stat $gbStats "meanNumberOfRequestsPerSecond.total"
$ghRps = Get-Stat $ghStats "meanNumberOfRequestsPerSecond.total"
$gRpsDelta = if ($gbRps -ne "N/A" -and $ghRps -ne "N/A") { [math]::Round($ghRps - $gbRps, 2) } else { "N/A" }
$gbTotal = Get-Stat $gbStats "numberOfRequests.total"
$ghTotal = Get-Stat $ghStats "numberOfRequests.total"
$gbKo = Get-Stat $gbStats "numberOfRequests.ko"
$ghKo = Get-Stat $ghStats "numberOfRequests.ko"
$gbErrRate = if ($gbTotal -ne "N/A" -and $gbTotal -gt 0) { [math]::Round(($gbKo / $gbTotal) * 100, 2) } else { "N/A" }
$ghErrRate = if ($ghTotal -ne "N/A" -and $ghTotal -gt 0) { [math]::Round(($ghKo / $ghTotal) * 100, 2) } else { "N/A" }
$gErrRateDelta = if ($gbErrRate -ne "N/A" -and $ghErrRate -ne "N/A") { [math]::Round($ghErrRate - $gbErrRate, 2) } else { "N/A" }
$gb50 = Get-Stat $gbStats "percentiles1.total"
$gh50 = Get-Stat $ghStats "percentiles1.total"
$gd50 = if ($gb50 -ne "N/A" -and $gh50 -ne "N/A") { $gh50 - $gb50 } else { "N/A" }
$gb75 = Get-Stat $gbStats "percentiles2.total"
$gh75 = Get-Stat $ghStats "percentiles2.total"
$gd75 = if ($gb75 -ne "N/A" -and $gh75 -ne "N/A") { $gh75 - $gb75 } else { "N/A" }
$gb95 = Get-Stat $gbStats "percentiles3.total"
$gh95 = Get-Stat $ghStats "percentiles3.total"
$gd95 = if ($gb95 -ne "N/A" -and $gh95 -ne "N/A") { $gh95 - $gb95 } else { "N/A" }
$gb99 = Get-Stat $gbStats "percentiles4.total"
$gh99 = Get-Stat $ghStats "percentiles4.total"
$gd99 = if ($gb99 -ne "N/A" -and $gh99 -ne "N/A") { $gh99 - $gb99 } else { "N/A" }
$gbMax = Get-Stat $gbStats "maxResponseTime.total"
$ghMax = Get-Stat $ghStats "maxResponseTime.total"
$gdMax = if ($gbMax -ne "N/A" -and $ghMax -ne "N/A") { $ghMax - $gbMax } else { "N/A" }

Write-Host "| Throughput (RPS) | $gbRps | $ghRps | $gRpsDelta |"
Write-Host "| Error Rate (%) | $gbErrRate | $ghErrRate | $gErrRateDelta |"
Write-Host "| p50 (ms) | $gb50 | $gh50 | $gd50 |"
Write-Host "| p75 (ms) | $gb75 | $gh75 | $gd75 |"
Write-Host "| p95 (ms) | $gb95 | $gh95 | $gd95 |"
Write-Host "| p99 (ms) | $gb99 | $gh99 | $gd99 |"
Write-Host "| Max (ms) | $gbMax | $ghMax | $gdMax |"
Write-Host ""
Write-Host "### Diagnostics Comparison"
Write-Host ""
if ($BaselineDiagnostics -ne "" -and $HighRpsDiagnostics -ne "" -and (Test-Path $BaselineDiagnostics) -and (Test-Path $HighRpsDiagnostics)) {
    $bDiag = Get-Content $BaselineDiagnostics | ConvertFrom-Json
    $hDiag = Get-Content $HighRpsDiagnostics | ConvertFrom-Json
    Write-Host "| Metric | Baseline | High RPS | Delta |"
    Write-Host "|---|---|---|---|"
    
    $diagMetrics = @("cpu", "memory", "gc_pause", "allocation_rate", "thread_count", "db_connections", "db_pool_utilization", "query_latency", "host_cpu", "host_memory")
    foreach ($metric in $diagMetrics) {
        $bVal = if ($null -ne $bDiag.$metric) { $bDiag.$metric } else { "N/A" }
        $hVal = if ($null -ne $hDiag.$metric) { $hDiag.$metric } else { "N/A" }
        $delta = if ($bVal -ne "N/A" -and $hVal -ne "N/A") { [math]::Round($hVal - $bVal, 2) } else { "N/A" }
        Write-Host "| $metric | $bVal | $hVal | $delta |"
    }
} else {
    Write-Host "_Diagnostics were not provided for comparison._"
}
