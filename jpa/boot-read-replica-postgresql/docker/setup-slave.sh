#!/bin/bash
set -Eeuo pipefail

export PGDATA=/var/lib/postgresql/data

echo "Setting up PostgreSQL slave..."

# Wait for master to be ready
echo "Waiting for master to be ready..."
until PGPASSWORD="$POSTGRES_PASSWORD" psql -h "$POSTGRES_MASTER_HOST" -p "$POSTGRES_MASTER_PORT" -U "$POSTGRES_USER" -d "my_database" -c '\l' > /dev/null 2>&1
do
  echo "Waiting for master database..."
  sleep 3
done

echo "Master is ready. Checking if slave data exists..."

if [ "$(ls -A $PGDATA 2>/dev/null)" ]; then
    echo "Data directory exists, starting PostgreSQL..."
    # Change ownership and start as postgres user
    chown -R postgres:postgres "$PGDATA"
    exec gosu postgres postgres -c hot_standby=on -c hot_standby_feedback=on
else
    echo "Data directory is empty, creating base backup..."
    
    # Create base backup from master
    PGPASSWORD="$REPLICATION_PASSWORD" pg_basebackup \
        -h "$POSTGRES_MASTER_HOST" \
        -p "$POSTGRES_MASTER_PORT" \
        -U "$REPLICATION_USER" \
        -D "$PGDATA" \
        -W \
        -v \
        -R \
        -X stream

    # Set proper permissions
    chmod 700 "$PGDATA"
    chown -R postgres:postgres "$PGDATA"
    
    # Add standby configuration
    cat >> "$PGDATA/postgresql.conf" <<EOF
hot_standby = on
hot_standby_feedback = on
EOF

    echo "Base backup complete. Starting PostgreSQL in standby mode..."
    exec gosu postgres postgres
fi
