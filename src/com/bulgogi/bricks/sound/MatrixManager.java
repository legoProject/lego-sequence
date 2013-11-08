package com.bulgogi.bricks.sound;

import android.util.Log;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.bulgogi.bricks.event.Events;

import de.greenrobot.event.EventBus;

public class MatrixManager implements ApplicationListener{
	private OrthographicCamera camera;
	private ToneMatrix mToneMatrix;
	
	@Override
	public void create() {
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		
		camera = new OrthographicCamera(1, h/w);
		
		mToneMatrix = new SequencialToneMatrix();
//		mToneMatrix = new MixToneMatrix(this);
		EventBus.getDefault().register(this);
		Log.i("MainActivity","create");
		System.out.println("create");
	}
	
	@Override
	public void dispose() {
		Log.i("MainActivity","dispose");
		mToneMatrix.releaseToneMatrix();		
	}
	
	@Override
	public void render() {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
	}
	
	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
		Log.i("MainActivity","pause");
	}

	@Override
	public void resume() {
		Log.i("MainActivity","resume");
		mToneMatrix.loadSound();
        mToneMatrix.playToneMatrix();
	}
	
	public void onEventMainThread(Events.PatternDetect patterns) {
    	Log.i("MainActivity","onEventMainThread : " + patterns);
    	
    	//test
    	boolean[][] testGrid = {
    			{true,false,false,true},
    			{true,false,false,false},
    			{false,true,true,false},
    			{true,false,false,true}
    	};
    	mToneMatrix.changeInputGrid(patterns.getPatterns());
}
}
