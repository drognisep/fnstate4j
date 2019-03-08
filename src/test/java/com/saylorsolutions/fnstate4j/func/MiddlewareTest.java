package com.saylorsolutions.fnstate4j.func;

import static java.lang.String.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.saylorsolutions.fnstate4j.Action;
import com.saylorsolutions.fnstate4j.State;

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
		assertFalse(Middleware.combine(Arrays.asList(new Middleware[] {truthy, falsy})).process(testAction, testState));
		assertFalse(Middleware.combine(Arrays.asList(new Middleware[] {falsy, truthy})).process(testAction, testState));
		assertTrue(Middleware.combine(Arrays.asList(new Middleware[] {truthy, truthy})).process(testAction, testState));
	}

	@Test
	public void testCombineMultiples() {
		assertFalse(Middleware.combine(truthy, falsy).process(testAction, testState));
		assertFalse(Middleware.combine(falsy, truthy).process(testAction, testState));
		assertTrue(Middleware.combine(truthy, truthy).process(testAction, testState));
	}

	@Test
	public void testAndThenChaining() {
		assertFalse(truthy.andThen(falsy).process(testAction, testState));
		assertFalse(falsy.andThen(truthy).process(testAction, testState));
		assertTrue(truthy.andThen(truthy).process(testAction, testState));
	}
}
