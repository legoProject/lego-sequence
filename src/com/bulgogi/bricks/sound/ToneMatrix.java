package com.bulgogi.bricks.sound;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;


public class ToneMatrix {

	public boolean grid[][];

	final static int GRID_COL_COUNT = 14;
	final static int GRID_ROW_COUNT = 14;
	final static int ONE_TONE_DURATION = 2000;
	final static int TOTAL_PLAYBACK_DURATION = 2000;
	final static int NEXT_TONE_PLAYBACK_OFFSET = (TOTAL_PLAYBACK_DURATION / GRID_COL_COUNT);
	private int counter;
	private Timer sequencer;

	/** Called when the activity is first created. */
	
	public ToneMatrix(Context context) {
		// load up sounds
		SoundManager.getInstance();

		counter = 0;

		// initialise grid to store on/off states
		grid = new boolean[GRID_ROW_COUNT][GRID_COL_COUNT];

		initialiseGrid();
	} 
	
	public void prepareToneMatrix(Context context) {
		SoundManager.getInstance().initSounds(context);
		SoundManager.getInstance().loadSounds();
	}
	
	public void releaseToneMatrix() {
		SoundManager.getInstance().releaseSound();
	}

	// function to wipe the board clean
	private void initialiseGrid(){

		for (int column = 0; column < GRID_COL_COUNT; column++) {
			for (int rowElement = 0; rowElement < GRID_ROW_COUNT; rowElement++) {
				grid[column][rowElement] = false;
			}
		}
	}

	// start running the sequencer
	public void playToneMatrix(){
		// use a timer to trigger the next step of the sequencer
		sequencer = new Timer();
		sequencer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				playSequencerLine();
			}
		}, 0, NEXT_TONE_PLAYBACK_OFFSET);
	}

	public void stopToneMatrix(){
		if(sequencer != null){
			sequencer.cancel();
			sequencer.purge();
			sequencer = null;
		}
	}

	public void playSequencerLine(){

		counter++;
		if(counter >= GRID_COL_COUNT) { 
			counter = 0;
		}
		
		for(int i = 0; i < GRID_ROW_COUNT; i++){
			if(grid[counter][i] == true){
				SoundManager.getInstance().playSound(i+1, 1);
			}
		}
	}
	
	public synchronized void setGrid(boolean [][] grid) {
		
		if (grid.length > GRID_ROW_COUNT || grid[0].length > GRID_ROW_COUNT) {
			throw new IllegalArgumentException();
		}
		this.grid = grid;
	}
}