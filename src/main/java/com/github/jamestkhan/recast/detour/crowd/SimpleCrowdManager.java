package com.github.jamestkhan.recast.detour.crowd;

import com.badlogic.gdx.math.Vector3;
import com.github.jamestkhan.recast.NavMeshData;
import com.github.jamestkhan.recast.builders.SampleAreaModifications;
import org.recast4j.detour.DefaultQueryFilter;
import org.recast4j.detour.FindNearestPolyResult;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.NavMeshQuery;
import org.recast4j.detour.QueryFilter;
import org.recast4j.detour.Result;
import org.recast4j.detour.crowd.Crowd;
import org.recast4j.detour.crowd.CrowdAgent;
import org.recast4j.detour.crowd.CrowdAgentParams;
import org.recast4j.detour.crowd.CrowdConfig;
import org.recast4j.detour.crowd.ObstacleAvoidanceQuery;

import static com.github.jamestkhan.recast.utils.PathUtils.vectorToFloatArray;

/**
 * A simple implementation of a CrowdManager.
 * @author JamesTKhan
 * @version August 29, 2022
 */
public class SimpleCrowdManager implements CrowdManager {

    private Crowd crowd;
    private final CrowdConfig config;
    private final NavMeshData navMeshData;

    protected final float[] tmpPos = new float[3];

    public SimpleCrowdManager(NavMeshData navMeshData, CrowdConfig crowdConfig) {
        this.navMeshData = navMeshData;
        this.config = crowdConfig;

        initCrowd(crowdConfig);
    }

    protected void initCrowd(CrowdConfig config) {
        NavMesh nav = navMeshData.getNavMesh();

        crowd = new Crowd(config, nav, __ -> new DefaultQueryFilter(SampleAreaModifications.SAMPLE_POLYFLAGS_ALL,
                SampleAreaModifications.SAMPLE_POLYFLAGS_DISABLED, new float[] { 1f, 10f, 1f, 1f, 2f, 1.5f }));

        // Setup local avoidance params to different qualities.
        // Use mostly default settings, copy from dtCrowd.
        ObstacleAvoidanceQuery.ObstacleAvoidanceParams params = new ObstacleAvoidanceQuery.ObstacleAvoidanceParams(crowd.getObstacleAvoidanceParams(0));

        // Low (11)
        params.velBias = 0.5f;
        params.adaptiveDivs = 5;
        params.adaptiveRings = 2;
        params.adaptiveDepth = 1;
        crowd.setObstacleAvoidanceParams(0, params);

        // Medium (22)
        params.velBias = 0.5f;
        params.adaptiveDivs = 5;
        params.adaptiveRings = 2;
        params.adaptiveDepth = 2;
        crowd.setObstacleAvoidanceParams(1, params);

        // Good (45)
        params.velBias = 0.5f;
        params.adaptiveDivs = 7;
        params.adaptiveRings = 2;
        params.adaptiveDepth = 3;
        crowd.setObstacleAvoidanceParams(2, params);

        // High (66)
        params.velBias = 0.5f;
        params.adaptiveDivs = 7;
        params.adaptiveRings = 3;
        params.adaptiveDepth = 3;

        crowd.setObstacleAvoidanceParams(3, params);
    }

    public void update(float deltaTime) {
        NavMesh nav = navMeshData.getNavMesh();
        if (nav == null) return;

        crowd.update(deltaTime, null);
    }

    public CrowdAgent addAgent(Vector3 position, CrowdAgentParams params) {
        vectorToFloatArray(position, tmpPos);
        return crowd.addAgent(tmpPos, params);
    }

    public CrowdAgent addAgent(Vector3 position, Vector3 target, CrowdAgentParams params) {
        CrowdAgent ag = addAgent(position, params);
        if (ag == null) return null;
        setAgentMoveTarget(ag, target);
        return ag;
    }

    public void setAgentMoveTarget(CrowdAgent agent, Vector3 moveTarget) {
        if (agent == null) return;

        vectorToFloatArray(moveTarget, tmpPos);

        // Find nearest point on navmesh and set move request to that location.
        NavMeshQuery navquery = navMeshData.getNavMeshQuery();
        QueryFilter filter = crowd.getFilter(0);
        float[] halfExtents = crowd.getQueryExtents();

        Result<FindNearestPolyResult> result = navquery.findNearestPoly(tmpPos, halfExtents, filter);
        long targetRef = result.result.getNearestRef();
        float[] targetPos = result.result.getNearestPos();
        crowd.requestMoveTarget(agent, targetRef, targetPos);
    }

    public void removeAgent(CrowdAgent agent) {
        crowd.removeAgent(agent);
    }

    @Override
    public Crowd getCrowd() {
        return crowd;
    }

    public CrowdConfig getConfig() {
        return config;
    }

    /**
     * A convenience method for getting some default params for a CrowdAgent.
     * Modify the returned CrowdAgentParams to suit your needs.
     *
     * @return a CrowdAgentParams with some default values
     */
    public static CrowdAgentParams getDefaultAgentParams() {
        //TODO Builder pattern?
        CrowdAgentParams ap = new CrowdAgentParams();

        ap.radius = .6f;
        ap.height = .6f;
        ap.maxAcceleration = 8.0f;
        ap.maxSpeed = 3.5f;
        ap.collisionQueryRange = ap.radius * 12.0f;
        ap.pathOptimizationRange = ap.radius * 30.0f;
        ap.updateFlags = CrowdAgentParams.DT_CROWD_OBSTACLE_AVOIDANCE
                | CrowdAgentParams.DT_CROWD_SEPARATION
                | CrowdAgentParams.DT_CROWD_ANTICIPATE_TURNS
                | CrowdAgentParams.DT_CROWD_OPTIMIZE_TOPO;

        // The index of the avoidance configuration to use for the agent.
        // [Limits: 0 <= value <= #DT_CROWD_MAX_OBSTAVOIDANCE_PARAMS]
        ap.obstacleAvoidanceType = 3;

        // How aggressive the agent manager should be at avoiding collisions with this agent. [Limit: >= 0]
        ap.separationWeight = 2f;
        return ap;
    }

}
