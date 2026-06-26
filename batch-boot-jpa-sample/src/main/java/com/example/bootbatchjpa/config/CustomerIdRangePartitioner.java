package com.example.bootbatchjpa.config;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class CustomerIdRangePartitioner implements Partitioner {

    private static final Logger log = LoggerFactory.getLogger(CustomerIdRangePartitioner.class);

    private final JdbcTemplate jdbcTemplate;

    public CustomerIdRangePartitioner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Long minId = jdbcTemplate.queryForObject("SELECT MIN(id) FROM customers", Long.class);
        Long maxId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM customers", Long.class);

        if (minId == null || maxId == null) {
            log.info("No records found in customers table. Returning single partition 0-0.");
            Map<String, ExecutionContext> result = new HashMap<>();
            ExecutionContext value = new ExecutionContext();
            value.putLong("minValue", 0L);
            value.putLong("maxValue", 0L);
            result.put("partition0", value);
            return result;
        }

        long targetSize = (maxId - minId) / gridSize + 1;
        Map<String, ExecutionContext> result = new HashMap<>();

        long start = minId;
        long end = start + targetSize - 1;

        int partitionNumber = 0;
        while (start <= maxId) {
            ExecutionContext value = new ExecutionContext();
            if (end >= maxId) {
                end = maxId;
            }
            value.putLong("minValue", start);
            value.putLong("maxValue", end);
            result.put("partition" + partitionNumber, value);

            log.info("Partition {}: start = {}, end = {}", partitionNumber, start, end);

            start = end + 1;
            end += targetSize;
            partitionNumber++;
        }

        return result;
    }
}
