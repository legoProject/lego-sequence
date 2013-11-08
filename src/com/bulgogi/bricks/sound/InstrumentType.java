package com.bulgogi.bricks.sound;

public enum InstrumentType {
	
	DRUM(1),
	TONE(2),
	MIX(3);
	
	private final int value;
	
	private InstrumentType(int value) {
		this.value = value; 
	}
	
	public int getValue(){
		return value;
	}
}
