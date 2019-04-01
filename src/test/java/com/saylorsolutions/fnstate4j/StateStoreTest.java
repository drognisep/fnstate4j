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

import java.util.UUID;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.saylorsolutions.fnstate4j.func.Middleware;
import com.saylorsolutions.fnstate4j.func.Reducer;

public class StateStoreTest {
	private static final String UPDATED_MESSAGE = "Updated message";
	private static final Consumer<State> LOGGING_SUBSCRIBER = s -> System.out.println("State updated: " + s);
	private static final Middleware LOGGING_MIDDLEWARE = (a, s) -> {
		System.out.println("Received Action: " + a);
		return true;
	};
	private StateStore store;
	private static final String ACTION_TYPE = "TEST";
	private static final String MESSAGE_STATE = "MESSAGE";
	private static final Reducer MESSAGE_REDUCER = (a, s) -> {
		if (a.getType().equals(ACTION_TYPE)) {
			String message = (String) a.getPayloadOrNull();
			return s.put(MESSAGE_STATE, message);
		}
		return s;
	};
	private String testSubscribeMessage;
	private Consumer<State> INSTANCE_FIELD_UPDATE_SUBSCRIBER = s -> {
		testSubscribeMessage = s.get(MESSAGE_STATE, String.class).orElse(null);
	};

	@Before
	public void setup() {
		this.store = new StateStore();
		this.store.subscribe(LOGGING_SUBSCRIBER);
		this.store.addMiddleware(LOGGING_MIDDLEWARE);
		this.store.blockingDispatch(Action.create("LOG_INITIAL_STATE"));
		this.store.addReducer(MESSAGE_REDUCER);
	}

	@After
	public void cleanup() {
		resetInstanceFieldMessage();
		this.store.unsubscribe(INSTANCE_FIELD_UPDATE_SUBSCRIBER);
	}

	@Test
	public final void testDispatch() {
		sendEmptyMessage();
		State state = getFreshState();
		assertNoMessagePresent(state);

		sendUpdatedMessage();
		state = getFreshState();
		assertMessagePresent(state);
	}

	@Test
	public final void testSubscribe() {
		this.store.subscribe(INSTANCE_FIELD_UPDATE_SUBSCRIBER);

		blockingSendUpdatedMessage();
		assertEquals(UPDATED_MESSAGE, testSubscribeMessage);
	}

	@Test
	public final void testDuplicateSubscribe() {
		final Consumer<State> no_op = (s) -> {};
		UUID expected = this.store.subscribe(no_op);
		assertSame(expected, this.store.subscribe(no_op));
	}

	@Test
	public final void testUnsubscribeUUID() {
		UUID id = this.store.subscribe(INSTANCE_FIELD_UPDATE_SUBSCRIBER);
		this.store.unsubscribe(id);

		blockingSendUpdatedMessage();
		assertNotEquals(UPDATED_MESSAGE, testSubscribeMessage);
	}

	@Test
	public final void testUnsubscribeConsumerOfState() {
		this.store.subscribe(INSTANCE_FIELD_UPDATE_SUBSCRIBER);
		this.store.unsubscribe(INSTANCE_FIELD_UPDATE_SUBSCRIBER);

		blockingSendUpdatedMessage();
		assertNotEquals(UPDATED_MESSAGE, testSubscribeMessage);
	}

	@Test
	public final void testRemoveReducer() {
		this.store.removeReducer(MESSAGE_REDUCER);
		sendEmptyMessage();
		State state = getFreshState();
		assertNoMessagePresent(state);

		sendUpdatedMessage();
		state = getFreshState();
		assertNull(state.getOrNull(MESSAGE_STATE));
	}

	@Test
	public final void testAddMiddleware() {
		this.store.addMiddleware((a, s) -> false);
		sendEmptyMessage();
		State state = getFreshState();
		assertNoMessagePresent(state);

		sendUpdatedMessage();
		state = getFreshState();
		assertNull(state.getOrNull(MESSAGE_STATE));
	}

	@Test
	public final void testRemoveMiddleware() {
		Middleware denyAll = (a, s) -> false;
		this.store.addMiddleware(denyAll);

		sendEmptyMessage();
		State state = getFreshState();
		assertNoMessagePresent(state);

		this.store.removeMiddleware(denyAll);
		sendUpdatedMessage();
		state = getFreshState();
		assertMessagePresent(state);
	}

	private void resetInstanceFieldMessage() {
		testSubscribeMessage = null;
	}

	private void blockingSendUpdatedMessage() {
		this.store.blockingDispatch(Action.create(ACTION_TYPE, UPDATED_MESSAGE));
	}

	private void sendUpdatedMessage() {
		this.store.dispatch(Action.create(ACTION_TYPE, UPDATED_MESSAGE));
	}

	private void sendEmptyMessage() {
		this.store.dispatch(Action.create(ACTION_TYPE));
	}

	private void assertMessagePresent(State state) {
		assertEquals(UPDATED_MESSAGE, state.getOrElse(MESSAGE_STATE, ""));
	}

	private void assertNoMessagePresent(State state) {
		assertFalse(state.get(MESSAGE_STATE).isPresent());
		assertNull(state.getOrNull(MESSAGE_STATE));
	}

	private State getFreshState() {
		return this.store.getState();
	}

}
