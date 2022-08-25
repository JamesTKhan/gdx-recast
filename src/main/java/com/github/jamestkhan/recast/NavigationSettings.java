package com.github.jamestkhan.recast;

import org.recast4j.recast.RecastConstants;

/**
 * Settings object for configuring Recast
 *
 * @author JamesTKhan
 * @version August 20, 2022
 */
public class NavigationSettings {
    public RecastConstants.PartitionType partitionType;

    public boolean useTiles;

    /** The width/depth size of tile's on the xz-plane. [Limit: >= 0] [Units: vx] **/
    public int tileSizeX;
    public int tileSizeZ;

    /** The xz-plane cell size to use for fields. [Limit: > 0] [Units: wu] */
    public float cellSize;

    /** The y-axis cell size to use for fields. [Limit: > 0] [Units: wu] */
    public float cellHeight;

    /** Minimum floor to 'ceiling' height that will still allow the floor area to be considered walkable. [Limit: >= 3] [Units: vx] */
    public float agentHeight;

    /**  The distance to erode/shrink the walkable area of the heightfield away from obstructions. [Limit: >=0] [Units: vx] */
    public float agentRadius;

    /** Maximum ledge height that is considered to still be traversable. [Limit: >=0] [Units: vx] **/
    public float agentMaxClimb;

    /** The maximum slope that is considered walkable. [Limits: 0 <= value < 90] [Units: Degrees] **/
    public float agentMaxSlope;

    /** The minimum number of cells allowed to form isolated island areas. [Limit: >=0] [Units: vx] **/
    public int regionMinSize;

    /**
     * Any regions with a span count smaller than this value will, if possible, be merged with larger regions. [Limit: >=0] [Units: vx]
     **/
    public int regionMergeSize;

    /** The maximum allowed length for contour edges along the border of the mesh. [Limit: >=0] [Units: vx] */
    public float maxEdgeLength;

    /** The maximum distance a simplfied contour's border edges should deviate the original raw contour. [Limit: >=0] [Units: vx] */
    public float maxEdgeError;

    /** The maximum number of vertices allowed for polygons generated during the contour to polygon conversion process. [Limit: >= 3] */
    public int maxVertsPerPoly;

    /** Sets the sampling distance to use when generating the detail mesh. (For height detail only.) [Limits: 0 or >= 0.9] [Units: wu] */
    public float detailSampleDistance;

    /** The maximum distance the detail mesh surface should deviate from heightfield data. (For height detail only.) [Limit: >=0] [Units: wu] */
    public float detailSampleMaxError;

    /** How many "steps" to take when path finding, the higher this is the less calculations needed */
    public float stepSize;

    /** How many iterations should we try for generating a path before giving up? */
    public int maxIterations;

    private NavigationSettings() {

    }

    public static final class Builder {
        private RecastConstants.PartitionType partitionType;
        private boolean useTiles;
        private int tileSizeX;
        private int tileSizeZ;
        private float cellSize;
        private float cellHeight;
        private float agentHeight;
        private float agentRadius;
        private float agentMaxClimb;
        private float agentMaxSlope;
        private int regionMinSize;
        private int regionMergeSize;
        private float maxEdgeLength;
        private float maxEdgeError;
        private int maxVertsPerPoly;
        private float detailSampleDistance;
        private float detailSampleMaxError;
        private float stepSize;
        private int maxIterations;

        private Builder() {
            // Set default values
            partitionType = RecastConstants.PartitionType.WATERSHED;
            useTiles = false;
            tileSizeX = 0;
            tileSizeZ = 0;
            cellSize = .30f;
            cellHeight = .20f;
            agentHeight = 2f;
            agentRadius = .60f;
            agentMaxClimb = 0.89f;
            agentMaxSlope = 45f;
            regionMinSize = 8;
            regionMergeSize = 20;
            maxEdgeLength = 12f;
            maxEdgeError = 1.3f;
            maxVertsPerPoly = 6;
            detailSampleDistance = 6f;
            detailSampleMaxError = 1f;
            stepSize = 1f;
            maxIterations = 1024;
        }

        public static Builder SettingsBuilder() {
            return new Builder();
        }

        public Builder partitionType(RecastConstants.PartitionType partitionType) {
            this.partitionType = partitionType;
            return this;
        }

        public Builder useTiles(boolean useTiles) {
            this.useTiles = useTiles;
            return this;
        }

        public Builder tileSizeX(int tileSizeX) {
            this.tileSizeX = tileSizeX;
            return this;
        }

        public Builder tileSizeZ(int tileSizeZ) {
            this.tileSizeZ = tileSizeZ;
            return this;
        }

        public Builder cellSize(float cellSize) {
            this.cellSize = cellSize;
            return this;
        }

        public Builder cellHeight(float cellHeight) {
            this.cellHeight = cellHeight;
            return this;
        }

        public Builder agentHeight(float agentHeight) {
            this.agentHeight = agentHeight;
            return this;
        }

        public Builder agentRadius(float agentRadius) {
            this.agentRadius = agentRadius;
            return this;
        }

        public Builder agentMaxClimb(float agentMaxClimb) {
            this.agentMaxClimb = agentMaxClimb;
            return this;
        }

        public Builder agentMaxSlope(float agentMaxSlope) {
            this.agentMaxSlope = agentMaxSlope;
            return this;
        }

        public Builder regionMinSize(int regionMinSize) {
            this.regionMinSize = regionMinSize;
            return this;
        }

        public Builder regionMergeSize(int regionMergeSize) {
            this.regionMergeSize = regionMergeSize;
            return this;
        }

        public Builder maxEdgeLength(float maxEdgeLength) {
            this.maxEdgeLength = maxEdgeLength;
            return this;
        }

        public Builder maxEdgeError(float maxEdgeError) {
            this.maxEdgeError = maxEdgeError;
            return this;
        }

        public Builder maxVertsPerPoly(int maxVertsPerPoly) {
            this.maxVertsPerPoly = maxVertsPerPoly;
            return this;
        }

        public Builder detailSampleDistance(float detailSampleDistance) {
            this.detailSampleDistance = detailSampleDistance;
            return this;
        }

        public Builder detailSampleMaxError(float detailSampleMaxError) {
            this.detailSampleMaxError = detailSampleMaxError;
            return this;
        }

        public Builder stepSize(float stepSize) {
            this.stepSize = stepSize;
            return this;
        }

        public Builder setMaxIterations(int iterations) {
            this.maxIterations = iterations;
            return this;
        }

        public NavigationSettings build() {
            NavigationSettings navigationSettings = new NavigationSettings();
            navigationSettings.agentMaxClimb = this.agentMaxClimb;
            navigationSettings.useTiles = this.useTiles;
            navigationSettings.detailSampleDistance = this.detailSampleDistance;
            navigationSettings.agentHeight = this.agentHeight;
            navigationSettings.regionMergeSize = this.regionMergeSize;
            navigationSettings.cellHeight = this.cellHeight;
            navigationSettings.maxVertsPerPoly = this.maxVertsPerPoly;
            navigationSettings.tileSizeZ = this.tileSizeZ;
            navigationSettings.maxEdgeLength = this.maxEdgeLength;
            navigationSettings.agentRadius = this.agentRadius;
            navigationSettings.regionMinSize = this.regionMinSize;
            navigationSettings.maxEdgeError = this.maxEdgeError;
            navigationSettings.cellSize = this.cellSize;
            navigationSettings.partitionType = this.partitionType;
            navigationSettings.agentMaxSlope = this.agentMaxSlope;
            navigationSettings.detailSampleMaxError = this.detailSampleMaxError;
            navigationSettings.tileSizeX = this.tileSizeX;
            navigationSettings.stepSize = this.stepSize;
            navigationSettings.maxIterations = this.maxIterations;
            return navigationSettings;
        }
    }

}
