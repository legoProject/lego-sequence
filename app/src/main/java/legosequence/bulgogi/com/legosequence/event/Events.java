package legosequence.bulgogi.com.legosequence.event;

import legosequence.bulgogi.com.legosequence.sound.InstrumentType;

public class Events {
    public static class PatternDetect {
        private final boolean[][] mPattern;

        private PatternDetect(boolean[][] pattern) {
            mPattern = pattern;
        }

        public static PatternDetect eventOf(boolean[][] pattern) {
            return new PatternDetect(pattern);
        }

        @Override
        public String toString() {
            return "LauncherEvent:AddShortcut : " + mPattern;
        }

        public boolean[][] getPatterns() {
            return mPattern;
        }
    }

    public static class SoundSwitching {
        private final InstrumentType mType;

        private SoundSwitching(InstrumentType type) {
            mType = type;
        }

        public static SoundSwitching eventOf(InstrumentType type) {
            return new SoundSwitching(type);
        }

        @Override
        public String toString() {
            return "LauncherEvent:AddShortcut : " + mType;
        }

        public InstrumentType getType() {
            return mType;
        }
    }

    public static class SoundRelease {

        private SoundRelease() {
        }

        public static SoundRelease eventOf() {
            return new SoundRelease();
        }

        @Override
        public String toString() {
            return "SoundRelease";
        }
    }
}
