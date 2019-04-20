package com.ibasco.sourcebuddy.config;

import com.google.gson.Gson;
import com.ibasco.sourcebuddy.Bootstrap;
import com.ibasco.sourcebuddy.annotations.AbstractComponent;
import com.ibasco.sourcebuddy.annotations.AbstractController;
import com.ibasco.sourcebuddy.annotations.AbstractService;
import com.ibasco.sourcebuddy.components.NotificationManager;
import com.ibasco.sourcebuddy.components.SpringHelper;
import com.ibasco.sourcebuddy.model.ServerDetailsModel;
import com.ibasco.sourcebuddy.repository.ServerDetailsRepository;
import com.ibasco.sourcebuddy.repository.impl.CustomRepositoryImpl;
import com.ibasco.sourcebuddy.util.ResourceUtil;
import com.ibasco.sourcebuddy.util.preload.SteamAppsPreload;
import com.maxmind.geoip2.DatabaseReader;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.Scanner;
import java.util.concurrent.*;

@Configuration
@ComponentScan(
        basePackageClasses = {Bootstrap.class, NotificationManager.class, ServerDetailsModel.class, SteamAppsPreload.class, SpringHelper.class},
        includeFilters = {@ComponentScan.Filter({AbstractComponent.class, AbstractController.class, AbstractService.class})}
)
@EnableTransactionManagement
@EnableJpaAuditing(auditorAwareRef = "auditAwareBean")
@EnableJpaRepositories(
        repositoryBaseClass = CustomRepositoryImpl.class,
        considerNestedRepositories = true,
        basePackageClasses = ServerDetailsRepository.class
)
@EnableAsync
public class AppConfig implements AsyncConfigurer {

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    private static final String GEOIP_DATABASE_PATH = ResourceUtil.loadResource("/geoip/GeoLite2-City.mmdb").getPath();

    @Value("${app.public-api-endpoint}")
    private String publicIpApiEndpoint;

    private final int DEFAULT_THREADS = Runtime.getRuntime().availableProcessors() * 2;

    @Bean
    @Profile("test")
    public DataSource dataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:sourcebuddy;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("sa");
        return dataSource;
    }

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

    @Bean(destroyMethod = "shutdownNow")
    public ExecutorService taskExecutorService() {
        log.info("taskExecutorService() : Using default number of threads: {}", getPoolSize());
        return new ThreadPoolExecutor(getPoolSize(), Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new LinkedBlockingDeque<>(),
                                      taskThreadFactory());
    }

    private int getPoolSize() {
        return Runtime.getRuntime().availableProcessors() + 1;
    }

    @Bean(destroyMethod = "shutdownNow")
    public ExecutorService steamWebExecutorService() {
        return new ThreadPoolExecutor(getPoolSize(), Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new LinkedBlockingDeque<>(),
                                      steamWebApiThreadFactory());
    }

    @Bean(destroyMethod = "shutdownNow")
    public ExecutorService sourceQueryExecutorService() {
        return new ThreadPoolExecutor(getPoolSize(), Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new LinkedBlockingDeque<>(),
                                      sourceQueryThreadFactory());
    }

    @Bean(destroyMethod = "shutdownNow")
    public ScheduledExecutorService scheduledTaskService() {
        return new ScheduledThreadPoolExecutor(DEFAULT_THREADS, scheduledTasksThreadFactory());
    }

    @Bean
    public ThreadFactory steamWebApiThreadFactory() {
        return r -> {
            Thread thread = new Thread(steamWebApiThreadGroup(), r);
            thread.setDaemon(true);
            thread.setName(String.format("sb-steam-web-%d", thread.getId()));
            return thread;
        };
    }

    @Bean
    public ThreadFactory sourceQueryThreadFactory() {
        return r -> {
            Thread thread = new Thread(sourceQueryThreadGroup(), r);
            thread.setDaemon(true);
            thread.setName(String.format("sb-sourcequery-%d", thread.getId()));
            return thread;
        };
    }

    @Bean
    public ThreadFactory taskThreadFactory() {
        return r -> {
            Thread thread = new Thread(taskThreadGroup(), r);
            //thread.setDaemon(true);
            thread.setName(String.format("sb-task-%d", thread.getId()));
            return thread;
        };
    }

    @Bean
    public ThreadFactory scheduledTasksThreadFactory() {
        return r -> {
            Thread thread = new Thread(scheduledTasksThreadGroup(), r);
            thread.setName(String.format("sb-sch-task-%d", thread.getId()));
            return thread;
        };
    }

    @Bean
    public HttpClient httpClient() {
        HttpClient.Builder builder = HttpClient.newBuilder().executor(taskExecutorService());
        return builder.build();
    }

    @Bean
    public ThreadGroup scheduledTasksThreadGroup() {
        return new ThreadGroup("sb-scheduled-tasks");
    }

    @Bean
    public ThreadGroup steamWebApiThreadGroup() {
        return new ThreadGroup("sb-steam-webapi");
    }

    @Bean
    public ThreadGroup sourceQueryThreadGroup() {
        return new ThreadGroup("sb-source-query");
    }

    @Bean
    public ThreadGroup taskThreadGroup() {
        return new ThreadGroup("sb-core-tasks");
    }

    @Bean
    public DatabaseReader geoIpDatabaseReader() throws IOException {
        return new DatabaseReader.Builder(new File(GEOIP_DATABASE_PATH)).build();
    }

    @Override
    public Executor getAsyncExecutor() {
        return taskExecutorService();
    }

    private class PublicIp {

        private String ipAddress;
    }
}
