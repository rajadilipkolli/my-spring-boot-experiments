param(
    [string]$Profile = "smoke",
    [int]$DurationMinutes = 1,
    [int]$WarmupMinutes = 1,
    [double]$TargetRps = 0
)

$DataDirectory = "target/loadtest-data"
$CompletionMarker = Join-Path $DataDirectory ".complete"
$RequiredFeederFiles = @{
    "authors.csv" = "email"
    "tags.csv" = "tag"
    "posts.csv" = "postId,weight"
    "post_tags.csv" = "tag,postId"
    "mutable_posts.csv" = "postId,authorEmail"
    "deletable_posts.csv" = "postId,authorEmail"
    "mutable_comments.csv" = "postId,commentId,authorEmail"
    "deletable_comments.csv" = "postId,commentId,authorEmail"
}

function Test-RequiredFeederFiles {
    foreach ($entry in $RequiredFeederFiles.GetEnumerator()) {
        $filePath = Join-Path $DataDirectory $entry.Key
        if (-not (Test-Path -LiteralPath $filePath -PathType Leaf)) {
            return $false
        }

        $lines = @(Get-Content -LiteralPath $filePath)
        if ($lines.Count -le 1 -or $lines[0] -ne $entry.Value) {
            return $false
        }
    }

    return $true
}

Stop-Process -Name java -ErrorAction SilentlyContinue

Write-Host "Starting infrastructure..."
docker compose -p boot-high-rps-sample -f docker/docker-compose-sentinel.yml up -d
docker compose -p boot-high-rps-sample -f docker/docker-compose.yml up -d

# Skip grafana-lgtm for stress profile — at 1000 RPS it consumes ~138% CPU and 3 GB RAM
# on the same host, competing with the app and inflating latency.
# All other profiles start LGTM so traces/metrics are available in Grafana.
if ($Profile -eq "stress") {
    Write-Host "Stress profile: starting exporters only (grafana-lgtm skipped to free CPU/RAM)..."
    docker compose -p boot-high-rps-sample -f docker/docker-compose-monitoring.yml stop grafana-lgtm
    docker compose -p boot-high-rps-sample -f docker/docker-compose-monitoring.yml up -d
} else {
    docker compose -p boot-high-rps-sample -f docker/docker-compose-monitoring.yml --profile monitoring up -d
}
if ($LASTEXITCODE -ne 0) {
    Write-Host "Monitoring startup failed!"
    exit 1
}

Write-Host "Building app..."
if ($env:OS -eq 'Windows_NT') {
    cmd /c "mvnw.cmd clean package -DskipTests"
} else {
    sh -c "./mvnw clean package -DskipTests"
}
if ($LASTEXITCODE -ne 0) {
    Write-Host "Maven build failed!"
    exit 1
}

Write-Host "Building Docker image..."
docker compose -p boot-high-rps-sample -f docker/docker-compose.yml build app
if ($LASTEXITCODE -ne 0) {
    Write-Host "Docker image build failed!"
    exit 1
}

Write-Host "Starting app behind load balancer..."
docker compose -p boot-high-rps-sample -f docker/docker-compose.yml up -d --scale app=2
docker restart boot-high-rps-sample-nginx-1

Write-Host "Waiting for app to start..."
$AppReady = $false
for ($i = 0; $i -lt 120; $i++) {
    try {
        $Response = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method Get -ErrorAction Stop
        if ($Response.status -eq 'UP') {
            $AppReady = $true
            break
        }
    } catch {
        # Ignore and retry
    }
    Start-Sleep -Seconds 2
}

if (-not $AppReady) {
    Write-Host "App failed to start!"
    docker logs boot-high-rps-sample-app-1
    exit 1
}

$RequiredFeederFilesValid = Test-RequiredFeederFiles
if ((Test-Path -LiteralPath $CompletionMarker -PathType Leaf) -and $RequiredFeederFilesValid) {
    Write-Host "Data already present under target/loadtest-data. Skipping Data Generator..."
} else {
    if (Test-Path -LiteralPath $DataDirectory) {
        Write-Host "Load-test data is incomplete. Clearing it before regeneration..."
        Remove-Item -LiteralPath $DataDirectory -Recurse -Force
    }

    Write-Host "App is UP! Running Data Generator..."
    if ($env:OS -eq 'Windows_NT') {
        cmd /c "mvnw.cmd exec:java -Dexec.mainClass=com.example.highrps.gatling.setup.DataGenerator -Dexec.classpathScope=test -DdataDir=$DataDirectory -Dprofile=$Profile"
    } else {
        sh -c "./mvnw exec:java -Dexec.mainClass=com.example.highrps.gatling.setup.DataGenerator -Dexec.classpathScope=test -DdataDir=$DataDirectory -Dprofile=$Profile"
    }

    if ($LASTEXITCODE -ne 0) {
        Write-Host "Data Generator failed!"
        exit 1
    }

    if (-not (Test-RequiredFeederFiles)) {
            Write-Host "Data Generator completed without producing valid feeder files!"
            exit 1
    }

    New-Item -ItemType File -Path $CompletionMarker -Force | Out-Null
}

$ExtraArgs = ""
if ($Profile -eq "stress" -and $PSBoundParameters.ContainsKey("TargetRps")) {
    Write-Host "TargetRps cannot be overridden for the stress profile."
    exit 1
}
if ($TargetRps -gt 0) {
    $ExtraArgs += " -DtargetRps=$TargetRps"
}

Write-Host "Running Gatling with Profile: $Profile..."
if ($env:OS -eq 'Windows_NT') {
    cmd /c "mvnw.cmd gatling:test -Dprofile=$Profile -DdurationMinutes=$DurationMinutes -DwarmupMinutes=$WarmupMinutes $ExtraArgs"
} else {
    sh -c "./mvnw gatling:test -Dprofile=$Profile -DdurationMinutes=$DurationMinutes -DwarmupMinutes=$WarmupMinutes $ExtraArgs"
}
$GatlingExitCode = $LASTEXITCODE

exit $GatlingExitCode
