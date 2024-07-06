package com.spring.batch.config.batch.job.next;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class StepNextJobConfiguration {

    @Bean
    public Job stepNextJob(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new JobBuilder("stepNextJob", jobRepository)
                .start(step1(transactionManager, jobRepository))
                .next(step2(transactionManager, jobRepository))
                .next(step3(transactionManager, jobRepository))
                .build();
    }

    @Bean
    public Step step1(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new StepBuilder("step1", jobRepository)
                .tasklet(makeStep1Tasklet(), transactionManager)
                .build();
    }

    private Tasklet makeStep1Tasklet() {
        return (contribution, chunkContext) -> {
            log.info(">>>>> This is Step1");
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Step step2(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new StepBuilder("step2", jobRepository)
                .tasklet(makeStep2Tasklet(), transactionManager)
                .build();
    }

    private Tasklet makeStep2Tasklet() {
        return (contribution, chunkContext) -> {
            log.info(">>>>> This is Step2");
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Step step3(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new StepBuilder("step3", jobRepository)
                .tasklet(makeStep3Tasklet(), transactionManager)
                .build();
    }

    private Tasklet makeStep3Tasklet() {
        return (contribution, chunkContext) -> {
            log.info(">>>>> This is Step3");
            return RepeatStatus.FINISHED;
        };
    }
}
