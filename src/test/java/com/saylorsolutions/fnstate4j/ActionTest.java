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

import org.junit.Test;

public class ActionTest {
	@Test
	public void testCorrectClass() throws Throwable {
		A a = new B();
		Action action = Action.create("Test", a);
		assertEquals(B.class, action.getPayloadClass());
		assertTrue(action.payloadAssignableTo(A.class));
		assertTrue(action.payloadAssignableTo(B.class));
	}

	@Test
	public void testNoPayloadAssignableToAnything() throws Throwable {
		A a = null;
		Action action = Action.create("Test", a);
		assertNull(action.getPayloadClass());
		assertTrue(action.payloadAssignableTo(A.class));
		assertTrue(action.payloadAssignableTo(B.class));
		assertTrue(action.payloadAssignableTo(String.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEmptyStringThrowsException() {
		Action.create("", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testWhitespaceStringThrowsException() {
		Action.create("  \t\r\n", null);
	}

	@Test(expected = NullPointerException.class)
	public void testNullStringThrowsException() {
		Action.create(null);
	}

	@Test
	public void testGetOrElse() {
		Action action = Action.create("Test", new B());
		B b = action.getPayloadOrElse((B)null);
		A a = action.getPayloadOrElse((A)null);
		assertNotNull(b);
		assertNotNull(a);
	}

	@Test(expected = ClassCastException.class)
	public void testGetOrElseIncompatible() {
		String s = Action.create("Test", new B()).getPayloadOrElse("Bad Default");
		fail("Should have thrown an exception: " + s);
	}

	@Test
	public void testHasPayload() {
		assertTrue(Action.create("Test", new B()).hasPayload());
		assertTrue(Action.create("Test", "").hasPayload());
		assertFalse(Action.create("Test").hasPayload());
		assertFalse(Action.create("Test", null).hasPayload());
	}

	private static class A {
		public A() {}
	}
	private static class B extends A {
		public B() {}
	}
}
