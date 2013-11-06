package com.bulgogi.bricks.event;

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
}
