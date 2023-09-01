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

import com.github.jamestkhan.recast.NavMeshGenSettings;
import com.github.jamestkhan.recast.geom.GdxInputGeomProvider;
import org.recast4j.detour.MeshData;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.NavMeshBuilder;
import org.recast4j.detour.NavMeshDataCreateParams;
import org.recast4j.detour.Tupple2;
import org.recast4j.recast.RecastBuilder;
import org.recast4j.recast.RecastBuilder.RecastBuilderResult;
import org.recast4j.recast.RecastBuilderConfig;
import org.recast4j.recast.RecastConfig;

import java.util.Collections;
import java.util.List;

public class SoloNavMeshBuilder extends AbstractNavMeshBuilder {

    public Tupple2<List<RecastBuilderResult>, NavMesh> build(GdxInputGeomProvider m_geom, NavMeshGenSettings navMeshGenSettings) {

        RecastBuilderResult rcResult = buildRecastResult(m_geom, navMeshGenSettings, true, true, true);
        return new Tupple2<>(Collections.singletonList(rcResult),
                buildNavMesh(
                        buildMeshData(m_geom, navMeshGenSettings.cellSize, navMeshGenSettings.cellHeight, navMeshGenSettings.agentHeight, navMeshGenSettings.agentRadius, navMeshGenSettings.agentMaxClimb, rcResult),
                        navMeshGenSettings.maxVertsPerPoly));
    }

    private NavMesh buildNavMesh(MeshData meshData, int m_vertsPerPoly) {
        return new NavMesh(meshData, m_vertsPerPoly, 0);
    }

    private RecastBuilderResult buildRecastResult(GdxInputGeomProvider m_geom, NavMeshGenSettings settings, boolean filterLowHangingObstacles, boolean filterLedgeSpans,
                                                  boolean filterWalkableLowHeightSpans) {

        RecastConfig cfg = new RecastConfig(settings.partitionType, settings.cellSize, settings.cellHeight, settings.agentMaxSlope, filterLowHangingObstacles,
                filterLedgeSpans, filterWalkableLowHeightSpans, settings.agentHeight, settings.agentRadius, settings.agentMaxClimb, settings.regionMinSize,
                settings.regionMergeSize, settings.maxEdgeLength, settings.maxEdgeError, settings.maxVertsPerPoly, settings.detailSampleDistance, settings.detailSampleMaxError,
                SampleAreaModifications.SAMPLE_AREAMOD_WALKABLE, true);

        RecastBuilderConfig bcfg = new RecastBuilderConfig(cfg, m_geom.getMeshBoundsMin(), m_geom.getMeshBoundsMax());
        RecastBuilder rcBuilder = new RecastBuilder();
        return rcBuilder.build(m_geom, bcfg);
    }

    private MeshData buildMeshData(GdxInputGeomProvider m_geom, float m_cellSize, float m_cellHeight, float m_agentHeight,
                                   float m_agentRadius, float m_agentMaxClimb, RecastBuilderResult rcResult) {
        NavMeshDataCreateParams params = getNavMeshCreateParams(m_geom, m_cellSize, m_cellHeight, m_agentHeight, m_agentRadius,
                m_agentMaxClimb, rcResult);
        return updateAreaAndFlags(NavMeshBuilder.createNavMeshData(params));
    }

}
