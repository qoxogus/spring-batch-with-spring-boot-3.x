package com.spring.batch.config.batch.job.scope;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
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
public class ScopeJobConfiguration {

    @Bean
    public Job scopeJob(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new JobBuilder("scopeJob", jobRepository)
                .start(scopeStep1(null, transactionManager, jobRepository))
                .next(scopeStep2(transactionManager, jobRepository))
                .build();
    }

    @Bean
    @JobScope
    public Step scopeStep1(
            @Value("#{jobParameters[requestDate]}") String requestDate,
            PlatformTransactionManager transactionManager,
            JobRepository jobRepository
    ) {
        return new StepBuilder("scopeStep1", jobRepository)
                .tasklet(makeScopeStep1Tasklet(requestDate), transactionManager)
                .build();
    }

    private Tasklet makeScopeStep1Tasklet(String requestDate) {
        return (contribution, chunkContext) -> {
            log.info(">>>>> This is scopeStep1");
            log.info(">>>>> requestDate = {}", requestDate);
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Step scopeStep2(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new StepBuilder("scopeStep2", jobRepository)
                .tasklet(makeScopeStep2Tasklet(null), transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public Tasklet makeScopeStep2Tasklet(@Value("#{jobParameters[requestDate]}") String requestDate) {
        return (contribution, chunkContext) -> {
            log.info(">>>>> This is scopeStep2");
            log.info(">>>>> requestDate = {}", requestDate);
            return RepeatStatus.FINISHED;
        };
    }
}
