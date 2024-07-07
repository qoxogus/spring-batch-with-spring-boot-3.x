package com.spring.batch.config.batch.job.writer.database;

import com.spring.batch.entity.pay.Pay;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JdbcItemWriterJobConfiguration {

    private final DataSource dataSource;

    private static final int CHUNK_SIZE = 10;

    @Bean
    public Job jdbcItemWriterJob(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new JobBuilder("jdbcItemWriterJob", jobRepository)
                .start(jdbcItemWriterStep(transactionManager, jobRepository))
                .build();
    }

    @Bean
    public Step jdbcItemWriterStep(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new StepBuilder("jdbcItemWriterStep", jobRepository)
                .<Pay, Pay>chunk(CHUNK_SIZE, transactionManager)
                .reader(jdbcItemWriterReader())
                .writer(jdbcItemWriter())
                .build();
    }

    @Bean
    public JdbcCursorItemReader<Pay> jdbcItemWriterReader() {
        final String query = "SELECT id, amount, tx_name, tx_date_time FROM pay";

        return new JdbcCursorItemReaderBuilder<Pay>()
                .fetchSize(CHUNK_SIZE)
                .dataSource(dataSource)
                .rowMapper(new BeanPropertyRowMapper<>(Pay.class))
                .sql(query)
                .name("jdbcItemWriterReader")
                .build();
    }

    /**
     * reader에서 넘어온 데이터를 하나씩 출력하는 writer
     */
    @Bean // beanMapped()을 사용할때는 필수
    public JdbcBatchItemWriter<Pay> jdbcItemWriter() {
        return new JdbcBatchItemWriterBuilder<Pay>()
                .dataSource(dataSource)
                .sql("insert into pay(amount, tx_name, tx_date_time) values (:amount, :txName, :txDateTime)")
                .beanMapped()
                .build();
    }
}
