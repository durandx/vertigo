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
package io.vertigo.core.spaces.component;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Container;

import java.util.HashSet;
import java.util.Set;

/**
 * Conteneur permettant de compter les clés non utilisées.
 *
 * @author pchretien
 */
final class ComponentProxyContainer implements Container {
	private final Container container;
	private final Set<String> usedKeys = new HashSet<>();

	ComponentProxyContainer(final Container container) {
		Assertion.checkNotNull(container);
		//-----
		this.container = container;
	}

	/** {@inheritDoc} */
	@Override
	public boolean contains(final String id) {
		return container.contains(id);
	}

	/** {@inheritDoc} */
	@Override
	public <O> O resolve(final String id, final Class<O> componentClass) {
		final O result = container.resolve(id, componentClass);
		usedKeys.add(id);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> keySet() {
		return container.keySet();
	}

	Set<String> getUsedKeys() {
		return usedKeys;
	}
}
