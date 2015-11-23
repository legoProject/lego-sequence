package legosequence.bulgogi.com.legosequence.config;

public class Constant {
    private Constant() {
    }

    public static enum SEQUENCE_TYPE {
        BLUE, GREEN, CYAN
    }

    public static final int SMALL_CELL_SIZE = 8;
    public static final int SMALL_GRID_SIZE = SMALL_CELL_SIZE * SMALL_CELL_SIZE;
    public static final int LARGE_CELL_SIZE = 16;
    public static final int LARGE_GRID_SIZE = LARGE_CELL_SIZE * LARGE_CELL_SIZE;
    public static final int SOUND_GRID_SIZE = 16;

    public static class Config {
        public static final boolean DEBUG = false;
    }

    public static class Extra {
    }
}
