#!/bin/bash
set -e

echo "Configuring PostgreSQL master for replication..."

# Update postgresql.conf for replication
cat >> "$PGDATA/postgresql.conf" <<EOF

# Replication settings
wal_level = replica
max_wal_senders = 3
max_replication_slots = 3
hot_standby = on
hot_standby_feedback = on
listen_addresses = '*'
EOF

# Update pg_hba.conf to allow replication connections
cat >> "$PGDATA/pg_hba.conf" <<EOF

# Replication connections
host replication repl_user 0.0.0.0/0 md5
EOF

echo "Master configuration complete"
