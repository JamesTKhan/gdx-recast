package com.github.jamestkhan.recast;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.github.jamestkhan.recast.utils.NavMeshGenerator;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author JamesTKhan
 * @version August 18, 2022
 */
public class PathFinderTest {

    @Test
    public void findPath() {
        NavMeshGenSettings settings = NavMeshGenSettings.Builder.SettingsBuilder()
                .agentRadius(1.0f)
                .agentHeight(2.0f)
                .agentMaxClimb(2.50f)
                .build();

        NavMeshGenerator builder = new NavMeshGenerator(getClass().getClassLoader().getResourceAsStream("nav_test.obj"));
        NavMeshData data = builder.build(settings);

        Pathfinder pathfinder = new Pathfinder(data);

        Array<float[]> paths = new Array<>();
        pathfinder.getPath(new Vector3(-0.99f, 15.24f, 11.98f), new Vector3(17.63f,-2.37f,-21.86f), paths);

        Assert.assertFalse(paths.isEmpty());
    }
}
