/*
 * Decompiled with CFR 0_114.
 */
package distanceRankers;

import java.io.PrintStream;
import java.util.Arrays;
import trajectory.Point;

public class Matrix {
    double[][] value;
    double[][] delta;
    public byte[][] parent;
    public Point[][] colEdits;
    public Point[][] rowEdits;

    public Matrix(int rowNum, int colNum) {
        this.value = new double[rowNum][colNum];
        this.delta = new double[rowNum][colNum];
        this.parent = new byte[rowNum][colNum];
        this.colEdits = new Point[rowNum][colNum];
        this.rowEdits = new Point[rowNum][colNum];
    }

    public void add(int i, int j, double val, int k, Point colEdit, Point rowEdit) {
        this.value[i][j] = val;
        this.parent[i][j] = (byte) k;
        this.colEdits[i][j] = colEdit;
        this.rowEdits[i][j] = rowEdit;
    }

    public int numRows() {
        return this.value.length;
    }

    public int numCols() {
        return this.value[0].length;
    }

    public double score() {
        return this.value[this.value.length - 1][this.value[0].length - 1];
    }

    public void printPath() {
        int i = this.numRows() - 1;
        int j = this.numCols() - 1;
        System.out.println(String.valueOf(i) + " " + j + " " + this.value[i][j] + " " + this.delta[i][j]);
        if (this.parent[i][j] == 1) {
            --i;
            --j;
        } else if (this.parent[i][j] == 2) {
            --j;
        } else {
            --i;
        }
        this.printPath(i, j);
    }

    public void getSubMatchLength() {
        int j = -1;
        double min = Double.MAX_VALUE;
        int i = 1;
        while (i < this.value.length) {
            if (this.value[i][this.value[0].length - 1] < min) {
                min = this.value[i][this.value[0].length - 1];
                j = i;
            }
            ++i;
        }
        i = this.numRows() - 1;
        System.out.println(String.valueOf(i) + " " + j + " " + this.value[i][j] + " " + this.delta[i][j]);
        if (this.parent[i][j] == 1) {
            --i;
            --j;
        } else if (this.parent[i][j] == 2) {
            --j;
        } else {
            --i;
        }
        this.printPath(i, j);
    }

    private void printPath(int i, int j) {
        System.out.println(String.valueOf(i) + " " + j + " " + this.value[i][j] + " " + this.delta[i][j]);
        if (i == 0 || j == 0) {
            return;
        }
        if (this.parent[i][j] == 1) {
            --i;
            --j;
        } else if (this.parent[i][j] == 2) {
            --j;
        } else {
            --i;
        }
        this.printPath(i, j);
    }

    public double subScore() {
        double min = Double.MAX_VALUE;
        int i = 1;
        while (i < this.value.length) {
            if (this.value[i][this.value[0].length - 1] < min) {
                min = this.value[i][this.value[0].length - 1];
            }
            ++i;
        }
        return min;
    }

    public void print() {
        System.out.println("=======================");
        int i = 0;
        while (i < this.value.length) {
            System.out.println(Arrays.toString(this.value[i]));
            ++i;
        }
        System.out.println("=======================");
    }

    public int getSubPoint() {
        double min = Double.MAX_VALUE;
        int minI = -1;
        int i = 1;
        while (i < this.value.length) {
            if (this.value[i][this.value[0].length - 1] < min) {
                min = this.value[i][this.value[0].length - 1];
                minI = i;
            }
            ++i;
        }
        return minI;
    }
}

