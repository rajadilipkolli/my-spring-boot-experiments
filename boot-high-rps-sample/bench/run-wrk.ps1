param(
    [string]$Url = 'http://localhost:8080/ping'
)
Write-Host "Running wrk against $Url"
wrk.exe -t4 -c400 -d30s --latency $Url
