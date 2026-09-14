param(
    [Parameter(Mandatory=True)]
    [string],
    
    [Parameter(Mandatory=True)]
    [string],
    
    [Parameter(Mandatory=False)]
    [string] = "",
    
    [Parameter(Mandatory=False)]
    [string] = ""
)

if (-not (Test-Path ) -or -not (Test-Path )) {
    Write-Error "Both stats files must exist."
    exit 1
}

 = Get-Content  | ConvertFrom-Json
 = Get-Content  | ConvertFrom-Json

Write-Host "## Performance Comparison"
Write-Host ""
Write-Host "### Per-Endpoint Statistics"
Write-Host ""

 = @("author_register", "post_create", "post_read", "comment_create", "comment_read", "tag_read", "post_update", "post_delete", "comment_update", "comment_delete")

foreach ( in ) {
     = .contents.
     = .contents.
    
    if ( -eq  -and  -eq ) { continue }
    
    Write-Host "#### Endpoint: "
    Write-Host "| Metric | Baseline | High RPS | Delta |"
    Write-Host "|---|---|---|---|"
    
    if ( -ne ) {  = .stats } else {  =  }
    if ( -ne ) {  = .stats } else {  =  }

    function Get-Stat(, ) {
        if ( -eq ) { return "N/A" }
         = 
        foreach ( in .Split(".")) {
            if ( -ne ) {  = . }
        }
        if ( -eq ) { return "N/A" }
        return 
    }
    
     = Get-Stat  "meanNumberOfRequestsPerSecond.total"
     = Get-Stat  "meanNumberOfRequestsPerSecond.total"
     = if ( -ne "N/A" -and  -ne "N/A") { [math]::Round( - , 2) } else { "N/A" }
    
     = Get-Stat  "percentiles1.total"
     = Get-Stat  "percentiles1.total"
     = if ( -ne "N/A" -and  -ne "N/A") {  -  } else { "N/A" }

     = Get-Stat  "percentiles2.total"
     = Get-Stat  "percentiles2.total"
     = if ( -ne "N/A" -and  -ne "N/A") {  -  } else { "N/A" }

     = Get-Stat  "percentiles3.total"
     = Get-Stat  "percentiles3.total"
     = if ( -ne "N/A" -and  -ne "N/A") {  -  } else { "N/A" }
    
     = Get-Stat  "percentiles4.total"
     = Get-Stat  "percentiles4.total"
     = if ( -ne "N/A" -and  -ne "N/A") {  -  } else { "N/A" }

     = Get-Stat  "maxResponseTime.total"
     = Get-Stat  "maxResponseTime.total"
     = if ( -ne "N/A" -and  -ne "N/A") {  -  } else { "N/A" }

     = Get-Stat  "numberOfRequests.ok"
     = Get-Stat  "numberOfRequests.ok"
     = Get-Stat  "numberOfRequests.ko"
     = Get-Stat  "numberOfRequests.ko"

     = Get-Stat  "numberOfRequests.total"
     = Get-Stat  "numberOfRequests.total"
    
     = if ( -ne "N/A" -and  -gt 0) { [math]::Round(( / ) * 100, 2) } else { "N/A" }
     = if ( -ne "N/A" -and  -gt 0) { [math]::Round(( / ) * 100, 2) } else { "N/A" }
     = if ( -ne "N/A" -and  -ne "N/A") { [math]::Round( - , 2) } else { "N/A" }

    Write-Host "| Throughput (RPS) |  |  |  |"
    Write-Host "| Response Time p50 (ms) |  |  |  |"
    Write-Host "| Response Time p75 (ms) |  |  |  |"
    Write-Host "| Response Time p95 (ms) |  |  |  |"
    Write-Host "| Response Time p99 (ms) |  |  |  |"
    Write-Host "| Max Response Time (ms) |  |  |  |"
    Write-Host "| Successful Requests |  |  | 0 |"
    Write-Host "| Failed Requests |  |  | 0 |"
    Write-Host "| Error Rate (%) |  |  |  |"
    Write-Host ""
}

Write-Host "### Global (Aggregate) Statistics (Complementary)"
Write-Host ""
Write-Host "| Metric | Baseline | High RPS | Delta |"
Write-Host "|---|---|---|---|"
 = .stats
 = .stats
 = Get-Stat  "meanNumberOfRequestsPerSecond.total"
 = Get-Stat  "meanNumberOfRequestsPerSecond.total"
 = if ( -ne "N/A" -and  -ne "N/A") { [math]::Round( - , 2) } else { "N/A" }
 = Get-Stat  "numberOfRequests.total"
 = Get-Stat  "numberOfRequests.total"
 = Get-Stat  "numberOfRequests.ko"
 = Get-Stat  "numberOfRequests.ko"
 = if ( -ne "N/A" -and  -gt 0) { [math]::Round(( / ) * 100, 2) } else { "N/A" }
 = if ( -ne "N/A" -and  -gt 0) { [math]::Round(( / ) * 100, 2) } else { "N/A" }
 = if ( -ne "N/A" -and  -ne "N/A") { [math]::Round( - , 2) } else { "N/A" }
 = Get-Stat  "percentiles1.total"
 = Get-Stat  "percentiles1.total"
 = Get-Stat  "percentiles2.total"
 = Get-Stat  "percentiles2.total"
 = Get-Stat  "percentiles3.total"
 = Get-Stat  "percentiles3.total"
 = Get-Stat  "percentiles4.total"
 = Get-Stat  "percentiles4.total"
 = Get-Stat  "maxResponseTime.total"
 = Get-Stat  "maxResponseTime.total"
Write-Host "| Throughput (RPS) |  |  |  |"
Write-Host "| Error Rate (%) |  |  |  |"
Write-Host "| p50 (ms) |  |  | 0 |"
Write-Host "| p75 (ms) |  |  | 0 |"
Write-Host "| p95 (ms) |  |  | 0 |"
Write-Host "| p99 (ms) |  |  | 0 |"
Write-Host "| Max (ms) |  |  | 0 |"
Write-Host ""
Write-Host "### Diagnostics Comparison"
Write-Host ""
if ( -ne "" -and  -ne "" -and (Test-Path ) -and (Test-Path )) {
     = Get-Content  | ConvertFrom-Json
     = Get-Content  | ConvertFrom-Json
    Write-Host "| Metric | Baseline | High RPS | Delta |"
    Write-Host "|---|---|---|---|"
    
     = @("cpu", "memory", "gc_pause", "allocation_rate", "thread_count", "db_connections", "db_pool_utilization", "query_latency", "host_cpu", "host_memory")
    foreach ( in ) {
         = if ( -ne .) { . } else { "N/A" }
         = if ( -ne .) { . } else { "N/A" }
         = if ( -ne "N/A" -and  -ne "N/A") { [math]::Round( - , 2) } else { "N/A" }
        Write-Host "|  |  |  |  |"
    }
} else {
    Write-Host "_Diagnostics were not provided for comparison._"
}
