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
-   How the state is mutated for this particular funcitonality is defined once, and only once.
-   Anything in the program may determine what the current state of the counter is.
-   Anything can _subscribe_ to the `StateStore` to be notified when state changes.

_To be continued..._
