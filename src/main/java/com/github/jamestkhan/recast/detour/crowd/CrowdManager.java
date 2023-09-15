package com.github.jamestkhan.recast.detour.crowd;

import com.badlogic.gdx.math.Vector3;
import org.recast4j.detour.crowd.CrowdAgent;
import org.recast4j.detour.crowd.CrowdAgentParams;

/**
 * A crowd manager is responsible for managing a group of agents and a Detour Crowd instance.
 * @author JamesTKhan
 * @version September 15, 2023
 */
public interface CrowdManager {

    /**
     * Update the crowd.
     * @param deltaTime the time since the last update
     */
    void update(float deltaTime);

    /**
     * Add a new agent to the crowd.
     * @param position the position of the age
     * @param params the params to use for the agent
     * @return the agent that was added
     */
    CrowdAgent addAgent(Vector3 position, CrowdAgentParams params);

    /**
     * Add a new agent to the crowd and set its move target.
     * @param position the position of the agent
     * @param target the target of the agent
     * @param params the params to use for the agent
     * @return the agent that was added
     */
    CrowdAgent addAgent(Vector3 position, Vector3 target, CrowdAgentParams params);

    /**
     * Set the move target of an agent.
     * @param agent the agent to set the move target for
     * @param moveTarget the move target to set
     */
    void setAgentMoveTarget(CrowdAgent agent, Vector3 moveTarget);

    /**
     * Remove an agent from the crowd.
     * @param agent the agent to remove
     */
    void removeAgent(CrowdAgent agent);
}
