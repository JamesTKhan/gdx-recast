package com.github.jamestkhan.recast;

import com.badlogic.gdx.math.Vector3;
import com.github.jamestkhan.recast.utils.CrowdTool;
import org.recast4j.detour.crowd.CrowdAgent;
import org.recast4j.detour.crowd.CrowdAgentParams;
import org.recast4j.detour.crowd.CrowdConfig;

import static com.github.jamestkhan.recast.utils.PathUtils.vectorToFloatArray;

/**
 * @author JamesTKhan
 * @version August 29, 2022
 */
public class CrowdPathfinder extends Pathfinder {
    private CrowdTool tool;

    public CrowdPathfinder(NavMeshData navMeshData, CrowdConfig crowdConfig) {
        super(navMeshData);
        tool = new CrowdTool(navMeshData, crowdConfig);
    }

    public void update(float deltaTime) {
        tool.update(deltaTime);
    }

    /**
     * Add a new agent to the crowd.
     * @param position the position of the age
     * @param params the params to use for the agent
     * @return the agent that was added
     */
    public CrowdAgent addAgent(Vector3 position, CrowdAgentParams params) {
        vectorToFloatArray(position, tmpPos);
        return tool.addAgent(tmpPos, params);
    }

    /**
     * Add a new agent to the crowd and set its move target.
     * @param position the position of the agent
     * @param target the target of the agent
     * @param params the params to use for the agent
     * @return the agent that was added
     */
    public CrowdAgent addAgent(Vector3 position, Vector3 target, CrowdAgentParams params) {
        CrowdAgent ag = addAgent(position, params);
        if (ag == null) return null;
        setAgentMoveTarget(ag, target);
        return ag;
    }

    public void setAgentMoveTarget(CrowdAgent crowdAgent, Vector3 moveTarget) {
        vectorToFloatArray(moveTarget, tmpPos);
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
