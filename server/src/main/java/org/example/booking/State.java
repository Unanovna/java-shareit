package org.example.booking;

import org.example.exception.ArgumentException;

import java.util.Optional;

public enum State {
    ALL,
    CURRENT,
    FUTURE,
    PAST,
    REJECTED,
    WAITING;

    public static Optional<State> from(String stringState) {
        for (State state : values()) {
            if (state.name().equalsIgnoreCase(stringState)) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }

    public static State getState(String text) {
        if ((text == null) || text.isBlank()) {
            return State.ALL;
        }
        try {
            return State.valueOf(text.toUpperCase().trim());
        } catch (Exception e) {
            throw new ArgumentException(String.format("Unknown state: %s", text));
        }
    }
}
