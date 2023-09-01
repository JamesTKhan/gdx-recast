package com.github.jamestkhan.recast;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.github.jamestkhan.recast.builders.SoloNavMeshBuilder;
import com.github.jamestkhan.recast.builders.TileNavMeshBuilder;
import com.github.jamestkhan.recast.geom.GdxInputGeomProvider;
import com.github.jamestkhan.recast.utils.NavMeshTool;
import com.github.jamestkhan.recast.utils.ObjImporter;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.Tupple2;
import org.recast4j.recast.RecastBuilder;

import java.io.InputStream;
import java.util.List;

/**
 * Entry point for gdx-recast.
 *
 * @author JamesTKhan
 * @version August 18, 2022
 */
public class Pathfinder {
    protected static Vector3 tmpVec = new Vector3();
    protected final SoloNavMeshBuilder soloNavMeshBuilder = new SoloNavMeshBuilder();
    protected final TileNavMeshBuilder tileNavMeshBuilder = new TileNavMeshBuilder();
    protected NavMeshData navMeshData;
    private NavMeshTool tool;

    public Pathfinder(FileHandle objModel, NavigationSettings navigationSettings) {
        this(objModel.read(), navigationSettings);
    }

    public Pathfinder(InputStream inputStream, NavigationSettings navigationSettings) {
        GdxInputGeomProvider geom = new ObjImporter().load(inputStream);
        init(geom, navigationSettings);
    }

    public Pathfinder(Array<ModelInstance> staticInstances, NavigationSettings navigationSettings) {
        // Build lists for Recast
        Array<Float> vertexList = new Array<>();
        Array<Integer> indexList = new Array<>();

        // for merging models/meshes, we must track the offset of indices per each model parsed
        int indicesOffset = 0;

        for (ModelInstance modelInstance : staticInstances) {
            indicesOffset = getVerticesIndicesFromModel(modelInstance, vertexList, indexList, indicesOffset);
        }

        GdxInputGeomProvider geom = new GdxInputGeomProvider(vertexList, indexList);
        init(geom, navigationSettings);
    }

    public Pathfinder(ModelInstance model, NavigationSettings navigationSettings) {
        // Build lists for Recast
        Array<Float> vertexList = new Array<>();
        Array<Integer> indexList = new Array<>();

        getVerticesIndicesFromModel(model, vertexList, indexList, 0);

        GdxInputGeomProvider geom = new GdxInputGeomProvider(vertexList, indexList);
        init(geom, navigationSettings);
    }

    private static int getVerticesIndicesFromModel(ModelInstance modelInstance, Array<Float> vertOut, Array<Integer> indexOut, int indicesOffset) {
        for (Mesh mesh : modelInstance.model.meshes) {
            VertexAttributes vertexAttributes = mesh.getVertexAttributes();
            int offset = vertexAttributes.getOffset(VertexAttributes.Usage.Position);

            int vertexSize = mesh.getVertexSize() / 4;
            int vertCount = mesh.getNumVertices() * mesh.getVertexSize() / 4;

            float[] vertices = new float[vertCount];
            short[] indices = new short[mesh.getNumIndices()];

            mesh.getVertices(vertices);
            mesh.getIndices(indices);

            // Get XYZ vertices position data
            for (int i = 0; i < vertices.length; i+=vertexSize) {
                float x = vertices[i + offset];
                float y = vertices[i+1 + offset];
                float z = vertices[i+2 + offset];

                // Apply the world transform to the vertices
                tmpVec.set(x,y,z);
                tmpVec.mul(modelInstance.transform);

                vertOut.add(tmpVec.x);
                vertOut.add(tmpVec.y);
                vertOut.add(tmpVec.z);
            }

            for (short index : indices) {
                indexOut.add((int) index + indicesOffset);
            }

            indicesOffset += vertices.length / vertexSize;
        }

        return indicesOffset;
    }

    protected void init(GdxInputGeomProvider geomProvider, NavigationSettings settings) {
        navMeshData = new NavMeshData(geomProvider, null, settings);
        buildNavMesh(settings);
    }

    /**
     * Build or rebuild the current NavMesh
     * @param settings the settings to use for building the navmesh
     */
    public void buildNavMesh(NavigationSettings settings) {
        // build nav mesh
        Tupple2<List<RecastBuilder.RecastBuilderResult>, NavMesh> buildResult;

        if (settings.useTiles) {
            buildResult = tileNavMeshBuilder.build(navMeshData.getInputGeom(), settings);
        } else {
            buildResult = soloNavMeshBuilder.build(navMeshData.getInputGeom(), settings);
        }

        navMeshData.update(navMeshData.getInputGeom(), buildResult.first, buildResult.second);
        tool = new NavMeshTool(navMeshData);
    }

    public NavMesh getNavMesh() {
        return navMeshData.getNavMesh();
    }

    public void setNavMesh(NavMesh navMesh) {
        navMeshData.update(navMeshData.getInputGeom(), null, navMesh);
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
}
