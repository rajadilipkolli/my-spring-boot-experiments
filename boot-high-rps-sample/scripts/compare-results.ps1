param(
    [Parameter(Mandatory=$true)]
    [string]$BaselineStats,
    
    [Parameter(Mandatory=$true)]
    [string]$HighRpsStats
)

if (-not (Test-Path $BaselineStats) -or -not (Test-Path $HighRpsStats)) {
    Write-Error "Both stats files must exist."
    exit 1
}

$baseline = Get-Content $BaselineStats | ConvertFrom-Json
$highRps = Get-Content $HighRpsStats | ConvertFrom-Json

$bStats = $baseline.stats
$hStats = $highRps.stats

$bReqs = $bStats.numberOfRequests.total
$hReqs = $hStats.numberOfRequests.total
$bOk = $bStats.numberOfRequests.ok
$hOk = $hStats.numberOfRequests.ok
$bKo = $bStats.numberOfRequests.ko
$hKo = $hStats.numberOfRequests.ko

$bErrRate = [math]::Round(($bKo / $bReqs) * 100, 2)
$hErrRate = [math]::Round(($hKo / $hReqs) * 100, 2)

$bMeanRps = $bStats.meanNumberOfRequestsPerSecond.total
$hMeanRps = $hStats.meanNumberOfRequestsPerSecond.total

# Percentiles
$b50 = $bStats.percentiles1.total
$h50 = $hStats.percentiles1.total
$b75 = $bStats.percentiles2.total
$h75 = $hStats.percentiles2.total
$b95 = $bStats.percentiles3.total
$h95 = $hStats.percentiles3.total
$b99 = $bStats.percentiles4.total
$h99 = $hStats.percentiles4.total

$bMax = $bStats.maxResponseTime.total
$hMax = $hStats.maxResponseTime.total

Write-Host "## Performance Comparison"
Write-Host ""
Write-Host "| Metric | Baseline | High RPS | Delta |"
Write-Host "|---|---|---|---|"
Write-Host "| Throughput (RPS) | $bMeanRps | $hMeanRps | $([math]::Round($hMeanRps - $bMeanRps, 2)) |"
Write-Host "| Error Rate (%) | $bErrRate | $hErrRate | $([math]::Round($hErrRate - $bErrRate, 2)) |"
Write-Host "| p50 (ms) | $b50 | $h50 | $($h50 - $b50) |"
Write-Host "| p75 (ms) | $b75 | $h75 | $($h75 - $b75) |"
Write-Host "| p95 (ms) | $b95 | $h95 | $($h95 - $b95) |"
Write-Host "| p99 (ms) | $b99 | $h99 | $($h99 - $b99) |"
Write-Host "| Max (ms) | $bMax | $hMax | $($hMax - $bMax) |"
Write-Host ""
Write-Host "### HTTP Status / Timeouts"
Write-Host "- Baseline: $bOk OK, $bKo KO"
Write-Host "- High RPS: $hOk OK, $hKo KO"
