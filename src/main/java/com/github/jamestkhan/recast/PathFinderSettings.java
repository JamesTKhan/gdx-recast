package com.github.jamestkhan.recast;

/**
 * Settings for the PathFinder
 *
 * @author JamesTKhan
 * @version September 01, 2023
 */
public class PathFinderSettings {

    /** How many iterations should we try for generating a path before giving up? */
    int maxIterations = 1024;
    /** How many "steps" to take when path finding, the higher this is the less calculations needed */
    float stepSize = 1f;

    public int getMaxIterations() {
        return maxIterations;
    }

    public float getStepSize() {
        return stepSize;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public void setStepSize(float stepSize) {
        this.stepSize = stepSize;
    }
}
