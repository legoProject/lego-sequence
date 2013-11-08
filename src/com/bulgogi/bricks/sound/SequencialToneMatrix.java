package com.bulgogi.bricks.sound;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;


public class SequencialToneMatrix implements ToneMatrix{

	public boolean grid[][];
	
	private final static String soundExt = ".ogg";
	
	private final static int GRID_COL_COUNT = 14;
	private final static int GRID_ROW_COUNT = 14;
	
	private final static int TOTAL_PLAYBACK_DURATION = 2000;
	private final static int NEXT_TONE_PLAYBACK_OFFSET = (TOTAL_PLAYBACK_DURATION / GRID_COL_COUNT) +50;
	
	private int counter;
	private Timer sequencer;

	private Sound[] tones;
	
	public SequencialToneMatrix() {
		counter = 0;
		grid = new boolean[GRID_ROW_COUNT][GRID_COL_COUNT];
		initialiseGrid();
	} 

	@Override
	public void loadSound(InstrumentType type) {
		
		this.tones = new Sound[14];
		
		String path = null;
		
		if(type == InstrumentType.DRUM) {
			path = "drum/d_";
		} else if (type == InstrumentType.TONE) {
			path = "sound/t_";
		}

		for (int i = 0; i < tones.length; i++) {
			String str = path + (i+1) + soundExt;
			tones[i] = Gdx.audio.newSound(Gdx.files.internal(str));
		}
	}

	
	private void playSound(int index)
	{
		if ((index >= 0) && (index <= this.tones.length - 1)) {
			this.tones[index].play();
		}
	} 
	
	@Override
	public void releaseToneMatrix() {
		if (sequencer != null)
			sequencer.cancel();
		
		for (int i = 0; i < tones.length; i++) {
			this.tones[i].stop();
			this.tones[i].dispose();
		}
		this.tones = null;
	}

	private void initialiseGrid(){
		for (int column = 0; column < GRID_COL_COUNT; column++) {
			for (int rowElement = 0; rowElement < GRID_ROW_COUNT; rowElement++) {
				grid[column][rowElement] = false;
			}
		}
	}
	
	@Override
	public void playToneMatrix(){
		sequencer = new Timer();
		sequencer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
					playSequencerLine();
			}

		}, 0, NEXT_TONE_PLAYBACK_OFFSET);
	}

	private void playSequencerLine(){
		
		Log.e("test", "playSequecerLine");

		counter++;
		if(counter >= GRID_COL_COUNT) { 
			counter = 0;
		}

		for(int i = 0; i < GRID_ROW_COUNT; i++){
			if(grid[counter][i] == true){
				playSound(i);
			}
		}
	}

	@Override
	public synchronized void changeInputGrid(boolean [][] grid) {

		if (grid.length > GRID_ROW_COUNT || grid[0].length > GRID_ROW_COUNT) {
			throw new IllegalArgumentException();
		}
		this.grid = grid;
	}

}