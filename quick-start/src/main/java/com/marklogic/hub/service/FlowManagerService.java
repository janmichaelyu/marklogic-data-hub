package com.marklogic.hub.service;

import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.DatabaseClientFactory.Authentication;
import com.marklogic.hub.FlowManager;
import com.marklogic.hub.config.EnvironmentConfiguration;
import com.marklogic.hub.exception.FlowManagerException;
import com.marklogic.hub.flow.Flow;

@Service
public class FlowManagerService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FlowManagerService.class);
	
	@Autowired
	private EnvironmentConfiguration environmentConfiguration;
	
	public FlowManager getFlowManager() {
		
		Authentication authMethod = Authentication.valueOf(environmentConfiguration.getMLAuth().toUpperCase());
		DatabaseClient client = DatabaseClientFactory.newClient(environmentConfiguration.getMLHost(), 
				Integer.parseInt(environmentConfiguration.getMLRestPort()), environmentConfiguration.getMLUsername(), environmentConfiguration.getMLPassword(), 
				authMethod);
		return new FlowManager(client);
		
	}
	
	public List<Flow> getFlows() {
		FlowManager flowManager = getFlowManager();
		return flowManager.getFlows();
	}
	
	public Flow getFlow(String flowName) {
		FlowManager flowManager = getFlowManager();
		return flowManager.getFlow(flowName);
	}
	
	public void installFlow(Flow flow) {
		FlowManager flowManager = getFlowManager();
		flowManager.installFlow(flow);
	}
	
	public void uninstallFlow(String flowName) {
		FlowManager flowManager = getFlowManager();
		flowManager.uninstallFlow(flowName);
	}
	
	public void runFlow(Flow flow, int batchSize) {
		FlowManager flowManager = getFlowManager();
		try {
			flowManager.runFlow(flow, batchSize);
		} catch (XMLStreamException e) {
			throw new FlowManagerException(e.getMessage(), e);
		}
	}
	
	public void runFlowsInParallel(Flow... flows) {
		FlowManager flowManager = getFlowManager();
		flowManager.runFlowsInParallel(flows);
	}
	
	

}