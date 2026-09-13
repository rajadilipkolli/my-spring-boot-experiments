# Load Testing Guide

This document outlines the procedure to load test `boot-high-rps-sample` and evaluate its maximum sustainable RPS.

## Prerequisites

- Docker and Docker Compose installed.
- Maven 3.8+ and JDK 25+.

## Infrastructure Startup Order

1. Start main infrastructure: `docker-compose -f docker/docker-compose.yml up -d`
2. Start monitoring stack: `docker-compose -f docker/docker-compose-monitoring.yml up -d`
3. Generate data: `mvn exec:java -Dexec.mainClass="com.example.highrps.gatling.setup.DataGenerator" -Dexec.classpathScope=test`

Alternatively, use the orchestration script: `.\scripts\run-load-test.ps1 -Profile normal`

## Load Profiles

You can override Gatling profiles using `-Dprofile=<name>`:
- `smoke`: 10 RPS for 5 mins (plus 5 min warmup).
- `normal`: 50 RPS for 15 mins (plus 5 min warmup).
- `high`: 100 RPS for 15 mins (plus 5 min warmup).
- `stress`: Stepped load from 100, 250, 500, to 1000 RPS.

## Properties Configuration

Refer to `src/test/resources/load-test.properties` for all overridable settings, including weights and threshold assertions. Pass them as JVM args (e.g., `-DtargetRps=200`).

## Baseline vs High RPS Comparison

1. **Baseline**: Checkout the earlier commit (before optimizations), compile, start infra, and run the `smoke` or `normal` profile. Save the `target/gatling/*/js/stats.json` file.
2. **High RPS**: Checkout `HEAD`, compile, start infra, and run the same profile. Save the Gatling `stats.json`.
3. **Compare**: Run `.\scripts\compare-results.ps1 -BaselineStats path/to/base.json -HighRpsStats path/to/head.json`

### Comparison Table Template

| Metric           | Baseline | High RPS | Delta |
|------------------|----------|----------|-------|
| Throughput (RPS) |          |          |       |
| Error Rate (%)   |          |          |       |
| p50 (ms)         |          |          |       |
| p75 (ms)         |          |          |       |
| p95 (ms)         |          |          |       |
| p99 (ms)         |          |          |       |
| Max (ms)         |          |          |       |

*Maximum sustainable RPS: [Add conclusion here]*
*Improvements/Regressions: [Add details here]*

## Post-Run Verification

The API has no native tag query endpoint. We resolve the tag to its associated post IDs client-side via the feeder and fetch each post individually.

The scenarios already contain built-in functional assertions:
- Author scenarios verify HTTP 201 and location URI.
- Post creation verifies retrieved post matches requested author and tags.
- Comment creation verifies the linked post ID.
- Read scenarios verify data presence.

You can also sample `target/loadtest-data/posts.csv` and execute `curl localhost:8080/api/posts/<id>` to verify persistence (after `500ms` if batch delays apply).
Verify concurrent comments aren't lost by querying `/api/posts/<id>/comments` for heavily hit posts.
