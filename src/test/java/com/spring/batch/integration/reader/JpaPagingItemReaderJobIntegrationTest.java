package com.spring.batch.integration.reader;

import com.spring.batch.config.batch.job.reader.database.JpaPagingItemReaderJobConfiguration;
import com.spring.batch.constant.base.PackageTestConstant;
import com.spring.batch.constant.pay.PayTestConstant;
import com.spring.batch.entity.pay.Pay;
import com.spring.batch.integration.config.batch.TestBatchConfig;
import com.spring.batch.repository.pay.PayRepository;
import org.junit.jupiter.api.*;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBatchTest
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
@EntityScan(value = PackageTestConstant.ENTITY_PACKAGE_PATH)
@EnableJpaRepositories(value = PackageTestConstant.REPOSITORY_PACKAGE_PATH)
@SpringJUnitConfig(classes = {JpaPagingItemReaderJobConfiguration.class, TestBatchConfig.class})
class JpaPagingItemReaderJobIntegrationTest {

    @Autowired private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired private Job job;

    @Autowired private PayRepository payRepository;

    @BeforeAll
    void setup() {
        this.jobLauncherTestUtils.setJob(job);
    }

    @BeforeEach
    void beforeEach() {
        List<Pay> pays = List.of(
                new Pay(PayTestConstant.AMOUNT_1, PayTestConstant.TX_NAME_1, PayTestConstant.DEFAULT_TX_DATE_TIME),
                new Pay(PayTestConstant.AMOUNT_2, PayTestConstant.TX_NAME_2, PayTestConstant.DEFAULT_TX_DATE_TIME),
                new Pay(PayTestConstant.AMOUNT_3, PayTestConstant.TX_NAME_3, PayTestConstant.DEFAULT_TX_DATE_TIME)
        );
        payRepository.saveAll(pays);
    }

    @AfterEach
    void afterEach() {
        payRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("jpaPagingItemReaderJob End To End Test")
    void testJpaPagingItemReaderJobTest() throws Exception {
        // given
        // this job has no parameters
        JobParameters jobParameters = new JobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());

        List<Pay> pays = payRepository.findAllByAmountGreaterThanEqual(PayTestConstant.AMOUNT_2);
        final int expectedPaySize = 2;
        assertEquals(expectedPaySize, pays.size());
        assertEquals(PayTestConstant.AMOUNT_2, pays.get(0).getAmount());
        assertEquals(PayTestConstant.TX_NAME_3, pays.get(1).getTxName());
        assertEquals(PayTestConstant.DEFAULT_TX_DATE_TIME, pays.get(1).getTxDateTime());
    }
}
