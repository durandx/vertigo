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
package io.vertigo.core;

import io.vertigo.core.spaces.component.ComponentSpace;
import io.vertigo.core.spaces.config.ConfigSpace;
import io.vertigo.core.spaces.definiton.DefinitionSpace;
import io.vertigo.lang.Assertion;

/**
 * Home : Classe d'entrée sur toutes les modules.
 * Life Cycle
 * starting ==> active ==> stopping ==> closed
 *
 * When error during starting
 * starting ==> stopping ==> closed ()
 *
 * 'starting' and 'stopping' sont are ephemeral transitions.
 *
 * @author pchretien
 */
public final class Home {
	private static App CURRENT_APP = null;

	private Home() {
		// Classe statique d'accès aux composants.
	}

	static void setApp(final App app) {
		CURRENT_APP = app;
	}

	/**
	 * @return Application
	 */
	public static App getApp() {
		Assertion.checkNotNull(CURRENT_APP, "app has not been started");
		return CURRENT_APP;
	}

	/**
	 * @return DefinitionSpace contains application's Definitions
	 */
	public static DefinitionSpace getDefinitionSpace() {
		return getApp().getDefinitionSpace();
	}

	/**
	 * @return ComponentSpace contains application's Components
	 */
	public static ComponentSpace getComponentSpace() {
		return getApp().getComponentSpace();
	}

	/**
	 * @return ConfigSpace contains application's parameters
	 */
	public static ConfigSpace getConfigSpace() {
		return getApp().getConfigSpace();
	}
}
