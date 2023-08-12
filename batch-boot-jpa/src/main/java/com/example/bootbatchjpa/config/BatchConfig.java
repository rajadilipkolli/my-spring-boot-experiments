package com.example.bootbatchjpa.config;

import com.example.bootbatchjpa.entities.Customer;
import com.example.bootbatchjpa.model.CustomerDTO;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration(proxyBeanMethods = false)
@EnableBatchProcessing
@Slf4j
@RequiredArgsConstructor
public class BatchConfig implements JobExecutionListener {

    private final EntityManagerFactory entityManagerFactory;

    @Bean
    Job allCustomersJob(
            JpaPagingItemReader<Customer> jpaPagingItemReader,
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager) {
        Step step = new StepBuilder("all-customers-step", jobRepository)
                .allowStartIfComplete(true)
                .<Customer, CustomerDTO>chunk(10, transactionManager)
                .reader(jpaPagingItemReader)
                .processor(customer -> new CustomerDTO(customer.getName(), customer.getAddress(), customer.getGender()))
                .writer(items -> {
                    log.info("Writing chunk of size {} at :{}", items.size(), LocalDateTime.now());
                    items.forEach(customerDTO -> log.info("Read customer: {}", customerDTO));
                })
                .faultTolerant()
                .build();

        return new JobBuilder("all-customers-job", jobRepository)
                .start(step)
                .incrementer(new RunIdIncrementer())
                .listener(this)
                .start(step)
                .build();
    }

    @Bean
    @StepScope
    JpaPagingItemReader<Customer> jpaPagingItemReader(
            @Value("#{stepExecution.jobExecution.jobParameters}") JobParameters jobParameters) {
        // use your jobParameters
        Map<String, Object> parameterValuesMap = new HashMap<>(2);
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
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("BATCH JOB COMPLETED SUCCESSFULLY");
        }
    }
}
