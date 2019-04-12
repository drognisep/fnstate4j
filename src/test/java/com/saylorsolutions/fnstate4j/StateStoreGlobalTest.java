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
	public void cannotResetInstance() throws IllegalArgumentException, IllegalAccessException {
		final Class<? extends Global> clazz = StateStore.Global.INSTANCE.getClass();
		final Field instance = clazz.getDeclaredFields()[0];
		instance.setAccessible(true);
		instance.set(StateStore.Global.INSTANCE, new StateStore());
	}
}
