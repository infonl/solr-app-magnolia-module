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
