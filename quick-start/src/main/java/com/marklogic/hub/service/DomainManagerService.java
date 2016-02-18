package com.marklogic.hub.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.DatabaseClientFactory.Authentication;
import com.marklogic.client.MarkLogicServerException;
import com.marklogic.hub.DomainManager;
import com.marklogic.hub.config.EnvironmentConfiguration;
import com.marklogic.hub.domain.Domain;
import com.marklogic.hub.exception.DomainManagerException;
import com.marklogic.hub.factory.DomainModelFactory;
import com.marklogic.hub.model.DomainModel;
import com.marklogic.hub.util.FileUtil;

@Service
public class DomainManagerService {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(DomainManagerService.class);

    @Autowired
    private EnvironmentConfiguration environmentConfiguration;

    public DomainManager getDomainManager() {

        Authentication authMethod = Authentication
                .valueOf(environmentConfiguration.getMLAuth().toUpperCase());
        DatabaseClient client = DatabaseClientFactory.newClient(
                environmentConfiguration.getMLHost(),
                Integer.parseInt(environmentConfiguration.getMLRestPort()),
                environmentConfiguration.getMLUsername(),
                environmentConfiguration.getMLPassword(), authMethod);
        return new DomainManager(client);

    }

    public List<DomainModel> getDomains() {
        List<DomainModel> domains = new ArrayList<>();
        List<Domain> domainsInServer = this.getDomainsInServer();
        String domainsPath = FileUtil.createFolderIfNecessary(
                environmentConfiguration.getUserPluginDir(),
                FileUtil.DOMAINS_FOLDER);
        List<String> domainNames = FileUtil.listDirectFolders(domainsPath);
        DomainModelFactory domainModelFactory = new DomainModelFactory(
                domainsInServer);
        for (String domainName : domainNames) {
            LOGGER.debug("Domain : " + domainName);
            domains.add(domainModelFactory.createDomain(domainName, domainsPath
                    + File.separator + domainName));
        }
        return domains;
    }

    private List<Domain> getDomainsInServer() {
        List<Domain> domainsInServer = new ArrayList<>();
        try {
            DomainManager domainManager = getDomainManager();
            domainsInServer = domainManager.getDomains();
        } catch (Exception e) {
            // TODO catch this temporarily
            // This should not return an error as the deploy to server should
            // validate the plugins beforehand
        }
        return domainsInServer;
    }

    public Domain getDomain(String domainName) {
        DomainManager domainManager = getDomainManager();
        return domainManager.getDomain(domainName);
    }

    public DomainModel createDomain(String domainName, String inputFlowName,
            String conformFlowName) {
        DomainModelFactory domainModelFactory = new DomainModelFactory();
        DomainModel domainModel;
        try {
            domainModel = domainModelFactory.createNewDomain(
                    environmentConfiguration.getUserPluginDir(), domainName,
                    inputFlowName, conformFlowName);
        } catch (IOException e) {
            throw new DomainManagerException(e.getMessage(), e);
        }
        return domainModel;
    }
}