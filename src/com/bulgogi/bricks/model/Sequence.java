package com.bulgogi.bricks.model;

import com.bulgogi.bricks.detector.*;

public class Sequence {
    private PlateDetector mPlateDetector;
    private PatternDetector mPatternDetector;

    public Sequence(PlateDetector plateDetector, PatternDetector patternDetector) {
        mPlateDetector = plateDetector;
        mPatternDetector = patternDetector;
    }

    public PlateDetector getPlateDetector() {
        return mPlateDetector;
    }

    public PatternDetector getPatternDetector() {
        return mPatternDetector;
    }

    public boolean isEnabled() {
        return mPlateDetector.isEnabled();
    }
}
