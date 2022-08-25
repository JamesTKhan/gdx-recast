/*
Copyright (c) 2009-2010 Mikko Mononen memon@inside.org
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
package com.github.jamestkhan.recast;

import com.github.jamestkhan.recast.geom.GdxInputGeomProvider;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.NavMeshQuery;
import org.recast4j.recast.RecastBuilder.RecastBuilderResult;

import java.util.List;

public class NavMeshData {

    private GdxInputGeomProvider inputGeom;
    private NavMesh navMesh;
    private NavMeshQuery navMeshQuery;
    private NavigationSettings settings;

    public NavMeshData(GdxInputGeomProvider inputGeom, NavMesh navMesh, NavigationSettings settings) {
        this.inputGeom = inputGeom;
        this.navMesh = navMesh;
        this.settings = settings;
        setQuery(navMesh);
    }

    private void setQuery(NavMesh navMesh) {
        navMeshQuery = navMesh != null ? new NavMeshQuery(navMesh) : null;
    }

    public GdxInputGeomProvider getInputGeom() {
        return inputGeom;
    }

    public NavMesh getNavMesh() {
        return navMesh;
    }

    public NavMeshQuery getNavMeshQuery() {
        return navMeshQuery;
    }

    public NavigationSettings getSettings() {
        return settings;
    }

    public void update(GdxInputGeomProvider geom, List<RecastBuilderResult> recastResults, NavMesh navMesh) {
        inputGeom = geom;
        this.navMesh = navMesh;
        setQuery(navMesh);
    }
}
