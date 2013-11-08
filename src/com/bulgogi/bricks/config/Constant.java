package com.bulgogi.bricks.config;

public class Constant {
	private Constant() {
	}

	public static enum SEQUENCE_TYPE {
		BLUE, GREEN, CYAN 
	};
	
	public static final int CELL_SIZE = 16;
	public static final int GRID_SIZE = CELL_SIZE * CELL_SIZE;
	
	public static final int SOUND_GRID_SIZE = 16;
	
	public static class Config {
		public static final boolean DEBUG = false;
	}

	public static class Extra {
	}
}
