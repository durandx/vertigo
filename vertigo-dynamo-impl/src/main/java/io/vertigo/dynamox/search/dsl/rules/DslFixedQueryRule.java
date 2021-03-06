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
package io.vertigo.dynamox.search.dsl.rules;

import io.vertigo.commons.parser.AbstractRule;
import io.vertigo.commons.parser.Rule;
import io.vertigo.commons.parser.SequenceRule;
import io.vertigo.dynamox.search.dsl.definition.DslFixedQueryDefinition;

import java.util.List;

/**
 * Parsing rule for fixedQuery.
 * (fixedQuery)
 * @author npiedeloup
 */
final class DslFixedQueryRule extends AbstractRule<DslFixedQueryDefinition, List<?>> {
	/** {@inheritDoc} */
	@Override
	public String getExpression() {
		return "fixedQuery";
	}

	/** {@inheritDoc} */
	@Override
	protected Rule<List<?>> createMainRule() {
		return new SequenceRule(
				DslSyntaxRules.SPACES,//0
				DslSyntaxRules.FIXED_WORD);//1
	}

	/** {@inheritDoc} */
	@Override
	protected DslFixedQueryDefinition handle(final List<?> parsing) {
		final String preSpaces = (String) parsing.get(0);
		final String fixedQuery = (String) parsing.get(1);
		return new DslFixedQueryDefinition(DslUtil.concat(preSpaces, fixedQuery));
	}

}
