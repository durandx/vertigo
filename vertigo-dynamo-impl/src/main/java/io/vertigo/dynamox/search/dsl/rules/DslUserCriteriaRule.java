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

import io.vertigo.dynamox.search.dsl.definition.DslUserCriteria;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * One user criteria.
 * A user query is many DslUserCriteria.
 * @author npiedeloup
 */
final class DslUserCriteriaRule {

	/**
	 * Regexp to parse USER query string.
	 * User can use some elaticSearch syntax + - * ? ~ or and ( ) field: [ to ] { to }
	 * Any use of these commands but and or ( ) desactivate process of the word
	 * Like : Harry~ (Potter or Poter) -azkaban year:1998
	 *
	 * Multiple regexp match a set of 4 groups :
	 *  1 : index field (optional)
	 *  2 : pre-word (optional, non word, non space, non "[{( )
	 *  3 : user word (non space, non "[]{}() )
	 *  4 : post-word (optional, non word, non space, non "]}) )
	 *  OR
	 *  1 : index field (optional)
	 *  2 : "
	 *  3 : anythings
	 *  4 : "
	 *  OR
	 *  1 : index field (optional)
	 *  2 : [ or {
	 *  3 : anythings
	 *  4 : ] or }
	 *  OR
	 *  1 : index field (mandatory)
	 *  2 : (
	 *  3 : anythings
	 *  4 : )
	 */
	private final static String CRITERIA_VALUE_OTHER_FIELD_PATTERN_STRING = "(?:(\\S+:)(\\()([^\\\"]*)(\\)))"; //attention a bien avoir 4 groups
	private final static String CRITERIA_VALUE_QUOTED_PATTERN_STRING = "(?:(\\S+:)?(\\\")([^\\\"]*)(\\\"))";
	private final static String CRITERIA_VALUE_RANGE_PATTERN_STRING = "(?:(\\S+:)?([\\[\\{])([^\\]\\}]*)([\\]\\}]))";
	private final static String CRITERIA_VALUE_STAR_PATTERN_STRING = "(?:(\\S+:)?(^|[\\s]*)(\\*)($|[\\s]+))";
	//private final static String WORD_RESERVERD_PATTERN = "\\s\\+\\-\\=\\&\\|\\>\\<\\!\\(\\)\\{\\}\\[\\]\\^\\\"\\~\\*\\?\\:\\/\\\\";
	//private final static String PREFIX_RESERVERD_PATTERN = "^\\s\\\"\\[\\{\\]\\}():,";
	//private final static String SUFFIX_RESERVERD_PATTERN = "^\\s\\\"\\[\\{\\]\\}():,";
	//\p{Punct}:  !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~
	private final static String WORD_RESERVERD_PATTERN = "^\\s!\"#$%&'()*+,-./:;<=>?@[\\\\]^`{|}~"; //Punct sauf _
	private final static String PREFIX_RESERVERD_PATTERN = "\\+\\-\\!\\*\\?\\~\\^\\=\\>\\<";
	private final static String SUFFIX_RESERVERD_PATTERN = "\\+\\-\\!\\*\\?\\~\\^\\=\\>\\<";
	//private final static String NOT_WORD_PATTERN = "\\s\\\"\\[\\{\\]\\}():";
	//private final static String CRITERIA_VALUE_WORD_PATTERN_STRING = "(?:(\\S+:)?([^\\w" + NOT_WORD_PATTERN + "]*)([^" + NOT_WORD_PATTERN + "]+)([^\\w" + NOT_WORD_PATTERN + "]*))";
	private final static String CRITERIA_VALUE_WORD_PATTERN_STRING = "(?:(\\S+:)?([" + PREFIX_RESERVERD_PATTERN + "]*?)([" + WORD_RESERVERD_PATTERN + "]+)((?:[\\^\\~][0-9]+)|(?:[" + SUFFIX_RESERVERD_PATTERN + "]*)))";
	private final static String CRITERIA_VALUE_PATTERN_STRING = "(?:((?:\\s|^).*?)?)(?:" //group 1
			+ CRITERIA_VALUE_OTHER_FIELD_PATTERN_STRING // group 2-5
			+ "|" + CRITERIA_VALUE_QUOTED_PATTERN_STRING // group 6-9
			+ "|" + CRITERIA_VALUE_RANGE_PATTERN_STRING // group 10-13
			+ "|" + CRITERIA_VALUE_STAR_PATTERN_STRING // group 14-17
			+ "|" + CRITERIA_VALUE_WORD_PATTERN_STRING // group 18-21
			+ ")(\\S*)"; // group 22
	private final static Pattern CRITERIA_VALUE_PATTERN = Pattern.compile(CRITERIA_VALUE_PATTERN_STRING);

	/**
	 * @param userString User string
	 * @return Parsed list of DslUserCriteria
	 */
	static List<DslUserCriteria> parse(final String userString) {
		final List<DslUserCriteria> userCriteria = new ArrayList<>();
		//split space chars to add preModifier and postModifier
		final Matcher criteriaValueMatcher = CRITERIA_VALUE_PATTERN.matcher(userString);
		while (criteriaValueMatcher.find()) {
			final String preMissingPart = DslUtil.nullToEmpty(criteriaValueMatcher.group(1));
			final String postMissingPart = criteriaValueMatcher.group(22);
			//les capturing groups matchs par group de 4, on cherche le premier qui match 4 par 4
			//on se base sur le overridedPreModifier qui n'est jamais null mais vide si match
			int foundGroup = 2;
			for (int i = 0; i < criteriaValueMatcher.groupCount() / 4; i++) {
				if (criteriaValueMatcher.group(i * 4 + 3) != null) {
					foundGroup = i * 4 + 2;
					break; //found !!
				}
			}
			final String overridedFieldName = DslUtil.nullToEmpty(criteriaValueMatcher.group(foundGroup));
			final String overridedPreModifier = DslUtil.nullToEmpty(criteriaValueMatcher.group(foundGroup + 1));
			final String criteriaValue = DslUtil.nullToEmpty(criteriaValueMatcher.group(foundGroup + 2));
			final String overridedPostModifier = DslUtil.nullToEmpty(criteriaValueMatcher.group(foundGroup + 3));
			userCriteria.add(new DslUserCriteria(preMissingPart, overridedFieldName, overridedPreModifier, criteriaValue, overridedPostModifier, postMissingPart));
		}
		return userCriteria;
	}
}
