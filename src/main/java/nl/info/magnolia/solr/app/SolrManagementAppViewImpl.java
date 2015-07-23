/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Info.nl
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
import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.module.indexer.ContentIndexerModule;
import info.magnolia.module.indexer.crawler.CrawlerConfig;
import info.magnolia.ui.vaadin.layout.SmallAppLayout;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * Solr Management App view implementation. Inspired by the AboutViewImpl class in the Magnolia About App.
 */
public class SolrManagementAppViewImpl implements SolrManagementAppView {
	private static final long serialVersionUID = 1L;

	public static final String STYLE_SECTION_TITLE = "section-title";
	public static final String STYLE_V_BUTTON_SMALLAPP = "v-button-smallapp";
	public static final String STYLE_COMMIT = "commit";
	public static final String STYLE_BUTTONS = "buttons";

	private final SmallAppLayout root = new SmallAppLayout();
	private Item dataSource;
	// make fields transient since we are a Serializable class but these fields are non-serializable
	private transient Map<String, Property.Viewer> dataBindings = new HashMap<>();
	private transient Listener listener;
	private transient SimpleTranslator i18n;
	private transient ContentIndexerModule contentIndexerModule;

	@Inject
	public SolrManagementAppViewImpl(final SimpleTranslator i18n, final ContentIndexerModule contentIndexerModule) {
		this.i18n = i18n;
		this.contentIndexerModule = contentIndexerModule;

		root.setDescription(i18n.translate("solr.app.management.description"));
		root.addSection(createSolrServerInformationSection());
		root.addSection(createSolrIndexManagementSection());
		root.addSection(createSolrCrawlerManagementSection());
	}

	protected Component createSolrServerInformationSection() {
		FormLayout layout = new FormLayout();

		Label serverInformationSectionTitle = new Label(i18n.translate("solr.app.serverInformation.label.title"));
		serverInformationSectionTitle.addStyleName(STYLE_SECTION_TITLE);

		// build and bind fields
		Component solrServerStatus = buildAndBind(SolrManagementSubApp.SOLR_SERVER_STATUS,
				  i18n.translate("solr.app.serverInformation.serverStatus"));
		Component nrDocumentsInIndex = buildAndBind(SolrManagementSubApp.SOLR_SERVER_NUMBER_OF_DOCUMENTS,
				  i18n.translate("solr.app.serverInformation.numberOfDocsInIndex"));

		Button refreshSolrServerStatusButton =
				  new Button(i18n.translate("solr.app.serverInformation.button.refreshSolrServerStatus.caption"),
							 event -> listener.refreshView()
				  );
		refreshSolrServerStatusButton.addStyleName(STYLE_V_BUTTON_SMALLAPP);
		refreshSolrServerStatusButton.addStyleName(STYLE_COMMIT);

		VerticalLayout buttons = new VerticalLayout();
		buttons.addStyleName(STYLE_BUTTONS);
		buttons.setSpacing(true);
		buttons.addComponent(refreshSolrServerStatusButton);

		layout.addComponent(serverInformationSectionTitle);
		layout.addComponent(solrServerStatus);
		layout.addComponent(nrDocumentsInIndex);
		layout.addComponent(buttons);

		return layout;
	}

	protected Component createSolrIndexManagementSection() {
		FormLayout layout = new FormLayout();

		Label manageSolrSectionTitle = new Label(i18n.translate("solr.app.manageSolrIndex.label.intro"));
		manageSolrSectionTitle.addStyleName(STYLE_SECTION_TITLE);
		layout.addComponent(manageSolrSectionTitle);
		layout.addComponent(new Label(i18n.translate("solr.app.manageSolrIndex.label.description")));

		Button clearSolrIndexButton =
				  new Button(i18n.translate("solr.app.management.button.clearSolrIndex.caption"),
						    event -> listener.clearSolrIndex()
				  );
		clearSolrIndexButton.addStyleName(STYLE_V_BUTTON_SMALLAPP);
		clearSolrIndexButton.addStyleName(STYLE_COMMIT);

		VerticalLayout buttons = new VerticalLayout();
		buttons.addStyleName(STYLE_BUTTONS);
		buttons.setSpacing(true);
		buttons.addComponent(clearSolrIndexButton);

		layout.addComponent(buttons);
		return layout;
	}

	protected Component createSolrCrawlerManagementSection() {
		FormLayout layout = new FormLayout();

		Label manageSolrSectionTitle = new Label(i18n.translate("solr.app.manageSolrCrawlers.label.intro"));
		manageSolrSectionTitle.addStyleName(STYLE_SECTION_TITLE);
		layout.addComponent(manageSolrSectionTitle);

		Map<String, CrawlerConfig> crawlers = contentIndexerModule.getCrawlers();
		if (crawlers.isEmpty()) {
			layout.addComponent(new Label(i18n.translate("solr.app.manageSolrCrawlers.noCrawlers.label.description")));
		} else {
			layout.addComponent(new Label(i18n.translate("solr.app.manageSolrCrawlers.label.description")));

			VerticalLayout buttons = new VerticalLayout();
			buttons.addStyleName(STYLE_BUTTONS);
			buttons.setSpacing(true);

			// only create buttons for crawlers that have been configured and are enabled in the content-indexer module.
			for (Map.Entry<String, CrawlerConfig> crawler : crawlers.entrySet()) {
				CrawlerConfig crawlerConfig = crawler.getValue();
				Button runSolrCrawlerButton =
						  new Button(i18n.translate("solr.app.management.button.runSolrCrawler.caption")
									 + " " + crawlerConfig.getName(),
								    event -> listener.runSolrCrawlerCommand(crawlerConfig.getName())
						  );
				runSolrCrawlerButton.addStyleName(STYLE_V_BUTTON_SMALLAPP);
				runSolrCrawlerButton.addStyleName(STYLE_COMMIT);
				buttons.addComponent(runSolrCrawlerButton);
			}
			layout.addComponent(buttons);
		}
		return layout;
	}

	@Override
	public void setListener(Listener listener) {
		this.listener = listener;
	}

	protected Component buildAndBind(String key, String caption) {
		Label field = new Label();
		field.setCaption(caption);
		dataBindings.put(key, field);
		return field;
	}

	@Override
	public void setDataSource(Item item) {
		this.dataSource = item;
		refresh();
	}

	protected void refresh() {
		for (Map.Entry<String, Property.Viewer> entry : dataBindings.entrySet()) {
			Property.Viewer field = entry.getValue();
			Property<?> property = dataSource.getItemProperty(entry.getKey());
			field.setPropertyDataSource(property);
		}
	}

	@Override
	public Component asVaadinComponent() {
		return root;
	}
}
