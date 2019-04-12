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

import static java.lang.String.format;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

public class CounterTest {
	public static final String DECREMENT_ACTION = "DECREMENT";
	public static final String INCREMENT_ACTION = "INCREMENT";
	public static final String COUNTER_STATE = "COUNTER";
	public static final Action ADD_ONE = Action.create(INCREMENT_ACTION);
	public static final Action REMOVE_ONE = Action.create(DECREMENT_ACTION);
	public static final StateStore STORE = StateStore.Global.instance();

	public static int counterValue;

	@BeforeClass
	public static void setupOnce() {
		STORE.addReducer((a, s) -> {
			int count = s.getOrElse(COUNTER_STATE, 0);
			if(a.getType().equals(INCREMENT_ACTION)) {
				int delta = a.getPayloadOrElse(1);
				return s.put(COUNTER_STATE, count + delta);
			} else if(a.getType().equals(DECREMENT_ACTION)) {
				int delta = a.getPayloadOrElse(1);
				return s.put(COUNTER_STATE, count - delta);
			}
			return s;
		});
		STORE.addMiddleware((a, s) -> {
			if(INCREMENT_ACTION.equals(a.getType()) || DECREMENT_ACTION.equals(a.getType())) {
				int delta = a.getPayloadOrElse(1);
				if(delta < 1) {
					System.err.println("Delta should be greater than or equal to 1");
					return false;
				} else {
					System.out.println(format("Good %s payload of %s", a.getType(), delta));
				}
			}
			return true;
		});
		STORE.subscribe(s -> {
			counterValue = s.getOrElse(COUNTER_STATE, 0);
			System.out.println(format("Counter: %d", counterValue));
		});
	}

	@Test
	public final void testChanges() {
		STORE.dispatch(ADD_ONE);
		STORE.dispatch(REMOVE_ONE);
		STORE.dispatch(REMOVE_ONE);

		// This is only guaranteed with a blocking StateStore.
		assertEquals(-1, counterValue);

		// Reset
		STORE.dispatch(ADD_ONE);
		assertEquals(0, counterValue);
	}

	@Test
	public final void testRejectActions() {
		assertEquals(0, counterValue);

		STORE.dispatch(Action.create(INCREMENT_ACTION, -3));
		STORE.dispatch(Action.create(INCREMENT_ACTION, -7));

		// May seem like it should be equal
		assertNotEquals(-10, counterValue);

		// Actions were actually rejected
		assertEquals(0, counterValue);
	}
}
