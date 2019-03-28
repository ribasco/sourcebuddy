package com.ibasco.sourcebuddy.config;

import com.google.gson.Gson;
import com.ibasco.sourcebuddy.Bootstrap;
import com.ibasco.sourcebuddy.components.NotificationManager;
import com.ibasco.sourcebuddy.constants.Beans;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.model.ServerDetailsModel;
import com.ibasco.sourcebuddy.repository.ServerDetailsRepository;
import com.ibasco.sourcebuddy.util.ResourceUtil;
import com.maxmind.geoip2.DatabaseReader;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.*;

@Configuration
@ComponentScan(basePackageClasses = {Bootstrap.class, NotificationManager.class, ServerDetailsModel.class})
@EnableCaching
@EnableTransactionManagement
@EnableJpaAuditing(auditorAwareRef = "auditAwareBean")
@EnableJpaRepositories(
        considerNestedRepositories = true,
        basePackageClasses = ServerDetailsRepository.class,
        transactionManagerRef = Beans.TRANSACTION_MANAGER,
        entityManagerFactoryRef = Beans.ENTITY_MANAGER_FACTORY
)
@EnableAsync
public class AppConfig implements AsyncConfigurer {

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    private static final String GEOIP_DATABASE_PATH = ResourceUtil.loadResource("/geoip/GeoLite2-City.mmdb").getPath();

    @Value("${app.public-api-endpoint}")
    private String publicIpApiEndpoint;

    @Value("${app.db.driver-class-name}")
    private String driverClassName;

    @Value("${app.db.url}")
    private String url;

    @Value("${app.db.username}")
    private String username;

    @Value("${app.db.password}")
    private String password;

    private ApplicationContext context;

    @Bean("publicIp")
    public InetAddress retrievePublicIp() {
        try {
            log.info("Retrieving public ipAddress from {}", publicIpApiEndpoint);
            var httpClient = (HttpURLConnection) URI.create(publicIpApiEndpoint).toURL().openConnection();
            httpClient.setRequestMethod("GET");
            var httpResponse = httpClient.getInputStream();
            var scn = new Scanner(httpResponse);
            var json_sb = new StringBuilder();
            while (scn.hasNext()) {
                json_sb.append(scn.next());
            }
            PublicIp publicIp = gsonProvider().fromJson(json_sb.toString(), PublicIp.class);
            return InetAddress.getByName(publicIp.ipAddress);
        } catch (IOException e) {
            log.error("Error during http client initialization", e);
        }
        return null;
    }

    @Bean("gsonProvider")
    public Gson gsonProvider() {
        return new Gson();
    }

    @Bean
    @Primary
    public PlatformTransactionManager defaultTransactionManager(@Qualifier(Beans.ENTITY_MANAGER_FACTORY) EntityManagerFactory entityManagerFactory) throws IOException {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }

    @Bean
    @Primary
    public EntityManagerFactory defaultEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setJpaDialect(new HibernateJpaDialect());
        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        factoryBean.setDataSource(dataSource());
        factoryBean.setJpaVendorAdapter(vendorAdapter);
        factoryBean.setJpaProperties(hibernateProperties());
        factoryBean.setPackagesToScan(ServerDetails.class.getPackageName());
        factoryBean.afterPropertiesSet();
        return factoryBean.getNativeEntityManagerFactory();
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        try {
            SingleConnectionDataSource scd = new SingleConnectionDataSource();

            BasicDataSource dataSource = new BasicDataSource();
            dataSource.setDriverClassName(driverClassName);
            dataSource.setUrl(url);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            /*dataSource.setInitialSize(5);
            dataSource.setMaxTotal(5);
            dataSource.setMaxIdle(5);*/
            dataSource.setDefaultAutoCommit(true);
            return dataSource;
        } catch (Exception e) {
            log.error("DBCP DataSource bean cannot be created!", e);
            return null;
        }
    }

    private Properties hibernateProperties() {
        Properties hibernateProp = new Properties();
        //hibernateProp.put("hibernate.dialect", "com.ibasco.sourcebuddy.util.dialect.SQLiteDialect");
        hibernateProp.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        hibernateProp.put("hibernate.format_sql", true);
        hibernateProp.put("hibernate.use_sql_comments", true);
        hibernateProp.put("hibernate.show_sql", false);
        hibernateProp.put("hibernate.max_fetch_depth", 3);
        hibernateProp.put("hibernate.jdbc.batch_size", 50);
        hibernateProp.put("hibernate.jdbc.fetch_size", 50);
        return hibernateProp;
    }

    @Bean(destroyMethod = "shutdownNow")
    public ExecutorService taskExecutorService() {
        int num = Runtime.getRuntime().availableProcessors() + 1;
        log.info("Using default number of threads: {}", num);
        //return Executors.newFixedThreadPool(num, taskThreadFactory());
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new SynchronousQueue<>(),
                                      taskThreadFactory());
    }

    @Bean(destroyMethod = "shutdownNow")
    public ExecutorService steamExecutorService() {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new SynchronousQueue<>(),
                                      steamThreadFactory());
    }

    @Bean
    public ThreadFactory steamThreadFactory() {
        return r -> {
            Thread thread = new Thread(steamThreadGroup(), r);
            thread.setDaemon(true);
            thread.setName(String.format("sb-steam-%d", thread.getId()));
            return thread;
        };
    }

    @Bean
    public ThreadFactory taskThreadFactory() {
        return r -> {
            Thread thread = new Thread(taskThreadGroup(), r);
            thread.setDaemon(true);
            thread.setName(String.format("sb-task-%d", thread.getId()));
            return thread;
        };
    }

    @Bean
    public ThreadGroup steamThreadGroup() {
        return new ThreadGroup("sb-steam-tasks");
    }

    @Bean
    public ThreadGroup taskThreadGroup() {
        return new ThreadGroup("sb-core-tasks");
    }

    @Bean
    public DatabaseReader geoIpDatabaseReader() throws IOException {
        return new DatabaseReader.Builder(new File(GEOIP_DATABASE_PATH)).build();
    }

    @Autowired
    public void setContext(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public Executor getAsyncExecutor() {
        return ForkJoinPool.commonPool();
    }

    private class PublicIp {

        private String ipAddress;
    }
}
