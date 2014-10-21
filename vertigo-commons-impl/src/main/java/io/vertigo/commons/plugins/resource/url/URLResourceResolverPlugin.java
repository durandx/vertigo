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
package io.vertigo.commons.plugins.resource.url;

import io.vertigo.commons.impl.resource.ResourceResolverPlugin;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Résolution des URL par le standard java.net.URL.
 *
 * @author npiedeloup
 */
public final class URLResourceResolverPlugin implements ResourceResolverPlugin {

	/** {@inheritDoc} */
	public Option<URL> resolve(final String resource) {
		Assertion.checkNotNull(resource);
		// ---------------------------------------------------------------------
		try {
			final URL url = new URL(resource);
			return checkUrlAvailable(url) ? Option.some(url) : Option.<URL> none();
		} catch (final MalformedURLException e) {
			return Option.none();
		}
	}

	private static boolean checkUrlAvailable(final URL url) {
		try (InputStream is = url.openStream()) {
			return is.read() > 0;
		} catch (final IOException e) {
			return false;
		}
	}
}
