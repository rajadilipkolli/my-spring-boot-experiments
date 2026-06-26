#!/bin/bash
set -Eeuo pipefail



echo "Setting up PostgreSQL slave..."

# Ensure the base postgresql directory is accessible by the postgres user
mkdir -p "$PGDATA"
chown -R postgres:postgres /var/lib/postgresql

# Wait for master to be ready
echo "Waiting for master to be ready..."
until PGPASSWORD="$POSTGRES_PASSWORD" psql -h "$POSTGRES_MASTER_HOST" -p "$POSTGRES_MASTER_PORT" -U "$POSTGRES_USER" -d "my_database" -c '\l' > /dev/null 2>&1
do
  echo "Waiting for master database..."
  sleep 3
done

echo "Master is ready. Checking if slave data exists..."

if [ -f "$PGDATA/PG_VERSION" ]; then
    echo "Data directory exists, starting PostgreSQL..."
    # Change ownership
    chown -R postgres:postgres /var/lib/postgresql

    # Add application connection rule if missing
    if ! grep -q "host all all ${APP_CIDR:-0.0.0.0/0} scram-sha-256" "$PGDATA/pg_hba.conf"; then
        echo "host all all ${APP_CIDR:-0.0.0.0/0} scram-sha-256" >> "$PGDATA/pg_hba.conf"
    fi

    exec gosu postgres postgres -D "$PGDATA" -c hot_standby=on -c hot_standby_feedback=on
else
    echo "Data directory is empty or incomplete, cleaning up and creating base backup..."
    rm -rf "${PGDATA:?}"/*
    
    # Create base backup from master
    export PGPASSWORD="$REPLICATION_PASSWORD"
    gosu postgres pg_basebackup \
        -h "$POSTGRES_MASTER_HOST" \
        -p "$POSTGRES_MASTER_PORT" \
        -U "$REPLICATION_USER" \
        -D "$PGDATA" \
        -v \
        -R \
        -X stream

    # Set proper permissions
    chmod 700 "$PGDATA"
    chown -R postgres:postgres /var/lib/postgresql
    
    # Add standby configuration
    cat >> "$PGDATA/postgresql.conf" <<EOF
hot_standby = on
hot_standby_feedback = on
EOF

    # Add application connection rule if missing
    if ! grep -q "host all all ${APP_CIDR:-0.0.0.0/0} scram-sha-256" "$PGDATA/pg_hba.conf"; then
        echo "host all all ${APP_CIDR:-0.0.0.0/0} scram-sha-256" >> "$PGDATA/pg_hba.conf"
    fi

    echo "Base backup complete. Starting PostgreSQL in standby mode..."
    exec gosu postgres postgres -D "$PGDATA"
fi
