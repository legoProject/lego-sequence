package com.bulgogi.bricks.sound;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;


public class MixToneMatrix implements ToneMatrix{

	public boolean mixGrid[][];
	
	private final static int MIX_GRID_COL_COUNT = 4;
	private final static int MIX_GRID_ROW_COUNT = 4;
	private final static int NEXT_TONE_PLAYBACK_OFFSET = 500;
	
	private Timer sequencer;

	private Music[][] mixTones;
	private boolean[][] played;
	
	public MixToneMatrix() {
		mixGrid = new boolean[MIX_GRID_ROW_COUNT][MIX_GRID_COL_COUNT];
		initializeGrid();
	} 

	@Override
	public void loadSound() {
		this.mixTones = new Music[MIX_GRID_ROW_COUNT][MIX_GRID_COL_COUNT];
		this.played = new boolean[MIX_GRID_ROW_COUNT][MIX_GRID_COL_COUNT];

		for(int i = 0; i < MIX_GRID_ROW_COUNT; i++){
			for (int j=0; j < MIX_GRID_COL_COUNT; j++){
				String str = "sound/" + (i+1) + "x" + (j+1) + ".ogg";
				//mixTones[i][j] = Gdx.audio.newSound(Gdx.files.internal(str));
				mixTones[i][j] = Gdx.audio.newMusic(Gdx.files.internal(str));
			}
		}
	}

	private void playSound(int x, int y)
	{
		if (played[x][y] == true) {
			return ;
		}
		Log.e("MixToneMatrix", "play()");
		this.mixTones[x][y].play();
		this.mixTones[x][y].setLooping(true);
		this.played[x][y] = true;
	} 
	
	private void stopLoopSound(int x, int y) {
		if (played[x][y] == false) {
			return ;
		}
		Log.e("MixToneMatrix", "stop()");
		mixTones[x][y].pause();
		played[x][y] = false;
	}

	@Override
	public void releaseToneMatrix() {
		for (int i = 0; i < mixTones.length; i++) {
			for (int j=0; j < mixTones[0].length; j++) {
				if (this.mixTones[i][j].isPlaying()) {
					this.mixTones[i][j].pause();
				}
				this.mixTones[i][j].dispose();
			}
		}
		
		this.mixTones = null;
	}

	private void initializeGrid(){
		for (int column = 0; column < MIX_GRID_COL_COUNT; column++) {
			for (int rowElement = 0; rowElement < MIX_GRID_ROW_COUNT; rowElement++) {
				mixGrid[column][rowElement] = false;
			}
		}
	}

	@Override
	public void playToneMatrix(){
		sequencer = new Timer();
		sequencer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				playMix();
			}

		}, 0, NEXT_TONE_PLAYBACK_OFFSET);
	}

	private void playMix() {
		
		for(int i = 0; i < MIX_GRID_ROW_COUNT; i++){
			for (int j=0; j < MIX_GRID_COL_COUNT; j++){
				if(mixGrid[i][j] == true){
					playSound(i, j);
				}else {
					stopLoopSound(i, j);
				}
			}
		}
	}

	@Override
	public synchronized void changeInputGrid(boolean [][] grid) {

		if (grid.length > MIX_GRID_COL_COUNT || grid[0].length > MIX_GRID_ROW_COUNT) {
			throw new IllegalArgumentException();
		}
		this.mixGrid = grid;
	}

}