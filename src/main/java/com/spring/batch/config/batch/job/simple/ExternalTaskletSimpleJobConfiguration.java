package com.spring.batch.config.batch.job.simple;

import com.spring.batch.config.batch.job.simple.tasklet.SimpleJobExternalTasklet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ExternalTaskletSimpleJobConfiguration {

    private final SimpleJobExternalTasklet simpleJobExternalTasklet; // @Autowired 가능

    @Bean
    public Job externalTaskletSimpleJob(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new JobBuilder("externalTaskletSimpleJob", jobRepository)
                .start(externalTaskletSimpleStep(transactionManager, jobRepository))
                .build();
    }

    public Step externalTaskletSimpleStep(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new StepBuilder("externalTaskletSimpleStep", jobRepository)
                .tasklet(simpleJobExternalTasklet, transactionManager)
                .build();
    }
}
