package com.bulgogi.bricks.sound;

public enum InstrumentType {

    TONE(0),
    DRUM(1),
    MIX(2);

    private final int value;

    private InstrumentType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
