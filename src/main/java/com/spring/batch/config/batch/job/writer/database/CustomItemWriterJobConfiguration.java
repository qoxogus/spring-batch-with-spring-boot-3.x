package com.spring.batch.config.batch.job.writer.database;

import com.spring.batch.entity.pay.Pay;
import com.spring.batch.entity.pay.Pay2;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CustomItemWriterJobConfiguration {

    private final EntityManagerFactory entityManagerFactory;

    private static final int CHUNK_SIZE = 10;

    @Bean
    public Job customItemWriterJob(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new JobBuilder("customItemWriterJob", jobRepository)
                .start(customItemWriterStep(transactionManager, jobRepository))
                .build();
    }

    @Bean
    public Step customItemWriterStep(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new StepBuilder("customItemWriterStep", jobRepository)
                .<Pay, Pay2>chunk(CHUNK_SIZE, transactionManager)
                .reader(customItemWriterReader())
                .processor(customItemWriterProcessor())
                .writer(customItemWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Pay> customItemWriterReader() {
        final String query = "SELECT p FROM Pay p";

        return new JpaPagingItemReaderBuilder<Pay>()
                .name("customItemWriterReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString(query)
                .build();
    }

    @Bean
    public ItemProcessor<Pay, Pay2> customItemWriterProcessor() {
        return pay -> new Pay2(pay.getAmount(), pay.getTxName(), pay.getTxDateTime());
    }

    @Bean
    public ItemWriter<Pay2> customItemWriter() {
        // 람다식 사용하지 않은 코드
        return new ItemWriter<Pay2>() {
            @Override
            public void write(Chunk<? extends Pay2> chunk) throws Exception {
                for (Pay2 item : chunk.getItems()) {
                    System.out.println(item);
                }
            }
        };

        // 람다식 사용한 코드
//        return chunk -> {
//            for (Pay2 item : chunk.getItems()) {
//                System.out.println(item);
//            }
//        };
    }
}
