package com.github.jamestkhan.recast.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.github.jamestkhan.recast.NavMeshData;
import com.github.jamestkhan.recast.NavMeshGenSettings;
import com.github.jamestkhan.recast.builders.SoloNavMeshBuilder;
import com.github.jamestkhan.recast.builders.TileNavMeshBuilder;
import com.github.jamestkhan.recast.geom.GdxInputGeomProvider;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.Tupple2;
import org.recast4j.recast.RecastBuilder;

import java.io.InputStream;
import java.util.List;

/**
 * Responsible for building the navigation mesh.
 *
 * @author JamesTKhan
 * @version September 01, 2023
 */
public class NavMeshGenerator {

    protected static Vector3 tmpVec = new Vector3();
    private final GdxInputGeomProvider geom;

    /**
     * Build a navigation mesh from the FileHandle of an .obj model.
     * @param objModel the FileHandle of the .obj model
     */
    public NavMeshGenerator(FileHandle objModel) {
        this(objModel.read());
    }

    /**
     * Build a navigation mesh from an input stream of an .obj model.
     * @param inputStream the input stream of the .obj model
     */
    public NavMeshGenerator(InputStream inputStream) {
        geom = new ObjImporter().load(inputStream);
    }

    /**
     * Build a navigation mesh from a list of ModelInstances.
     * @param staticInstances the list of models to build the navmesh from
     */
    public NavMeshGenerator(Array<ModelInstance> staticInstances) {
        // Build lists for Recast
        Array<Float> vertexList = new Array<>();
        Array<Integer> indexList = new Array<>();

        // for merging models/meshes, we must track the offset of indices per each model parsed
        int indicesOffset = 0;

        for (ModelInstance modelInstance : staticInstances) {
            indicesOffset = getVerticesIndicesFromModel(modelInstance, vertexList, indexList, indicesOffset);
        }

        geom = new GdxInputGeomProvider(vertexList, indexList);
    }

    /**
     * Build a navigation mesh from a ModelInstance.
     * @param modelInstance the model to build the navmesh from
     */
    public NavMeshGenerator(ModelInstance modelInstance) {
        // Build lists for Recast
        Array<Float> vertexList = new Array<>();
        Array<Integer> indexList = new Array<>();

        getVerticesIndicesFromModel(modelInstance, vertexList, indexList, 0);

        geom = new GdxInputGeomProvider(vertexList, indexList);
    }

    /**
     * Provide your own GdxInputGeomProvider.
     * Useful if you want to provide vertices and indices yourself.
     * @param provider the provider to use
     */
    public NavMeshGenerator(GdxInputGeomProvider provider)  {
        geom = provider;
    }

    /**
     * Build the NavMesh
     * @param settings the settings to use for building the navmesh
     * @return the navmesh data, contains the input geometry, build results, and navmesh
     */
    public NavMeshData build(NavMeshGenSettings settings) {
        return buildNavMesh(settings, geom);
    }

    /**
     * Build or rebuild the current NavMesh
     *
     * @param settings the settings to use for building the navmesh
     * @return the navmesh data, contains the input geometry, build results, and navmesh
     */
    public static NavMeshData buildNavMesh(NavMeshGenSettings settings, GdxInputGeomProvider geom) {
        // build nav mesh
        Tupple2<List<RecastBuilder.RecastBuilderResult>, NavMesh> buildResult;

        if (settings.useTiles) {
            TileNavMeshBuilder tileNavMeshBuilder = new TileNavMeshBuilder();
            buildResult = tileNavMeshBuilder.build(geom, settings);
        } else {
            SoloNavMeshBuilder soloNavMeshBuilder = new SoloNavMeshBuilder();
            buildResult = soloNavMeshBuilder.build(geom, settings);
        }

        return new NavMeshData(buildResult.second);
    }

    private static int getVerticesIndicesFromModel(ModelInstance modelInstance, Array<Float> vertOut, Array<Integer> indexOut, int indicesOffset) {
        for (Mesh mesh : modelInstance.model.meshes) {
            VertexAttributes vertexAttributes = mesh.getVertexAttributes();
            int offset = vertexAttributes.getOffset(VertexAttributes.Usage.Position);

            int vertexSize = mesh.getVertexSize() / 4;
            int vertCount = mesh.getNumVertices() * vertexSize;

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

}
