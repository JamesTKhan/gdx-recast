/*
recast4j copyright (c) 2015-2019 Piotr Piastucki piotr@jtilia.org

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
package com.github.jamestkhan.recast.builders;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.github.jamestkhan.recast.NavigationSettings;
import com.github.jamestkhan.recast.builders.AbstractNavMeshBuilder;
import com.github.jamestkhan.recast.builders.SampleAreaModifications;
import com.github.jamestkhan.recast.geom.GdxInputGeomProvider;

import org.recast4j.detour.DetourCommon;
import org.recast4j.detour.MeshData;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.NavMeshBuilder;
import org.recast4j.detour.NavMeshDataCreateParams;
import org.recast4j.detour.NavMeshParams;
import org.recast4j.detour.Tupple2;
import org.recast4j.recast.Recast;
import org.recast4j.recast.RecastBuilder;
import org.recast4j.recast.RecastBuilder.RecastBuilderResult;
import org.recast4j.recast.RecastConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TileNavMeshBuilder extends AbstractNavMeshBuilder {

    // This is the GWT version
//    private final ExecutorService executor;

    public TileNavMeshBuilder() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        Gdx.app.log("GWT", "Using GWT version of TileNavMeshBuilder");
//        executor = Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors() / 2),
//                new RecastBuilderThreadFactory());
    }

    public Tupple2<List<RecastBuilderResult>, NavMesh> build(GdxInputGeomProvider m_geom, NavigationSettings navigationSettings) {

        List<RecastBuilderResult> rcResult = buildRecastResult(m_geom, navigationSettings, true, true, true);
        return new Tupple2<>(rcResult,
                buildNavMesh(m_geom,
                        buildMeshData(m_geom, navigationSettings.cellSize, navigationSettings.cellHeight, navigationSettings.agentHeight, navigationSettings.agentRadius, navigationSettings.agentMaxClimb, rcResult),
                        navigationSettings.cellSize, navigationSettings.tileSizeX, navigationSettings.maxVertsPerPoly));
    }

    private List<RecastBuilderResult> buildRecastResult(GdxInputGeomProvider m_geom, NavigationSettings settings, boolean filterLowHangingObstacles, boolean filterLedgeSpans,
                                                        boolean filterWalkableLowHeightSpans) {

        RecastConfig cfg = new RecastConfig(settings.useTiles, settings.tileSizeX, settings.tileSizeZ, RecastConfig.calcBorder(settings.agentRadius, settings.cellSize),
                settings.partitionType, settings.cellSize, settings.cellHeight, settings.agentMaxSlope, filterLowHangingObstacles,
                filterLedgeSpans, filterWalkableLowHeightSpans, settings.agentHeight, settings.agentRadius, settings.agentMaxClimb,
                settings.regionMinSize * settings.regionMinSize * settings.cellSize * settings.cellSize,
                settings.regionMergeSize * settings.regionMergeSize * settings.cellSize * settings.cellSize, settings.maxEdgeLength, settings.maxEdgeError, settings.maxVertsPerPoly,
                true, settings.detailSampleDistance, settings.detailSampleMaxError, SampleAreaModifications.SAMPLE_AREAMOD_WALKABLE);

        RecastBuilder rcBuilder = new RecastBuilder();
        return rcBuilder.buildTiles(m_geom, cfg, Optional.empty());
    }

    private NavMesh buildNavMesh(GdxInputGeomProvider geom, List<MeshData> meshData, float cellSize, int tileSize,
                                 int vertsPerPoly) {
        NavMeshParams navMeshParams = new NavMeshParams();
        navMeshParams.orig[0] = geom.getMeshBoundsMin()[0];
        navMeshParams.orig[1] = geom.getMeshBoundsMin()[1];
        navMeshParams.orig[2] = geom.getMeshBoundsMin()[2];
        navMeshParams.tileWidth = tileSize * cellSize;
        navMeshParams.tileHeight = tileSize * cellSize;

        // snprintf(text, 64, "Tiles %d x %d", tw, th);

        navMeshParams.maxTiles = getMaxTiles(geom, cellSize, tileSize);
        navMeshParams.maxPolys = getMaxPolysPerTile(geom, cellSize, tileSize);
        NavMesh navMesh = new NavMesh(navMeshParams, vertsPerPoly);
        meshData.forEach(md -> navMesh.addTile(md, 0, 0));
        return navMesh;
    }

    public int getMaxTiles(GdxInputGeomProvider geom, float cellSize, int tileSize) {
        int tileBits = getTileBits(geom, cellSize, tileSize);
        return 1 << tileBits;
    }

    public int getMaxPolysPerTile(GdxInputGeomProvider geom, float cellSize, int tileSize) {
        int polyBits = 22 - getTileBits(geom, cellSize, tileSize);
        return 1 << polyBits;
    }

    private int getTileBits(GdxInputGeomProvider geom, float cellSize, int tileSize) {
        int[] wh = Recast.calcGridSize(geom.getMeshBoundsMin(), geom.getMeshBoundsMax(), cellSize);
        int tw = (wh[0] + tileSize - 1) / tileSize;
        int th = (wh[1] + tileSize - 1) / tileSize;
        int tileBits = Math.min(DetourCommon.ilog2(DetourCommon.nextPow2(tw * th)), 14);
        return tileBits;
    }

    public int[] getTiles(GdxInputGeomProvider geom, float cellSize, int tileSize) {
        int[] wh = Recast.calcGridSize(geom.getMeshBoundsMin(), geom.getMeshBoundsMax(), cellSize);
        int tw = (wh[0] + tileSize - 1) / tileSize;
        int th = (wh[1] + tileSize - 1) / tileSize;
        return new int[] { tw, th };
    }

    private List<MeshData> buildMeshData(GdxInputGeomProvider m_geom, float m_cellSize, float m_cellHeight, float m_agentHeight,
                                         float m_agentRadius, float m_agentMaxClimb, List<RecastBuilderResult> rcResult) {

        // Add tiles to nav mesh
        List<MeshData> meshData = new ArrayList<>();
        for (RecastBuilderResult result : rcResult) {
            int x = result.tileX;
            int z = result.tileZ;
            NavMeshDataCreateParams params = getNavMeshCreateParams(m_geom, m_cellSize, m_cellHeight, m_agentHeight,
                    m_agentRadius, m_agentMaxClimb, result);
            params.tileX = x;
            params.tileZ = z;
            MeshData md = NavMeshBuilder.createNavMeshData(params);
            if (md != null) {
                meshData.add(updateAreaAndFlags(md));
            }
        }
        return meshData;
    }

}
