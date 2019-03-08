package com.saylorsolutions.fnstate4j;

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
}
