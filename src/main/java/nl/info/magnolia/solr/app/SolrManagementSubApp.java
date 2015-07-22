/*
 *  Solr App Magnolia Module
 *
 *  Copyright (C) 2015 Info.nl
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public
 *  License along with this program.  If not, see:
 *  <http://www.gnu.org/licenses/gpl-3.0.html>.
 */
package nl.info.magnolia.solr.app;

import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import info.magnolia.commands.CommandsManager;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.search.solrsearchprovider.MagnoliaSolrBridge;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.framework.app.BaseSubApp;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * Solr Management sub app definition. Implements all supported sub app actions.
 */
public class SolrManagementSubApp extends BaseSubApp<SolrManagementAppView> implements SolrManagementAppView.Listener {
	private static final Logger LOG = LoggerFactory.getLogger(SolrManagementSubApp.class);

	public static final String SOLR_COMMAND_CATALOG_NAME = "solr";
	public static final String CLEAR_SOLR_COMMAND_NAME = "clearSolr";
	public static final String CONTENT_INDEXER_COMMAND_CATALOG_NAME = "content-indexer";
	public static final String RUN_SOLR_CRAWLER_COMMAND_NAME = "crawlerIndexer";
	public static final String CRAWLER_CONFIG_COMMAND_PARAMETER = "crawlerConfig";
	public static final String SOLR_SERVER_NUMBER_OF_DOCUMENTS = "solrServerNumberOfDocuments";
	public static final String SOLR_SERVER_STATUS = "solrServerStatus";

	private CommandsManager commandsManager;
	// object to transport prepared data to the view
	protected Item viewData = new PropertysetItem();
	private MagnoliaSolrBridge magnoliaSolrBridge;
	private boolean solrServerUp;
	private final SimpleTranslator i18n;
	private SolrManagementAppView view;

	@Inject
	public SolrManagementSubApp(SubAppContext subAppContext, SolrManagementAppView view,
	                            CommandsManager commandsManager, MagnoliaSolrBridge magnoliaSolrBridge,
	                            SimpleTranslator i18n) {
		super(subAppContext, view);
		this.commandsManager = commandsManager;
		this.magnoliaSolrBridge = magnoliaSolrBridge;
		this.i18n = i18n;
		this.view = view;

		solrServerUp = isSolrServerUp();
		prepareView(view);
	}

	protected void prepareView(SolrManagementAppView view) {
		viewData.removeItemProperty(SOLR_SERVER_STATUS);
		viewData.removeItemProperty(SOLR_SERVER_NUMBER_OF_DOCUMENTS);
		String solrServerUrl = "";
		if (magnoliaSolrBridge.getSolrServer() instanceof HttpSolrServer) {
			solrServerUrl = " " + ((HttpSolrServer) magnoliaSolrBridge.getSolrServer()).getBaseURL();
		}
		String solrServerStatus = i18n.translate("solr.app.serverInformation")
				  + solrServerUrl + " " +
				  (solrServerUp ? i18n.translate("solr.app.serverInformation.running") :
				  i18n.translate("solr.app.serverInformation.notRunning"));
		viewData.addItemProperty(SOLR_SERVER_STATUS, new ObjectProperty<>(solrServerStatus));

		String solrIndexSize = i18n.translate("solr.app.serverInformation.indexSizeUnknown");
		if (solrServerUp) {
			solrIndexSize = Long.toString(getNumberOfDocumentsInSolrIndex());
		}
		viewData.addItemProperty(SOLR_SERVER_NUMBER_OF_DOCUMENTS,
				  new ObjectProperty<>(solrIndexSize));

		view.setDataSource(viewData);
		view.setListener(this);
	}

	@Override
	public void clearSolrIndex() {
		try {
			commandsManager.executeCommand(SOLR_COMMAND_CATALOG_NAME, CLEAR_SOLR_COMMAND_NAME, null);
		} catch (Exception e) {
			LOG.error("Failed to execute '{}' command in catalog '{}'", CLEAR_SOLR_COMMAND_NAME,
					  SOLR_COMMAND_CATALOG_NAME, e);
		} finally {
			refreshView();
		}
	}

	@Override
	public void runSolrCrawlerCommand(String crawlerConfigValue) {
		final Map<String, Object> params = new HashMap<>();
		params.put(CRAWLER_CONFIG_COMMAND_PARAMETER, crawlerConfigValue);
		try {
			commandsManager.executeCommand(CONTENT_INDEXER_COMMAND_CATALOG_NAME, RUN_SOLR_CRAWLER_COMMAND_NAME, params);
		} catch (Exception e) {
			LOG.error("Failed to execute '{}' command in catalog '{}'", RUN_SOLR_CRAWLER_COMMAND_NAME,
					  CONTENT_INDEXER_COMMAND_CATALOG_NAME, e);
		} finally {
			refreshView();
		}
	}

	/**
	 * Updates Solr server information for view and stops and starts the subapp to refresh the view.
	 *
	 * @param location location
	 */
	@Override
	public void locationChanged(Location location) {
		solrServerUp = isSolrServerUp();
		prepareView(view);
		stop();
		start(location);
	}

	@Override
	public void refreshView() {
		locationChanged(getCurrentLocation());
	}

	protected long getNumberOfDocumentsInSolrIndex() {
		long numberOfDocuments = 0;
		try {
			SolrQuery query = new SolrQuery("*:*");
			query.setRows(0); // don't actually request any data
			numberOfDocuments = magnoliaSolrBridge.getSolrServer().query(query).getResults().getNumFound();
		} catch (SolrServerException e) {
			LOG.error("Failed to perform query on Solr server. Returning '0' documents as default.", e);
		}
		return numberOfDocuments;
	}

	protected boolean isSolrServerUp() {
		boolean isSolrServerUp = false;
		try {
			isSolrServerUp = this.magnoliaSolrBridge.testServerConnection();
		} catch (HttpSolrServer.RemoteSolrException e) {
			LOG.error("Failed to connect to Solr server {}", this.magnoliaSolrBridge.getSolrServer(), e);
		}
		return isSolrServerUp;
	}
}
