import os
import subprocess
import json
import urllib.request

def run_cmd(cmd, outfile):
    try:
        with open(outfile, 'w') as f:
            subprocess.run(cmd, shell=True, stdout=f, stderr=subprocess.STDOUT)
    except Exception as e:
        print(f"Error running {cmd}: {e}")

def fetch_url(url, outfile):
    try:
        req = urllib.request.Request(url)
        with urllib.request.urlopen(req, timeout=5) as response:
            with open(outfile, 'wb') as f:
                f.write(response.read())
    except Exception as e:
        print(f"Error fetching {url}: {e}")

def main():
    """Collect container logs and metrics in the local diagnostics directory."""
    out_dir = "diagnostics"
    os.makedirs(out_dir, exist_ok=True)

    print("Collecting diagnostics...")
    run_cmd("docker stats --no-stream", f"{out_dir}/docker-stats.txt")
    run_cmd("docker ps", f"{out_dir}/docker-ps.txt")

    # Logs
    run_cmd("docker logs boot-high-rps-sample-app-1", f"{out_dir}/app-1.log")
    run_cmd("docker logs boot-high-rps-sample-app-2", f"{out_dir}/app-2.log")
    run_cmd("docker logs postgresql", f"{out_dir}/postgres.log")
    run_cmd("docker logs redis-master", f"{out_dir}/redis-master.log")
    run_cmd("docker logs slave-redis", f"{out_dir}/slave-redis.log")
    run_cmd("docker logs sentinel-1", f"{out_dir}/sentinel-1.log")
    run_cmd("docker logs sentinel-2", f"{out_dir}/sentinel-2.log")
    run_cmd("docker logs sentinel-3", f"{out_dir}/sentinel-3.log")
    run_cmd("docker logs node-exporter", f"{out_dir}/node-exporter.log")
    run_cmd("docker logs postgres-exporter", f"{out_dir}/postgres-exporter.log")
    run_cmd("docker logs grafana-lgtm", f"{out_dir}/grafana-lgtm.log")

    # Metrics
    fetch_url("http://localhost:8080/actuator/metrics", f"{out_dir}/actuator-metrics-list.json")
    fetch_url("http://localhost:8080/actuator/prometheus", f"{out_dir}/app-prometheus.txt")
    fetch_url("http://localhost:9187/metrics", f"{out_dir}/postgres-exporter-metrics.txt")
    fetch_url("http://localhost:9100/metrics", f"{out_dir}/node-exporter-metrics.txt")
    fetch_url("http://localhost:9090/api/v1/targets", f"{out_dir}/prometheus-targets.json")

    print("Diagnostics collected in diagnostics/")

if __name__ == '__main__':
    main()
