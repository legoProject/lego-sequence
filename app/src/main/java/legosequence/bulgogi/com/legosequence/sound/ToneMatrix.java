package legosequence.bulgogi.com.legosequence.sound;

public interface ToneMatrix {
    void changeInputGrid(boolean[][] grid);

    void loadSound(InstrumentType type);

    void playToneMatrix();

    void releaseToneMatrix();
}
