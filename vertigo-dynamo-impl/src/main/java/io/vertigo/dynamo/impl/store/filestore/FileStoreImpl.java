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
package io.vertigo.dynamo.impl.store.filestore;

import io.vertigo.dynamo.domain.model.FileInfoURI;
import io.vertigo.dynamo.file.model.FileInfo;
import io.vertigo.dynamo.impl.store.filestore.logical.LogicalFileStore;
import io.vertigo.dynamo.store.filestore.FileStore;
import io.vertigo.lang.Assertion;

/**
 * Implementation of FileStore.
 * @author pchretien
 */
public final class FileStoreImpl implements FileStore {
	private final FileStorePlugin fileStore;

	/**
	 * Constructeur.
	 * @param fileStoreConfig Config of the fileStore
	 */
	public FileStoreImpl(final FileStoreConfig fileStoreConfig) {
		Assertion.checkNotNull(fileStoreConfig);
		//-----
		fileStore = new LogicalFileStore(fileStoreConfig.getLogicalFileStoreConfiguration());
	}

	/** {@inheritDoc} */
	@Override
	public void create(final FileInfo fileInfo) {
		Assertion.checkNotNull(fileInfo);
		//-----
		fileStore.create(fileInfo);
	}

	/** {@inheritDoc} */
	@Override
	public void update(final FileInfo fileInfo) {
		Assertion.checkNotNull(fileInfo);
		//-----
		fileStore.update(fileInfo);
	}

	/** {@inheritDoc} */
	@Override
	public void delete(final FileInfoURI uri) {
		Assertion.checkNotNull(uri);
		//-----
		fileStore.remove(uri);
	}

	/** {@inheritDoc} */
	@Override
	public FileInfo get(final FileInfoURI uri) {
		Assertion.checkNotNull(uri);
		//-----
		final FileInfo fileInfo = fileStore.load(uri);
		//-----
		Assertion.checkNotNull(fileInfo, "Le fichier {0} n''a pas été trouvé", uri);
		return fileInfo;
	}
}
