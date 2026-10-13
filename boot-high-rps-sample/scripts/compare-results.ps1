param(
    [Parameter(Mandatory=$True)]
    [string]$BaselineStats,
    
    [Parameter(Mandatory=$True)]
    [string]$NewStats,
    
    [Parameter(Mandatory=$False)]
    [string]$BaselineDiagnostics = "",
    
    [Parameter(Mandatory=$False)]
    [string]$NewDiagnostics = ""
)

if (-not (Test-Path $BaselineStats) -or -not (Test-Path $NewStats)) {
    Write-Error "Both stats files must exist."
    exit 1
}

$base = Get-Content $BaselineStats | ConvertFrom-Json
$new = Get-Content $NewStats | ConvertFrom-Json

Write-Output "## Performance Comparison"
Write-Output ""
Write-Output "### Per-Endpoint Statistics"
Write-Output ""

$endpoints = @("author_register", "post_create", "post_read", "comment_create", "comment_read", "tag_read", "post_update", "post_delete", "Verify Deleted Post", "comment_update", "comment_delete", "Verify Deleted Comment")

function Get-Stat($statsObj, $path) {
    if ($null -eq $statsObj) { return "N/A" }
    $val = $statsObj
    foreach ($p in $path.Split(".")) {
        if ($null -ne $val) { $val = $val.$p }
    }
    if ($null -eq $val) { return "N/A" }
    return $val
}

foreach ($endpoint in $endpoints) {
    $bEnd = $base.contents.$endpoint
    $nEnd = $new.contents.$endpoint
    
    if ($null -eq $bEnd -and $null -eq $nEnd) { continue }
    
    Write-Output "#### Endpoint: $endpoint"
    Write-Output "| Metric | Baseline | High RPS | Delta |"
    Write-Output "|---|---|---|---|"
    
    if ($null -ne $bEnd) { $bStats = $bEnd.stats } else { $bStats = $null }
    if ($null -ne $nEnd) { $nStats = $nEnd.stats } else { $nStats = $null }

    $bRps = Get-Stat $bStats "meanNumberOfRequestsPerSecond.total"
    $nRps = Get-Stat $nStats "meanNumberOfRequestsPerSecond.total"
    $dRps = if ($bRps -ne "N/A" -and $nRps -ne "N/A") { [math]::Round($nRps - $bRps, 2) } else { "N/A" }
    
    $bP50 = Get-Stat $bStats "percentiles1.total"
    $nP50 = Get-Stat $nStats "percentiles1.total"
    $dP50 = if ($bP50 -ne "N/A" -and $nP50 -ne "N/A") { $nP50 - $bP50 } else { "N/A" }

    $bP75 = Get-Stat $bStats "percentiles2.total"
    $nP75 = Get-Stat $nStats "percentiles2.total"
    $dP75 = if ($bP75 -ne "N/A" -and $nP75 -ne "N/A") { $nP75 - $bP75 } else { "N/A" }

    $bP95 = Get-Stat $bStats "percentiles3.total"
    $nP95 = Get-Stat $nStats "percentiles3.total"
    $dP95 = if ($bP95 -ne "N/A" -and $nP95 -ne "N/A") { $nP95 - $bP95 } else { "N/A" }
    
    $bP99 = Get-Stat $bStats "percentiles4.total"
    $nP99 = Get-Stat $nStats "percentiles4.total"
    $dP99 = if ($bP99 -ne "N/A" -and $nP99 -ne "N/A") { $nP99 - $bP99 } else { "N/A" }

    $bMax = Get-Stat $bStats "maxResponseTime.total"
    $nMax = Get-Stat $nStats "maxResponseTime.total"
    $dMax = if ($bMax -ne "N/A" -and $nMax -ne "N/A") { $nMax - $bMax } else { "N/A" }

    $bOk = Get-Stat $bStats "numberOfRequests.ok"
    $nOk = Get-Stat $nStats "numberOfRequests.ok"
    $bKo = Get-Stat $bStats "numberOfRequests.ko"
    $nKo = Get-Stat $nStats "numberOfRequests.ko"

    $bTotal = Get-Stat $bStats "numberOfRequests.total"
    $nTotal = Get-Stat $nStats "numberOfRequests.total"
    
    $bErrRate = if ($bKo -ne "N/A" -and $bTotal -gt 0) { [math]::Round(($bKo / $bTotal) * 100, 2) } else { "N/A" }
    $nErrRate = if ($nKo -ne "N/A" -and $nTotal -gt 0) { [math]::Round(($nKo / $nTotal) * 100, 2) } else { "N/A" }
    $dErrRate = if ($bErrRate -ne "N/A" -and $nErrRate -ne "N/A") { [math]::Round($nErrRate - $bErrRate, 2) } else { "N/A" }

    Write-Output "| Throughput (RPS) | $bRps | $nRps | $dRps |"
    Write-Output "| Response Time p50 (ms) | $bP50 | $nP50 | $dP50 |"
    Write-Output "| Response Time p75 (ms) | $bP75 | $nP75 | $dP75 |"
    Write-Output "| Response Time p95 (ms) | $bP95 | $nP95 | $dP95 |"
    Write-Output "| Response Time p99 (ms) | $bP99 | $nP99 | $dP99 |"
    Write-Output "| Max Response Time (ms) | $bMax | $nMax | $dMax |"
    Write-Output "| Successful Requests | $bOk | $nOk | - |"
    Write-Output "| Failed Requests | $bKo | $nKo | - |"
    Write-Output "| Error Rate (%) | $bErrRate | $nErrRate | $dErrRate |"
    Write-Output ""
}

Write-Output "### Global (Aggregate) Statistics (Complementary)"
Write-Output ""
Write-Output "| Metric | Baseline | High RPS | Delta |"
Write-Output "|---|---|---|---|"
$bGlob = $base.stats
$nGlob = $new.stats
$bRpsG = Get-Stat $bGlob "meanNumberOfRequestsPerSecond.total"
$nRpsG = Get-Stat $nGlob "meanNumberOfRequestsPerSecond.total"
$dRpsG = if ($bRpsG -ne "N/A" -and $nRpsG -ne "N/A") { [math]::Round($nRpsG - $bRpsG, 2) } else { "N/A" }
$bTotalG = Get-Stat $bGlob "numberOfRequests.total"
$nTotalG = Get-Stat $nGlob "numberOfRequests.total"
$bKoG = Get-Stat $bGlob "numberOfRequests.ko"
$nKoG = Get-Stat $nGlob "numberOfRequests.ko"
$bErrRateG = if ($bKoG -ne "N/A" -and $bTotalG -gt 0) { [math]::Round(($bKoG / $bTotalG) * 100, 2) } else { "N/A" }
$nErrRateG = if ($nKoG -ne "N/A" -and $nTotalG -gt 0) { [math]::Round(($nKoG / $nTotalG) * 100, 2) } else { "N/A" }
$dErrRateG = if ($bErrRateG -ne "N/A" -and $nErrRateG -ne "N/A") { [math]::Round($nErrRateG - $bErrRateG, 2) } else { "N/A" }
$bP50G = Get-Stat $bGlob "percentiles1.total"
$nP50G = Get-Stat $nGlob "percentiles1.total"
$bP75G = Get-Stat $bGlob "percentiles2.total"
$nP75G = Get-Stat $nGlob "percentiles2.total"
$bP95G = Get-Stat $bGlob "percentiles3.total"
$nP95G = Get-Stat $nGlob "percentiles3.total"
$bP99G = Get-Stat $bGlob "percentiles4.total"
$nP99G = Get-Stat $nGlob "percentiles4.total"
$bMaxG = Get-Stat $bGlob "maxResponseTime.total"
$nMaxG = Get-Stat $nGlob "maxResponseTime.total"
Write-Output "| Throughput (RPS) | $bRpsG | $nRpsG | $dRpsG |"
Write-Output "| Error Rate (%) | $bErrRateG | $nErrRateG | $dErrRateG |"
Write-Output "| p50 (ms) | $bP50G | $nP50G | $(if ($bP50G -ne 'N/A' -and $nP50G -ne 'N/A') {$nP50G - $bP50G} else {'N/A'}) |"
Write-Output "| p75 (ms) | $bP75G | $nP75G | $(if ($bP75G -ne 'N/A' -and $nP75G -ne 'N/A') {$nP75G - $bP75G} else {'N/A'}) |"
Write-Output "| p95 (ms) | $bP95G | $nP95G | $(if ($bP95G -ne 'N/A' -and $nP95G -ne 'N/A') {$nP95G - $bP95G} else {'N/A'}) |"
Write-Output "| p99 (ms) | $bP99G | $nP99G | $(if ($bP99G -ne 'N/A' -and $nP99G -ne 'N/A') {$nP99G - $bP99G} else {'N/A'}) |"
Write-Output "| Max (ms) | $bMaxG | $nMaxG | $(if ($bMaxG -ne 'N/A' -and $nMaxG -ne 'N/A') {$nMaxG - $bMaxG} else {'N/A'}) |"
Write-Output ""
Write-Output "### Diagnostics Comparison"
Write-Output ""
if ($BaselineDiagnostics -ne "" -and $NewDiagnostics -ne "" -and (Test-Path $BaselineDiagnostics) -and (Test-Path $NewDiagnostics)) {
    $bDiag = Get-Content $BaselineDiagnostics | ConvertFrom-Json
    $nDiag = Get-Content $NewDiagnostics | ConvertFrom-Json
    Write-Output "| Metric | Baseline | High RPS | Delta |"
    Write-Output "|---|---|---|---|"
    
    $metrics = @("cpu", "memory", "gc_pause", "allocation_rate", "thread_count", "db_connections", "db_pool_utilization", "query_latency", "host_cpu", "host_memory")
    foreach ($metric in $metrics) {
        $bM = if ($null -ne $bDiag.$metric) { $bDiag.$metric } else { "N/A" }
        $nM = if ($null -ne $nDiag.$metric) { $nDiag.$metric } else { "N/A" }
        $dM = if ($bM -ne "N/A" -and $nM -ne "N/A") { [math]::Round($nM - $bM, 2) } else { "N/A" }
        Write-Output "| $metric | $bM | $nM | $dM |"
    }
} else {
    Write-Output "_Diagnostics were not provided for comparison._"
}
