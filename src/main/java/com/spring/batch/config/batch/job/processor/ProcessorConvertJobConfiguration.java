package com.spring.batch.config.batch.job.processor;

import com.spring.batch.entity.pay.Pay;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ProcessorConvertJobConfiguration {

    public static final String JOB_NAME = "processorConvert";
    public static final String BEAN_PREFIX = JOB_NAME + "_";

    private final EntityManagerFactory entityManagerFactory;

    @Value(value = "${chunkSize:1000}")
    private int chunkSize;

    @Bean(value = JOB_NAME + "Job")
    public Job job(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new JobBuilder(JOB_NAME + "Job", jobRepository)
                .preventRestart()
                .start(step(transactionManager, jobRepository))
                .build();
    }

    @Bean(value = BEAN_PREFIX + "Step")
    public Step step(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new StepBuilder(BEAN_PREFIX + "Step", jobRepository)
                .<Pay, String>chunk(chunkSize, transactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Pay> reader() {
        return new JpaPagingItemReaderBuilder<Pay>()
                .name(BEAN_PREFIX + "Reader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT p FROM Pay p")
                .build();
    }

    @Bean
    public ItemProcessor<Pay, String> processor() {
        return pay -> {
            boolean isIgnoreTarget = pay.getId() % 2 == 0L;
            if(isIgnoreTarget){
                log.info(">>>>>>>>> Pay txName={}, isIgnoreTarget={}", pay.getTxName(), isIgnoreTarget);
                return null;
            }

            return pay.getTxName();
        };
    }

    private ItemWriter<String> writer() {
        return chunk -> {
            for (String item : chunk.getItems()) {
                log.info("Pay txName={}", item);
            }
        };
    }
}
