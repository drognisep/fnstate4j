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

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import com.saylorsolutions.fnstate4j.func.Middleware;
import com.saylorsolutions.fnstate4j.func.Reducer;

/**
 * The {@code StateStore} is responsible for maintaining the {@code Reducer} and
 * {@code Middleware} chains, as well as handling dispatch calls to update the
 * state.
 *
 * @author Doug Saylor (doug at saylorsolutions.com)
 *
 */
public class StateStore {
	private transient State state;
	private transient Reducer rootReducer;
	private transient final Set<Reducer> reducers = new HashSet<>();
	private transient Middleware rootMiddleware;
	private transient final Set<Middleware> middlewares = new HashSet<>();
	private transient final Map<UUID, Consumer<State>> subscribers = new ConcurrentHashMap<>();
	private transient boolean nonBlocking; // Defaults to be blocking
	private transient final ExecutorService executor = Executors.newSingleThreadExecutor();

	public StateStore(State initialState, Reducer rootReducer, Middleware rootMiddleware, boolean nonBlocking) {
		super();

		Reducer newReducer = rootReducer == null ? Reducer.NO_OP : rootReducer;
		Middleware newMiddleware = rootMiddleware == null ? Middleware.NO_OP : rootMiddleware;

		this.state = initialState;
		this.rootReducer = newReducer;
		this.reducers.add(newReducer);
		this.rootMiddleware = newMiddleware;
		this.middlewares.add(newMiddleware);
		this.nonBlocking = nonBlocking;
	}

	public StateStore(State initialState, Reducer rootReducer, Middleware rootMiddleware) {
		this(initialState, rootReducer, rootMiddleware, false);
	}

	public StateStore(State initialState) {
		this(initialState, Reducer.NO_OP, Middleware.NO_OP);
	}

	public StateStore() {
		this(new State());
	}

	/**
	 * Dispatches an {@code Action} while respecting the value of {@code isNonBlocking}.
	 * @param action The action to be dispatched.
	 */
	public void dispatch(final Action action) {
		internalDispatch(action, nonBlocking);
	}

	/**
	 * Dispatches an {@code Action} and blocks the calling thread, regardless of the
	 * value of {@code StateStore#nonBlocking}.
	 *
	 * @param action The action to be dispatched.
	 * @see StateStore#isNonBlocking()
	 * @see StateStore#setNonBlocking(boolean)
	 */
	public void blockingDispatch(final Action action) {
		internalDispatch(action, false);
	}

	/**
	 * Dispatches an {@code Action} and uses an executor thread, regardless of the
	 * value of {@code StateStore#nonBlocking}.
	 *
	 * @param action The action to be dispatched.
	 * @see StateStore#isBlocking()
	 * @see StateStore#setNonBlocking(boolean)
	 */
	public void concurrentDispatch(final Action action) {
		internalDispatch(action, true);
	}

	private void internalDispatch(final Action action, final boolean nonBlocking) {
		if (this.rootMiddleware.process(action, this.state)) {
			synchronized (this.state) {
				final State oldState = this.state;
				this.state = this.rootReducer.reduce(action, oldState);
				if (nonBlocking) {
					this.subscribers.forEach((u, c) -> executor.execute(() -> c.accept(this.state)));
				} else {
					this.subscribers.forEach((u, c) -> c.accept(this.state));
				}
			}
		}
	}

	public UUID subscribe(Consumer<State> subscriber) {
		synchronized (subscribers) {
			Optional<Entry<UUID, Consumer<State>>> existingId = findConsumerEntry(subscriber);
			UUID id;
			if (existingId.isPresent())
				id = existingId.get().getKey();
			else
				id = UUID.randomUUID();
			subscribers.put(id, subscriber);
			return id;
		}
	}

	public void unsubscribe(UUID id) {
		subscribers.remove(id);
	}

	public void unsubscribe(Consumer<State> subscriber) {
		Optional<Entry<UUID, Consumer<State>>> entry = findConsumerEntry(subscriber);
		if (entry.isPresent())
			unsubscribe(entry.get().getKey());
	}

	private Optional<Entry<UUID, Consumer<State>>> findConsumerEntry(Consumer<State> subscriber) {
		Optional<Entry<UUID, Consumer<State>>> existingId = this.subscribers.entrySet().stream()
				.filter(e -> e.getValue().equals(subscriber)).findFirst();
		return existingId;
	}

	/**
	 * @return Whether subscribers are called in a separate thread.
	 */
	public boolean isNonBlocking() {
		return this.nonBlocking;
	}

	/**
	 * @return Whether subscribers are called by the same thread that initiates a state change.
	 */
	public boolean isBlocking() {
		return !isNonBlocking();
	}

	public void setNonBlocking(boolean nonBlocking) {
		this.nonBlocking = nonBlocking;
	}

	public State getState() {
		return this.state;
	}

	/**
	 * Adds a new {@code Reducer} to the chain. Does not allow duplicates.
	 *
	 * @param reducer
	 */
	public void addReducer(Reducer reducer) {
		Objects.requireNonNull(reducer);
		synchronized (this.reducers) {
			this.reducers.add(reducer);
			this.rootReducer = Reducer.combine(this.reducers);
		}
	}

	/**
	 * Removes the specified {@code Reducer} from the chain, if it exists.
	 *
	 * @param reducer
	 */
	public void removeReducer(Reducer reducer) {
		Objects.requireNonNull(reducer);
		synchronized (this.reducers) {
			this.reducers.remove(reducer);
			this.rootReducer = Reducer.combine(this.reducers);
		}
	}

	/**
	 * Adds a new {@code Middleware} to the chain. Does not allow duplicates.
	 *
	 * @param middleware
	 */
	public void addMiddleware(Middleware middleware) {
		Objects.requireNonNull(middleware);
		synchronized (this.middlewares) {
			this.middlewares.add(middleware);
			this.rootMiddleware = Middleware.combine(this.middlewares);
		}
	}

	/**
	 * Removes the specified {@code Middleware} from the chain, if it exists.
	 *
	 * @param middleware
	 */
	public void removeMiddleware(Middleware middleware) {
		Objects.requireNonNull(middleware);
		synchronized (this.middlewares) {
			this.middlewares.remove(middleware);
			this.rootMiddleware = Middleware.combine(this.middlewares);
		}
	}

	/**
	 * Enum to hold a final instance of the global {@code StateStore}. See https://stackoverflow.com/q/43662578.
	 *
	 * @author Doug Saylor (doug at saylorsolutions.com)
	 */
	public static enum Global {
		INSTANCE;

		public final StateStore instance = new StateStore();

		public static final StateStore instance() {
			return INSTANCE.instance;
		}
	}
}
