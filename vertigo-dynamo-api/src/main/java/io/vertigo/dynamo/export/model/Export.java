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
package io.vertigo.dynamo.export.model;

import io.vertigo.lang.Assertion;

import java.util.ArrayList;
import java.util.List;

/**
 * Données à exporter.
 * @author pchretien
 */
public final class Export {
	/**
	 * Orientation des documents.
	 */
	public enum Orientation {
		/**Paysage.*/
		Landscape,
		/**Portait.*/
		Portait;
	}

	private final List<ExportSheet> sheets;

	private final ExportFormat format;
	private final String fileName;
	private final String title;
	private final String author;
	private final Export.Orientation orientation;

	/**
	 * Constructeur.
	 * @param format type du format de sortie. Ceci configurera le Handler de traitement de l'edition
	 * @param fileName nom du fichier de sortie.
	 */
	Export(final ExportFormat format, final String fileName, final String title, final String author, final Export.Orientation orientation, final List<ExportSheet> sheets) {
		Assertion.checkNotNull(format);
		Assertion.checkArgNotEmpty(fileName, "a fileName is required");
		//Assertion.checkNotNull(title);
		//Assertion.checkNotNull(author);
		Assertion.checkNotNull(orientation);
		Assertion.checkNotNull(sheets);
		//-----
		this.format = format;
		this.fileName = fileName;
		this.title = title;
		this.orientation = orientation;
		this.author = author;
		this.sheets = new ArrayList<>(sheets);
	}

	/**
	 * @return format de sortie du document
	 */
	public ExportFormat getFormat() {
		return format;
	}

	/**
	 * @return Nom du fichier
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @return Titre du document (Facultatif)
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return Auteur du document (Facultatif)
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @return Orientation du document (mode portrait si non renseigné)
	 */
	public Orientation getOrientation() {
		return orientation;
	}

	/**
	 * @return Liste des paramètres de données à exporter
	 */
	public List<ExportSheet> getSheets() {
		return sheets;
	}
}
