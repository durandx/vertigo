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
package io.vertigo.util;

import io.vertigo.lang.Assertion;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements parsing of a date expression.
 * y=year, M=month, w=week
 * d=day, h=hour, m=minute, s= second
 * Mind the UpperCase : 'M'onth and 'm'inute !
 * now+1d
 * now-6d
 * now+2w
 * now-12M
 * now-2y
 * "06/12/2003", "dd/MM/yyyy"
 *
 * @author mlaroche
 */
final class DateQueryParserUtil {
	private static final Map<String, Integer> CALENDAR_UNITS = createCalendarUnits();
	private static final Pattern PATTERN = Pattern.compile("([0-9]{1,})([y,M, w,d,h,m,s]{1})");
	private static final String NOW = "now";

	private DateQueryParserUtil() {
		//private
	}

	private static Map<String, Integer> createCalendarUnits() {
		final Map<String, Integer> units = new HashMap<>(5);
		units.put("y", Calendar.YEAR);
		units.put("M", Calendar.MONTH);
		units.put("w", Calendar.WEEK_OF_YEAR);
		units.put("d", Calendar.DAY_OF_YEAR);
		units.put("h", Calendar.HOUR_OF_DAY);
		units.put("m", Calendar.MINUTE);
		units.put("s", Calendar.SECOND);
		return units;
	}

	/**
	 * Retourne la date correspondant à l'expression passée en parametre.
	 * La syntaxe est de type now((+/-)eeeUNIT) ou une date au format dd/MM/yy
	 *
	 * @param dateQuery Expression
	 * @param datePattern Pattern used to define a date (dd/MM/YYYY)
	 * @return date
	 */
	static Date parse(final String dateQuery, final String datePattern) {
		Assertion.checkArgNotEmpty(dateQuery);
		Assertion.checkArgNotEmpty(datePattern, "you must define a valid datePattern such as dd/MM/yyyy or MM/dd/yy");
		// ---
		if (NOW.equals(dateQuery)) {
			//today is gonna be the day
			return new Date();
		}
		if (dateQuery.startsWith(NOW)) {
			final int index = NOW.length();
			final char operator = dateQuery.charAt(index);
			final int sign;
			if ('+' == operator) {
				sign = 1;
			} else if ('-' == operator) {
				sign = -1;
			} else {
				throw new RuntimeException("a valid operator (+ or -) is expected :'" + operator + "' on " + dateQuery);
			}
			//---
			//operand = 21d
			final String operand = dateQuery.substring(index + 1);
			//NOW+21DAY or NOW-12MONTH
			final Matcher matcher = PATTERN.matcher(operand);
			Assertion.checkState(matcher.matches(), "Le second operande ne respecte pas le pattern {0}", PATTERN.toString());
			//---
			final int unitCount = sign * Integer.valueOf(matcher.group(1));
			final String calendarUnit = matcher.group(2);
			//We check that we have found a real unit Calendar and not 'NOW+15DAL'
			if (!CALENDAR_UNITS.containsKey(calendarUnit)) {
				throw new RuntimeException("unit '" + calendarUnit + "' is not allowed. You must use a unit among : " + CALENDAR_UNITS.keySet());
			}
			//---
			final Calendar calendar = new GregorianCalendar();
			calendar.add(CALENDAR_UNITS.get(calendarUnit), unitCount);
			return calendar.getTime();
		}

		//We are expecting a date respectig pattern
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);
		try {
			final Calendar calendar = new GregorianCalendar();
			calendar.setTime(simpleDateFormat.parse(dateQuery));
			return calendar.getTime();
		} catch (final ParseException e) {
			throw new RuntimeException("La date " + dateQuery + " ne respecte pas le pattern : " + simpleDateFormat.toPattern().toString());
		}

	}
}
