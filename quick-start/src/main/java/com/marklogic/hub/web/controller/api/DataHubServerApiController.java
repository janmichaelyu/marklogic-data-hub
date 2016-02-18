package com.marklogic.hub.web.controller.api;

import java.io.File;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.marklogic.hub.config.EnvironmentConfiguration;
import com.marklogic.hub.exception.DataHubException;
import com.marklogic.hub.model.DomainModel;
import com.marklogic.hub.service.DataHubService;
import com.marklogic.hub.service.DomainManagerService;
import com.marklogic.hub.web.bean.SyncStatusBean;
import com.marklogic.hub.web.controller.BaseController;
import com.marklogic.hub.web.form.LoginForm;

@RestController
@RequestMapping("/api/data-hub")
@Scope("session")
public class DataHubServerApiController extends BaseController {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(DataHubServerApiController.class);

	@Autowired
	private EnvironmentConfiguration environmentConfiguration;

	@Autowired
	private DataHubService dataHubService;

	@Autowired
	private DomainManagerService domainManagerService;
	
	@Autowired
	private SyncStatusBean syncStatus;

	@RequestMapping(value = "login", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_UTF8_VALUE }, produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public LoginForm postLogin(@RequestBody LoginForm loginForm,
			BindingResult bindingResult, HttpSession session,
			HttpServletRequest request) throws Exception {
		try {
			if (isValidDirectory(loginForm.getUserPluginDir())) {

				updateEnvironmentConfiguration(loginForm);

				loginForm.setInstalled(dataHubService.isInstalled());
				loginForm.setServerVersionAccepted(dataHubService
						.isServerAcceptable());
				loginForm.setHasErrors(false);
				loginForm.setLoggedIn(true);
				loginForm.setDomains(domainManagerService.getDomains());
				loginForm.setSelectedDomain(loginForm.getDomains() != null
						&& !loginForm.getDomains().isEmpty() ? loginForm
						.getDomains().get(0) : null);

				environmentConfiguration.saveConfigurationToFile();

				session.setAttribute("loginForm", loginForm);
			} else {
				loginForm.setLoggedIn(false);
				displayError(loginForm, null, null,
						loginForm.getUserPluginDir()
								+ " is not a valid directory.");
			}
		} catch (DataHubException e) {
			LOGGER.error("Login failed", e);
			loginForm.setLoggedIn(false);
			displayError(loginForm, null, null, e.getMessage());
		}

		return loginForm;
	}

	private boolean isValidDirectory(String userPluginDir) {
		File file = new File(userPluginDir);
		if (file.exists() && file.isDirectory()) {
			return true;
		}

		File parentFile = file.getParentFile();
		if (parentFile.exists() && parentFile.isDirectory()) {
			file.mkdir();
			return true;
		}
		return false;
	}

	@RequestMapping(value = "login", method = RequestMethod.GET)
	public LoginForm getLoginStatus(HttpSession session) {
		LoginForm loginForm = (LoginForm) session.getAttribute("loginForm");
		if (loginForm == null) {
			loginForm = new LoginForm();
			this.environmentConfiguration.loadConfigurationFromFile();
			this.retrieveEnvironmentConfiguration(loginForm);
			session.setAttribute("loginForm", loginForm);
		} else {
			loginForm.setDomains(domainManagerService.getDomains());
		}
		return loginForm;
	}

	@RequestMapping(value = "logout", method = RequestMethod.POST)
	public LoginForm postLogout(HttpSession session) {
		LoginForm loginForm = (LoginForm) session.getAttribute("loginForm");
		loginForm.setLoggedIn(false);
		this.environmentConfiguration.removeSavedConfiguration();
		this.retrieveEnvironmentConfiguration(loginForm);
		return loginForm;
	}

	@RequestMapping(value = "install", method = RequestMethod.POST)
	public void install() {
		dataHubService.install();
	}

	@RequestMapping(value = "uninstall", method = RequestMethod.POST)
	public void uninstall() {
		dataHubService.uninstall();
	}

	@RequestMapping(value = "install-user-modules", method = RequestMethod.POST)
	public Set<File> installUserModules(HttpSession session) {
	    synchronized (syncStatus) {
	        Set<File> files = dataHubService.installUserModules();

	        // refresh the list of domains saved in the session
	        List<DomainModel> domains = domainManagerService.getDomains();
	        LoginForm loginForm = (LoginForm) session.getAttribute("loginForm");
	        loginForm.refreshDomains(domains);

	        // set synched = true
	        for (DomainModel domainModel : loginForm.getDomains()) {
	            domainModel.setSynched(true);
	            domainModel.setInputFlowsSynched(true);
	            domainModel.setConformFlowsSynched(true);
	        }

	        syncStatus.clearModifications();
	        syncStatus.notifyAll();

	        return files;
	    }
	}

	private void updateEnvironmentConfiguration(LoginForm loginForm) {
		environmentConfiguration.setMLHost(loginForm.getMlHost());
		environmentConfiguration.setMLRestPort(loginForm.getMlRestPort());
		environmentConfiguration.setMLUsername(loginForm.getMlUsername());
		environmentConfiguration.setMLPassword(loginForm.getMlPassword());
		environmentConfiguration.setUserPluginDir(loginForm.getUserPluginDir());
	}

	private void retrieveEnvironmentConfiguration(LoginForm loginForm) {
		loginForm.setMlHost(environmentConfiguration.getMLHost());
		loginForm.setMlRestPort(environmentConfiguration.getMLRestPort());
		loginForm.setMlUsername(environmentConfiguration.getMLUsername());
		loginForm.setMlPassword(environmentConfiguration.getMLPassword());
		loginForm.setUserPluginDir(environmentConfiguration.getUserPluginDir());
	}
}