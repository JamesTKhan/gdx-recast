package com.github.jamestkhan.recast;

import org.recast4j.recast.RecastConstants;

/**
 * Settings object for generating a NavMesh.
 * Ex.
 * NavMeshGenSettings settings = NavMeshGenSettings.Builder.SettingsBuilder()
 *         .agentRadius(1.0f)
 *         .agentHeight(2.0f)
 *         .agentMaxClimb(2.50f)
 *         .build();
 *
 * @author JamesTKhan
 * @version August 20, 2022
 */
public class NavMeshGenSettings {
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

    private NavMeshGenSettings() {

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

        public NavMeshGenSettings build() {
            NavMeshGenSettings navMeshGenSettings = new NavMeshGenSettings();
            navMeshGenSettings.agentMaxClimb = this.agentMaxClimb;
            navMeshGenSettings.useTiles = this.useTiles;
            navMeshGenSettings.detailSampleDistance = this.detailSampleDistance;
            navMeshGenSettings.agentHeight = this.agentHeight;
            navMeshGenSettings.regionMergeSize = this.regionMergeSize;
            navMeshGenSettings.cellHeight = this.cellHeight;
            navMeshGenSettings.maxVertsPerPoly = this.maxVertsPerPoly;
            navMeshGenSettings.tileSizeZ = this.tileSizeZ;
            navMeshGenSettings.maxEdgeLength = this.maxEdgeLength;
            navMeshGenSettings.agentRadius = this.agentRadius;
            navMeshGenSettings.regionMinSize = this.regionMinSize;
            navMeshGenSettings.maxEdgeError = this.maxEdgeError;
            navMeshGenSettings.cellSize = this.cellSize;
            navMeshGenSettings.partitionType = this.partitionType;
            navMeshGenSettings.agentMaxSlope = this.agentMaxSlope;
            navMeshGenSettings.detailSampleMaxError = this.detailSampleMaxError;
            navMeshGenSettings.tileSizeX = this.tileSizeX;
            return navMeshGenSettings;
        }
    }

}
