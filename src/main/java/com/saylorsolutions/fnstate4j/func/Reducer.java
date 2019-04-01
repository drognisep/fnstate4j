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

	/**
	 * Combine multiple {@code Reducer} instances into a single chain.
	 *
	 * @param first       The base {@code Reducer}, may not be null.
	 * @param middlewares 0..n instances to be combined with {@code first}.
	 * @return The created chain, or {@code NO_OP} if the parameters are not valid.
	 * @see Reducer#NO_OP
	 */
	public static Reducer combine(Reducer first, Reducer... others) {
		if (first == null)
			return NO_OP;
		if(others == null)
			return first;
		return Arrays.stream(others).filter(r -> r != null).reduce(first, (r1, r2) -> r1.andThen(r2));
	}

	public static Reducer combine(Collection<Reducer> coll) {
		if (coll == null || coll.isEmpty())
			return NO_OP;
		return coll.stream().filter(r -> r != null).reduce(NO_OP, (r1, r2) -> r1.andThen(r2));
	}
}
