package com.github.jamestkhan.recast;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.github.jamestkhan.recast.utils.NavMeshTool;
import org.recast4j.detour.NavMesh;

/**
 * Entry point for gdx-recast.
 *
 * @author JamesTKhan
 * @version August 18, 2022
 */
public class Pathfinder {
    protected NavMeshData navMeshData;
    private final NavMeshTool tool;

    public Pathfinder(NavMeshData navMeshData) {
        this(new PathFinderSettings(), navMeshData);
    }

    public Pathfinder(PathFinderSettings settings, NavMeshData navMeshData) {
        this.navMeshData = navMeshData;
        tool = new NavMeshTool(settings, navMeshData);
    }

    /**
     * Get the current NavMesh
     * @return the current navmesh
     */
    public NavMesh getNavMesh() {
        return navMeshData.getNavMesh();
    }

    /**
     * Set the current NavMesh
     * @param navMesh the navmesh to set
     */
    public void setNavMesh(NavMesh navMesh) {
        navMeshData.update(navMesh);
    }

    /**
     * Get a path from the start position to the end position
     *
     * @param start World start point
     * @param end World end point
     * @param pathOut list of floats [x,y,z] that will be populated
     */
    public void getPath(Vector3 start, Vector3 end, Array<float[]> pathOut) {
        pathOut.clear();
        tool.setPositions(start, end);
        tool.recalculate(pathOut);
    }

    public PathFinderSettings getSettings() {
        return tool.getSettings();
    }
}
