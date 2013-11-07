package com.bulgogi.bricks.sound;

public interface ToneMatrix {
	abstract public void changeInputGrid(boolean[][] grid);
	abstract public void loadSound();
	abstract public void playToneMatrix();
	abstract public void releaseToneMatrix();
}
