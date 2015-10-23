/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Edgar Vonk, Info.nl
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package nl.info.magnolia.solr.app;

import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import info.magnolia.commands.CommandsManager;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.search.solrsearchprovider.MagnoliaSolrBridge;
import info.magnolia.search.solrsearchprovider.MagnoliaSolrSearchProviderModule;
import info.magnolia.search.solrsearchprovider.config.SolrServerConfig;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.framework.app.BaseSubApp;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Solr Management sub app definition. Implements all supported sub app actions.
 */
public class SolrManagementSubApp extends BaseSubApp<SolrManagementAppView> implements SolrManagementAppView.Listener {
	private static final Logger LOG = LoggerFactory.getLogger(SolrManagementSubApp.class);

	public static final String SOLR_APP_COMMAND_CATALOG_NAME = "solr-app";
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
	private SolrServerConfig solrServerConfig;
	private boolean solrServerUp;
	private final SimpleTranslator i18n;
	private SolrManagementAppView view;

	@Inject
	public SolrManagementSubApp(SubAppContext subAppContext, SolrManagementAppView view,
	                            CommandsManager commandsManager, MagnoliaSolrBridge magnoliaSolrBridge,
	                            MagnoliaSolrSearchProviderModule magnoliaSolrSearchProviderModule, SimpleTranslator i18n) {
		super(subAppContext, view);
		this.commandsManager = commandsManager;
		this.magnoliaSolrBridge = magnoliaSolrBridge;
		this.solrServerConfig = magnoliaSolrSearchProviderModule.getSolrConfig();
		this.i18n = i18n;
		this.view = view;

		solrServerUp = isSolrServerUp();
		prepareView(view);
	}

	protected void prepareView(SolrManagementAppView view) {
		viewData.removeItemProperty(SOLR_SERVER_STATUS);
		viewData.removeItemProperty(SOLR_SERVER_NUMBER_OF_DOCUMENTS);
		String solrServerUrl = solrServerConfig.getBaseURL();
		String solrServerStatus = i18n.translate("solr.app.serverInformation")
				  + " " + solrServerUrl + " " +
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
			commandsManager.executeCommand(SOLR_APP_COMMAND_CATALOG_NAME, CLEAR_SOLR_COMMAND_NAME, null);
		} catch (Exception e) {
			LOG.error("Failed to execute '{}' command in catalog '{}'", CLEAR_SOLR_COMMAND_NAME,
					  SOLR_APP_COMMAND_CATALOG_NAME, e);
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
			numberOfDocuments = magnoliaSolrBridge.getSolrClient().query(query).getResults().getNumFound();
		} catch (SolrServerException|IOException e) {
			LOG.error("Failed to perform query on Solr server. Returning '0' documents as default.", e);
		}
		return numberOfDocuments;
	}

	protected boolean isSolrServerUp() {
		boolean isSolrServerUp = false;
		try {
			isSolrServerUp = this.magnoliaSolrBridge.testServerConnection();
		} catch (HttpSolrClient.RemoteSolrException e) {
			LOG.error("Failed to connect to Solr server {}", this.magnoliaSolrBridge.getSolrClient(), e);
		}
		return isSolrServerUp;
	}
}
