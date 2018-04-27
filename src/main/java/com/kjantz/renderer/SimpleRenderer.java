package com.kjantz.renderer;

/* File Creation Info:
 * User: KristianJ
 * Date: 27.04.2018
 * Time: 10:47
 *
 * Copyright LucaNet AG
 */

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import static java.lang.Math.*;

/**
 * Not documented yet.
 * //since - VERSION
 * //author - EXPERT TEAM
 */
public class SimpleRenderer {

    // =========================== Class Variables ===========================79
    public static List<int[]> EDGES = new ArrayList<>();
    public static List<Vector3D> CUBE = generateCube();

    private static List<Vector3D> generateCube() {
        List<Vector3D> res = new ArrayList<>();
        res.add(new Vector3D(-1, +1, -1));
        res.add(new Vector3D(+1, +1, -1));
        res.add(new Vector3D(+1, -1, -1));
        res.add(new Vector3D(-1, -1, -1));
        res.add(new Vector3D(-1, +1, +1));
        res.add(new Vector3D(+1, +1, +1));
        res.add(new Vector3D(+1, -1, +1));
        res.add(new Vector3D(-1, -1, +1));

        EDGES.add(new int[]{1, 3, 4});
        EDGES.add(new int[]{0, 2, 5});
        EDGES.add(new int[]{1, 3, 6});
        EDGES.add(new int[]{0, 2, 7});
        EDGES.add(new int[]{0, 5, 7});
        EDGES.add(new int[]{1, 4, 6});
        EDGES.add(new int[]{2, 5, 7});
        EDGES.add(new int[]{3, 4, 6});

        return res;
    }

    // =============================  Variables  =============================79
    private Vector3D cameraPos = new Vector3D(1, 1, 1);
    private Vector3D cameraOrientation = new Vector3D(0, 0, 0);
    private Vector3D viewerPosition = new Vector3D(0, 0, -10);

    private BlockRealMatrix m1 = new BlockRealMatrix(3, 3);
    private BlockRealMatrix m2 = new BlockRealMatrix(3, 3);
    private BlockRealMatrix m3 = new BlockRealMatrix(3, 3);


    private static int X = 0;
    private static int Y = 1;
    private static int Z = 2;


    private final BlockRealMatrix m4;

    // ============================  Constructors  ===========================79

    public static Vector3D rotateY3D(double theta, Vector3D node) {
        double sinTheta = sin(theta);
        double cosTheta = cos(theta);
        double x = node.getX();
        double z = node.getZ();
        return new Vector3D(x * cosTheta - z * sinTheta, node.getY(), z * cosTheta + x * sinTheta);
    }

    public static Vector3D rotateX3D(double theta, Vector3D node) {
        double sinTheta = sin(theta);
        double cosTheta = cos(theta);
        double y = node.getY();
        double z = node.getZ();
        return new Vector3D(node.getX(),y * cosTheta - z * sinTheta, z * cosTheta + y * sinTheta);
    }


    public SimpleRenderer() {
        m1.setRow(0, new double[]{1, 0, 0});
        m1.setRow(1, new double[]{0, cos(cameraOrientation.getX()), sin(cameraOrientation.getX())});
        m1.setRow(2, new double[]{0, -sin(cameraOrientation.getX()), cos(cameraOrientation.getX())});


        m2.setRow(0, new double[]{cos(cameraOrientation.getY()), 0, -sin(cameraOrientation.getY())});
        m2.setRow(1, new double[]{0, 1, 0});
        m2.setRow(2, new double[]{sin(cameraOrientation.getY()), 0, cos(cameraOrientation.getY())});

        m3.setRow(0, new double[]{cos(cameraOrientation.getZ()), sin(cameraOrientation.getZ()), 0});
        m3.setRow(1, new double[]{-sin(cameraOrientation.getZ()), cos(cameraOrientation.getZ()), 0});
        m3.setRow(2, new double[]{0, 0, 1});

        m4 = m1.multiply(m2).multiply(m3);
    }

    // ===========================  public  Methods  =========================79

    public Vector2D project(Vector3D pos) {
        final Vector3D subtract = pos.subtract(cameraPos);
        final RealVector diff = new ArrayRealVector(new double[]{subtract.getX(), subtract.getY(), subtract.getZ()});
        final RealVector d = m4.preMultiply(diff);

        final double x = (viewerPosition.getZ() / d.getEntry(Z) * d.getEntry(X)) - viewerPosition.getX();
        double y = (viewerPosition.getZ() / d.getEntry(Z) * d.getEntry(Y)) - viewerPosition.getY();


        Vector2D res = new Vector2D(x, y);

        return res;
    }

    public Vector2D project2(Vector3D pos, double distance) {
        double X = ((pos.getX() - cameraPos.getX()) * (distance / pos.getZ())) + cameraPos.getX();
        double Y = ((pos.getY() - cameraPos.getY()) * (distance / pos.getZ())) + cameraPos.getY();
        return new Vector2D(X, Y);
    }

    // =================  protected/package local  Methods ===================79
    public static void main(String[] args) {
        SimpleRenderer r = new SimpleRenderer();
        System.out.println(r.project(new Vector3D(1, 0, -10)));
        System.out.println(r.project2(new Vector3D(1, 0, -10), 1));
    }

    public void setCameraOrientation(Vector3D cameraOrientation) {
        this.cameraOrientation = cameraOrientation;
    }

    public void setCameraPosition(Vector3D cameraPos) {
        this.cameraPos = cameraPos;

    }

    // ===========================  private  Methods  ========================79
    // ============================  Inner Classes  ==========================79
    // ============================  End of class  ===========================79
}

