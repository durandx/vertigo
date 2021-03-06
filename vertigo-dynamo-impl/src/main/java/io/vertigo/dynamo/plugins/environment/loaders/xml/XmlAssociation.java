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
package io.vertigo.dynamo.plugins.environment.loaders.xml;

import io.vertigo.lang.Assertion;

/**
 * Association.
 * Une association est définie par deux noeuds A et B.
 * Chaque noeud possède des propriétés :
 *  - Navigabilité
 *  - Cardinalité (appelée multiplicité dans poweramc)
 *  - Libellé
 *  - Code de l'entité à laquelle est attachée le noeud.
 *
 *  D'autre part l'association possède son propre code.
 * @author pchretien, pforhan
 */
public final class XmlAssociation {
	private final String code;
	private final String packageName;

	private final String multiplicityA;
	private final String multiplicityB;

	private final boolean navigabilityA;
	private final boolean navigabilityB;

	private final String roleLabelA;
	private final String roleLabelB;

	private final String codeA;
	private final String codeB;

	/**
	 * Constructeur.
	 */
	public XmlAssociation(final String code, final String packageName, final String multiplicityA, final String multiplicityB, final String roleLabelA, final String roleLabelB, final String codeA, final String codeB, final boolean navigabilityA, final boolean navigabilityB) {
		Assertion.checkArgNotEmpty(code);
		//Assertion.notEmpty(packageName);
		Assertion.checkArgNotEmpty(multiplicityA);
		Assertion.checkArgNotEmpty(multiplicityB);
		Assertion.checkArgNotEmpty(roleLabelA);
		Assertion.checkArgNotEmpty(roleLabelB);
		Assertion.checkArgNotEmpty(codeA);
		Assertion.checkArgNotEmpty(codeB);
		//-----
		this.code = code;
		this.packageName = packageName;

		// On gère le cas du 1 tout seul.
		if ("1".equals(multiplicityA)) {
			this.multiplicityA = "1..1";
		} else {
			this.multiplicityA = multiplicityA;
		}
		if ("1".equals(multiplicityB)) {
			this.multiplicityB = "1..1";
		} else {
			this.multiplicityB = multiplicityB;
		}
		this.roleLabelA = roleLabelA;
		this.roleLabelB = roleLabelB;

		this.codeA = codeA;
		this.codeB = codeB;

		this.navigabilityA = navigabilityA;
		this.navigabilityB = navigabilityB;
	}

	/**
	 * Formatage du code : AAA_YYY_{CODE NAME}
	 * AAA = Trois première lettre du code de A
	 * BBB = Trois première lettre du code de B
	 * Le nom défini par l'utilisateur est facultatif.
	 *
	 * @return Code de l'association
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Le code suivant est déduit du code.
	 * @return Nom de l'association défini par l'utilisateur. (Peut être null)
	 */
	public String getCodeName() {
		if (code.length() > 8 && code.charAt(3) == '_' && code.charAt(7) == '_') {
			return code.substring(8);
		}
		return null;
	}

	/**
	 * @return Nom du package.
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * @return Multiplicité de A.
	 */
	public String getMultiplicityA() {
		return multiplicityA;
	}

	/**
	 * @return Multiplicité de B.
	 */
	public String getMultiplicityB() {
		return multiplicityB;
	}

	/**
	 * @return Navigabilité de A.
	 */
	public boolean isNavigableA() {
		return navigabilityA;
	}

	/**
	 * @return Navigabilité de B.
	 */
	public boolean isNavigableB() {
		return navigabilityB;
	}

	/**
	 * @return Libellé du noeud A.
	 */
	public String getRoleLabelA() {
		return roleLabelA;
	}

	/**
	 * @return Libellé du noeud B.
	 */
	public String getRoleLabelB() {
		return roleLabelB;
	}

	/**
	 * @return Code de l'entité A participant à l'association

	 */
	public String getCodeA() {
		return codeA;
	}

	/**
	 * @return Code de l'entité B participant à l'association.
	 */
	public String getCodeB() {
		return codeB;
	}
}
