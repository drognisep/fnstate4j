package com.saylorsolutions.fnstate4j.func;

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
