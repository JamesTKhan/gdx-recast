package com.github.jamestkhan.recast;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.github.jamestkhan.recast.utils.CrowdTool;
import org.recast4j.detour.crowd.CrowdAgent;

import java.io.InputStream;

/**
 * @author JamesTKhan
 * @version August 29, 2022
 */
public class CrowdPathfinder extends Pathfinder {
    private final float[] tmpPos = new float[3];
    private CrowdTool tool;

    public CrowdPathfinder(FileHandle objModel, NavigationSettings navigationSettings) {
        super(objModel, navigationSettings);
    }

    public CrowdPathfinder(InputStream inputStream, NavigationSettings navigationSettings) {
        super(inputStream, navigationSettings);
    }

    public CrowdPathfinder(Array<ModelInstance> staticInstances, NavigationSettings navigationSettings) {
        super(staticInstances, navigationSettings);
    }

    public CrowdPathfinder(ModelInstance model, NavigationSettings navigationSettings) {
        super(model, navigationSettings);
    }

    public void update(float deltaTime) {
        tool.update(deltaTime);
    }

    public CrowdAgent addAgent(Vector3 currentPosition) {
        tmpPos[0] = currentPosition.x;
        tmpPos[1] = currentPosition.y;
        tmpPos[2] = currentPosition.z;
        return tool.addAgent(tmpPos);
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
     * Build or rebuild the current NavMesh
     * @param settings the settings to use for building the navmesh
     */
    @Override
    public void buildNavMesh(NavigationSettings settings) {
        super.buildNavMesh(settings);
        tool = new CrowdTool(navMeshData);
    }
}
