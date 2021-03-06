/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.vega.impl.webservice;

import io.vertigo.core.AppListener;
import io.vertigo.core.Home;
import io.vertigo.core.spaces.component.ComponentSpace;
import io.vertigo.core.spaces.definiton.DefinitionSpace;
import io.vertigo.lang.Assertion;
import io.vertigo.vega.plugins.webservice.handler.AccessTokenWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.CorsAllowerWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.ExceptionWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.HandlerChain;
import io.vertigo.vega.plugins.webservice.handler.JsonConverterWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.PaginatorAndSortWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.RateLimitingWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.RestfulServiceWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.SecurityWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.SessionInvalidateWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.SessionWebServiceHandlerPlugin;
import io.vertigo.vega.plugins.webservice.handler.ValidatorWebServiceHandlerPlugin;
import io.vertigo.vega.webservice.WebServiceManager;
import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.metamodel.WebServiceDefinition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

/**
 * Webservice manager.
 * Use some plugins :
 * - WebServiceIntrospectorPlugin : introspect WebService and register WebServiceDefinitions
 * - RoutesRegisterPlugin : Register WebServiceDefinitions to Routing engine (Jersey, Spark or other)
 * - List<WebServiceHandlerPlugin> : Ordered handlers list to managed : request to WebService impl and callback response
 *
 * @author npiedeloup
 */
public final class WebServiceManagerImpl implements WebServiceManager {

	private static final String STANDARD_REST_HANDLER_PLUGINS_SETTINGS_MSG = "Standard configuration (order is important) :\n"
			+ "- " + ExceptionWebServiceHandlerPlugin.class.getSimpleName() + "\n"
			+ "- " + CorsAllowerWebServiceHandlerPlugin.class.getSimpleName() + "\n"
			+ "- " + SessionInvalidateWebServiceHandlerPlugin.class.getSimpleName() + "\n"
			+ "- " + SessionWebServiceHandlerPlugin.class.getSimpleName() + "\n"
			+ "- " + RateLimitingWebServiceHandlerPlugin.class.getSimpleName() + "\n"
			+ "- " + SecurityWebServiceHandlerPlugin.class.getSimpleName() + "\n"
			+ "- " + AccessTokenWebServiceHandlerPlugin.class.getSimpleName() + "\n"
			+ "- " + JsonConverterWebServiceHandlerPlugin.class.getSimpleName() + "\n"
			+ "- " + PaginatorAndSortWebServiceHandlerPlugin.class.getSimpleName() + "\n"
			+ "- " + ValidatorWebServiceHandlerPlugin.class.getSimpleName() + "\n"
			+ "- " + RestfulServiceWebServiceHandlerPlugin.class.getSimpleName() + "\n";

	private final WebServiceIntrospectorPlugin webServiceIntrospectorPlugin;
	private final WebServerPlugin webServerPlugin;
	private final HandlerChain handlerChain;

	/**
	 * Constructor.
	 * @param webServiceIntrospectorPlugin WebServiceIntrospector Plugin
	 * @param webServerPlugin WebServer use to serve routes
	 * @param restHandlerPlugins WebServiceHandler plugins
	 */
	@Inject
	public WebServiceManagerImpl(
			final WebServiceIntrospectorPlugin webServiceIntrospectorPlugin,
			final WebServerPlugin webServerPlugin,
			final List<WebServiceHandlerPlugin> restHandlerPlugins) {
		Assertion.checkNotNull(webServiceIntrospectorPlugin);
		Assertion.checkNotNull(webServerPlugin);
		Assertion.checkArgument(!restHandlerPlugins.isEmpty(), "No WebServiceHandlerPlugins found, check you have declared your WebServiceHandlerPlugins in RestManagerImpl.\n{0}", STANDARD_REST_HANDLER_PLUGINS_SETTINGS_MSG);
		Assertion.checkNotNull(webServerPlugin);
		//-----
		final List<WebServiceHandlerPlugin> sortedWebServiceHandlerPlugins = sortWebServiceHandlerPlugins(restHandlerPlugins);
		//-----
		Assertion.checkArgument(sortedWebServiceHandlerPlugins.get(sortedWebServiceHandlerPlugins.size() - 1) instanceof RestfulServiceWebServiceHandlerPlugin,
				"WebServiceHandlerPlugins must end with a RestfulServiceHandler in order to dispatch request to WebService, check your WebServiceHandlerPlugins in RestManagerImpl.\n{0}", STANDARD_REST_HANDLER_PLUGINS_SETTINGS_MSG);
		//-----
		this.webServiceIntrospectorPlugin = webServiceIntrospectorPlugin;
		this.webServerPlugin = webServerPlugin;
		handlerChain = new HandlerChain(sortedWebServiceHandlerPlugins);
		//we do nothing with webServerPlugin
		Home.getApp().registerAppListener(new AppListener() {
			/** {@inheritDoc} */
			@Override
			public void onPostStart() {
				final List<WebServiceDefinition> webServiceDefinitions = WebServiceManagerImpl.this.scanComponents(Home.getComponentSpace());
				WebServiceManagerImpl.this.registerWebServiceDefinitions(Home.getDefinitionSpace(), webServiceDefinitions);
			}
		});
	}

	private static List<WebServiceHandlerPlugin> sortWebServiceHandlerPlugins(final List<WebServiceHandlerPlugin> restHandlerPlugins) {
		final List<WebServiceHandlerPlugin> sortedWebServiceHandlerPlugins = new ArrayList<>();
		WebServiceHandlerPlugin restfulServiceWebServiceHandlerPlugin = null;
		for (final WebServiceHandlerPlugin restHandlerPlugin : restHandlerPlugins) {
			if (restHandlerPlugin instanceof RestfulServiceWebServiceHandlerPlugin) {
				restfulServiceWebServiceHandlerPlugin = restHandlerPlugin;
			} else {
				sortedWebServiceHandlerPlugins.add(restHandlerPlugin);
			}
		}
		//Rule : RestfulServiceWebServiceHandlerPlugin is at the end
		if (restfulServiceWebServiceHandlerPlugin != null) {
			sortedWebServiceHandlerPlugins.add(restfulServiceWebServiceHandlerPlugin);
		}
		return sortedWebServiceHandlerPlugins;
	}

	/**
	 * Scan WebServices as WebServiceDefinitions on all the components.
	 * @param componentSpace ComponentSpace
	 * @return Scanned webServiceDefinitions
	 */
	List<WebServiceDefinition> scanComponents(final ComponentSpace componentSpace) {
		final List<WebServiceDefinition> allWebServiceDefinitions = new ArrayList<>();

		//1- We introspect all RestfulService class
		for (final String componentId : componentSpace.keySet()) {
			final Object component = componentSpace.resolve(componentId, Object.class);
			if (component instanceof WebServices) {
				final List<WebServiceDefinition> webServiceDefinitions = webServiceIntrospectorPlugin.instrospectWebService(((WebServices) component).getClass());
				allWebServiceDefinitions.addAll(webServiceDefinitions);
			}
		}

		//2- We sort by path, parameterized path should be after strict path
		Collections.sort(allWebServiceDefinitions, new WebServiceDefinitionComparator());
		return allWebServiceDefinitions;
	}

	/**
	 * Register WebServiceDefinitions to DefinitionSpace.
	 * @param definitionSpace DefinitionSpace
	 * @param webServiceDefinitions WebServiceDefinitions
	 */
	void registerWebServiceDefinitions(final DefinitionSpace definitionSpace, final List<WebServiceDefinition> webServiceDefinitions) {
		// We register WebService Definition in this order
		for (final WebServiceDefinition webServiceDefinition : webServiceDefinitions) {
			definitionSpace.put(webServiceDefinition);
		}
		webServerPlugin.registerWebServiceRoute(handlerChain, webServiceDefinitions);
	}

	private static final class WebServiceDefinitionComparator implements Comparator<WebServiceDefinition>, Serializable {
		private static final long serialVersionUID = -3628192753809615711L;

		WebServiceDefinitionComparator() {
			//rien
		}

		/** {@inheritDoc} */
		@Override
		public int compare(final WebServiceDefinition webServiceDefinition1, final WebServiceDefinition webServiceDefinition2) {
			return webServiceDefinition1.getPath().compareTo(webServiceDefinition2.getPath());
		}
	}
}
