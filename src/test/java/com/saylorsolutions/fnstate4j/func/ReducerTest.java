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

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.saylorsolutions.fnstate4j.Action;
import com.saylorsolutions.fnstate4j.State;

public class ReducerTest {
	private static final String actionType = "INC";
	private static final String stateKey = "VAL";
	private Reducer increment;
	private State state;

	@Before
	public void setup() {
		this.increment = (a, s) -> {
			if(a.getType().equals(actionType)) {
				return s.put(stateKey, s.getOrElse(stateKey, Integer.valueOf(0)) + 1);
			}
			return s;
		};
		this.state = new State();
	}

	@Test
	public void testIncrement() {
		State reducedState = increment.reduce(Action.create(actionType), state);
		assertEquals(Integer.valueOf(1), reducedState.getOrElse(stateKey, Integer.valueOf(77)));
	}

	@Test
	public void testReducerCombineCollection() {
		State reducedState = Reducer.combine(Arrays.asList(new Reducer[] {increment, increment})).reduce(Action.create(actionType), state);
		assertEquals(Integer.valueOf(2), reducedState.getOrElse(stateKey, Integer.valueOf(77)));
	}

	@Test
	public void testReducerCombine() {
		State reducedState = Reducer.combine(increment, increment).reduce(Action.create(actionType), state);
		assertEquals(Integer.valueOf(2), reducedState.getOrElse(stateKey, Integer.valueOf(77)));
	}

	@Test
	public void testAndThenChain() {
		State reducedState = increment.andThen(increment).reduce(Action.create(actionType), state);
		assertEquals(Integer.valueOf(2), reducedState.getOrElse(stateKey, Integer.valueOf(77)));
	}
}
