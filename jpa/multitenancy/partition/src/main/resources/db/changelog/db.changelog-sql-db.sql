CREATE SEQUENCE  IF NOT EXISTS customers_seq START WITH 1 INCREMENT BY 50;

create TABLE if not exists CUSTOMERS (
                                         id bigint not null default nextval('customers_seq'),
                                         text varchar(255),
                                         tenant varchar(255) not null,
                                         primary key (id, tenant)
) PARTITION BY LIST (tenant);

CREATE TABLE if not exists customers_dbsystc PARTITION OF customers FOR VALUES IN ('dbsystc');
CREATE TABLE if not exists customers_def PARTITION OF customers DEFAULT;

CREATE SEQUENCE IF NOT EXISTS orders_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE IF NOT EXISTS ORDERS (
                                      id BIGINT NOT NULL DEFAULT nextval('orders_seq'),
                                      amount DECIMAL(10, 2) NOT NULL,
                                      order_date DATE NOT NULL,
                                      PRIMARY KEY (id, order_date)
) PARTITION BY RANGE (order_date);

CREATE TABLE IF NOT EXISTS orders_default PARTITION OF orders DEFAULT;

-- Add a function to dynamically create partitions for each year
DO $$
    BEGIN
        BEGIN
        FOR year IN 2020..2030 LOOP
            EXECUTE format(
                        'CREATE TABLE IF NOT EXISTS orders_%s PARTITION OF orders FOR VALUES FROM (''%s-01-01'') TO (''%s-01-01'');',
                        year, year, year + 1
                        );
            END LOOP;
        EXCEPTION
            WHEN OTHERS THEN
            RAISE NOTICE 'Error creating partition: %', SQLERRM;
        END;
    END $$;
