package com.spring.batch.config.batch.job.reader.database;

import com.spring.batch.entity.pay.Pay;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JdbcCursorItemReaderJobConfiguration {

    private final DataSource dataSource; // DataSource DI

    private static final int CHUNK_SIZE = 10;

    @Bean
    public Job jdbcCursorItemReaderJob(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new JobBuilder("jdbcCursorItemReaderJob", jobRepository)
                .start(jdbcCursorItemReaderStep(transactionManager, jobRepository))
                .build();
    }

    @Bean
    public Step jdbcCursorItemReaderStep(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new StepBuilder("jdbcCursorItemReaderStep", jobRepository)
                .<Pay, Pay>chunk(CHUNK_SIZE, transactionManager)
                .reader(jdbcCursorItemReader())
                .writer(jdbcCursorItemWriter())
                .build();
    }

    @Bean
    public JdbcCursorItemReader<Pay> jdbcCursorItemReader() {
        final String query = "SELECT id, amount, tx_name, tx_date_time FROM pay";

        return new JdbcCursorItemReaderBuilder<Pay>()
                .fetchSize(CHUNK_SIZE)
                .dataSource(dataSource)
                .rowMapper(new BeanPropertyRowMapper<>(Pay.class))
                .sql(query)
                .name("jdbcCursorItemReader")
                .build();
    }

    private ItemWriter<Pay> jdbcCursorItemWriter() {
        return list -> {
            for (Pay pay: list) {
                log.info("Current Pay={}", pay);
            }
        };
    }
}
