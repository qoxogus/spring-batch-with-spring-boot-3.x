package com.spring.batch.config.batch.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
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
public class StepNextConditionalJobConfiguration {

    @Bean
    public Job stepNextConditionalJob(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new JobBuilder("stepNextConditionalJob", jobRepository)
                .start(conditionalJobStep1(transactionManager, jobRepository))
                    .on(ExitStatus.FAILED.getExitCode()) // FAILED 일 경우
                    .to(conditionalJobStep3(transactionManager, jobRepository)) // step3으로 이동한다
                    .on("*") // step3의 결과와 관계 없이
                    .end() // step3으로 이동하면 flow를 종료한다.
                .from(conditionalJobStep1(transactionManager, jobRepository)) // step1로부터
                    .on("*") // FAILED 외 모든 경우
                    .to(conditionalJobStep2(transactionManager, jobRepository)) // step2로 이동한다.
                    .next(conditionalJobStep3(transactionManager, jobRepository)) // step2가 정상 종료되면 step3로 이동한다
                    .on("*") // step3의 결과와 관계 없이
                    .end() // step3로 이동하면 flow를 종료한다.
                .end() // job 종료
                .build();
    }

    @Bean
    public Step conditionalJobStep1(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new StepBuilder("conditionalJobStep1", jobRepository)
                .tasklet(makeStep1Tasklet(), transactionManager)
                .build();
    }

    private Tasklet makeStep1Tasklet() {
        return (contribution, chunkContext) -> {
            log.info(">>>>> This is stepNextConditionalJob Step1");

            /*
             ExitStatus를 FAILED로 지정한다.
             해당 status를 보고 flow가 진행된다.
             */
//            contribution.setExitStatus(ExitStatus.FAILED);

            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Step conditionalJobStep2(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new StepBuilder("conditionalJobStep2", jobRepository)
                .tasklet(makeStep2Tasklet(), transactionManager)
                .build();
    }

    private Tasklet makeStep2Tasklet() {
        return (contribution, chunkContext) -> {
            log.info(">>>>> This is stepNextConditionalJob Step2");
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Step conditionalJobStep3(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new StepBuilder("conditionalJobStep3", jobRepository)
                .tasklet(makeStep3Tasklet(), transactionManager)
                .build();
    }

    private Tasklet makeStep3Tasklet() {
        return (contribution, chunkContext) -> {
            log.info(">>>>> This is stepNextConditionalJob Step3");
            return RepeatStatus.FINISHED;
        };
    }
}
