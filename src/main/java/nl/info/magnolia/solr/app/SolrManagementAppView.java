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
 *
 */
package nl.info.magnolia.solr.app;

import com.vaadin.data.Item;
import info.magnolia.ui.api.app.SubApp;
import info.magnolia.ui.api.view.View;

/**
 * Solr Management app view definition. Defines an interface for all available actions provided by the sub app.
 */
public interface SolrManagementAppView extends View {

	void setListener(Listener listener);
	void setDataSource(Item item);

	interface Listener extends SubApp {
		void clearSolrIndex();
		void runSolrCrawlerCommand(String crawlerConfigValue);
		void refreshView();
	}
}
