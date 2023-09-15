package com.github.jamestkhan.recast.detour.crowd;

import com.badlogic.gdx.math.Vector3;
import com.github.jamestkhan.recast.NavMeshData;
import org.recast4j.detour.crowd.CrowdAgent;
import org.recast4j.detour.crowd.CrowdAgentParams;
import org.recast4j.detour.crowd.CrowdConfig;

import static com.github.jamestkhan.recast.utils.PathUtils.vectorToFloatArray;

/**
 * A simple implementation of a CrowdManager.
 * @author JamesTKhan
 * @version August 29, 2022
 */
public class SimpleCrowdManager implements CrowdManager {
    protected CrowdTool tool;
    protected final float[] tmpPos = new float[3];

    public SimpleCrowdManager(NavMeshData navMeshData, CrowdConfig crowdConfig) {
        tool = new CrowdTool(navMeshData, crowdConfig);
    }

    public void update(float deltaTime) {
        tool.update(deltaTime);
    }

    public CrowdAgent addAgent(Vector3 position, CrowdAgentParams params) {
        vectorToFloatArray(position, tmpPos);
        return tool.addAgent(tmpPos, params);
    }

    public CrowdAgent addAgent(Vector3 position, Vector3 target, CrowdAgentParams params) {
        CrowdAgent ag = addAgent(position, params);
        if (ag == null) return null;
        setAgentMoveTarget(ag, target);
        return ag;
    }

    public void setAgentMoveTarget(CrowdAgent agent, Vector3 moveTarget) {
        vectorToFloatArray(moveTarget, tmpPos);
        tool.setMoveTarget(agent, tmpPos);
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
