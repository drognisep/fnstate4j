package com.saylorsolutions.fnstate4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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
 * @author Doug Saylor <doug@saylorsolutions.com>
 *
 */
public class StateStore {
	private State state = new State();
	private Reducer rootReducer;
	private Set<Reducer> reducers;
	private Middleware rootMiddleware;
	private Set<Middleware> middlewares;
	private Map<UUID, Consumer<State>> subscribers = new HashMap<>();
	private boolean nonBlocking;
	private ExecutorService executor = Executors.newSingleThreadExecutor();

	public StateStore(State initialState, Reducer rootReducer, Middleware rootMiddleware, boolean nonBlocking) {
		super();
		this.state = initialState;
		this.rootReducer = rootReducer;
		this.reducers.add(rootReducer);
		this.rootMiddleware = rootMiddleware;
		this.middlewares.add(rootMiddleware);
		this.nonBlocking = nonBlocking;
	}

	public StateStore(State initialState, Reducer rootReducer, Middleware rootMiddleware) {
		this(initialState, rootReducer, rootMiddleware, true);
	}

	public StateStore(State initialState) {
		this(initialState, Reducer.NO_OP, Middleware.NO_OP);
	}

	public StateStore() {
		this(new State());
	}

	public void dispatch(final Action action) {
		internalDispatch(action, nonBlocking);
	}

	/**
	 * Dispatches an {@code Action} and blocks the calling thread, regardless of the
	 * value of {@code StateStore#nonBlocking}.
	 *
	 * @param action
	 * @see StateStore#isNonBlocking()
	 * @see StateStore#setNonBlocking(boolean)
	 */
	public void blockingDispatch(final Action action) {
		internalDispatch(action, false);
	}

	private void internalDispatch(final Action action, final boolean nonBlocking) {
		if(this.rootMiddleware.process(action, this.state)) {
			synchronized(this.state) {
				this.state = this.rootReducer.reduce(action, this.state);
				if(nonBlocking) {
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
		if(entry.isPresent()) unsubscribe(entry.get().getKey());
	}

	private Optional<Entry<UUID, Consumer<State>>> findConsumerEntry(Consumer<State> subscriber) {
		Optional<Entry<UUID, Consumer<State>>> existingId = this.subscribers.entrySet().stream().filter(e -> e.getValue().equals(subscriber)).findFirst();
		return existingId;
	}

	/**
	 * @return Whether subscribers are called in a separate thread.
	 */
	public boolean isNonBlocking() {
		return this.nonBlocking;
	}

	public void setNonBlocking(boolean nonBlocking) {
		this.nonBlocking = nonBlocking;
	}

	public State getState() {
		return this.state;
	}

	/**
	 * Adds a new {@code Reducer} to the chain. Does not allow duplicates.
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
	 * @param middleware
	 */
	public void removeMiddleware(Middleware middleware) {
		Objects.requireNonNull(middleware);
		synchronized (this.middlewares) {
			this.middlewares.remove(middleware);
			this.rootMiddleware = Middleware.combine(this.middlewares);
		}
	}
}
