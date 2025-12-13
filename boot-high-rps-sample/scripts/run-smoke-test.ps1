# Run a quick smoke test for boot-high-rps-sample
Param()

Write-Host "Starting Docker services (Kafka, Zookeeper, Redis)..."
docker compose -f docker/docker-compose.yml up -d

Write-Host "Building project..."
mvn -DskipTests package

Write-Host "Starting application in background (logs will follow)..."
Start-Process -NoNewWindow -FilePath 'java' -ArgumentList '-jar','target/boot-high-rps-sample-0.1.0-SNAPSHOT.jar'
Start-Sleep -Seconds 5

Write-Host "Posting sample event to /events"
$body = '{"id":"user-smoke","value":123}'
Invoke-RestMethod -Method Post -ContentType 'application/json' -Body $body http://localhost:8080/events

Write-Host "Waiting for scheduled aggregator to run..."
Start-Sleep -Seconds 10

Write-Host "Fetching stats for user-smoke"
try {
    $res = Invoke-RestMethod http://localhost:8080/stats/user-smoke
    Write-Host "Stats response:`n$res"
} catch {
    Write-Host "Failed to fetch stats: $_"
}

Write-Host "Smoke test finished. Tail application logs with 'docker compose logs -f' or check the application console."
