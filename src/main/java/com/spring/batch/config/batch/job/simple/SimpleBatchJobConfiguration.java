package com.spring.batch.config.batch.job.simple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SimpleBatchJobConfiguration {

    @Bean
    public Job simpleJob(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new JobBuilder("simpleJob", jobRepository)
                .start(simpleStep1(null, transactionManager, jobRepository))
                .next(simpleStep2(null, transactionManager, jobRepository))
                .build();
    }

    @Bean
    @JobScope
    public Step simpleStep1(
            @Value("#{jobParameters[requestDate]}") String requestDate,
            PlatformTransactionManager transactionManager,
            JobRepository jobRepository
    ) {
        return new StepBuilder("simpleStep1", jobRepository)
                .tasklet(makeSimpleStep1Tasklet(requestDate), transactionManager)
                .build();
    }

    private Tasklet makeSimpleStep1Tasklet(String requestDate) {
        return (contribution, chunkContext) -> {
            log.info(">>>>> This is Step1");
            log.info(">>>>> requestDate = {}", requestDate);
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    @JobScope
    public Step simpleStep2(
            @Value("#{jobParameters[requestDate]}") String requestDate,
            PlatformTransactionManager transactionManager,
            JobRepository jobRepository
    ) {
        return new StepBuilder("simpleStep2", jobRepository)
                .tasklet(makeSimpleStep2Tasklet(requestDate), transactionManager)
                .build();
    }

    private Tasklet makeSimpleStep2Tasklet(String requestDate) {
        return (contribution, chunkContext) -> {
            log.info(">>>>> This is Step2");
            log.info(">>>>> requestDate = {}", requestDate);
            return RepeatStatus.FINISHED;
        };
    }
}
