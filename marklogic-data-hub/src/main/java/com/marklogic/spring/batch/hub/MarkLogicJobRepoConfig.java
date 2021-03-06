package com.marklogic.spring.batch.hub;

import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.helper.DatabaseClientConfig;
import com.marklogic.client.helper.DatabaseClientProvider;
import com.marklogic.spring.batch.config.support.BatchDatabaseClientProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MarkLogicJobRepoConfig extends FlowConfig {

    private DatabaseClientConfig getConfig() {
        DatabaseClientConfig config = new DatabaseClientConfig(
            hubConfig.host,
            hubConfig.jobPort,
            hubConfig.username,
            hubConfig.password
        );

        config.setDatabase(hubConfig.jobDbName);
        config.setAuthentication(DatabaseClientFactory.Authentication.valueOfUncased(hubConfig.jobAuthMethod.toLowerCase()));

        return config;
    }

    @Bean
    public DatabaseClientProvider databaseClientProvider() {
        DatabaseClientConfig config = getConfig();
        logger.info("Connecting to MarkLogic via: " + config);
        return new BatchDatabaseClientProvider(config);
    }

    @Bean
    public DatabaseClientProvider jobRepositoryDatabaseClientProvider() {
        DatabaseClientConfig config = getConfig();
        logger.info("Connecting to MarkLogic JobRepository via: " + config);
        return new BatchDatabaseClientProvider(config);
    }

}
