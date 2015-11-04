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
package io.vertigo.dynamo.plugins.store.filestore.fs;

import io.vertigo.core.Home;
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
import io.vertigo.dynamo.impl.file.model.AbstractFileInfo;
import io.vertigo.dynamo.impl.store.filestore.FileStorePlugin;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.transaction.VTransaction;
import io.vertigo.dynamo.transaction.VTransactionManager;
import io.vertigo.dynamo.transaction.VTransactionResourceId;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Permet de gérer les accès atomiques à n'importe quel type de stockage SQL/
 * non SQL pour les traitements de FileInfo.
 *
 * @author pchretien, npiedeloup, skerdudou
 */
public final class FsFileStorePlugin implements FileStorePlugin {
	private static final String DEFAULT_STORE_NAME = "main";
	private static final String STORE_READ_ONLY = "Le store est en readOnly";
	private static final String USER_HOME = "user.home";
	private static final String USER_DIR = "user.dir";
	private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

	/**
	 * Identifiant de ressource FileSystem par défaut.
	 */
	private final VTransactionResourceId<FsTransactionResource> fsResourceId;

	/**
	 * Liste des champs du Dto de stockage.
	 * Ces champs sont obligatoire sur les Dt associés aux fileInfoDefinitions
	 *
	 * @author npiedeloup
	 */
	private enum DtoFields {
		/** Champ FILE_NAME */
		FILE_NAME,
		/** Champ MIME_TYPE */
		MIME_TYPE,
		/** Champ LAST_MODIFIED */
		LAST_MODIFIED,
		/** Champ LENGTH */
		LENGTH,
		/** Champ FILE_PATH */
		FILE_PATH
	}

	/**
	 * Le store est-il en mode readOnly ?
	 */
	private final boolean readOnly;
	private final FileManager fileManager;
	private final String name;
	private final String documentRoot;
	private final DtDefinition storeDtDefinition;
	private final VTransactionManager transactionManager;

	/**
	 * Constructeur.
	 * @param name Store name
	 * @param storeDtDefinitionName Nom du dt de stockage
	 * @param fileManager Manager de gestion des fichiers
	 * @param path le chemin jndi pour récupérer le paramètre path dans le context
	 * @param transactionManager Manager des transactions
	 */
	@Inject
	public FsFileStorePlugin(@Named("name") final Option<String> name, @Named("storeDtName") final String storeDtDefinitionName, @Named("path") final String path, final VTransactionManager transactionManager, final FileManager fileManager) {
		Assertion.checkNotNull(name);
		Assertion.checkArgNotEmpty(storeDtDefinitionName);
		Assertion.checkArgNotEmpty(path);
		Assertion.checkNotNull(transactionManager);
		Assertion.checkNotNull(fileManager);
		Assertion.checkArgument(path.endsWith("/"), "store path must ends with / ({0})", path);
		//-----
		this.name = name.getOrElse(DEFAULT_STORE_NAME);
		readOnly = false;
		this.transactionManager = transactionManager;
		this.fileManager = fileManager;
		documentRoot = translatePath(path);
		storeDtDefinition = Home.getDefinitionSpace().resolve(storeDtDefinitionName, DtDefinition.class);
		fsResourceId = new VTransactionResourceId<>(VTransactionResourceId.Priority.NORMAL, "FS-" + name);
	}

	private static String translatePath(final String path) {
		return path
				.replaceAll(USER_HOME, System.getProperty(USER_HOME).replace('\\', '/'))
				.replaceAll(USER_DIR, System.getProperty(USER_DIR).replace('\\', '/'))
				.replaceAll(JAVA_IO_TMPDIR, System.getProperty(JAVA_IO_TMPDIR).replace('\\', '/'));
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public FileInfo load(final FileInfoURI uri) {
		// récupération de l'objet en base
		final URI<DtObject> dtoUri = createDtObjectURI(uri);
		final DtObject fileInfoDto = getStoreManager().getDataStore().get(dtoUri);

		// récupération du fichier
		final String fileName = getValue(fileInfoDto, DtoFields.FILE_NAME, String.class);
		final String mimeType = getValue(fileInfoDto, DtoFields.MIME_TYPE, String.class);
		final Date lastModified = getValue(fileInfoDto, DtoFields.LAST_MODIFIED, Date.class);
		final Long length = getValue(fileInfoDto, DtoFields.LENGTH, Long.class);
		final String filePath = getValue(fileInfoDto, DtoFields.FILE_PATH, String.class);

		final InputStreamBuilder inputStreamBuilder = new FileInputStreamBuilder(new File(documentRoot + filePath));
		final VFile vFile = fileManager.createFile(fileName, mimeType, lastModified, length, inputStreamBuilder);

		// retourne le fileinfo avec le fichier et son URI
		final FsFileInfo fsFileInfo = new FsFileInfo(uri.getDefinition(), vFile);
		fsFileInfo.setURIStored(uri);
		return fsFileInfo;
	}

	private static class FsFileInfo extends AbstractFileInfo {
		private static final long serialVersionUID = -1610176974946554828L;

		protected FsFileInfo(final FileInfoDefinition fileInfoDefinition, final VFile vFile) {
			super(fileInfoDefinition, vFile);
		}
	}

	private DtObject createFileInfoDto(final FileInfo fileInfo) {
		final DtObject fileInfoDto = createDtObject(fileInfo.getDefinition());
		//-----
		final VFile vFile = fileInfo.getVFile();
		setValue(fileInfoDto, DtoFields.FILE_NAME, vFile.getFileName());
		setValue(fileInfoDto, DtoFields.MIME_TYPE, vFile.getMimeType());
		setValue(fileInfoDto, DtoFields.LAST_MODIFIED, vFile.getLastModified());
		setValue(fileInfoDto, DtoFields.LENGTH, vFile.getLength());
		if (fileInfo.getURI() == null) {
			// cas de la création, on ajoute en base un chemin fictif (colonne not null)
			setValue(fileInfoDto, DtoFields.FILE_PATH, "/dev/null");
		} else {
			// cas de l'update
			setPkValue(fileInfoDto, fileInfo.getURI().getKey());

			// récupération de l'objet en base pour récupérer le path du fichier et ne pas modifier la base
			final URI<DtObject> dtoUri = createDtObjectURI(fileInfo.getURI());
			final DtObject fileInfoDtoBase = getStoreManager().getDataStore().get(dtoUri);
			final String pathToSave = getValue(fileInfoDtoBase, DtoFields.FILE_PATH, String.class);
			setValue(fileInfoDto, DtoFields.FILE_PATH, pathToSave);
		}
		return fileInfoDto;
	}

	private void saveFile(final FileInfo fileInfo, final String pathToSave) {
		try (InputStream inputStream = fileInfo.getVFile().createInputStream()) {
			obtainFsTransactionRessource().saveFile(inputStream, documentRoot + pathToSave);
		} catch (final IOException e) {
			throw new RuntimeException("Impossible de lire le fichier uploadé.", e);
		}
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

		// cas de la création
		final Object fileInfoDtoId = DtObjectUtil.getId(fileInfoDto);
		Assertion.checkNotNull(fileInfoDtoId, "ID  du fichier doit être renseignée.");
		final FileInfoURI uri = createURI(fileInfo.getDefinition(), fileInfoDtoId);
		fileInfo.setURIStored(uri);

		// on met a jour la base
		final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd/", Locale.FRANCE);
		final String pathToSave = format.format(new Date()) + fileInfoDtoId;
		setValue(fileInfoDto, DtoFields.FILE_PATH, pathToSave);
		//-----
		getStoreManager().getDataStore().update(fileInfoDto);
		//-----
		saveFile(fileInfo, pathToSave);
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

		final String pathToSave = getValue(fileInfoDto, DtoFields.FILE_PATH, String.class);
		//-----
		saveFile(fileInfo, pathToSave);
	}

	private static FileInfoURI createURI(final FileInfoDefinition fileInfoDefinition, final Object key) {
		return new FileInfoURI(fileInfoDefinition, key);
	}

	/** {@inheritDoc} */
	@Override
	public void remove(final FileInfoURI uri) {
		Assertion.checkArgument(!readOnly, STORE_READ_ONLY);

		final URI<DtObject> dtoUri = createDtObjectURI(uri);
		//-----suppression du fichier
		final DtObject fileInfoDto = getStoreManager().getDataStore().get(dtoUri);
		final String path = getValue(fileInfoDto, DtoFields.FILE_PATH, String.class);
		obtainFsTransactionRessource().deleteFile(documentRoot + path);
		//-----suppression en base
		getStoreManager().getDataStore().delete(dtoUri);
	}

	/**
	 * Création d'une URI de DTO à partir de l'URI de FileInfo
	 *
	 * @param uri URI de FileInfo
	 * @return URI du DTO utilisé en BDD pour stocker.
	 */
	private URI<DtObject> createDtObjectURI(final FileInfoURI uri) {
		Assertion.checkNotNull(uri, "uri du fichier doit être renseignée.");
		//-----
		// Il doit exister un DtObjet associé, avec la structure attendue.
		return new URI<>(storeDtDefinition, uri.getKey());
	}

	/**
	 * Création d'un DTO à partir d'une definition de FileInfo
	 *
	 * @param fileInfoDefinition Definition de FileInfo
	 * @return DTO utilisé en BDD pour stocker.
	 */
	private DtObject createDtObject(final FileInfoDefinition fileInfoDefinition) {
		Assertion.checkNotNull(fileInfoDefinition, "fileInfoDefinition du fichier doit être renseignée.");
		//-----
		// Il doit exister un DtObjet associé, avec la structure attendue.
		return DtObjectUtil.createDtObject(storeDtDefinition);
	}

	/**
	 * Retourne une valeur d'un champ à partir du DtObject.
	 *
	 * @param dto DtObject
	 * @param field Nom du champs
	 * @return Valeur typé du champ
	 */
	private static <V> V getValue(final DtObject dto, final DtoFields field, final Class<V> valueClass) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		final DtField dtField = dtDefinition.getField(field.name());
		return valueClass.cast(dtField.getDataAccessor().getValue(dto));
	}

	/**
	 * Fixe une valeur d'un champ d'un DtObject.
	 *
	 * @param dto DtObject
	 * @param field Nom du champs
	 * @param value Valeur
	 */
	private static void setValue(final DtObject dto, final DtoFields field, final Object value) {
		final DtField dtField = DtObjectUtil.findDtDefinition(dto).getField(field.name());
		dtField.getDataAccessor().setValue(dto, value);
	}

	private static void setPkValue(final DtObject dto, final Object value) {
		final DtField dtField = DtObjectUtil.findDtDefinition(dto).getIdField().get();
		dtField.getDataAccessor().setValue(dto, value);
	}

	private static final class FileInputStreamBuilder implements InputStreamBuilder {

		private final File file;

		FileInputStreamBuilder(final File file) {
			this.file = file;
		}

		/** {@inheritDoc} */
		@Override
		public InputStream createInputStream() throws IOException {
			return new FileInputStream(file);
		}
	}

	private static StoreManager getStoreManager() {
		return Home.getComponentSpace().resolve(StoreManager.class);
	}

	/** récupère la transaction courante. */
	private VTransaction getCurrentTransaction() {
		return transactionManager.getCurrentTransaction();
	}

	/** récupère la ressource FS de la transaction et la créé si nécessaire. */
	private FsTransactionResource obtainFsTransactionRessource() {
		FsTransactionResource resource = getCurrentTransaction().getResource(fsResourceId);

		if (resource == null) {
			// Si aucune ressource de type FS existe sur la transaction, on la créé
			resource = new FsTransactionResource();
			getCurrentTransaction().addResource(fsResourceId, resource);
		}
		return resource;
	}

	/**
	 * récupère la valeur de documentRoot.
	 *
	 * @return valeur de documentRoot
	 */
	public String getDocumentRoot() {
		return documentRoot;
	}
}
