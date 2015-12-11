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
package io.vertigo.commons.parser;

import io.vertigo.lang.Assertion;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The first rule that matches is taken.
 * If no rule is found then an notFoundException is thrown. 
 * 
 * @author pchretien
 */
public final class FirstOfRule implements Rule<Choice> {
	private final List<Rule<?>> rules;
	private final String expression;

	/**
	 * Constructor.
	 */
	public FirstOfRule(final Rule<?>... rules) {
		this(Arrays.asList(rules));
	}

	/**
	 * Constructor.
	 */
	public FirstOfRule(final List<Rule<?>> rules) {
		Assertion.checkNotNull(rules);
		//-----
		this.rules = Collections.unmodifiableList(rules);
		//---
		final StringBuilder buffer = new StringBuilder();
		for (final Rule<?> rule : rules) {
			if (buffer.length() > 0) {
				buffer.append(" | ");
			}
			buffer.append(rule.getExpression());
		}
		expression = buffer.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String getExpression() {
		return expression;
	}

	@Override
	public Parser<Choice> createParser() {
		return new Parser<Choice>() {
			private Choice result;

			/**
			 * @return the choice number that succeeded.
			 */
			@Override
			public Choice get() {
				return result;
			}

			/** {@inheritDoc} */
			@Override
			public int parse(final String text, final int start) throws NotFoundException {
				//Règle ayant été le plus profond
				NotFoundException best = null;
				int bestIndex = -1;
				for (int i = 0; i < rules.size(); i++) {
					try {
						final Parser<?> parser = rules.get(i).createParser();
						final int end = parser.parse(text, start);
						result = new Choice(i, parser.get());
						if (end < bestIndex) {
							throw best; //Si on a plus avancé avec une autre règle c'est que celle ci n'avance pas assez (typiquement une WhiteSpace seule, ou une OptionRule)
						}
						return end;
					} catch (final NotFoundException e) {
						if (e.getIndex() > bestIndex) {
							bestIndex = e.getIndex();
							best = e;
						}
						//Tant que l'on a des erreurs sur l'évaluation des règles
						//on recommence jusqu'à trouver la première qui fonctionne.
					}
				}
				//Nothing has been found
				if (best == null) {
					throw new NotFoundException(text, start, null, "No rule found when evalutating  FirstOf : '{0}'", getExpression());
				}
				throw best;
			}
		};
	}
}
