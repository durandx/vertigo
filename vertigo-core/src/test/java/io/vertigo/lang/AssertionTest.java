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
package io.vertigo.lang;

import org.junit.Test;

/**
 * Test de l'utilitaire des assertions.
 *
 * @author pchretien
 */
public final class AssertionTest {
	@Test
	public void testCheckNotNull() {
		Assertion.checkNotNull("notNull");
	}

	@Test
	public void testCheckNotNull2() {
		Assertion.checkNotNull("notNull", "message");
	}

	@Test
	public void testCheckNotNull3() {
		Assertion.checkNotNull("notNull", "message: {0} ", "param");
	}

	@Test(expected = NullPointerException.class)
	public void testCheckNotNullFail() {
		Assertion.checkNotNull(null);
	}

	@Test(expected = NullPointerException.class)
	public void testCheckNotNull3FailWithMessage() {
		Assertion.checkNotNull(null, "message: {0} ", "param");
	}

	@Test
	public void testCheckArgument() {
		Assertion.checkArgument(true, "message");
	}

	@Test
	public void testCheckArgument2() {
		Assertion.checkArgument(true, "message {0}", "param");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckArgumentFail() {
		Assertion.checkArgument(false, "message");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckArgument2Fail() {
		Assertion.checkArgument(false, "message {0}", "param");
	}

	//-----
	@Test
	public void testCheckState() {
		Assertion.checkState(true, "message");
	}

	@Test
	public void testCheckState2() {
		Assertion.checkState(true, "message {0}", "param1");
	}

	@Test(expected = IllegalStateException.class)
	public void testCheckStateFail() {
		Assertion.checkState(false, "message");
	}

	@Test(expected = IllegalStateException.class)
	public void testCheckState2Fail() {
		Assertion.checkState(false, "message {0}", "param1");
	}

	@Test
	public void testCheckNotEmpty() {
		Assertion.checkArgNotEmpty("test", "message");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckNotEmptyFail() {
		Assertion.checkArgNotEmpty("  ", "message");
	}

	@Test(expected = NullPointerException.class)
	public void testCheckNotEmpty2Fail() {
		Assertion.checkArgNotEmpty(null, "message {0}", "param");
	}
}
