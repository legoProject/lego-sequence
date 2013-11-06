package com.bulgogi.bricks.event;

public class Events {
	public static class PatternDetact {
		private final boolean[][] mPattern;
		
		private PatternDetact(boolean[][] pattern) {
			mPattern = pattern;
		}
		
		public static PatternDetact eventOf(boolean[][] pattern) {
			return new PatternDetact(pattern);
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
