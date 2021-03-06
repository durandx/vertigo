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
package io.vertigo.dynamox.domain.formatter;

import io.vertigo.core.Home;
import io.vertigo.core.locale.LocaleManager;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Formatter;
import io.vertigo.dynamo.domain.metamodel.FormatterException;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.JsonExclude;
import io.vertigo.lang.MessageText;
import io.vertigo.util.StringUtil;

import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Gestion des formattages de dates.
 * Args contient plusieurs arguments séparés par des points virgules ';'
 *
 * Le premier argument est obligatoire il représente le format d'affichage d'une date .
 * Les arguments suivants sont facultatifs ils représentent les autres formats de saisie autorisés.
 * Par défaut le premier format de saisie autorisé est le format d'affichage.
 * En effet, pour des raisons ergonomiques il est toujours préférable de pouvoir saisir ce qui est affiché.
 *
 * Exemple 1 d'argument : "dd/MM/yyyy "
 *  On affiche la date au format dd/MM/yyyy
 *  En saisie on autorise dd/MM/yyyy

 * Exemple 2 d'argument : "dd/MM/yyyy ; dd/MM/yy"
 *  On affiche la date au format dd/MM/yyyy
 *  En saisie on autorise dd/MM/yyyy et dd/MM/yy
 *
 * @author pchretien
 */
public final class FormatterDate implements Formatter {
	/**
	 * Année minimum tolérée pour les dates.
	 */
	public static final int MIN_YEAR = 1850;

	/**
	 * Année maximum tolérée pour les dates.
	 */
	public static final int MAX_YEAR = 2150;

	/**
	 * Format d'affichage de la date
	 */
	private final String pattern;

	/**
	 * Format(s) étendu(s) de la date en saisie.
	 * Cette variable n'est créée qu'au besoin.
	 */
	@JsonExclude
	private final List<MessageText> lstExFillInFormat;

	/**
	 * Constructeur.
	 */

	public FormatterDate(final String args) {
		// Les arguments ne doivent pas être vides.
		assertArgs(args != null);
		//-----
		final StringTokenizer st = new StringTokenizer(args, ";");

		//Affichage des dates renseignées
		assertArgs(st.hasMoreTokens());
		pattern = st.nextToken().trim();

		//Saisie des dates
		lstExFillInFormat = new java.util.ArrayList<>(st.countTokens() + 1);
		//Le format d'affichage est le premier format de saisie autorisé
		lstExFillInFormat.add(new MessageText(pattern, null));

		//Autres saisies autorisées (facultatifs)
		while (st.hasMoreTokens()) {
			lstExFillInFormat.add(new MessageText(st.nextToken().trim(), null));
		}
	}

	private static void assertArgs(final boolean test) {
		Assertion.checkArgument(test, "Les arguments pour la construction de FormatterDate sont invalides :format affichage;{autres formats de saisie}");
	}

	/** {@inheritDoc} */
	@Override
	public String valueToString(final Object objValue, final DataType dataType) {
		Assertion.checkArgument(dataType == DataType.Date, "Formatter ne s'applique qu'aux dates");
		//-----
		return dateToString((Date) objValue);
	}

	/** {@inheritDoc} */
	@Override
	public Object stringToValue(final String strValue, final DataType dataType) throws FormatterException {
		Assertion.checkArgument(dataType == DataType.Date, "Formatter ne s'applique qu'aux dates");
		//-----
		final String sValue = StringUtil.isEmpty(strValue) ? null : strValue.trim();

		return stringToDate(sValue);
	}

	/**
	 * Convertit une String en Date
	 * on utilise un format de présentation et un de saisie
	 * Si néanmoins la date est entre 0 et 9 alors on estime que la date est en 200x.
	 *
	 * @param dateString String
	 * @return Date
	 * @throws FormatterException Erreur de parsing
	 */
	private Date stringToDate(final String dateString) throws FormatterException {
		if (dateString == null) {
			return null;
		}
		final int length = dateString.length();
		for (int i = 0; i < length; i++) {
			if (Character.isLetter(dateString.charAt(i))) {
				//Le parser de date java est trop permissif, on réduit les caractères.
				throw new FormatterException(Resources.DYNAMOX_DATE_NOT_FORMATTED_LETTER);
			}
		}

		Date dateValue = null;
		//StringToDate renvoit null si elle n'a pas réussi à convertir la date
		for (int i = 0; i < lstExFillInFormat.size() && dateValue == null; i++) {
			dateValue = stringToDate(dateString, lstExFillInFormat.get(i));
		}

		//Si dateValue est null c'est que toutes les convertions ont échouées.
		if (dateValue == null) {
			throw new FormatterException(Resources.DYNAMOX_DATE_NOT_FORMATTED);
		}
		return dateValue;
	}

	/*
	 * Convertit une String en Date suivant un format de saisie
	 * Si la date est entre 0 et 9 alors on estime que la date est en 200x
	 *
	 * Cette méthode retourne null si la chaine n'a pas pu être convertie en date
	 */
	private static Date stringToDate(final String dateString, final MessageText dateFormat) {
		Date dateValue;

		//Formateur de date on le crée à chaque fois car l'implémentation de DateFormat est non synchronisé !
		final java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat(dateFormat.getDisplay(), getLocaleManager().getCurrentLocale());
		formatter.setLenient(false);

		try {
			final ParsePosition parsePosition = new ParsePosition(0);
			dateValue = formatter.parse(dateString, parsePosition);

			final Calendar calendar = Calendar.getInstance();
			calendar.setTime(dateValue);
			int year = calendar.get(Calendar.YEAR);
			// Rq : l'année 0 n'existe pas en java.
			if (year >= 0 && year <= 9) {
				year += 2000;
				calendar.set(Calendar.YEAR, year);
				dateValue = calendar.getTime();
			}
			//si le parsing n'a pas consommé toute la chaine, on refuse la converssion
			if (parsePosition.getIndex() != dateString.length()) {
				dateValue = null;
			}
			if (year < MIN_YEAR || year > MAX_YEAR) {
				dateValue = null;
			}
		} catch (final Exception e) {
			//Le parsing a échoué on retourne null
			dateValue = null;
		}
		return dateValue;
	}

	/**
	 * Formate une date en String.
	 * @param dateValue Date
	 *
	 * @return Date formattée
	 */
	private String dateToString(final Date dateValue) {
		final String dateString;
		if (dateValue == null) {
			dateString = ""; //Affichage d'une date non renseignée;
		} else {
			final java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat(pattern, getLocaleManager().getCurrentLocale());
			formatter.setLenient(false);
			dateString = formatter.format(dateValue);
		}
		return dateString;
	}

	private static LocaleManager getLocaleManager() {
		return Home.getComponentSpace().resolve(LocaleManager.class);
	}
}
