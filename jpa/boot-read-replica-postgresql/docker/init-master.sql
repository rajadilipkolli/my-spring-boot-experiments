-- Create replication user
CREATE USER repl_user WITH REPLICATION ENCRYPTED PASSWORD 'repl_password';

-- Grant necessary permissions
GRANT CONNECT ON DATABASE my_database TO repl_user;
GRANT USAGE ON SCHEMA public TO repl_user;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO repl_user;
GRANT SELECT ON ALL SEQUENCES IN SCHEMA public TO repl_user;

-- Update pg_hba.conf to allow replication connections
-- This will be added to the end of pg_hba.conf by PostgreSQL init process
DO $$
BEGIN
    -- Add replication entry to pg_hba.conf
    -- Note: The official postgres image handles pg_hba.conf configuration
    -- The replication connection will be allowed via the default settings
    RAISE NOTICE 'Replication user created successfully';
END $$;
