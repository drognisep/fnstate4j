package com.saylorsolutions.fnstate4j;

import static org.junit.Assert.*;

import java.lang.reflect.Field;

import org.junit.Test;

import com.saylorsolutions.fnstate4j.StateStore.Global;

public class StateStoreGlobalTest {
	@Test
	public void sameInstance() {
		assertSame(StateStore.Global.instance(), StateStore.Global.instance());
	}

	@Test(expected = IllegalAccessException.class)
	public void resistsNewInstanceReflection() throws IllegalArgumentException, IllegalAccessException {
		Class<StateStore.Global> global = StateStore.Global.class;
		final Field[] holderFields = global.getDeclaredFields();
		holderFields[0].setAccessible(true);
		holderFields[0].set(null, new StateStore());
	}

	@Test(expected = IllegalAccessException.class)
	public void cannotResetInstance() throws IllegalArgumentException, IllegalAccessException {
		final Class<? extends Global> clazz = StateStore.Global.INSTANCE.getClass();
		final Field instance = clazz.getDeclaredFields()[0];
		instance.setAccessible(true);
		instance.set(StateStore.Global.INSTANCE, new StateStore());
	}
}
