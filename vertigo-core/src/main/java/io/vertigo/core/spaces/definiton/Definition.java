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
package io.vertigo.core.spaces.definiton;

import java.util.regex.Pattern;

/**
 * Définition.
 * 
 * Les Définitions de service, de DT, les domaines, les Formatters sont des définitions.
 * De maniére plus générale tout élément qui sert établir le modèle est une définition.
 * Une définition sert à modéliser le métier.
 *
 * Une définition 
 *  - n'est pas serializable.
 *  - est invariante (non mutable) dans le temps.
 *  - est chargée au (re)démarrage du serveur.
 *  - possède un nom unique qui doit vérifier le pattern ci dessous
 *
 * @author  pchretien
 */
public interface Definition {
	char SEPARATOR = '_';
	/**
	 * Expression réguliére vérifiée par les noms des définitions.
	 * 2 exemples acceptés :
	 * TO_TO
	 * ou 
	 * TO_TO$TI_TI
	 */
	Pattern REGEX_DEFINITION_URN = Pattern.compile("[A-Z0-9_]{3,60}([$][A-Z0-9_]{3,60})?");

	/**
	 * @return Nom de la définition.
	 */
	String getName();
}
