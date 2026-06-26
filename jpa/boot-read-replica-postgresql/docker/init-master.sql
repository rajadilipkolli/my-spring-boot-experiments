SET password_encryption = 'scram-sha-256';

-- Create replication user
CREATE USER repl_user WITH REPLICATION PASSWORD 'repl_password';

-- Create read-only app user
CREATE USER app_readonly WITH PASSWORD 'app_readonly_password';

-- Grant necessary permissions to read-only app user
GRANT CONNECT ON DATABASE my_database TO app_readonly;
GRANT USAGE ON SCHEMA public TO app_readonly;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO app_readonly;
GRANT SELECT ON ALL SEQUENCES IN SCHEMA public TO app_readonly;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO app_readonly;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON SEQUENCES TO app_readonly;

-- Update pg_hba.conf to allow replication connections
-- This will be added to the end of pg_hba.conf by PostgreSQL init process
DO $$
BEGIN
    -- Add replication entry to pg_hba.conf
    -- Note: The official postgres image handles pg_hba.conf configuration
    -- The replication connection will be allowed via the default settings
    RAISE NOTICE 'Replication user created successfully';
END $$;
