package com.example.bootbatchjpa.config;

import com.example.bootbatchjpa.entities.Customer;
import com.example.bootbatchjpa.model.CustomerDTO;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.EnableJdbcJobRepository;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.JpaPagingItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;

@Configuration(proxyBeanMethods = false)
@EnableBatchProcessing
@EnableJdbcJobRepository
class BatchConfig implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(BatchConfig.class);

    private final EntityManagerFactory entityManagerFactory;

    BatchConfig(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Bean
    Job allCustomersJob(JpaPagingItemReader<Customer> jpaPagingItemReader, JobRepository jobRepository) {
        Step step = new StepBuilder("all-customers-step", jobRepository)
                .allowStartIfComplete(true)
                .<Customer, CustomerDTO>chunk(10)
                .reader(jpaPagingItemReader)
                .processor(getCustomerCustomerDTOItemProcessor())
                .writer(getCustomerDTOItemWriter())
                .faultTolerant() // tell to spring batch that this step can face errors
                .skip(DataAccessException.class) // skip all DataAccessException
                .skipLimit(20) // the number of times you want to skip DataAccessException.class
                .build();

        return new JobBuilder("all-customers-job", jobRepository)
                .start(step)
                .incrementer(new RunIdIncrementer())
                .listener(this)
                .build();
    }

    private ItemWriter<CustomerDTO> getCustomerDTOItemWriter() {
        return items -> {
            log.info("Writing chunk of size {} at :{}", items.size(), LocalDateTime.now());
            items.forEach(customerDTO -> log.info("Read customer: {}", customerDTO));
        };
    }

    private ItemProcessor<Customer, CustomerDTO> getCustomerCustomerDTOItemProcessor() {
        return customer -> new CustomerDTO(customer.getName(), customer.getAddress(), customer.getGender());
    }

    @Bean
    @StepScope
    JpaPagingItemReader<Customer> jpaPagingItemReader(
            @Value("#{stepExecution.jobExecution.jobParameters}") JobParameters jobParameters) {
        // use your jobParameters
        Map<String, Object> parameterValuesMap = HashMap.newHashMap(2);
        parameterValuesMap.put("minId", jobParameters.getLong("minId"));
        parameterValuesMap.put("maxId", jobParameters.getLong("maxId"));
        return new JpaPagingItemReaderBuilder<Customer>()
                .name("jpaPagingItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT c FROM Customer c WHERE c.id BETWEEN :minId AND :maxId ORDER BY c.id")
                .parameterValues(parameterValuesMap)
                .pageSize(10)
                .build();
    }

    @Override
    public void afterJob(@NonNull JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("BATCH JOB COMPLETED SUCCESSFULLY");
        }
    }
}
