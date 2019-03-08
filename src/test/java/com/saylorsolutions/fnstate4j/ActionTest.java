package com.saylorsolutions.fnstate4j;

import static org.junit.Assert.*;

import org.junit.Test;

import com.saylorsolutions.fnstate4j.Action;

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
		fail("Should have thrown an exception");
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
