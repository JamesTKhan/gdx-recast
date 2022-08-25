/*
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

package com.github.jamestkhan.recast.geom;

import org.recast4j.detour.MeshTile;
import org.recast4j.detour.NavMesh;

public class NavMeshUtils {

    public static float[][] getNavMeshBounds(NavMesh mesh) {
        float[] bmin = new float[] { Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY };
        float[] bmax = new float[] { Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY };
        for (int t = 0; t < mesh.getMaxTiles(); ++t) {
            MeshTile tile = mesh.getTile(t);
            if (tile != null && tile.data != null) {
                for (int i = 0; i < tile.data.verts.length; i += 3) {
                    bmin[0] = Math.min(bmin[0], tile.data.verts[i]);
                    bmin[1] = Math.min(bmin[1], tile.data.verts[i + 1]);
                    bmin[2] = Math.min(bmin[2], tile.data.verts[i + 2]);
                    bmax[0] = Math.max(bmax[0], tile.data.verts[i]);
                    bmax[1] = Math.max(bmax[1], tile.data.verts[i + 1]);
                    bmax[2] = Math.max(bmax[2], tile.data.verts[i + 2]);
                }
            }
        }
        return new float[][] { bmin, bmax };
    }
}
