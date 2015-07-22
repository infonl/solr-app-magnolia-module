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

import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.AppView;
import info.magnolia.ui.api.app.SubAppDescriptor;
import info.magnolia.ui.api.location.DefaultLocation;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.framework.app.BaseApp;

import javax.inject.Inject;

/**
 * Solr App class that opens all configured sub apps in tabs.
 */
public class SolrApp extends BaseApp {

	public static final String APP_NAME = "solr";

	@Inject
	public SolrApp(AppContext appContext, AppView view) {
		super(appContext, view);
	}

	/**
	 * Similar to e.g. the SecurityApp class all configured sub apps are opened; the first one is opened last and is the
	 * one the user will see when he/she first opens the app.
	 *
	 * @param location the app location
	 */
	@Override
	public void start(Location location) {
		super.start(location);
		SubAppDescriptor first = null;
		for (SubAppDescriptor subAppDescriptor : appContext.getAppDescriptor().getSubApps().values()) {
			if (first == null) {
				first = subAppDescriptor;
				continue;
			}
			getAppContext().openSubApp(new DefaultLocation(Location.LOCATION_TYPE_APP, APP_NAME, subAppDescriptor.getName()));
		}
		getAppContext().openSubApp(new DefaultLocation(Location.LOCATION_TYPE_APP, APP_NAME, first.getName()));
	}
}
