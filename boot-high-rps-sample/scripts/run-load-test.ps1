param(
    [string]$Profile = "smoke",
    [string]$BaseUrl = "http://localhost:8080"
)

Write-Host "Starting infrastructure..."
docker-compose -f docker/docker-compose.yml up -d
docker-compose -f docker/docker-compose-monitoring.yml up -d

Write-Host "Waiting for application to start..."
Start-Sleep -Seconds 20

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
