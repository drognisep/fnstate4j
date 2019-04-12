package com.saylorsolutions.fnstate4j.func;

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

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import com.saylorsolutions.fnstate4j.Action;
import com.saylorsolutions.fnstate4j.State;

/**
 * Used to pre-process {@code Action}s that are dispatched to the
 * {@code StateStore}. The {@code process} method can cencel further processing
 * of the {@code Action} by returning false. {@code Action} and state
 * modifications will not be persisted along the {@code Middleware} chain. If
 * this is a requirement, cancel the current dispatch after dispatching a new
 * {@code Action} instead.
 *
 * <pre>
 * Middleware redispatch = (a, s) -&gt; {
 * 	if (a.getType().equals("REDISPATCH_ME")) {
 * 		AppStateStore.instance().dispatch(Action.create("NEW_ACTION"));
 * 		return false;
 * 	}
 * };
 * </pre>
 *
 * @author Doug Saylor (doug at saylorsolutions.com)
 *
 */
@FunctionalInterface
public interface Middleware {
	/**
	 * Placeholder {@code Middleware} that just accepts the given action.
	 */
	public static final Middleware NO_OP = (a, s) -> true;

	/**
	 * Represents an operation that may cancel the propagation of a state mutating
	 * {@code Action}.
	 *
	 * @param action The action to be examined.
	 * @param state  The current state, provided for context.
	 * @return Whether or not the {@code Action} should continue to be processed. It
	 *         is cancelled otherwise.
	 */
	public boolean process(Action action, State state);

	public default Middleware andThen(final Middleware other) {
		Objects.requireNonNull(other, "Cannot merge null middleware");

		return (a, s) -> {
			if (process(a, s))
				return other.process(a, s);
			return false;
		};
	}

	/**
	 * Combine multiple {@code Middleware} instances into a single chain.
	 *
	 * @param first       The base {@code Middleware}, may not be null.
	 * @param middlewares 0..n instances to be combined with {@code first}.
	 * @return The created chain, or {@code NO_OP} if the parameters are not valid.
	 * @see Middleware#NO_OP
	 */
	public static Middleware combine(final Middleware first, final Middleware... middlewares) {
		if (first == null)
			return NO_OP;
		if(middlewares == null)
			return first;
		return Arrays.stream(middlewares).filter(m -> m != null).reduce(first, (m1, m2) -> m1.andThen(m2));
	}

	public static Middleware combine(Collection<Middleware> coll) {
		if (coll == null || coll.isEmpty())
			return NO_OP;
		return coll.stream().filter(m -> m != null).reduce(NO_OP, (m1, m2) -> m1.andThen(m2));
	}
}
