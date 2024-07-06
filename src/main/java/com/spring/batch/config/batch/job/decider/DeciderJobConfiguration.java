package com.spring.batch.config.batch.job.decider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Random;

@Slf4j
@Configuration
public class DeciderJobConfiguration {

    @Bean
    public Job deciderJob(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new JobBuilder("deciderJob", jobRepository)
                .start(startStep(transactionManager, jobRepository))
                .next(decider()) // 홀수 | 짝수 구분
                .from(decider()) // decider의 상태가
                    .on(CustomExitStatus.ODD.exitCode) // ODD라면
                    .to(oddStep(transactionManager, jobRepository)) // oddStep으로 간다.
                .from(decider()) // decider의 상태가
                    .on(CustomExitStatus.EVEN.exitCode) // EVEN이라면
                    .to(evenStep(transactionManager, jobRepository)) // evenStep으로 간다.
                .end() // builder 종료
                .build();
    }

    @Bean
    public Step startStep(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new StepBuilder("startStep", jobRepository)
                .tasklet(makeStartStepTasklet(), transactionManager)
                .build();
    }

    private Tasklet makeStartStepTasklet() {
        return (contribution, chunkContext) -> {
            log.info(">>>>> Start!");
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Step evenStep(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new StepBuilder("evenStep", jobRepository)
                .tasklet(makeEvenStepTasklet(), transactionManager)
                .build();
    }

    private Tasklet makeEvenStepTasklet() {
        return (contribution, chunkContext) -> {
            log.info(">>>>> 짝수입니다.");
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Step oddStep(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new StepBuilder("oddStep", jobRepository)
                .tasklet(makeOddStepTasklet(), transactionManager)
                .build();
    }

    private Tasklet makeOddStepTasklet() {
        return (contribution, chunkContext) -> {
            log.info(">>>>> 홀수입니다.");
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public JobExecutionDecider decider() {
        return new OddDecider();
    }

    public static class OddDecider implements JobExecutionDecider {

        @Override
        public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
            Random rand = new Random();

            int randomNumber = rand.nextInt(50) + 1;
            log.info("랜덤숫자: {}", randomNumber);

            if(randomNumber % 2 == 0) {
                return new FlowExecutionStatus(CustomExitStatus.EVEN.exitCode);
            } else {
                return new FlowExecutionStatus(CustomExitStatus.ODD.exitCode);
            }
        }
    }

    public enum CustomExitStatus {

        EVEN("EVEN"),
        ODD("ODD"),

        ;

        private final String exitCode;

        CustomExitStatus(String exitCode) {
            this.exitCode = exitCode;
        }

        public String getExitCode() {
            return exitCode;
        }
    }
}
