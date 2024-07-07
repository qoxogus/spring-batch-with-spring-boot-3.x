package com.spring.batch.integration.simple;

import com.spring.batch.config.batch.job.simple.SimpleJobConfiguration;
import com.spring.batch.integration.config.batch.TestBatchConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBatchTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringJUnitConfig(classes = {SimpleJobConfiguration.class, TestBatchConfig.class})
class SimpleJobIntegrationTest {

    @Autowired private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired private Job job;

    public static final String JOB_PARAMETER_KEY = "requestDate";
    public static final String JOB_PARAMETER_VALUE = "2000-01-01";


    @BeforeAll
    public void setup() {
        this.jobLauncherTestUtils.setJob(job);
    }

    @Test
    @DisplayName("simpleJob End To End Test")
    void testSimpleJob() throws Exception {
        // given
        JobParameters jobParameters = new JobParametersBuilder()
                .addString(JOB_PARAMETER_KEY, JOB_PARAMETER_VALUE)
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
        assertEquals(
                JOB_PARAMETER_VALUE,
                jobExecution.getJobParameters().getParameters().get(JOB_PARAMETER_KEY).getValue()
        );
    }
}