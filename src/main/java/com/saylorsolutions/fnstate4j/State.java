package com.saylorsolutions.fnstate4j;

import java.util.Objects;
import java.util.Optional;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Set;

public class State {
	private Map<String, Object> innerMap = HashMap.<String, Object>empty();
	private State prevState = null;

	public State() {}
	private State(Map<String, Object> newState, State oldState) {
		this.innerMap = newState;
		this.prevState = oldState;
	}

	/**
	 * Merge two states, preferring {@code theirs} in the case of conflicts. Cannot
	 * time travel before a merge because there are two possible ancestors for any
	 * given key in the {@code State}.
	 *
	 * @param ours
	 * @param theirs
	 * @return The merged {@code State}.
	 */
	public static State merge(State ours, State theirs) {
		Objects.requireNonNull(ours, "Cannot merge null State");
		Objects.requireNonNull(theirs, "Cannot merge null State");
		return new State(theirs.innerMap.merge(ours.innerMap), null);
	}

	public Optional<Object> get(String key) {
		return innerMap.get(key).toJavaOptional();
	}

	@SuppressWarnings("unchecked")
	public <T> Optional<T> get(String key, Class<T> desiredType) {
		return (Optional<T>)innerMap.get(key).toJavaOptional();
	}
	@SuppressWarnings("unchecked")
	public <T> T getOrElse(String key, T defaultValue) {
		return (T)innerMap.getOrElse(key, defaultValue);
	}

	public Object getOrNull(String key) {
		return this.getOrElse(key, null);
	}

	public Set<String> keySet() {
		return innerMap.keySet();
	}

	public State put(String key, Object value) {
		return new State(innerMap.put(key, value), this);
	}

	public boolean hasKey(String key) {
		return this.getOrNull(key) != null;
	}

	public int size() {
		return innerMap.size();
	}

	public boolean isEmpty() {
		return innerMap.isEmpty();
	}

	public boolean canTimeTravel() {
		return this.prevState != null;
	}

	public Optional<State> getPreviousState() {
		return Optional.ofNullable(this.prevState);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		this.innerMap.forEach(t -> sb.append(String.format(", ['%s':'%s']", t._1, t._2)));
		String mapString = sb.length() > 2 ? sb.substring(2) : sb.toString();
		return String.format("State [entries='%s']", mapString);
	}
}
