#!/bin/bash
set -euo pipefail

echo "Configuring PostgreSQL master for replication..."

# Update postgresql.conf for replication
cat >> "$PGDATA/postgresql.conf" <<EOF

# Replication settings
password_encryption = 'scram-sha-256'
wal_level = replica
wal_keep_size = 64MB
max_wal_senders = 3
max_replication_slots = 3
hot_standby = on
hot_standby_feedback = on
listen_addresses = '*'
EOF

# Update pg_hba.conf to allow replication connections
cat >> "$PGDATA/pg_hba.conf" <<EOF

# Replication connections
host replication repl_user ${REPL_NETWORK:-0.0.0.0/0} scram-sha-256

# Application connections
host all all ${APP_CIDR:-0.0.0.0/0} scram-sha-256
EOF

# Trigger Postgres reload so the new rule takes effect if it's already running
pg_ctl reload -D "$PGDATA" || true

echo "Master configuration complete"
