package com.bulgogi.bricks.sound;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class AudioTrackSoundPlayer {
    private HashMap<String, InputStream> audioStream = null;
    private Context context;

    public AudioTrackSoundPlayer(Context context) {
        this.context = context;
        audioStream = new HashMap<String, InputStream>();
    }

    public void playNote(String note) {
//		if (!isNotePlaying(note))
//		{
        PlayThread thread = new PlayThread(note);
        thread.start();
//			threadMap.put(note, thread);
//		}
    }

	/*public void stopNote(String note)
    {
		PlayThread thread = threadMap.get(note);
		if (thread != null)
		{
			thread.requestStop();
			threadMap.remove(note);
		}
	}*/

/*	public boolean isNotePlaying(String note)
    {
		return threadMap.containsKey(note);
	}*/

    private void loadAudioStreams(String path) {

    }

    private class PlayThread extends Thread {
        String note;
        boolean stop = false;
        AudioTrack audioTrack = null;

        public PlayThread(String note) {
            super();
            this.note = note;
        }

        public void run() {
            try {
                String path = note + ".wav";

                AssetManager assetManager = context.getAssets();
                AssetFileDescriptor ad = assetManager.openFd(path);
                long fileSize = ad.getLength();
                int bufferSize = 4096;
                byte[] buffer = new byte[bufferSize];

                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
                audioTrack.play();

                InputStream audioStream = null;
                audioStream = assetManager.open(path);

                int headerOffset = 0x2C;
                audioStream.read(buffer, 0, headerOffset);

                long bytesWritten = 0;
                int bytesRead = 0;
                Log.e("test", "play:" + path);
                bytesWritten = 0;
                bytesRead = 0;

                // read until end of file
                while (bytesRead != -1) {
                    bytesRead = audioStream.read(buffer, 0, bufferSize);
                    audioTrack.write(buffer, 0, bytesRead);
                }
                Log.e("test", "stop:" + path);
                audioTrack.stop();
                audioTrack.release();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public synchronized void requestStop() {
            stop = true;
        }
    }
}
