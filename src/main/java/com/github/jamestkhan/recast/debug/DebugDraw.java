package com.github.jamestkhan.recast.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import org.recast4j.detour.MeshTile;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.Poly;
import org.recast4j.detour.PolyDetail;
import org.recast4j.detour.Result;
import org.recast4j.detour.Tupple2;
import org.recast4j.detour.crowd.CrowdAgent;

/**
 * @author JamesTKhan
 * @version August 21, 2022
 */
public class DebugDraw {

    public final CustomShapeRenderer shapeRenderer;
    private final Array<Float> vertices = new Array<>();
    private ModelBatch modelBatch = new ModelBatch();
    private Model navMeshModel;
    private ModelInstance navMeshInstance;
    private int polyFlags;
    private NavMesh currentNavMesh;
    private Camera cam;

    private final Color baseTileColor = Color.TEAL;
    private final Color[] tileColors = new Color[]{
            baseTileColor,
            baseTileColor.cpy().lerp(Color.BLUE, 0.2f),
            baseTileColor.cpy().lerp(Color.BLACK, 0.2f),
            baseTileColor.cpy().lerp(Color.BLUE, 0.4f),
            baseTileColor.cpy().lerp(Color.BLACK, 0.4f),
            baseTileColor.cpy().lerp(Color.BLUE, 0.6f),
            baseTileColor.cpy().lerp(Color.BLACK, 0.6f),
    };

    private Material lineMaterial;
    private Material meshMaterial;

    public DebugDraw(Camera camera) {
        cam = camera;
        shapeRenderer = new CustomShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
    }

    public void debugDrawNavMeshPolysWithFlags(NavMesh mesh, int polyFlags) {
        // Do we need to rebuild the navmesh model?
        if (currentNavMesh != mesh || this.polyFlags != polyFlags) {
            currentNavMesh = mesh;
            this.polyFlags = polyFlags;

            if (navMeshModel != null) navMeshModel.dispose();

            navMeshModel = buildNavMeshModel(mesh, polyFlags);
            navMeshInstance = new ModelInstance(navMeshModel);
        }

        modelBatch.begin(cam);
        modelBatch.render(navMeshInstance);
        modelBatch.end();
    }

    public void drawCrowdAgents(Array<CrowdAgent> crowdAgents) {
        setOpenGLState();
        shapeRenderer.begin();
        shapeRenderer.setProjectionMatrix(cam.combined);
        shapeRenderer.setColor(Color.FOREST);
        shapeRenderer.set(ShapeRenderer.ShapeType.Filled);

        for (CrowdAgent agent : crowdAgents) {
            float radius = agent.params.radius;
            float height = agent.params.height;
            shapeRenderer.box(agent.npos[0], agent.npos[1], agent.npos[2], radius, height, radius);
        }

        shapeRenderer.end();
    }

    private void setOpenGLState() {
        Gdx.gl.glDepthFunc(GL20.GL_LESS);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void renderStartPos(Vector3 start) {
        renderStartPos(start.x, start.y, start.z);
    }

    public void renderStartPos(float x, float y, float z) {
        setOpenGLState();
        shapeRenderer.begin();
        shapeRenderer.setProjectionMatrix(cam.combined);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
        renderBoxCentered(x, y, z, 2f, 2f, 2f);
        shapeRenderer.end();
    }

    public void renderEndPos(Vector3 start) {
        renderEndPos(start.x, start.y, start.z);
    }

    public void renderEndPos(float x, float y, float z) {
        setOpenGLState();
        shapeRenderer.begin();
        shapeRenderer.setProjectionMatrix(cam.combined);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
        renderBoxCentered(x, y, z, 2f, 2f, 2f);
        shapeRenderer.end();
    }

    public void renderPath(Array<float[]> paths) {
        renderPath(paths, true);
    }

    public void renderPath(Array<float[]> paths, boolean renderStartAndEnd) {
        setOpenGLState();
        shapeRenderer.begin();
        shapeRenderer.setProjectionMatrix(cam.combined);
        for (int i = 1; i < paths.size; i += 2) {

            if (!renderStartAndEnd) {
                if (i == 1) {
                    continue;
                } else if (i >= paths.size - 2) {
                    continue;
                }
            }
            shapeRenderer.setColor(Color.BLUE);

            float[] pos = paths.get(i - 1);
            float[] nextPos = paths.get(i);

            renderLine(pos[0], pos[1], pos[2], nextPos[0], nextPos[1], nextPos[2], 0f);
        }
        shapeRenderer.end();
    }

    private void renderLine(float x1, float y1, float z1, float x2, float y2, float z2, float offset) {
        shapeRenderer.line(x1 + offset, y1 + 0.5f, z1 + offset, x2 + offset, y2 + 0.5f, z2 + offset);
    }

    private void renderBoxCentered(float x, float y, float z, float width, float height, float depth) {
        float halfWidth = width / 2.0f;
        float halfDepth = depth / 2.0f;
        shapeRenderer.box(x - halfWidth, y, z + halfDepth, width, height, depth);
    }

    private Model buildNavMeshModel(NavMesh mesh, int polyFlags) {
        if (lineMaterial == null) {
            lineMaterial = new Material();
            lineMaterial.set(ColorAttribute.createDiffuse(Color.BLACK));
            lineMaterial.set(new BlendingAttribute(true, .1f));
        }

        if (meshMaterial == null) {
            meshMaterial = new Material();
            meshMaterial.set(new BlendingAttribute(true, .3f));
        }

        // The height offset is used to raise the navmesh off the surface so it's not clipping into the surface
        float heightOffset = 0.1f;

        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();

        Array<Integer> tileIndexes = new Array<>();

        // accumulate vertices
        for (int i = 0; i < mesh.getMaxTiles(); ++i) {
            MeshTile tile = mesh.getTile(i);
            if (tile == null || tile.data == null || tile.data.header == null) {
                continue;
            }
            long base = mesh.getPolyRefBase(tile);

            // Filter by flags
            for (int j = 0; j < tile.data.header.polyCount; ++j) {
                Poly p = tile.data.polys[j];
                if ((p.flags & polyFlags) == 0) {
                    continue;
                }
                addVertices(mesh, base | j);
            }

            tileIndexes.add(vertices.size);
        }

        Array<Vector3> tris = new Array<>();
        MeshPartBuilder mpb = modelBuilder.part("navMesh", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorUnpacked, meshMaterial);

        // Set first tile color
        mpb.setColor(tileColors[0]);

        int j = 0;
        while (j < vertices.size) {

            if (j > 0 && j % 32400 == 0) {
                // Every 32,400 vertices, create line mesh and make a new navmesh part
                createLineMesh(modelBuilder, tris);
                tris.clear();

                mpb = modelBuilder.part("navMesh", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorUnpacked, meshMaterial);
            }

            float x1 = vertices.get(j);
            float y1 = vertices.get(j + 1) + heightOffset;
            float z1 = vertices.get(j + 2);

            float x2 = vertices.get(j + 3);
            float y2 = vertices.get(j + 4) + heightOffset;
            float z2 = vertices.get(j + 5);

            float x3 = vertices.get(j + 6);
            float y3 = vertices.get(j + 7) + heightOffset;
            float z3 = vertices.get(j + 8);

            Vector3 v1 = new Vector3(x1, y1, z1);
            Vector3 v2 = new Vector3(x2, y2, z2);
            Vector3 v3 = new Vector3(x3, y3, z3);

            int tileIdx = tileIndexes.get(0);
            if (j >= tileIdx) {
                mpb.setColor(tileColors[tileIdx % tileColors.length]);
                tileIndexes.removeIndex(0);
            }

            mpb.triangle(v1, v2, v3);

            // Track tris for creating line mesh
            tris.add(v1);
            tris.add(v2);
            tris.add(v3);
            j += 9;
        }

        createLineMesh(modelBuilder, tris);

        vertices.clear();
        return modelBuilder.end();
    }

    private void createLineMesh(ModelBuilder modelBuilder, Array<Vector3> tris) {
        MeshPartBuilder lineBuilder = modelBuilder.part("navMeshLines", GL20.GL_LINES, VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorUnpacked, lineMaterial);
        for (int k = 0; k < tris.size; k += 3) {
            Vector3 v1 = tris.get(k);
            Vector3 v2 = tris.get(k + 1);
            Vector3 v3 = tris.get(k + 2);

            lineBuilder.line(v1, v2);
            lineBuilder.line(v2, v3);
            lineBuilder.line(v3, v1);
        }
    }

    /**
     * Add vertices for a given NavMesh to the vertices array.
     */
    public void addVertices(NavMesh mesh, long ref) {
        if (ref == 0) {
            return;
        }
        Result<Tupple2<MeshTile, Poly>> tileAndPolyResult = mesh.getTileAndPolyByRef(ref);
        if (tileAndPolyResult.failed()) {
            return;
        }
        Tupple2<MeshTile, Poly> tileAndPoly = tileAndPolyResult.result;
        MeshTile tile = tileAndPoly.first;
        Poly poly = tileAndPoly.second;

        int ip = poly.index;

        if (poly.getType() == Poly.DT_POLYTYPE_OFFMESH_CONNECTION) {
//            OffMeshConnection con = tile.data.offMeshCons[ip - tile.data.header.offMeshBase];
//
//            begin(DebugDrawPrimitives.LINES, 2.0f);
//
//            // Connection arc.
//            appendArc(con.pos[0], con.pos[1], con.pos[2], con.pos[3], con.pos[4], con.pos[5], 0.25f,
//                    (con.flags & 1) != 0 ? 0.6f : 0.0f, 0.6f, c);
//
//            end();
        } else {
            addVertices(tile, ip);
        }

    }

    /**
     * Add vertices for a given tile to the vertices array.
     */
    private void addVertices(MeshTile tile, int index) {
        Poly p = tile.data.polys[index];
        if (tile.data.detailMeshes != null) {
            PolyDetail pd = tile.data.detailMeshes[index];
            if (pd != null) {
                for (int j = 0; j < pd.triCount; ++j) {
                    int t = (pd.triBase + j) * 4;
                    for (int k = 0; k < 3; ++k) {
                        int v = tile.data.detailTris[t + k];
                        if (v < p.vertCount) {
                            vertex(tile.data.verts[p.verts[v] * 3], tile.data.verts[p.verts[v] * 3 + 1],
                                    tile.data.verts[p.verts[v] * 3 + 2]);
                        } else {
                            vertex(tile.data.detailVerts[(pd.vertBase + v - p.vertCount) * 3],
                                    tile.data.detailVerts[(pd.vertBase + v - p.vertCount) * 3 + 1],
                                    tile.data.detailVerts[(pd.vertBase + v - p.vertCount) * 3 + 2]);
                        }
                    }
                }
            }
        } else {
            for (int j = 1; j < p.vertCount - 1; ++j) {
                vertex(tile.data.verts[p.verts[0] * 3], tile.data.verts[p.verts[0] * 3 + 1],
                        tile.data.verts[p.verts[0] * 3 + 2]);
                for (int k = 0; k < 2; ++k) {
                    vertex(tile.data.verts[p.verts[j + k] * 3], tile.data.verts[p.verts[j + k] * 3 + 1],
                            tile.data.verts[p.verts[j + k] * 3 + 2]);
                }
            }
        }
    }

    public void vertex(float x, float y, float z) {
        vertices.add(x);
        vertices.add(y);
        vertices.add(z);
    }
}
