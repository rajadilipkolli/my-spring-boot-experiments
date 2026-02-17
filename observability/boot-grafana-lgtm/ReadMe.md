# boot-grafana-lgtm

LGTM stands for Loki, Grafana, Tempo and Mimir. It’s Grafana’s tool stack that enables logs, metrics, and traces to be collected and visualized within a single stack of tools that works in harmony

## Grafana

Grafana is the visualization tool for the whole stack. It allows you to visualize metrics, logs, traces, and many more with its plentiful dashboards and integrations.

## Loki

Loki is the log aggregator of the LGTM stack. Logs are pushed to Loki, indexed based on given labels and stored within a defined storage. This can be the filesystem, a database (Cassandra, BigTable, DynamoDB), or an object storage. The logs can then be queried from Grafana or through Loki’s API using LogQL, a query language similar to Prometheus’ PromQL.

## Mimir

 Mimir is the long-term metric storage of the LGTM stack. It receives Prometheus metrics through its Prometheus remote write API and stores them on object storage. Metrics can be queried through Grafana or Mimir’s API using PromQL. Mimir is Prometheus-compatible, meaning that any application using Prometheus APIs can use Mimir without any change in the codebase.

## Tempo

Tempo is the tracing backend of the LGTM stack. It collects traces in Jaeger, Zipkin, and OpenTelemetry formats. You can then query the traces from Grafana and visualize the path of every request within a distributed system.

## Flow

![img.png](img.png)
