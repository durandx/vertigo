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
package io.vertigo.dynamo.plugins.store.filestore.db;

import io.vertigo.core.Home;
import io.vertigo.dynamo.domain.metamodel.DataStream;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.FileInfoURI;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.metamodel.FileInfoDefinition;
import io.vertigo.dynamo.file.model.FileInfo;
import io.vertigo.dynamo.file.model.InputStreamBuilder;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.dynamo.impl.store.filestore.FileStorePlugin;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.lang.Assertion;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.inject.Inject;

/**
 * Permet de gérer les accès atomiques à n'importe quel type de stockage SQL/
 * non SQL pour les traitements de FileInfo.
 *
 * @author pchretien, npiedeloup
 */
public final class DbFileStorePlugin implements FileStorePlugin {
	private static final String STORE_READ_ONLY = "Le store est en readOnly";

	/**
	 * Liste des champs du Dto de stockage.
	 * Ces champs sont obligatoire sur les Dt associés aux fileInfoDefinitions
	 * @author npiedeloup
	 */
	private enum DtoFields {
		FILE_NAME, MIME_TYPE, LAST_MODIFIED, LENGTH, FILE_DATA
	}

	/**
	 * Le store est-il en mode readOnly ?
	 */
	private final boolean readOnly;
	private final FileManager fileManager;

	/**
	 * Constructeur.
	 * @param fileManager Manager de gestion des fichiers
	 */
	@Inject
	public DbFileStorePlugin(final FileManager fileManager) {
		Assertion.checkNotNull(fileManager);
		//-----
		readOnly = false;
		this.fileManager = fileManager;
	}

	/** {@inheritDoc} */
	@Override
	public FileInfo load(final FileInfoURI uri) {
		final URI<DtObject> dtoUri = createDtObjectURI(uri);
		final DtObject fileInfoDto = getStoreManager().getDataStore().get(dtoUri);
		final InputStreamBuilder inputStreamBuilder = new DataStreamInputStreamBuilder((DataStream) getValue(fileInfoDto, DtoFields.FILE_DATA));
		final String fileName = (String) getValue(fileInfoDto, DtoFields.FILE_NAME);
		final String mimeType = (String) getValue(fileInfoDto, DtoFields.MIME_TYPE);
		final Date lastModified = (Date) getValue(fileInfoDto, DtoFields.LAST_MODIFIED);
		final Long length = (Long) getValue(fileInfoDto, DtoFields.LENGTH);
		final VFile vFile = fileManager.createFile(fileName, mimeType, lastModified, length, inputStreamBuilder);
		//TODO passer par une factory de FileInfo à partir de la FileInfoDefinition (comme DomainFactory)
		return new DatabaseFileInfo(uri.<FileInfoDefinition> getDefinition(), vFile);
	}

	private static DtObject createFileInfoDto(final FileInfo fileInfo) {
		final DtObject fileInfoDto = createDtObject(fileInfo.getDefinition());
		//-----
		final VFile vFile = fileInfo.getVFile();
		setValue(fileInfoDto, DtoFields.FILE_NAME, vFile.getFileName());
		setValue(fileInfoDto, DtoFields.MIME_TYPE, vFile.getMimeType());
		setValue(fileInfoDto, DtoFields.LAST_MODIFIED, vFile.getLastModified());
		setValue(fileInfoDto, DtoFields.LENGTH, vFile.getLength());
		setValue(fileInfoDto, DtoFields.FILE_DATA, new FileInfoDataStream(vFile));

		if (fileInfo.getURI() != null) {
			setPkValue(fileInfoDto, fileInfo.getURI().getKey());
		}
		return fileInfoDto;
	}

	/** {@inheritDoc} */
	@Override
	public void create(final FileInfo fileInfo) {
		Assertion.checkArgument(!readOnly, STORE_READ_ONLY);
		Assertion.checkNotNull(fileInfo.getURI() == null, "Only file without any id can be created.");
		//-----
		final DtObject fileInfoDto = createFileInfoDto(fileInfo);
		//-----
		getStoreManager().getDataStore().create(fileInfoDto);
		//-----
		final Object fileInfoDtoId = DtObjectUtil.getId(fileInfoDto);
		Assertion.checkNotNull(fileInfoDtoId, "ID  du fichier doit être renseignée.");
		final FileInfoURI uri = createURI(fileInfo.getDefinition(), fileInfoDtoId);
		fileInfo.setURIStored(uri);
	}

	/** {@inheritDoc} */
	@Override
	public void update(final FileInfo fileInfo) {
		Assertion.checkArgument(!readOnly, STORE_READ_ONLY);
		Assertion.checkNotNull(fileInfo.getURI() != null, "Only file with an id can be updated.");
		//-----
		final DtObject fileInfoDto = createFileInfoDto(fileInfo);
		//-----
		getStoreManager().getDataStore().update(fileInfoDto);
	}

	private static FileInfoURI createURI(final FileInfoDefinition fileInfoDefinition, final Object key) {
		return new FileInfoURI(fileInfoDefinition, key);
	}

	/** {@inheritDoc} */
	@Override
	public void remove(final FileInfoURI uri) {
		Assertion.checkArgument(!readOnly, STORE_READ_ONLY);
		//-----
		final URI dtoUri = createDtObjectURI(uri);
		getStoreManager().getDataStore().delete(dtoUri);
	}

	/**
	 * Création d'une URI de DTO à partir de l'URI de FileInfo
	 * @param uri URI de FileInfo
	 * @return URI du DTO utilisé en BDD pour stocker.
	 */
	private static URI<DtObject> createDtObjectURI(final FileInfoURI uri) {
		Assertion.checkNotNull(uri, "uri du fichier doit être renseignée.");
		//-----
		final FileInfoDefinition fileInfoDefinition = uri.<FileInfoDefinition> getDefinition();
		final String fileDefinitionRoot = fileInfoDefinition.getRoot();
		//Pour ce fileStore, on utilise le root de la fileDefinition comme nom de la table de stockage.
		//Il doit exister un DtObjet associé, avec la structure attendue.
		final DtDefinition dtDefinition = Home.getDefinitionSpace().resolve(fileDefinitionRoot, DtDefinition.class);
		return new URI<>(dtDefinition, uri.getKey());
	}

	/**
	 * Création d'un DTO à partir d'une definition de FileInfo
	 * @param fileInfoDefinition Definition de FileInfo
	 * @return DTO utilisé en BDD pour stocker.
	 */
	private static DtObject createDtObject(final FileInfoDefinition fileInfoDefinition) {
		Assertion.checkNotNull(fileInfoDefinition, "fileInfoDefinition du fichier doit être renseignée.");
		//-----
		final String fileDefinitionRoot = fileInfoDefinition.getRoot();
		//Pour ce fileStore, on utilise le root de la fileDefinition comme nom de la table de stockage.
		//Il doit exister un DtObjet associé, avec la structure attendue.
		final DtDefinition dtDefinition = Home.getDefinitionSpace().resolve(fileDefinitionRoot, DtDefinition.class);
		return DtObjectUtil.createDtObject(dtDefinition);
	}

	/**
	 * Retourne une valeur d'un champ à partir du DtObject.
	 *
	 * @param dto DtObject
	 * @param field Nom du champs
	 * @return Valeur typé du champ
	 */
	private static Object getValue(final DtObject dto, final DtoFields field) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		final DtField dtField = dtDefinition.getField(field.name());
		return dtField.getDataAccessor().getValue(dto);
	}

	/**
	 * Fixe une valeur d'un champ d'un DtObject.
	 *
	 * @param dto DtObject
	 * @param field Nom du champs
	 * @param value Valeur
	 */
	private static void setValue(final DtObject dto, final DtoFields field, final Object value) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		final DtField dtField = dtDefinition.getField(field.name());
		dtField.getDataAccessor().setValue(dto, value);
	}

	private static void setPkValue(final DtObject dto, final Object value) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		final DtField dtField = dtDefinition.getIdField().get();
		dtField.getDataAccessor().setValue(dto, value);
	}

	private static final class FileInfoDataStream implements DataStream {
		private final VFile vFile;

		FileInfoDataStream(final VFile vFile) {
			this.vFile = vFile;
		}

		@Override
		public InputStream createInputStream() throws IOException {
			return vFile.createInputStream();
		}

		@Override
		public long getLength() {
			return vFile.getLength();
		}
	}

	private static final class DataStreamInputStreamBuilder implements InputStreamBuilder {
		private final DataStream dataStream;

		DataStreamInputStreamBuilder(final DataStream dataStream) {
			this.dataStream = dataStream;
		}

		/** {@inheritDoc} */
		@Override
		public InputStream createInputStream() throws IOException {
			return dataStream.createInputStream();
		}
	}

	private static StoreManager getStoreManager() {
		return Home.getComponentSpace().resolve(StoreManager.class);
	}
}
