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
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ProcessorCompositeJobConfiguration {

    public static final String JOB_NAME = "processorComposite";

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

    @Bean(value = JOB_NAME + "Step")
    public Step step(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new StepBuilder(JOB_NAME + "Step", jobRepository)
                .<Pay, String>chunk(chunkSize, transactionManager)
                .reader(reader())
                .processor(compositeProcessor())
                .writer(writer())
                .build();
    }

    @Bean(value = JOB_NAME + "Reader")
    public JpaPagingItemReader<Pay> reader() {
        return new JpaPagingItemReaderBuilder<Pay>()
                .name(JOB_NAME + "Reader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT p FROM Pay p")
                .build();
    }

    @Bean
    public CompositeItemProcessor compositeProcessor() {
        List<ItemProcessor> delegates = new ArrayList<>(2);
        delegates.add(processor1());
        delegates.add(processor2());

        CompositeItemProcessor processor = new CompositeItemProcessor<>();
        processor.setDelegates(delegates);

        return processor;
    }

    public ItemProcessor<Pay, String> processor1() {
        return Pay::getTxName;
    }

    public ItemProcessor<String, String> processor2() {
        return name -> "안녕하세요. "+ name + "입니다.";
    }

    private ItemWriter<String> writer() {
        return chunk -> {
            for (String item : chunk.getItems()) {
                log.info("Pay introduce={}", item);
            }
        };
    }
}
