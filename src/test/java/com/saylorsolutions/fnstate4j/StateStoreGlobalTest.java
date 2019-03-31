package com.saylorsolutions.fnstate4j;

import static org.junit.Assert.*;

import org.junit.Test;

public class StateStoreGlobalTest {
	@Test
	public void sameInstance() {
		assertSame(StateStore.Global.instance(), StateStore.Global.instance());
	}
}
