package com.github.jamestkhan.recast;

import com.badlogic.gdx.math.Vector3;
import com.github.jamestkhan.recast.utils.CrowdTool;
import org.recast4j.detour.crowd.CrowdAgent;
import org.recast4j.detour.crowd.CrowdAgentParams;
import org.recast4j.detour.crowd.CrowdConfig;

/**
 * @author JamesTKhan
 * @version August 29, 2022
 */
public class CrowdPathfinder extends Pathfinder {
    private final float[] tmpPos = new float[3];
    private CrowdTool tool;

    public CrowdPathfinder(NavMeshData navMeshData, CrowdConfig crowdConfig) {
        super(navMeshData);
        tool = new CrowdTool(navMeshData, crowdConfig);
    }

    public void update(float deltaTime) {
        tool.update(deltaTime);
    }

    public CrowdAgent addAgent(Vector3 currentPosition, CrowdAgentParams params) {
        tmpPos[0] = currentPosition.x;
        tmpPos[1] = currentPosition.y;
        tmpPos[2] = currentPosition.z;
        return tool.addAgent(tmpPos, params);
    }

    public void setAgentMoveTarget(CrowdAgent crowdAgent, Vector3 moveTarget) {
        tmpPos[0] = moveTarget.x;
        tmpPos[1] = moveTarget.y;
        tmpPos[2] = moveTarget.z;
        tool.setMoveTarget(crowdAgent, tmpPos);
    }

    public void removeAgent(CrowdAgent agent) {
        tool.removeAgent(agent);
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
        ap.updateFlags = CrowdAgentParams.DT_CROWD_OBSTACLE_AVOIDANCE | CrowdAgentParams.DT_CROWD_SEPARATION | CrowdAgentParams.DT_CROWD_ANTICIPATE_TURNS | CrowdAgentParams.DT_CROWD_OPTIMIZE_TOPO | CrowdAgentParams.DT_CROWD_ANTICIPATE_TURNS;

        // The index of the avoidance configuration to use for the agent.
        // [Limits: 0 <= value <= #DT_CROWD_MAX_OBSTAVOIDANCE_PARAMS]
        ap.obstacleAvoidanceType = 3;

        // How aggressive the agent manager should be at avoiding collisions with this agent. [Limit: >= 0]
        ap.separationWeight = 2f;
        return ap;
    }

}
