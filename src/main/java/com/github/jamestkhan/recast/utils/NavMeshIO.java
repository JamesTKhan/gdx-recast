package com.github.jamestkhan.recast.utils;

import org.recast4j.detour.NavMesh;
import org.recast4j.detour.io.MeshSetReader;
import org.recast4j.detour.io.MeshSetWriter;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.Files;

/**
 * Simple utility class for saving and loading NavMeshes.
 * @author JamesTKhan
 * @version August 31, 2023
 */
public class NavMeshIO {

    public static void save(NavMesh navMesh, File file) throws IOException {
        MeshSetWriter writer = new MeshSetWriter();
        writer.write(Files.newOutputStream(file.toPath()), navMesh, ByteOrder.BIG_ENDIAN, false);
    }

    public static NavMesh load(File file) throws IOException {
        MeshSetReader reader = new MeshSetReader();
        return reader.read(Files.newInputStream(file.toPath()), 3);
    }
}
