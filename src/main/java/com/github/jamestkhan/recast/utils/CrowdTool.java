/*
Copyright (c) 2009-2010 Mikko Mononen memon@inside.org
recast4j copyright (c) 2021 Piotr Piastucki piotr@jtilia.org

This software is provided 'as-is', without any express or implied
warranty.  In no event will the authors be held liable for any damages
arising from the use of this software.
Permission is granted to anyone to use this software for any purpose,
including commercial applications, and to alter it and redistribute it
freely, subject to the following restrictions:
1. The origin of this software must not be misrepresented; you must not
 claim that you wrote the original software. If you use this software
 in a product, an acknowledgment in the product documentation would be
 appreciated but is not required.
2. Altered source versions must be plainly marked as such, and must not be
 misrepresented as being the original software.
3. This notice may not be removed or altered from any source distribution.
*/
package com.github.jamestkhan.recast.utils;

import com.github.jamestkhan.recast.NavMeshData;
import com.github.jamestkhan.recast.builders.SampleAreaModifications;
import org.recast4j.detour.DefaultQueryFilter;
import org.recast4j.detour.DetourCommon;
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


import java.util.HashMap;
import java.util.Map;

public class CrowdTool implements Tool {
    private final NavMeshData navMeshData;
    private NavMesh m_nav;
    private Crowd crowd;
    private static final int AGENT_MAX_TRAIL = 64;

    @Override
    public NavMesh getNavMesh() {
        return navMeshData.getNavMesh();
    }

    private class AgentTrail {
        float[] trail = new float[AGENT_MAX_TRAIL * 3];
        int htrail;
    }

    private final Map<Long, AgentTrail> m_trails = new HashMap<>();
    private float[] m_targetPos;
    private long m_targetRef;

    public CrowdTool(NavMeshData navMeshData) {
        this.navMeshData = navMeshData;

        NavMesh nav = navMeshData.getNavMesh();
        if (nav != null && m_nav != nav) {
            m_nav = nav;

            CrowdConfig config = new CrowdConfig(navMeshData.getSettings().agentRadius);

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
    }

    public void removeAgent(CrowdAgent agent) {
        crowd.removeAgent(agent);
    }

    public CrowdAgent addAgent(float[] p) {
        CrowdAgentParams ap = getAgentParams();
        CrowdAgent ag = crowd.addAgent(p, ap);
        if (ag != null) {
            if (m_targetRef != 0)
                crowd.requestMoveTarget(ag, m_targetRef, m_targetPos);

            // Init trail
            AgentTrail trail = m_trails.computeIfAbsent(ag.idx, __ -> new AgentTrail());
            for (int i = 0; i < AGENT_MAX_TRAIL; ++i) {
                trail.trail[i * 3] = p[0];
                trail.trail[i * 3 + 1] = p[1];
                trail.trail[i * 3 + 2] = p[2];
            }
            trail.htrail = 0;
        }

        return ag;
    }

    private CrowdAgentParams getAgentParams() {
        CrowdAgentParams ap = new CrowdAgentParams();
        ap.radius = navMeshData.getSettings().agentRadius;
        ap.height = navMeshData.getSettings().agentHeight;
        ap.maxAcceleration = 8.0f;
        ap.maxSpeed = 3.5f;
        ap.collisionQueryRange = ap.radius * 12.0f;
        ap.pathOptimizationRange = ap.radius * 30.0f;
        ap.updateFlags = getUpdateFlags();

        // TODO make this configurable?

        // The index of the avoidance configuration to use for the agent.
        // [Limits: 0 <= value <= #DT_CROWD_MAX_OBSTAVOIDANCE_PARAMS]
        ap.obstacleAvoidanceType = 3;

        // How aggresive the agent manager should be at avoiding collisions with this agent. [Limit: >= 0]
        ap.separationWeight = 2f;
        return ap;
    }

    public void setMoveTarget(CrowdAgent agent, float[] p) {
        if (agent == null) return;

        // Find nearest point on navmesh and set move request to that location.
        NavMeshQuery navquery = navMeshData.getNavMeshQuery();
        QueryFilter filter = crowd.getFilter(0);
        float[] halfExtents = crowd.getQueryExtents();

        Result<FindNearestPolyResult> result = navquery.findNearestPoly(p, halfExtents, filter);
        m_targetRef = result.result.getNearestRef();
        m_targetPos = result.result.getNearestPos();
        crowd.requestMoveTarget(agent, m_targetRef, m_targetPos);
    }

    private float[] calcVel(float[] pos, float[] tgt, float speed) {
        float[] vel = DetourCommon.vSub(tgt, pos);
        vel[1] = 0.0f;
        DetourCommon.vNormalize(vel);
        return DetourCommon.vScale(vel, speed);
    }

    public void update(float dt) {
        updateTick(dt);
    }

    private void updateTick(float dt) {
        if (crowd == null)
            return;
        NavMesh nav = navMeshData.getNavMesh();
        if (nav == null)
            return;

        crowd.update(dt, null);

        // Update agent trails
        for (CrowdAgent ag : crowd.getActiveAgents()) {
            AgentTrail trail = m_trails.get(ag.idx);
            // Update agent movement trail.
            trail.htrail = (trail.htrail + 1) % AGENT_MAX_TRAIL;
            trail.trail[trail.htrail * 3] = ag.npos[0];
            trail.trail[trail.htrail * 3 + 1] = ag.npos[1];
            trail.trail[trail.htrail * 3 + 2] = ag.npos[2];
        }

//        m_agentDebug.vod.normalizeSamples();

        // m_crowdSampleCount.addSample((float) crowd.getVelocitySampleCount());
    }

    private void updateAgentParams() {
        if (crowd == null) {
            return;
        }

        int updateFlags = getUpdateFlags();
       // profilingTool.updateAgentParams(updateFlags, toolParams.m_obstacleAvoidanceType.get(0), toolParams.m_separationWeight.get(0));
        for (CrowdAgent ag : crowd.getActiveAgents()) {
            CrowdAgentParams params = new CrowdAgentParams();
            params.radius = ag.params.radius;
            params.height = ag.params.height;
            params.maxAcceleration = ag.params.maxAcceleration;
            params.maxSpeed = ag.params.maxSpeed;
            params.collisionQueryRange = ag.params.collisionQueryRange;
            params.pathOptimizationRange = ag.params.pathOptimizationRange;
            params.obstacleAvoidanceType = ag.params.obstacleAvoidanceType;
            params.queryFilterType = ag.params.queryFilterType;
            params.userData = ag.params.userData;
            params.updateFlags = updateFlags;
            // TODO make this configurable?

            // The index of the avoidance configuration to use for the agent.
            // [Limits: 0 <= value <= #DT_CROWD_MAX_OBSTAVOIDANCE_PARAMS]
            params.obstacleAvoidanceType = 3;

            // How aggresive the agent manager should be at avoiding collisions with this agent. [Limit: >= 0]
            params.separationWeight = 2f;
            crowd.updateAgentParameters(ag, params);
        }
    }

    private int getUpdateFlags() {
        //TODO Make this configurable, for now it uses all flags
//        int updateFlags = 0;
//        if (toolParams.m_anticipateTurns) {
//            updateFlags |= CrowdAgentParams.DT_CROWD_ANTICIPATE_TURNS;
//        }
//        if (toolParams.m_optimizeVis) {
//            updateFlags |= CrowdAgentParams.DT_CROWD_OPTIMIZE_VIS;
//        }
//        if (toolParams.m_optimizeTopo) {
//            updateFlags |= CrowdAgentParams.DT_CROWD_OPTIMIZE_TOPO;
//        }
//        if (toolParams.m_obstacleAvoidance) {
//            updateFlags |= CrowdAgentParams.DT_CROWD_OBSTACLE_AVOIDANCE;
//        }
//        if (toolParams.m_separation) {
//            updateFlags |= CrowdAgentParams.DT_CROWD_SEPARATION;
//        }
//        return updateFlags;
        return CrowdAgentParams.DT_CROWD_OBSTACLE_AVOIDANCE | CrowdAgentParams.DT_CROWD_SEPARATION | CrowdAgentParams.DT_CROWD_ANTICIPATE_TURNS | CrowdAgentParams.DT_CROWD_OPTIMIZE_TOPO | CrowdAgentParams.DT_CROWD_ANTICIPATE_TURNS;
    }

}
