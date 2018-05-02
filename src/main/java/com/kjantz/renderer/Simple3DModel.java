package com.kjantz.renderer;

/* File Creation Info:
 * User: KristianJ
 * Date: 27.04.2018
 * Time: 18:42
 *
 * Copyright LucaNet AG
 */

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.List;

/**
 * Not documented yet.
 * //since - VERSION
 * //author - EXPERT TEAM
 */
public class Simple3DModel {
    // =========================== Class Variables ===========================79
    public static Simple3DModel CUBE = generateCube();

    private static Simple3DModel generateCube() {
        List<Vector3D> vertices = new ArrayList<>();
        vertices.add(new Vector3D(-1, +1, -1));
        vertices.add(new Vector3D(+1, +1, -1));
        vertices.add(new Vector3D(+1, -1, -1));
        vertices.add(new Vector3D(-1, -1, -1));
        vertices.add(new Vector3D(-1, +1, +1));
        vertices.add(new Vector3D(+1, +1, +1));
        vertices.add(new Vector3D(+1, -1, +1));
        vertices.add(new Vector3D(-1, -1, +1));


        List<int[]> edges = new ArrayList<>();
        edges.add(new int[]{1, 3, 4});
        edges.add(new int[]{0, 2, 5});
        edges.add(new int[]{1, 3, 6});
        edges.add(new int[]{0, 2, 7});
        edges.add(new int[]{0, 5, 7});
        edges.add(new int[]{1, 4, 6});
        edges.add(new int[]{2, 5, 7});
        edges.add(new int[]{3, 4, 6});
        return new Simple3DModel(vertices, edges);
    }


    // =============================  Variables  =============================79
    private List<Vector3D> vertices = new ArrayList<>();
    private List<int[]> edges = new ArrayList<>();

    public Simple3DModel(List<Vector3D> vertices, List<int[]> edges) {
        this.vertices.addAll(vertices);
        this.edges.addAll(edges);
    }
    // ============================  Constructors  ===========================79

    public List<Vector3D> getVertices() {
        return vertices;
    }

    public List<int[]> getEdges() {
        return edges;
    }

    // ===========================  public  Methods  =========================79
    // =================  protected/package local  Methods ===================79
    // ===========================  private  Methods  ========================79
    // ============================  Inner Classes  ==========================79
    // ============================  End of class  ===========================79
}

