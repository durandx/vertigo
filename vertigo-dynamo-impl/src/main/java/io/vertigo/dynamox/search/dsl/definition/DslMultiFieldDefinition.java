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
package io.vertigo.dynamox.search.dsl.definition;

import io.vertigo.lang.Assertion;

import java.util.List;

/**
 * Multi fields definition.
 * (preBody)\[(fields)+,\](postBody)
 * @author npiedeloup
 */
public final class DslMultiFieldDefinition {
	private final String preBody;
	private final List<DslFieldDefinition> fields;
	private final String postBody;

	/**
	 * @param preBody String before body
	 * @param fields List of Index's fields
	 * @param postBody String after body
	 */
	public DslMultiFieldDefinition(final String preBody, final List<DslFieldDefinition> fields, final String postBody) {
		Assertion.checkNotNull(preBody);
		Assertion.checkNotNull(fields);
		Assertion.checkNotNull(postBody);
		//-----
		this.preBody = preBody;
		this.fields = fields;
		this.postBody = postBody;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder()
				.append(preBody).append("[");
		String sep = "";
		for (final DslFieldDefinition field : fields) {
			sb.append(sep).append(field);
			sep = ",";
		}
		sb.append("]").append(postBody);
		return sb.toString();
	}

	/**
	 * @return preBody
	 */
	public final String getPreBody() {
		return preBody;
	}

	/**
	 * @return fields
	 */
	public final List<DslFieldDefinition> getFields() {
		return fields;
	}

	/**
	 * @return postBody
	 */
	public final String getPostBody() {
		return postBody;
	}
}
