# Load Testing Guide

This document outlines the procedure to load test oot-high-rps-sample and evaluate its maximum sustainable RPS.

## Identical-Configuration Rule
Baseline and high-RPS runs must keep the dataset (including mutable/deletable pools), the traffic mix and all weights (including the four mutation weights), the target RPS, duration, warm-up, ramp, load-generator configuration, JVM configuration, database configuration, and infrastructure identical. Only the implementation under test changes.

## Infrastructure Startup Order

1. Start main infrastructure: docker compose -f docker/docker-compose.yml up -d
2. Start monitoring stack: docker compose -f docker/docker-compose-monitoring.yml up -d
3. Build the application and load-test classes: ./mvnw -DskipTests clean package
4. In a separate terminal, start the application and leave it running: java -jar target/boot-high-rps-sample-0.1.0-SNAPSHOT.jar --spring.profiles.active=local
5. Wait for http://localhost:8080/actuator/health to report UP.
6. Generate data: ./mvnw exec:java -Dexec.mainClass="com.example.highrps.gatling.setup.DataGenerator" -Dexec.classpathScope=test

Alternatively, use the orchestration script: .\scripts\run-load-test.ps1 -Profile normal

The data generator builds shared read/create pools, as well as dedicated mutable and deletable pools. The deletable pools are sized large enough so each run uses them as a non-repeating queue. For longer runs, increase the deletable pool sizes in your config to avoid starvation.

## Load Profiles

You can override Gatling profiles using -Dprofile=<name>:
- smoke: 10 RPS for 5 mins (plus 5 min warmup).
- 
ormal: 50 RPS for 15 mins (plus 5 min warmup).
- high: 100 RPS for 15 mins (plus 5 min warmup).
- stress: Stepped load from 100, 250, 500, to 1000 RPS.

## Properties Configuration

Refer to src/test/resources/load-test.properties for all overridable settings.

### Operation Weights
| Operation | Default Weight | Purpose |
|---|---|---|
| readPostWeight | 41.0 | Read a post by ID |
| readCommentsWeight | 20.0 | Read comments for a post |
| readTagPostsWeight | 10.0 | Read posts by tag |
| createCommentWeight | 15.0 | Create a new comment |
| createPostWeight | 8.0 | Create a new post |
| registerAuthorWeight | 2.0 | Register a new author |
| postUpdateWeight | 1.0 | Update a post (mutable pool) |
| postDeleteWeight | 1.0 | Delete a post (deletable pool) |
| commentUpdateWeight | 1.0 | Update a comment (mutable pool) |
| commentDeleteWeight | 1.0 | Delete a comment (deletable pool) |

Mutation weights are kept low by default to simulate typical read-heavy CMS traffic, but are fully configurable.

## Baseline vs High RPS Comparison

Per-endpoint (per-API) statistics are the **primary** comparison view, providing granular insight into how specific API optimizations perform. Global statistics are provided as a complementary view.

1. **Baseline**: Checkout the earlier commit (before optimizations), compile, start infra, and run the smoke or 
ormal profile. Save the 	arget/gatling/*/js/stats.json file.
2. **High RPS**: Checkout HEAD, compile, start infra, and run the same profile. Save the Gatling stats.json.
3. **Compare**: Run .\scripts\compare-results.ps1 -BaselineStats path/to/base.json -HighRpsStats path/to/head.json

## Viewing Reports
You can view the detailed Gatling HTML report at:
	arget/gatling/<sim>/index.html

## Diagnostics Collection
Diagnostics are collected as a first-class output of each benchmark run.
* **What is collected:** JVM/app metrics (CPU, heap, GC, allocation, threads), database metrics (connections, pool utilization, query latency), host metrics (CPU/Memory), docker stats snapshots, application logs, and GC logs.
* **When/How:** Collected in CI via collect_diagnostics.py before the monitoring containers are torn down, ensuring data survives assertion or comparison failures.
* **Storage:** Locally or in CI, artifacts are stored in performance-results/baseline/<profile>/diagnostics/ and performance-results/high-rps/<profile>/diagnostics/.
* **Usage:** Use these signals alongside per-endpoint stats to correlate latency spikes or throughput caps with resource saturation.

## Post-Run Verification

The scenarios already contain built-in functional assertions:
- Author scenarios verify HTTP 201 and location URI. (Note: Author update/delete is intentionally excluded since authors are shared heavily across posts).
- Post creation verifies retrieved post matches requested author and tags.
- Comment creation verifies the linked post ID.
- Read scenarios verify data presence.
- **Update semantics**: A sampled updated resource returns its new fields on a follow-up GET, with relationships preserved.
- **Delete semantics**: A sampled deleted resource returns 404 Not Found on a follow-up GET.

## Portability (k6/Kubernetes)
The externalized workload model (scenarios, weights, feeders, data distribution, profiles, ramp) is fully reusable for a future k6 or Kubernetes-native distributed implementation. Only the tool syntax changes, while the logic, pools, and traffic distribution remain portable.
