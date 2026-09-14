param(
    [string]$Profile = "smoke",
    [string]$BaseUrl = "http://localhost:8080"
)

Write-Host "Starting infrastructure..."
docker-compose -f docker/docker-compose.yml up -d
docker-compose -f docker/docker-compose-monitoring.yml up -d

Write--Host "Checking application health..."
$healthUrl = "$($BaseUrl.TrimEnd('/'))/actuator/health"
$deadline = (Get-Date).AddSeconds(60)
$applicationReady = $false
do {
    try {
        $health = Invoke-RestMethod -Uri $healthUrl -TimeoutSec 5
        $applicationReady = $health.status -eq "UP"
    } catch {
        $applicationReady = $false
    }

    if (-not $applicationReady) {
        Start-Sleep -Seconds 2
    }
} while (-not $applicationReady -and (Get-Date) -lt $deadline)

if (-not $applicationReady) {
    throw "Application prerequisite is not ready at $healthUrl. Start boot-high-rps-sample with the local profile before running this script."
}

Write-Host "Running Data Generator..."
# Compile test classes if not already
mvn test-compile -q
# Execute DataGenerator
mvn exec:java -Dexec.mainClass="com.example.highrps.gatling.setup.DataGenerator" -Dexec.classpathScope=test -DbaseUrl=$BaseUrl

Write-Host "Starting Gatling Test with profile: $Profile"
$env:MANAGEMENT_METRICS_TAGS_ENV = "loadtest"
mvn gatling:test -Dprofile=$Profile -DbaseUrl=$BaseUrl

Write-Host "Load test completed. Reports are generated in target/gatling"
Write-Host "Remember to copy the report to performance-results/ if you want to keep it."
