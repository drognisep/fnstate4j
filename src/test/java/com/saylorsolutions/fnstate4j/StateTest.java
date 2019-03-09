package com.saylorsolutions.fnstate4j;

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

import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

public class StateTest {
	private static final String TEST1_KEY = "Test1";
	private static final String TEST1_VALUE = "Test1";
	private static final String TEST2_KEY = "Test2";
	private static final String TEST2_VALUE = "Test2";
	private State state;

	@Before
	public void setup() {
		state = new State().put(TEST1_KEY, TEST1_VALUE).put(TEST2_KEY, TEST2_VALUE);

		// Sanity test
		assertEquals(2, state.size());
		assertTrue(state.hasKey(TEST1_KEY));
		assertTrue(state.hasKey(TEST2_KEY));
	}

	@Test
	public void testGet() {
		assertEquals(TEST1_KEY, state.get(TEST1_KEY, String.class).get());
		assertEquals(TEST2_KEY, state.get(TEST2_KEY, String.class).get());
	}

	@Test
	public void testGetOrNullOrElse() {
		assertNotNull(state.getOrNull(TEST1_KEY));
		assertNotNull(state.getOrElse(TEST1_KEY, null));
		assertNull(state.getOrNull("alsdkjfslkfjdslkf"));
		assertNull(state.getOrElse("sdklfjosiejrlskdjflsijuie", null));
	}

	@Test
	public void testGetOptional() {
		Optional<Object> test1 = state.get(TEST1_KEY);
		assertTrue(test1.isPresent());
		assertTrue(test1.get().equals(TEST1_VALUE));
	}

	@Test
	public void testPut() {
		State orig = state;
		assertNotSame(orig, state.put("Test3", new B()));
		assertFalse(state.hasKey("Test3"));
	}

	@Test(expected = ClassCastException.class)
	public void testGetWithWrongTypeThrowsException() {
		final String testKey = "Test3";
		A testValue = new A();
		State test = state.put(testKey, testValue);
		assertNotNull(test.getOrNull(testKey));
		C c = test.getOrElse(testKey, new C());
		fail("Assigning an A to an instance of C should throw an exception: " + c);
	}

	@Test
	public void testMergeState() {
		State other = new State().put("Test3", "Test3");
		state = State.merge(state, other);
		assertTrue(state.hasKey(TEST1_KEY));
		assertTrue(state.hasKey(TEST2_KEY));
		assertTrue(state.hasKey("Test3"));
		assertFalse(state.canTimeTravel());
	}

	@Test
	public void testKeySet() {
		Set<String> keySet = state.keySet();
		Set<String> expected = HashSet.<String>of(TEST1_KEY, TEST2_KEY);
		assertEquals(expected, keySet);
	}

	@Test
	public void testTimeTravel() {
		State orig = new State();
		State test = orig.put("Test", "Test");
		assertTrue(orig.isEmpty());
		assertNotSame(orig, test);
		assertTrue(test.canTimeTravel());
		assertSame(orig, test.getPreviousState().get());
	}

	private static class A {
		public A() {}
	}
	private static class B extends A {
		public B() {}
	}
	private static class C {
		public C() {}
	}
}
