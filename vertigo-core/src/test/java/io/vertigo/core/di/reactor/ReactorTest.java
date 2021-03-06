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
package io.vertigo.core.di.reactor;

import io.vertigo.core.component.di.DIException;
import io.vertigo.core.component.di.reactor.DIReactor;
import io.vertigo.core.di.data.A;
import io.vertigo.core.di.data.B;
import io.vertigo.core.di.data.C;
import io.vertigo.core.di.data.D;
import io.vertigo.core.di.data.E;
import io.vertigo.core.di.data.F;
import io.vertigo.core.di.data.P;
import io.vertigo.core.di.data.P3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * A ne dépend de personne.
 * B dépend de A.
 * C dépend de B et D.;
 * D dépend de C.
 * E dépend d'options de A et B.
 * F dépende de A et de paramètres nommés.
 *
 * @author pchretien
 */
public final class ReactorTest {
	private static void nop(final Object o) {
		//NOP
	}

	@Test(expected = NullPointerException.class)
	public void testIdArgument() {
		final DIReactor reactor = new DIReactor()
				.addComponent(null, A.class);
		nop(reactor);
	}

	@Test(expected = NullPointerException.class)
	public void testClassArgument() {
		final DIReactor reactor = new DIReactor()
				.addComponent("a", null);
		nop(reactor);
	}

	@Test
	public void testA() {
		final List<String> list = new DIReactor()
				.addComponent("a", A.class)
				.proceed();
		Assert.assertEquals(1, list.size());
		Assert.assertEquals("a", list.get(0));
	}

	@Test
	public void testB() {
		final List<String> list = new DIReactor()
				.addComponent("a", A.class)
				.addComponent("b", B.class)
				.proceed();
		Assert.assertEquals(2, list.size());
		Assert.assertEquals("a", list.get(0));
		Assert.assertEquals("b", list.get(1));
	}

	@Test
	public void testB2() {
		//La resolution fonctionne ; l'erreur ne survenant qu'à la résolution
		final List<String> list = new DIReactor()
				.addComponent("a", A.class)
				.addComponent("b", B.class)
				.proceed();
		Assert.assertEquals(2, list.size());
		Assert.assertEquals("a", list.get(0));
	}

	@Test
	public void testBWithParent() {
		//rappel B dépend de A
		final List<String> list = new DIReactor()
				.addParent("a")
				.addComponent("b", B.class)
				.proceed();
		Assert.assertEquals(1, list.size());
		Assert.assertEquals("b", list.get(0));
	}

	@Test
	/** On change l'ordre ; on vérifie que Reactor gère bien l'ordre */
	public void testDependency2bis() {
		final List<String> list = new DIReactor()
				.addComponent("b", B.class)
				.addComponent("a", A.class)
				.proceed();
		Assert.assertEquals(2, list.size());
		Assert.assertEquals("a", list.get(0));
		Assert.assertEquals("b", list.get(1));
	}

	@Test
	/** On teste les options */
	public void testOption() {
		final List<String> list = new DIReactor()
				.addComponent("e", E.class)
				.addComponent("a", A.class)
				.addComponent("p3", P3.class) //Plugin objligatoire
				.addComponent("p", P.class) //Plugin facultatif car liste 
				.addComponent("p2", P.class) //Plugin facultatif car liste 
				.proceed();
		//E dépend de option(A) et de option(B) donc A doit être le premier élément listé
		Assert.assertEquals(5, list.size());
		Assert.assertEquals("a", list.get(0));
		//		Assert.assertEquals("p3", list.get(1));
		//		Assert.assertEquals("p", list.get(1));
		//		Assert.assertEquals("p2", list.get(1));
		Assert.assertEquals("e", list.get(4));
	}

	@Test
	/** On teste les paramètres */
	public void testParams() {
		final Set<String> params = new HashSet<>();
		params.add("param1");
		params.add("param2");
		final List<String> list = new DIReactor()
				.addComponent("a", A.class)
				.addComponent("f", F.class, params)
				.proceed();
		Assert.assertEquals(2, list.size());
		Assert.assertEquals("a", list.get(0));
		Assert.assertEquals("f", list.get(1));
	}

	@Test
	/** On teste les paramètres */
	public void testParams2() {
		final Set<String> params = new HashSet<>();
		params.add("a");
		final List<String> list = new DIReactor()
				.addComponent("b", B.class, params)
				.proceed();
		Assert.assertEquals(1, list.size());
		Assert.assertEquals("b", list.get(0));
	}

	@Test(expected = DIException.class)
	public void testDependencyMissing() {
		final List<String> list = new DIReactor()
				.addComponent("a", A.class)
				.addComponent("b", B.class)
				.addComponent("c", C.class)
				.proceed();
		nop(list);
	}

	@Test(expected = DIException.class)
	public void testDependencyCyclic() {
		final List<String> list = new DIReactor()
				.addComponent("a", A.class)
				.addComponent("b", B.class)
				.addComponent("c", C.class)
				.addComponent("D", D.class)
				.proceed();
		nop(list);
	}

	@Test(expected = DIException.class)
	public void testDependencyMultiple() {
		final List<String> list = new DIReactor()
				.addComponent("a", A.class)
				.addComponent("b", B.class)
				.addComponent("b", B.class)
				.proceed();
		nop(list);
	}

	@Test(expected = DIException.class)
	public void testDependencyMultiple2() {
		final List<String> list = new DIReactor()
				.addComponent("a", A.class)
				.addComponent("b", B.class)
				.addParent("b")
				.proceed();
		nop(list);
		Assert.fail();
	}
}
