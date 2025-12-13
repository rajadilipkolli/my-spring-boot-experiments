#!/usr/bin/env bash
# Example: ./run-wrk.sh http://localhost:8080/ping
URL=${1:-http://localhost:8080/ping}
wrk -t4 -c400 -d30s --latency $URL
