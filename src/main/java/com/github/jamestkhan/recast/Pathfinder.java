package com.github.jamestkhan.recast;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.github.jamestkhan.recast.utils.NavMeshTool;
import org.recast4j.detour.FindNearestPolyResult;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.QueryFilter;
import org.recast4j.detour.Result;

/**
 * Entry point for gdx-recast.
 *
 * @author JamesTKhan
 * @version August 18, 2022
 */
public class Pathfinder {
    protected NavMeshData navMeshData;
    private final NavMeshTool tool;
    protected final float[] tmpPos = new float[3];
    protected final float[] tmpExt = new float[3];

    public Pathfinder(NavMeshData navMeshData) {
        this(new PathFinderSettings(), navMeshData);
    }

    public Pathfinder(PathFinderSettings settings, NavMeshData navMeshData) {
        this.navMeshData = navMeshData;
        tool = new NavMeshTool(settings, navMeshData);
    }

    /**
     * Get the current NavMesh
     *
     * @return the current navmesh
     */
    public NavMesh getNavMesh() {
        return navMeshData.getNavMesh();
    }

    /**
     * Set the current NavMesh
     *
     * @param navMesh the navmesh to set
     */
    public void setNavMesh(NavMesh navMesh) {
        navMeshData.update(navMesh);
    }


    /**
     * Finds the nearest polygon on the navigation mesh based on the provided center and half extents.
     * <p>
     * This convenience method takes in {@code Vector3} arguments for center and half extents, converts them
     * to float arrays, and then delegates the search to the underlying Recast's {@code findNearestPoly} method.
     * </p>
     *
     * @param center      The center point in the world space from where the nearest polygon should be searched.
     * @param halfExtents The half extents of the search box in each dimension (x, y, z).
     * @param filter      The query filter to apply during the search.
     *
     * @return A result object containing information about the nearest polygon. If no polygon is found nearby, the
     * result object's {@code getNearestRef} method will return 0. If an error occurs, the result object's succeeded
     * method will return false.
     */
    public Result<FindNearestPolyResult> findNearestPoly(Vector3 center, Vector3 halfExtents, QueryFilter filter) {
        vectorToFloatArray(center, tmpPos);
        vectorToFloatArray(halfExtents, tmpExt);
        return navMeshData.getNavMeshQuery().findNearestPoly(tmpPos, tmpExt, filter);
    }

    /**
     * Get a path from the start position to the end position
     *
     * @param start   World start point
     * @param end     World end point
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

    /**
     * Copy the values from the vector to the array
     */
    private void vectorToFloatArray(Vector3 vec, float[] arr) {
        arr[0] = vec.x;
        arr[1] = vec.y;
        arr[2] = vec.z;
    }
}
