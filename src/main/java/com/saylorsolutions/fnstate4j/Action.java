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

import java.util.Objects;
import java.util.Optional;

/**
 * An action, used to represent a potential state change in the system.
 *
 * @author Doug Saylor <doug@saylorsolutions.com>
 */
public class Action {
	private String type;
	private Optional<Object> payload;
	private Class<?> clazz;

	private Action(String type, Object payload) {
		Objects.requireNonNull(type);
		this.type = type;
		this.payload = Optional.ofNullable(payload);
		this.clazz = this.payload.isPresent() ? this.payload.get().getClass() : null;
	}

	/**
	 * Creates an action with the given action type and payload.
	 * 
	 * @param type    The type used to determine how reducers should change the
	 *                state.
	 * @param payload May be null. Carries state information to accompany the
	 *                action.
	 * @return A newly created, immutable {@code Action}.
	 */
	public static Action create(String type, Object payload) {
		final String typeParm = Objects.requireNonNull(type, "'type' parameter must not be null").trim();
		if (typeParm.isEmpty()) {
			throw new IllegalArgumentException("'type' parameter must not be empty or entirely whitespace");
		}
		return new Action(type, payload);
	}

	/**
	 * Creates an action with the given action type.
	 * 
	 * @param type The type used to determine how reducers should change the state.
	 * @return A newly created, immutable {@code Action}.
	 */
	public static Action create(String type) {
		return create(type, null);
	}

	public String getType() {
		return type;
	}

	public Optional<Object> getPayload() {
		return payload;
	}

	public Class<?> getPayloadClass() {
		return clazz;
	}

	/**
	 * No attempt is made to validate that the payload may be casted to the desired
	 * type.
	 *
	 * @param other The default value.
	 * @return The value of the payload if present, or the default value if it's
	 *         not.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getPayloadOrElse(T other) {
		return (T) payload.orElse(other);
	}

	/**
	 * @param clazz The reference type to compare against.
	 * @return Whether or not the payload may be assigned to a reference of the
	 *         given class. If the payload is null, then this always returns true.
	 */
	public boolean payloadAssignableTo(Class<?> clazz) {
		Objects.requireNonNull(clazz, "Can't compare the payload Class to a null Class");
		if (this.clazz == null) return true;
		return clazz.isAssignableFrom(this.clazz);
	}

	public boolean hasPayload() {
		return this.payload.isPresent();
	}

	public Object getPayloadOrNull() {
		if(hasPayload()) return this.payload.get();
		return null;
	}

	@Override
	public String toString() {
		return String.format("Action [type='%s', payload='%s']", getType(), getPayloadOrNull());
	}
}
