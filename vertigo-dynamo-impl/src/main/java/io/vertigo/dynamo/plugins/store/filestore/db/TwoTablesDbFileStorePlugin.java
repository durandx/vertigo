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
import io.vertigo.dynamo.domain.metamodel.DtFieldName;
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
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Permet de gérer le CRUD sur un fichier stocké sur deux tables (Méta données / Données).
 *
 * @author sezratty
 */
public final class TwoTablesDbFileStorePlugin extends AbstractDbFileStorePlugin implements FileStorePlugin {

	/**
	 * Liste des champs du Dto de stockage.
	 * Ces champs sont obligatoire sur les Dt associés aux fileInfoDefinitions
	 */
	private enum DtoFields implements DtFieldName {
		FILE_NAME, MIME_TYPE, LAST_MODIFIED, LENGTH, FILE_DATA, FMD_ID, FDT_ID
	}

	private final FileManager fileManager;
	private final DtDefinition storeMetaDataDtDefinition;
	private final DtDefinition storeFileDtDefinition;

	/**
	 * Constructor.
	 * @param name This store name
	 * @param storeMetaDataDtDefinitionName MetaData storing dtDefinition
	 * @param storeFileDtDefinitionName File storing dtDefinition
	 * @param fileManager Files manager
	 */
	@Inject
	public TwoTablesDbFileStorePlugin(@Named("name") final Option<String> name, @Named("storeMetaDataDtName") final String storeMetaDataDtDefinitionName, @Named("storeFileDtName") final String storeFileDtDefinitionName, final FileManager fileManager) {
		super(name);
		Assertion.checkNotNull(fileManager);
		//-----
		this.fileManager = fileManager;
		storeMetaDataDtDefinition = Home.getDefinitionSpace().resolve(storeMetaDataDtDefinitionName, DtDefinition.class);
		storeFileDtDefinition = Home.getDefinitionSpace().resolve(storeFileDtDefinitionName, DtDefinition.class);
	}

	/** {@inheritDoc} */
	@Override
	public FileInfo load(final FileInfoURI fileInfoUri) {
		checkDefinitionStoreBinding(fileInfoUri.getDefinition());
		// Ramène FileMetada
		final URI<DtObject> dtoMetaDataUri = new URI<>(storeMetaDataDtDefinition, fileInfoUri.getKey());
		final DtObject fileMetadataDto = getStoreManager().getDataStore().get(dtoMetaDataUri);
		final Object fdtId = getValue(fileMetadataDto, DtoFields.FDT_ID, Object.class);

		// Ramène FileData
		final URI<DtObject> dtoDataUri = new URI<>(storeFileDtDefinition, fdtId);

		final DtObject fileDataDto = getStoreManager().getDataStore().get(dtoDataUri);
		// Construction du vFile.
		final InputStreamBuilder inputStreamBuilder = new DataStreamInputStreamBuilder(getValue(fileDataDto, DtoFields.FILE_DATA, DataStream.class));
		final String fileName = getValue(fileMetadataDto, DtoFields.FILE_NAME, String.class);
		final String mimeType = getValue(fileMetadataDto, DtoFields.MIME_TYPE, String.class);
		final Date lastModified = getValue(fileMetadataDto, DtoFields.LAST_MODIFIED, Date.class);
		final Long length = getValue(fileMetadataDto, DtoFields.LENGTH, Long.class);
		final VFile vFile = fileManager.createFile(fileName, mimeType, lastModified, length, inputStreamBuilder);

		//TODO passer par une factory de FileInfo à partir de la FileInfoDefinition (comme DomainFactory)
		final FileInfo fileInfo = new DatabaseFileInfo(fileInfoUri.getDefinition(), vFile);
		fileInfo.setURIStored(fileInfoUri);
		return fileInfo;
	}

	/** {@inheritDoc} */
	@Override
	public void create(final FileInfo fileInfo) {
		checkReadonly();
		checkDefinitionStoreBinding(fileInfo.getDefinition());
		Assertion.checkArgument(fileInfo.getURI() == null, "Only file without any id can be created");
		//-----
		final DtObject fileMetadataDto = createMetaDataDtObject(fileInfo);
		final DtObject fileDataDto = createFileDtObject(fileInfo);
		//-----
		getStoreManager().getDataStore().create(fileDataDto);
		setValue(fileMetadataDto, DtoFields.FDT_ID, DtObjectUtil.getId(fileDataDto));
		getStoreManager().getDataStore().create(fileMetadataDto);
		final FileInfoURI fileInfoUri = createURI(fileInfo.getDefinition(), DtObjectUtil.getId(fileMetadataDto));
		fileInfo.setURIStored(fileInfoUri);
	}

	/** {@inheritDoc} */
	@Override
	public void update(final FileInfo fileInfo) {
		checkReadonly();
		checkDefinitionStoreBinding(fileInfo.getDefinition());
		Assertion.checkArgument(fileInfo.getURI() != null, "Only file with id can be updated");
		//-----
		final DtObject fileMetadataDto = createMetaDataDtObject(fileInfo);
		final DtObject fileDataDto = createFileDtObject(fileInfo);
		//-----
		setValue(fileMetadataDto, DtoFields.FMD_ID, fileInfo.getURI().getKey());
		// Chargement du FDT_ID
		final URI<DtObject> dtoMetaDataUri = new URI<>(storeMetaDataDtDefinition, fileInfo.getURI().getKey());
		final DtObject fileMetadataDtoOld = getStoreManager().getDataStore().get(dtoMetaDataUri);
		final Object fdtId = getValue(fileMetadataDtoOld, DtoFields.FDT_ID, Object.class);
		setValue(fileMetadataDto, DtoFields.FDT_ID, fdtId);
		setValue(fileDataDto, DtoFields.FDT_ID, fdtId);
		getStoreManager().getDataStore().update(fileDataDto);
		getStoreManager().getDataStore().update(fileMetadataDto);
	}

	private static FileInfoURI createURI(final FileInfoDefinition fileInfoDefinition, final Object key) {
		return new FileInfoURI(fileInfoDefinition, key);
	}

	/** {@inheritDoc} */
	@Override
	public void remove(final FileInfoURI fileInfoUri) {
		checkReadonly();
		checkDefinitionStoreBinding(fileInfoUri.getDefinition());
		//-----
		final URI<DtObject> dtoMetaDataUri = new URI<>(storeMetaDataDtDefinition, fileInfoUri.getKey());
		final DtObject fileMetadataDtoOld = getStoreManager().getDataStore().get(dtoMetaDataUri);
		final Object fdtId = getValue(fileMetadataDtoOld, DtoFields.FDT_ID, Object.class);
		final URI<DtObject> dtoDataUri = new URI<>(storeFileDtDefinition, fdtId);

		getStoreManager().getDataStore().delete(dtoMetaDataUri);
		getStoreManager().getDataStore().delete(dtoDataUri);
	}

	private DtObject createMetaDataDtObject(final FileInfo fileInfo) {
		final DtObject fileMetadataDto = DtObjectUtil.createDtObject(storeMetaDataDtDefinition);
		final VFile vFile = fileInfo.getVFile();
		setValue(fileMetadataDto, DtoFields.FILE_NAME, vFile.getFileName());
		setValue(fileMetadataDto, DtoFields.MIME_TYPE, vFile.getMimeType());
		setValue(fileMetadataDto, DtoFields.LAST_MODIFIED, vFile.getLastModified());
		setValue(fileMetadataDto, DtoFields.LENGTH, vFile.getLength());
		return fileMetadataDto;
	}

	private DtObject createFileDtObject(final FileInfo fileInfo) {
		final DtObject fileDataDto = DtObjectUtil.createDtObject(storeFileDtDefinition);
		final VFile vFile = fileInfo.getVFile();
		setValue(fileDataDto, DtoFields.FILE_NAME, vFile.getFileName());
		setValue(fileDataDto, DtoFields.FILE_DATA, new VFileDataStream(vFile));
		return fileDataDto;
	}

}
