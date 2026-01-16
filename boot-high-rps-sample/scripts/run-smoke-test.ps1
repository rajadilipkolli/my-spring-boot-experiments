# Run a quick smoke test for boot-high-rps-sample
Param()

Write-Host "Starting Docker services (Kafka, Postgresql, Redis)..."
docker compose -f docker/docker-compose.yml up -d postgresqldb redis kafka

Write-Host "Building project..."
mvn clean -DskipTests package

Start-Sleep -Seconds 10

Write-Host "Starting application in background (logs will follow)..."
Start-Process -NoNewWindow -FilePath 'java' -ArgumentList '-Dspring.profiles.active=local','-jar','target/boot-high-rps-sample-0.1.0-SNAPSHOT.jar'
Start-Sleep -Seconds 5

Write-Host "Posting sample event to api/posts"
$body = '{
           "title": "title1",
           "content": "hello 1234",
           "email": "user@example.com",
           "published": false,
           "details": {
             "detailsKey": "spring",
             "createdBy": "smoke-test"
           },
           "tags": [
             {
               "tagName": "java",
               "tagDescription": "beautiful programming language"
             },
             {
               "tagName": "spring",
               "tagDescription": "the best framework"
             }
           ]
         }'
Invoke-RestMethod -Method Post -ContentType 'application/json' -Body $body http://localhost:8080/api/posts

Write-Host "Waiting for scheduled aggregator to run..."
Start-Sleep -Seconds 10

Write-Host "Fetching Post details for title1"
try {
    $res = Invoke-RestMethod http://localhost:8080/api/posts/title1
    Write-Host "Stats response:`n$res"
} catch {
    Write-Host "Failed to fetch stats: $_"
}

Write-Host "Smoke test finished. Tail application logs with 'docker compose logs -f' or check the application console."
