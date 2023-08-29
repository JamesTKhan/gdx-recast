package com.github.jamestkhan.recast.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.jamestkhan.recast.NavMeshData;
import com.github.jamestkhan.recast.builders.SampleAreaModifications;
import org.recast4j.detour.DefaultQueryFilter;
import org.recast4j.detour.DetourCommon;
import org.recast4j.detour.MoveAlongSurfaceResult;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.NavMeshQuery;
import org.recast4j.detour.Result;
import org.recast4j.detour.Tupple2;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.recast4j.detour.DetourCommon.vCopy;
import static org.recast4j.detour.DetourCommon.vMad;

/**
 * @author James Pooley
 * @version June 20, 2022
 */
public class NavMeshTool implements Tool {
    private final DefaultQueryFilter queryFilter;
    private final NavMeshData navMeshData;

    private boolean startPosSet;
    private boolean endPosSet;
    private final float[] startPos = new float[3];
    private final float[] endPos = new float[3];

    private final float[] m_polyPickExt = new float[] { 2, 4, 2 };
    private boolean enableRaycast = true;

    public NavMeshTool(NavMeshData sample) {
        this.navMeshData = sample;
        queryFilter = new DefaultQueryFilter(SampleAreaModifications.SAMPLE_POLYFLAGS_ALL,
                SampleAreaModifications.SAMPLE_POLYFLAGS_DISABLED, new float[] { 1f, 1f, 1f, 1f, 2f, 1.5f });
    }

    public void setPositions(Vector3 start, Vector3 end) {
            startPosSet = true;
            startPos[0] = start.x;
            startPos[1] = start.y;
            startPos[2] = start.z;

            endPosSet = true;
            endPos[0] = end.x;
            endPos[1] = end.y;
            endPos[2] = end.z;
    }

    public void recalculate(Array<float[]> pathOut) {
        if (!startPosSet || !endPosSet) throw new GdxRuntimeException("Start and end positions must be set.");

        NavMesh m_navMesh = navMeshData.getNavMesh();
        NavMeshQuery m_navQuery = navMeshData.getNavMeshQuery();

        long startPolygonRef = m_navQuery.findNearestPoly(startPos, m_polyPickExt, queryFilter).result.getNearestRef();
        long endPolygonRef = m_navQuery.findNearestPoly(endPos, m_polyPickExt, queryFilter).result.getNearestRef();

        List<Long> polys1 = m_navQuery.findPath(startPolygonRef, endPolygonRef, startPos, endPos, queryFilter,
                enableRaycast ? NavMeshQuery.DT_FINDPATH_ANY_ANGLE : 0, Float.MAX_VALUE).result;

        if (polys1 == null)
            return;

        if (!polys1.isEmpty()) {
            List<Long> polys = new ArrayList<>(polys1);
            // Iterate over the path to find smooth path on the detail mesh surface.
            float[] iterPos = m_navQuery.closestPointOnPoly(startPolygonRef, startPos).result.getClosest();
            float[] targetPos = m_navQuery.closestPointOnPoly(polys.get(polys.size() - 1), endPos).result
                    .getClosest();

            int maxIterations = navMeshData.getSettings().maxIterations;
            float stepSize = navMeshData.getSettings().stepSize;
            float SLOP = 0.1f;

            pathOut.add(iterPos);

            // Move towards target a small advancement at a time until target reached or
            // when ran out of memory to store the path.
            while (!polys.isEmpty() && pathOut.size < maxIterations) {
                // Find location to steer towards.
                Optional<PathUtils.SteerTarget> steerTarget = PathUtils.getSteerTarget(m_navQuery, iterPos, targetPos,
                        SLOP, polys);
                if (!steerTarget.isPresent()) {
                    break;
                }
                boolean endOfPath = (steerTarget.get().steerPosFlag & NavMeshQuery.DT_STRAIGHTPATH_END) != 0;
                boolean offMeshConnection = (steerTarget.get().steerPosFlag
                        & NavMeshQuery.DT_STRAIGHTPATH_OFFMESH_CONNECTION) != 0;

                // Find movement delta.
                float[] delta = DetourCommon.vSub(steerTarget.get().steerPos, iterPos);
                float len = (float) Math.sqrt(RecastMath.vDot(delta, delta));
                // If the steer target is end of path or off-mesh link, do not move past the location.
                if ((endOfPath || offMeshConnection) && len < stepSize) {
                    len = 1;
                } else {
                    len = stepSize / len;
                }
                float[] moveTgt = vMad(iterPos, delta, len);
                // Move
                Result<MoveAlongSurfaceResult> result = m_navQuery.moveAlongSurface(polys.get(0), iterPos,
                        moveTgt, queryFilter);
                MoveAlongSurfaceResult moveAlongSurface = result.result;

                iterPos = new float[3];
                iterPos[0] = moveAlongSurface.getResultPos()[0];
                iterPos[1] = moveAlongSurface.getResultPos()[1];
                iterPos[2] = moveAlongSurface.getResultPos()[2];

                List<Long> visited = result.result.getVisited();
                polys = PathUtils.fixupCorridor(polys, visited);
                polys = PathUtils.fixupShortcuts(polys, m_navQuery);

                Result<Float> polyHeight = m_navQuery.getPolyHeight(polys.get(0), moveAlongSurface.getResultPos());
                if (polyHeight.succeeded()) {
                    iterPos[1] = polyHeight.result;
                }

                // Handle end of path and off-mesh links when close enough.
                if (endOfPath && PathUtils.inRange(iterPos, steerTarget.get().steerPos, SLOP, 1.0f)) {
                    // Reached end of path.
                    vCopy(iterPos, targetPos);
                    if (pathOut.size < maxIterations) {
                        pathOut.add(iterPos);
                    }
                    break;
                } else if (offMeshConnection
                        && PathUtils.inRange(iterPos, steerTarget.get().steerPos, SLOP, 1.0f)) {
                    // Reached off-mesh connection.
                    // Advance the path up to and over the off-mesh connection.
                    long prevRef = 0;
                    long polyRef = polys.get(0);
                    int npos = 0;
                    while (npos < polys.size() && polyRef != steerTarget.get().steerPosRef) {
                        prevRef = polyRef;
                        polyRef = polys.get(npos);
                        npos++;
                    }
                    polys = polys.subList(npos, polys.size());

                    // Handle the connection.
                    Result<Tupple2<float[], float[]>> offMeshCon = m_navMesh
                            .getOffMeshConnectionPolyEndPoints(prevRef, polyRef);
                    if (offMeshCon.succeeded()) {
                        float[] startPos = offMeshCon.result.first;
                        float[] endPos = offMeshCon.result.second;
                        if (pathOut.size < maxIterations) {
                            pathOut.add(startPos);
                            // Hack to make the dotted path not visible during off-mesh connection.
                            if ((pathOut.size & 1) != 0) {
                                pathOut.add(startPos);
                            }
                        }
                        // Move position at the other side of the off-mesh link.
                        vCopy(iterPos, endPos);
                        iterPos[1] = m_navQuery.getPolyHeight(polys.get(0), iterPos).result;
                    }
                }

                // Store results.
                if (pathOut.size < maxIterations) {
                    pathOut.add(iterPos);
                }
            }

            if (pathOut.size >= maxIterations) {
                Gdx.app.debug(this.getClass().getSimpleName(), "Max path iterations reached.");
            }
        }
    }

    @Override
    public NavMesh getNavMesh() {
        return navMeshData.getNavMesh();
    }
}
