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
package io.vertigo.commons.codec;

import io.vertigo.commons.codec.Codec;

/**
 * @author dchallas
 * @param <S> Type Source à encoder
 * @param <T> Type cible, résultat de l'encodage
 */
public abstract class AbstractCodecTest<S, T> extends AbstractEncoderTest<Codec<S, T>, S, T> {
	/**
	 * test l'encodage et de décodage de chaines non null.
	 * @throws Exception si problème
	 */
	public abstract void testDecode() throws Exception;

	/**
	 * test le décodage de chaines non encodées avec l'encodeur.
	 * @throws Exception si problème
	 */
	public abstract void testFailDecode() throws Exception;
}
