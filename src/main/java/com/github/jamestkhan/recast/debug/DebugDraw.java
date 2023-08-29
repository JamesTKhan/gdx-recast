package com.github.jamestkhan.recast.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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

    public final CustomShapeRenderer shapeRenderer = new CustomShapeRenderer();
    private final Array<Float> vertices = new Array<>();

    public void begin(Camera camera) {
        Gdx.gl.glDepthFunc(GL20.GL_LESS);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.begin();
    }

    public void debugDrawNavMeshPolysWithFlags(NavMesh mesh, int polyFlags) {
        vertices.clear();
        shapeRenderer.setColor(Color.TEAL.r, Color.TEAL.g, Color.TEAL.b, .4f);

        for (int i = 0; i < mesh.getMaxTiles(); ++i) {
            MeshTile tile = mesh.getTile(i);
            if (tile == null || tile.data == null || tile.data.header == null) {
                continue;
            }
            long base = mesh.getPolyRefBase(tile);

            for (int j = 0; j < tile.data.header.polyCount; ++j) {
                Poly p = tile.data.polys[j];
                if ((p.flags & polyFlags) == 0) {
                    continue;
                }
                debugDrawNavMeshPoly(mesh, base | j);
            }
        }

        int i = 0;
        float heightOffset = 0.2f;
        while (i < vertices.size) {
            shapeRenderer.line(vertices.get(i), vertices.get(i+1) + heightOffset, vertices.get(i+2), vertices.get(i+3), vertices.get(i+4) + heightOffset, vertices.get(i+5));
            shapeRenderer.line(vertices.get(i+3), vertices.get(i+4) + heightOffset, vertices.get(i+5), vertices.get(i+6), vertices.get(i+7) + heightOffset, vertices.get(i+8));
            shapeRenderer.line(vertices.get(i+6), vertices.get(i+7) + heightOffset, vertices.get(i+8), vertices.get(i), vertices.get(i+1) + heightOffset, vertices.get(i+2));
            i+= 9;
        }
    }

    public void debugDrawNavMeshPoly(NavMesh mesh, long ref) {
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

        Gdx.gl.glDepthMask((false));

       // int c = duTransCol(col, 64);
        int c = 0;
        int ip = poly.index;

        if (poly.getType() == Poly.DT_POLYTYPE_OFFMESH_CONNECTION) {
            System.out.println("off");
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
            drawPoly(tile, ip);
        }

        Gdx.gl.glDepthMask((true));
    }

    private void drawPoly(MeshTile tile, int index) {
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

    public void drawCrowdAgents(Array<CrowdAgent> crowdAgents) {
        shapeRenderer.setColor(Color.FOREST);
        shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
        for (CrowdAgent agent : crowdAgents) {
            //shapeRenderer.circle(agent.npos[0], agent.npos[1], agent.npos[2], agent.params.radius);
            float radius = agent.params.radius;
            shapeRenderer.box(agent.npos[0], agent.npos[1], agent.npos[2], radius, radius, radius);
        }
    }

    public void vertex(float x, float y, float z) {
        vertices.add(x);
        vertices.add(y);
        vertices.add(z);
    }

    public void end() {
        shapeRenderer.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
    }

    public void renderStartPos(Vector3 start) {
        renderStartPos(start.x, start.y, start.z);
    }

    public void renderStartPos(float x, float y, float z) {
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.circle( x, y + 2.1f, z, 2f, 360);
        shapeRenderer.set(ShapeRenderer.ShapeType.Line);
    }

    public void renderEndPos(Vector3 start) {
        renderEndPos(start.x, start.y, start.z);
    }

    public void renderEndPos(float x, float y, float z) {
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.circle( x, y + 2.1f, z, 2f, 360);
        shapeRenderer.set(ShapeRenderer.ShapeType.Line);
    }

    public void renderPath(Array<float[]> paths) {
        renderPath(paths, true);
    }

    public void renderPath(Array<float[]> paths, boolean renderStartAndEnd) {
        for (int i = 1; i < paths.size; i +=2) {

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

            renderLine(pos[0], pos[1], pos[2],nextPos[0], nextPos[1], nextPos[2], 0f);
        }
    }

    private void renderLine(float x1, float y1, float z1, float x2, float y2, float z2, float offset) {
        shapeRenderer.line(x1 + offset, y1 + 0.5f, z1 + offset, x2 + offset, y2 + 0.5f, z2 + offset);
    }

}
