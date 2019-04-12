package com.saylorsolutions.fnstate4j.func;

/*-
 * #%L
 * Functional State for Java
 * %%
 * Copyright (C) 2019 Joseph D. Saylor
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * #L%
 */
import static java.lang.String.format;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.saylorsolutions.fnstate4j.Action;
import com.saylorsolutions.fnstate4j.State;

import io.vavr.collection.List;

public class MiddlewareTest {
	private static final String TEST1_TYPE = "TYPE1";
	private static final String TEST2_TYPE = "TYPE2";
	private static final Middleware LOGGER = (a, s) -> {
		s = s == null ? new State() : s;
		System.out.println(format("Action: type='%s' payload='%s'", a.getType(), a.getPayloadOrNull()));
		System.out.println(format("State: keySet='%s'", s.keySet()));
		return true;
	};
	private Middleware truthy;
	private Middleware falsy;
	private State testState;
	private Action testAction;

	@Before
	public void setup() {
		truthy = LOGGER.andThen((a, s) -> true);
		falsy = LOGGER.andThen((a, s) -> false);

		testState = new State().put(TEST1_TYPE, TEST2_TYPE);
		testAction = Action.create("TEST");
	}

	@Test
	public void testMiddlewarePropagation() {
		assertTrue(truthy.process(testAction, testState));
		assertFalse(falsy.process(testAction, testState));
		assertTrue(truthy.process(testAction, null));
		assertFalse(falsy.process(testAction, null));
	}

	@Test
	public void testCombineCollection() {
		assertFalse(
				Middleware.combine(Arrays.asList(new Middleware[] { truthy, falsy })).process(testAction, testState));
		assertFalse(
				Middleware.combine(Arrays.asList(new Middleware[] { falsy, truthy })).process(testAction, testState));
		assertTrue(
				Middleware.combine(Arrays.asList(new Middleware[] { truthy, truthy })).process(testAction, testState));
	}

	@Test
	public void testCombineCollectionNegative() {
		Middleware test1 = Middleware.combine((Collection<Middleware>)null);
		Middleware test2 = Middleware.combine(Collections.emptyList());
		Middleware test3 = Middleware.combine(List.<Middleware>of(null, null).toJavaList());

		assertSame(Middleware.NO_OP, test1);
		assertSame(Middleware.NO_OP, test2);
		assertSame(Middleware.NO_OP, test3);
	}

	@Test
	public void testCombineMultiples() {
		assertFalse(Middleware.combine(truthy, falsy).process(testAction, testState));
		assertFalse(Middleware.combine(falsy, truthy).process(testAction, testState));
		assertTrue(Middleware.combine(truthy, truthy).process(testAction, testState));
	}

	@Test
	public void testCombineMultiplesNegative() {
		Middleware mw = (a, s) -> true;
		Middleware test1 = Middleware.combine(null, mw);
		Middleware test2 = Middleware.combine(mw, (Middleware[])null);

		assertSame(Middleware.NO_OP, test1);
		assertSame(mw, test2);
	}

	@Test
	public void testAndThenChaining() {
		assertFalse(truthy.andThen(falsy).process(testAction, testState));
		assertFalse(falsy.andThen(truthy).process(testAction, testState));
		assertTrue(truthy.andThen(truthy).process(testAction, testState));
	}
}
