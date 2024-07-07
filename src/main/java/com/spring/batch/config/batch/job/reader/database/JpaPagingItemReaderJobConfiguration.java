package com.spring.batch.config.batch.job.reader.database;

import com.spring.batch.entity.pay.Pay;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JpaPagingItemReaderJobConfiguration {

    private final EntityManagerFactory entityManagerFactory;

    private static final int CHUNK_SIZE = 10;

    @Bean
    public Job jpaPagingItemReaderJob(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new JobBuilder("jpaPagingItemReaderJob", jobRepository)
                .start(jpaPagingItemReaderStep(transactionManager, jobRepository))
                .build();
    }

    @Bean
    public Step jpaPagingItemReaderStep(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new StepBuilder("jpaPagingItemReaderStep", jobRepository)
                .<Pay, Pay>chunk(CHUNK_SIZE, transactionManager)
                .reader(jpaPagingItemReader())
                .writer(jpaPagingItemWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Pay> jpaPagingItemReader() {
        final String query = "SELECT p FROM Pay p WHERE amount >= 2000";

        return new JpaPagingItemReaderBuilder<Pay>()
                .name("jpaPagingItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString(query)
                .build();
    }

    private ItemWriter<Pay> jpaPagingItemWriter() {
        return list -> {
            for (Pay pay: list) {
                log.info("Current Pay={}", pay);
            }
        };
    }
}
