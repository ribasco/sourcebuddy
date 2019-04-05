package com.ibasco.sourcebuddy.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class BatchConfig /*implements BatchConfigurer*/ {

    private DataSource dataSource;

    private PlatformTransactionManager transactionManager;

    private JobBuilderFactory jobBuilderFactory;

    private StepBuilderFactory stepBuilderFactory;

/*    @Autowired
    public void setDataSource(@Qualifier("dataSource") DataSource dataSource) {
        this.dataSource = dataSource;
    }    @Autowired

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Autowired
    public void setJobBuilderFactory(JobBuilderFactory jobBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
    }

    @Autowired
    public void setStepBuilderFactory(StepBuilderFactory stepBuilderFactory) {
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Override
    public JobRepository getJobRepository() throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    @Override
    public PlatformTransactionManager getTransactionManager() throws Exception {
        return transactionManager;
    }

    @Override
    public JobLauncher getJobLauncher() throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(getJobRepository());
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

    @Override
    public JobExplorer getJobExplorer() throws Exception {
        return null;
    }*/
}
