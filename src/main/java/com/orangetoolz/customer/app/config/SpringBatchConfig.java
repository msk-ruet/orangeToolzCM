package com.orangetoolz.customer.app.config;

import com.orangetoolz.customer.app.entity.Customer;
import com.orangetoolz.customer.app.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;


@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class SpringBatchConfig {

    private JobBuilderFactory jobBuilderFactory;

    private StepBuilderFactory stepBuilderFactory;

    private CustomerRepository customerRepository;


    @Bean
    @StepScope
    public FlatFileItemReader<Customer> reader(@Value("#{jobParameters[filePath]}") String filePath) {
        FlatFileItemReader<Customer> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new FileSystemResource(filePath));
        itemReader.setName("csvReader");
        itemReader.setLineMapper(rowMapper());
        return itemReader;
    }

    private LineMapper<Customer> rowMapper() {
        DefaultLineMapper<Customer> rowMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer rowTokenizer = new DelimitedLineTokenizer();
        rowTokenizer.setDelimiter(",");
        rowTokenizer.setStrict(false);
        rowTokenizer.setNames("firstName","lastName","city","state","code","mobile","email","ip");
        BeanWrapperFieldSetMapper<Customer> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Customer.class);
        rowMapper.setLineTokenizer(rowTokenizer);
        rowMapper.setFieldSetMapper(fieldSetMapper);
        return rowMapper;

    }

    @Bean
    public CustomerProcessor processor() {
        return new CustomerProcessor();
    }

    @Bean
    public RepositoryItemWriter<Customer> writer() {
        RepositoryItemWriter<Customer> writer = new RepositoryItemWriter<>();
        writer.setRepository(customerRepository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("csv-step").<Customer, Customer>chunk(10)
                .reader(reader(null))
                .processor(processor())
                .writer(writer())
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public Job runJob() {
        return jobBuilderFactory.get("importCustomers")
                .flow(step1()).end().build();

    }

    @Bean(name = "taskExecutor")
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
        simpleAsyncTaskExecutor.setConcurrencyLimit(10);
        simpleAsyncTaskExecutor.setThreadNamePrefix("CustomerThread-");
        return simpleAsyncTaskExecutor;
    }

    @Bean(name ="executor")
    public Executor executor(){
        ThreadPoolTaskExecutor executor=new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("CustomerThread-");
        executor.initialize();
        return executor;
    }
}
