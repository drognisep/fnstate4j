package com.saylorsolutions.fnstate4j;

import static org.junit.Assert.*;

import java.lang.reflect.Field;

import org.junit.Test;

public class StateStoreGlobalTest {
	@Test
	public void sameInstance() {
		assertSame(StateStore.Global.instance(), StateStore.Global.instance());
	}

	@Test(expected = IllegalAccessException.class)
	public void resistsHolderReflection() throws IllegalArgumentException, IllegalAccessException {
		Class<StateStore.Global> global = StateStore.Global.class;
		final Class<?> holder = global.getDeclaredClasses()[0];
		final Field[] holderFields = holder.getDeclaredFields();
		holderFields[0].setAccessible(true);
		holderFields[0].set(null, new StateStore());
	}

	@Test(expected = IllegalAccessException.class)
	public void resistsGlobalReflection() throws IllegalArgumentException, IllegalAccessException {
		final Class<?> global = StateStore.class.getDeclaredClasses()[0];
		final Class<?> holder = global.getDeclaredClasses()[0];
		final Field[] holderFields = holder.getDeclaredFields();
		holderFields[0].setAccessible(true);
		holderFields[0].set(null, new StateStore());
	}

	@Test(expected = ClassNotFoundException.class)
	public void resistsByNameReference() throws ClassNotFoundException {
		final Class<?> holder = Class.forName("com.saylorsolutions.fnstate4j.StateStore.Global.Holder");
	}
}
