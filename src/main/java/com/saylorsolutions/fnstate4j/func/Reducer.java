package com.saylorsolutions.fnstate4j.func;

import java.util.Arrays;
import java.util.Collection;

import com.saylorsolutions.fnstate4j.Action;
import com.saylorsolutions.fnstate4j.State;

@FunctionalInterface
public interface Reducer {
	/**
	 * Reducer placeholder that just forwards the state, as-is.
	 */
	public static final Reducer NO_OP = (a, s) -> s;

	public State reduce(Action action, State state);

	public default Reducer andThen(Reducer other) {
		return (a, s) -> other.reduce(a, reduce(a, s));
	}

	public static Reducer combine(Reducer first, Reducer... others) {
		if(first == null) return NO_OP;
		return Arrays.stream(others).filter(r -> r != null).reduce(first, (r1, r2) -> r1.andThen(r2));
	}

	public static Reducer combine(Collection<Reducer> coll) {
		if(coll == null) return NO_OP;
		return coll.stream().filter(r -> r != null).reduce(NO_OP, (r1, r2) -> r1.andThen(r2));
	}
}
