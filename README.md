# fnstate4j

Creates a Java port of base Redux functionality

## Why?

I wanted this kind of functionality for JavaFX/Swing apps, and I really wanted to build it myself because
it sounded like a fun way to think through some of the crazier use-cases for app-wide state.

## How does it work?

For those unfamiliar with how Redux works, check out this
[neat cartoon guide](https://code-cartoons.com/a-cartoon-intro-to-redux-3afb775501a6).

If you're instead looking for details about _this_ library, then read on!

## The 3 rules

These are the rules I'm trying to adhere to in this library.

### Rule #1: [Single source of truth](https://redux.js.org/introduction/three-principles#single-source-of-truth)

This is achieved by using the `AppStateStore` to initialize a singleton that may be statically referenced
through out the application. With this in place, all of the disparate components may add `Middleware`s,
`Reducer`s, dispatch `Action`s, etc.

### Rule #2: [State is read-only](https://redux.js.org/introduction/three-principles#state-is-read-only)

Using an immutable `State` object is part of this, and only allowing state updates by `Reducer`s is the other.

### Rule #3: [Changes are made with pure functions](https://redux.js.org/introduction/three-principles#changes-are-made-with-pure-functions)

`Reducers` are where the magic happens for this. These are the only things able to make any actual changes to the state.

## Examples

How about a simple (and cliche) counter example? Here's the list of requirements.

-   The counter may only be updated in the app state.
-   How the state is mutated for this particular functionality is defined once, and only once.
-   Anything in the program may determine what the current state of the counter is.
-   Anything can _subscribe_ to the `StateStore` to be notified when state changes.
-   Ensure that only positive integers are used as the `Action` payload.

The easiest way to accomplish this is to use a blocking (the default) `StateStore` to manage the state of the counter, and subscribe to changes to update another variable. We could just examine the current state, but with this approach we can illustrate all of the moving pieces.

**Setting up constants is a good way to avoid issues**

```Java
final String DECREMENT_ACTION = "DECREMENT";
final String INCREMENT_ACTION = "INCREMENT";
final String COUNTER_STATE = "COUNTER";
// Using global state allows anything to subscribe to the counter value.
final StateStore STORE = StateStore.Global.instance();
```

**This `Reducer` will react to state changes**

```Java
STORE.addReducer((a, s) -> {
    int count = s.getOrElse(COUNTER_STATE, 0);
    if(a.getType().equals(INCREMENT_ACTION)) {
        int delta = a.getPayloadOrElse(1);
        return s.put(COUNTER_STATE, count + delta);
    } else if(a.getType().equals(DECREMENT_ACTION)) {
        int delta = a.getPayloadOrElse(1);
        return s.put(COUNTER_STATE, count - delta);
    }
    return s;
});
```

**This `Middleware` will reject integers <= 1 for these `Actions`**

```Java
STORE.addMiddleware((a, s) -> {
    if(INCREMENT_ACTION.equals(a.getType()) || DECREMENT_ACTION.equals(a.getType())) {
        int delta = a.getPayloadOrElse(1);
        if(delta < 1) {
            System.err.println("Delta should be greater than or equal to 1");
            return false;
        } else {
            System.out.println(String.format("Good %s payload of %s", a.getType(), delta));
        }
    }
    return true;
});
```

**Here a consumer is registering to update a variable**

```Java
STORE.subscribe(s -> {
    counterValue = s.getOrElse(COUNTER_STATE, 0);
    System.out.println("Counter: " + counterValue);
});
```

**Here it is all together**

```Java
package com.saylorsolutions.fnstate4j;

import static java.lang.String.format;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

public class CounterTest {
    public static final String DECREMENT_ACTION = "DECREMENT";
    public static final String INCREMENT_ACTION = "INCREMENT";
    public static final String COUNTER_STATE = "COUNTER";
    public static final Action ADD_ONE = Action.create(INCREMENT_ACTION);
    public static final Action REMOVE_ONE = Action.create(DECREMENT_ACTION);
    public static final StateStore STORE = StateStore.Global.instance();

    public static int counterValue;

    @BeforeClass
    public static void setupOnce() {
        STORE.addReducer((a, s) -> {
            int count = s.getOrElse(COUNTER_STATE, 0);
            if(a.getType().equals(INCREMENT_ACTION)) {
                int delta = a.getPayloadOrElse(1);
                return s.put(COUNTER_STATE, count + delta);
            } else if(a.getType().equals(DECREMENT_ACTION)) {
                int delta = a.getPayloadOrElse(1);
                return s.put(COUNTER_STATE, count - delta);
            }
            return s;
        });
        STORE.addMiddleware((a, s) -> {
            if(INCREMENT_ACTION.equals(a.getType()) || DECREMENT_ACTION.equals(a.getType())) {
                int delta = a.getPayloadOrElse(1);
                if(delta < 1) {
                    System.err.println("Delta should be greater than or equal to 1");
                    return false;
                } else {
                    System.out.println(format("Good %s payload of %s", a.getType(), delta));
                }
            }
            return true;
        });
        STORE.subscribe(s -> {
            counterValue = s.getOrElse(COUNTER_STATE, 0);
            System.out.println(format("Counter: %d", counterValue));
        });
    }

    @Test
    public final void testChanges() {
        STORE.dispatch(ADD_ONE);
        STORE.dispatch(REMOVE_ONE);
        STORE.dispatch(REMOVE_ONE);

        // This is only guaranteed with a blocking StateStore.
        assertEquals(-1, counterValue);

        // Reset
        STORE.dispatch(ADD_ONE);
        assertEquals(0, counterValue);
    }

    @Test
    public final void testRejectActions() {
        assertEquals(0, counterValue);

        STORE.dispatch(Action.create(INCREMENT_ACTION, -3));
        STORE.dispatch(Action.create(INCREMENT_ACTION, -7));

        // May seem like it should be equal
        assertNotEquals(-10, counterValue);

        // Actions were actually rejected
        assertEquals(0, counterValue);
    }
}
```

## Conclusion

It might seem like a lot of code for just a simple counter, but the ROI is often gained later when many different modules want to respond to changing state without tightly coupling themselves to the source(s) of that change. This is an incredibly powerful concept with numerous applications in modern applications.

Happy coding! :)
