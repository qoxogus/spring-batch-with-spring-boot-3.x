package com.spring.batch.config.batch.job.simple.tasklet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StepScope
public class SimpleJobExternalTasklet implements Tasklet {

    @Value("#{jobParameters[requestDate]}")
    private String requestDate;

    public SimpleJobExternalTasklet() {
        log.info(">>>>>>> tasklet 생성");
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info(">>>>> This is externalTaskletSimpleStep");
        log.info(">>>>> requestDate = {}", requestDate);
        return RepeatStatus.FINISHED;
    }
}
