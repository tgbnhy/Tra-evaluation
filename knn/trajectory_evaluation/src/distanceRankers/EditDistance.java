/*
 * Decompiled with CFR 0_114.
 */
package distanceRankers;

import Launcher.Launch;
import distanceRankers.Matrix;
import distanceRankers.TrajectoryDistance;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import trajectory.Box;
import trajectory.BoxEdge;
import trajectory.Edge;
import trajectory.Point;
import trajectory.STpoint;
import trajectory.Summary;
import trajectory.Trajectory;

public class EditDistance
implements TrajectoryDistance {
    double totalLength;
    public Matrix matrix;
    double time;
    boolean computeTime;

    @Override
    public String getName() {
        return "EditDistance";
    }

    public EditDistance(boolean time) {
        this.computeTime = time;
    }

    public EditDistance() {
        this.computeTime = true;
    }

    @Override
    public String getPythonSymbol() {
        return "s";
    }

    @Override
    public double[] getDistance(Trajectory t1, Trajectory t2) {
        this.matrix = new Matrix(t1.edges.size() + 1, t2.edges.size() + 1);
        this.initializeMatrix();
        this.totalLength = t1.length + t2.length;
        int i = 1;
        while (i < this.matrix.numRows()) {
            int j = 1;
            while (j < this.matrix.numCols()) {
                double prevPointEdge;
                double rowDelta = Double.MAX_VALUE;
                double colDelta = Double.MAX_VALUE;
                double rowCoverage1 = Double.MAX_VALUE;
                double rowCoverage2 = Double.MAX_VALUE;
                double colCoverage1 = Double.MAX_VALUE;
                double colCoverage2 = Double.MAX_VALUE;
                double rowSpatialScore = Double.MAX_VALUE;
                double colSpatialScore = Double.MAX_VALUE;
                STpoint t2Insert = null;
                STpoint t1Insert = null;
                Point t2Edit = null;
                Point t1Edit = null;
                if (i > 1) {
                    t1Edit = this.matrix.rowEdits[i - 1][j];
                    t2Edit = this.matrix.colEdits[i - 1][j];
                    prevPointEdge = t1Edit.euclidean(t1.getPoint(i - 1));
                    t2Insert = this.lineMap((STpoint)t2Edit, t2.getPoint(j), t1.getPoint(i - 1));
                    double rowEditDistance = t2Insert.euclidean(t1.getPoint(i - 1));
                    double rowEditEdge = t2Edit.euclidean(t2Insert);
                    t2Insert.time = 0.0;
                    rowCoverage1 = (rowEditEdge + prevPointEdge) / this.totalLength;
                    rowCoverage2 = (t2.getPoint(j).euclidean(t2Insert) + t1.edgeLength(i - 1)) / this.totalLength;
                    rowDelta = this.matrix.value[i - 1][j] - this.matrix.delta[i - 1][j] + (rowEditDistance + t1Edit.euclidean(t2Edit)) * rowCoverage1;
                    rowSpatialScore = rowDelta + (rowEditDistance + t2.getPoint(j).euclidean(t1.getPoint(i))) * rowCoverage2;
                }
                if (j > 1) {
                    t1Edit = this.matrix.rowEdits[i][j - 1];
                    t2Edit = this.matrix.colEdits[i][j - 1];
                    if (t1Edit == null) break;
                    prevPointEdge = t2Edit.euclidean(t2.getPoint(j - 1));
                    t1Insert = this.lineMap((STpoint)t1Edit, t1.getPoint(i), t2.getPoint(j - 1));
                    double colEditDistance = t1Insert.euclidean(t2.getPoint(j - 1));
                    double colEditEdge = t1Edit.euclidean(t1Insert);
                    t1Insert.time = 0.0;
                    colCoverage1 = (colEditEdge + prevPointEdge) / this.totalLength;
                    colCoverage2 = (t1.getPoint(i).euclidean(t1Insert) + t2.edgeLength(j - 1)) / this.totalLength;
                    colDelta = this.matrix.value[i][j - 1] - this.matrix.delta[i][j - 1] + (colEditDistance + t1Edit.euclidean(t2Edit)) * colCoverage1;
                    colSpatialScore = colDelta + (colEditDistance + t1.getPoint(i).euclidean(t2.getPoint(j))) * colCoverage2;
                }
                double diagCoverage = (t1.edgeLength(i - 1) + t2.edgeLength(j - 1)) / this.totalLength;
                double subScore = (t2.getPoint(j).euclidean(t1.getPoint(i)) + t2.getPoint(j - 1).euclidean(t1.getPoint(i - 1))) * diagCoverage;
                double diagScore = this.matrix.value[i - 1][j - 1] + subScore;
                if (diagScore <= colSpatialScore && diagScore <= rowSpatialScore) {
                    this.matrix.add(i, j, diagScore, 1, t2.getPoint(j - 1), t1.getPoint(i - 1));
                    this.matrix.delta[i][j] = diagScore - this.matrix.value[i - 1][j - 1];
                } else if (colSpatialScore < rowSpatialScore || colSpatialScore == rowSpatialScore && t2.edges.size() > t1.edges.size()) {
                    this.matrix.add(i, j, colSpatialScore, 2, t2.getPoint(j - 1), t1Insert);
                    this.matrix.delta[i][j] = colSpatialScore - colDelta;
                } else {
                    this.matrix.add(i, j, rowSpatialScore, 3, t2Insert, t1.getPoint(i - 1));
                    this.matrix.delta[i][j] = rowSpatialScore - rowDelta;
                }
                ++j;
            }
            ++i;
        }
        double[] answer = new double[]{this.matrix.score(), this.time};
        return answer;
    }

    public double[] getSubDistance(Trajectory t1, Trajectory t2) {
        int j;
        this.matrix = new Matrix(t1.edges.size() + 1, t2.edges.size() + 1);
        this.subInitializeMatrix();
        this.totalLength = 1.0;
        this.time = 0.0;
        int i = 1;
        while (i < this.matrix.numRows()) {
            j = 1;
            while (j < this.matrix.numCols()) {
                double prevPointEdge;
                double rowDelta = Double.MAX_VALUE;
                double colDelta = Double.MAX_VALUE;
                double rowCoverage1 = Double.MAX_VALUE;
                double rowCoverage2 = Double.MAX_VALUE;
                double colCoverage1 = Double.MAX_VALUE;
                double colCoverage2 = Double.MAX_VALUE;
                double rowSpatialScore = Double.MAX_VALUE;
                double colSpatialScore = Double.MAX_VALUE;
                STpoint t2Insert = null;
                STpoint t1Insert = null;
                Point t2Edit = null;
                Point t1Edit = null;
                if (i > 1) {
                    t1Edit = this.matrix.rowEdits[i - 1][j];
                    t2Edit = this.matrix.colEdits[i - 1][j];
                    prevPointEdge = t1Edit.euclidean(t1.getPoint(i - 1));
                    t2Insert = this.lineMap((STpoint)t2Edit, t2.getPoint(j), t1.getPoint(i - 1));
                    double rowEditDistance = t2Insert.euclidean(t1.getPoint(i - 1));
                    double rowEditEdge = t2Edit.euclidean(t2Insert);
                    t2Insert.time = ((STpoint)t2Edit).time + rowEditEdge / t2.getEdge((int)(j - 1)).speed;
                    rowCoverage1 = (rowEditEdge + prevPointEdge) / this.totalLength;
                    rowCoverage2 = (t2.getPoint(j).euclidean(t2Insert) + t1.edgeLength(i - 1)) / this.totalLength;
                    rowDelta = this.matrix.value[i - 1][j] - this.matrix.delta[i - 1][j] + (rowEditDistance + t1Edit.euclidean(t2Edit)) * rowCoverage1;
                    rowSpatialScore = rowDelta + (rowEditDistance + t2.getPoint(j).euclidean(t1.getPoint(i))) * rowCoverage2;
                }
                if (j > 1) {
                    t1Edit = this.matrix.rowEdits[i][j - 1];
                    t2Edit = this.matrix.colEdits[i][j - 1];
                    if (t1Edit == null) break;
                    prevPointEdge = t2Edit.euclidean(t2.getPoint(j - 1));
                    t1Insert = this.lineMap((STpoint)t1Edit, t1.getPoint(i), t2.getPoint(j - 1));
                    double colEditDistance = t1Insert.euclidean(t2.getPoint(j - 1));
                    double colEditEdge = t1Edit.euclidean(t1Insert);
                    t1Insert.time = ((STpoint)t1Edit).time + colEditEdge / t1.getEdge((int)(i - 1)).speed;
                    colCoverage1 = (colEditEdge + prevPointEdge) / this.totalLength;
                    colCoverage2 = (t1.getPoint(i).euclidean(t1Insert) + t2.edgeLength(j - 1)) / this.totalLength;
                    colDelta = this.matrix.value[i][j - 1] - this.matrix.delta[i][j - 1] + (colEditDistance + t1Edit.euclidean(t2Edit)) * colCoverage1;
                    colSpatialScore = colDelta + (colEditDistance + t1.getPoint(i).euclidean(t2.getPoint(j))) * colCoverage2;
                }
                double diagCoverage = (t1.edgeLength(i - 1) + t2.edgeLength(j - 1)) / this.totalLength;
                double subScore = (t2.getPoint(j).euclidean(t1.getPoint(i)) + t2.getPoint(j - 1).euclidean(t1.getPoint(i - 1))) * diagCoverage;
                double diagScore = this.matrix.value[i - 1][j - 1] + subScore;
                if (diagScore <= colSpatialScore && diagScore <= rowSpatialScore) {
                    this.matrix.add(i, j, diagScore, 1, t2.getPoint(j - 1), t1.getPoint(i - 1));
                    this.matrix.delta[i][j] = diagScore - this.matrix.value[i - 1][j - 1];
                } else if (colSpatialScore < rowSpatialScore || colSpatialScore == rowSpatialScore && t2.edges.size() > t1.edges.size()) {
                    this.matrix.add(i, j, colSpatialScore, 2, t2.getPoint(j - 1), t1Insert);
                    this.matrix.delta[i][j] = colSpatialScore - colDelta;
                } else {
                    this.matrix.add(i, j, rowSpatialScore, 3, t2Insert, t1.getPoint(i - 1));
                    this.matrix.delta[i][j] = rowSpatialScore - rowDelta;
                }
                ++j;
            }
            ++i;
        }
        i = this.matrix.numRows() - 1;
        j = this.matrix.numCols() - 1;
        ArrayList<Edge> e1 = new ArrayList<Edge>();
        ArrayList<Edge> e2 = new ArrayList<Edge>();
        if (this.computeTime) {
            this.computeTemporalScore(t1, t2, i, j, e1, e2);
        }
        double[] answer = new double[]{this.matrix.subScore(), this.time};
        return answer;
    }

    public double[] getDistance(Summary s1, Trajectory t2) {
        int j;
        this.matrix = new Matrix(s1.edges.size() + 1, t2.edges.size() + 1);
        this.initializeMatrix();
        this.totalLength = s1.length + t2.length;
        this.time = 0.0;
        int i = 1;
        while (i < this.matrix.numRows()) {
            j = 1;
            while (j < this.matrix.numCols()) {
                double prevPointEdge;
                double rowDelta = Double.MAX_VALUE;
                double colDelta = Double.MAX_VALUE;
                double rowCoverage1 = Double.MAX_VALUE;
                double rowCoverage2 = Double.MAX_VALUE;
                double colCoverage1 = Double.MAX_VALUE;
                double colCoverage2 = Double.MAX_VALUE;
                double rowSpatialScore = Double.MAX_VALUE;
                double colSpatialScore = Double.MAX_VALUE;
                STpoint t2Insert = null;
                Box t1Insert = null;
                Point t2Edit = null;
                Point t1Edit = null;
                if (i > 1) {
                    t1Edit = this.matrix.rowEdits[i - 1][j];
                    t2Edit = this.matrix.colEdits[i - 1][j];
                    prevPointEdge = t1Edit.euclidean(s1.getBox(i - 1));
                    t2Insert = this.lineMap(t2Edit, t2.getPoint(j), s1.getBox(i - 1));
                    double rowEditDistance = t2Insert.euclidean(s1.getBox(i - 1));
                    double rowEditEdge = t2Edit.euclidean(t2Insert);
                    rowCoverage1 = (rowEditEdge + prevPointEdge) / this.totalLength;
                    rowCoverage2 = (t2.getPoint(j).euclidean(t2Insert) + s1.edgeLength(i - 1)) / this.totalLength;
                    rowDelta = this.matrix.value[i - 1][j] - this.matrix.delta[i - 1][j] + (rowEditDistance + t1Edit.euclidean(t2Edit)) * rowCoverage1;
                    rowSpatialScore = rowDelta + (rowEditDistance + t2.getPoint(j).euclidean(s1.getBox(i))) * rowCoverage2;
                }
                if (j > 1) {
                    t1Edit = this.matrix.rowEdits[i][j - 1];
                    t2Edit = this.matrix.colEdits[i][j - 1];
                    prevPointEdge = t2Edit.euclidean(t2.getPoint(j - 1));
                    t1Insert = this.lineMap((Box)t1Edit, s1.getBox(i), t2.getPoint(j - 1));
                    double colEditDistance = t1Insert.euclidean(t2.getPoint(j - 1));
                    double colEditEdge = t1Edit.euclidean(t1Insert);
                    colCoverage1 = (colEditEdge + prevPointEdge) / this.totalLength;
                    colCoverage2 = (s1.getBox(i).euclidean(t1Insert) + t2.edgeLength(j - 1)) / this.totalLength;
                    colDelta = this.matrix.value[i][j - 1] - this.matrix.delta[i][j - 1] + (colEditDistance + t1Edit.euclidean(t2Edit)) * colCoverage1;
                    colSpatialScore = colDelta + (colEditDistance + s1.getBox(i).euclidean(t2.getPoint(j))) * colCoverage2;
                }
                double diagCoverage = (s1.edgeLength(i - 1) + t2.edgeLength(j - 1)) / this.totalLength;
                double subScore = (t2.getPoint(j).euclidean(s1.getBox(i)) + t2.getPoint(j - 1).euclidean(s1.getBox(i - 1))) * diagCoverage;
                double diagScore = this.matrix.value[i - 1][j - 1] + subScore;
                if (diagScore <= colSpatialScore && diagScore <= rowSpatialScore) {
                    this.matrix.add(i, j, diagScore, 1, t2.getPoint(j - 1), s1.getBox(i - 1));
                    this.matrix.delta[i][j] = diagScore - this.matrix.value[i - 1][j - 1];
                } else if (colSpatialScore < rowSpatialScore || colSpatialScore == rowSpatialScore && t2.edges.size() > s1.edges.size()) {
                    this.matrix.add(i, j, colSpatialScore, 2, t2.getPoint(j - 1), t1Insert);
                    this.matrix.delta[i][j] = colSpatialScore - colDelta;
                } else {
                    this.matrix.add(i, j, rowSpatialScore, 3, t2Insert, s1.getBox(i - 1));
                    this.matrix.delta[i][j] = rowSpatialScore - rowDelta;
                }
                ++j;
            }
            ++i;
        }
        i = this.matrix.numRows() - 1;
        j = this.matrix.numCols() - 1;
        ArrayList e1 = new ArrayList();
        ArrayList e2 = new ArrayList();
        double[] answer = new double[]{this.matrix.score(), this.time};
        return answer;
    }

    public double getSubDistance(Box b, Trajectory t) {
        double d = 0.0;
        STpoint s = t.edges.get((int)0).p1;
        STpoint e = t.edges.get((int)(t.edges.size() - 1)).p2;
        double recLength = Math.max(Math.abs(s.x - e.x), Math.abs(s.y - e.y));
        int i = 0;
        while (i < t.edges.size()) {
            STpoint p1 = t.edges.get((int)i).p1;
            STpoint p2 = t.edges.get((int)i).p2;
            double d1 = b.euclidean(p1);
            double d2 = b.euclidean(p2);
            double minX = p1.x < p2.x ? p1.x : p2.x;
            double maxX = p1.x > p2.x ? p1.x : p2.x;
            double minY = p1.y < p2.y ? p1.y : p2.y;
            double maxY = p1.y > p2.y ? p1.y : p2.y;
            double recEdge = Math.min(maxX - minX, maxY - minY);
            Box b1 = new Box(minX, minY, maxX, maxY);
            d = d1 == 0.0 || d2 == 0.0 || b1.euclidean(b) == 0.0 ? (d += (d1 + d2) * Math.min(t.edgeLength(i), recEdge) / (t.length + recLength)) : (d += (d1 + d2) * t.edgeLength(i) / (t.length + recLength));
            ++i;
        }
        return d > 0.0 ? d : 0.0;
    }

    public double[] getSubDistance(Summary s1, Trajectory t2) {
        int j;
        this.matrix = new Matrix(s1.edges.size() + 1, t2.edges.size() + 1);
        this.subInitializeMatrix();
        this.totalLength = s1.length + t2.length;
        this.time = 0.0;
        int i = 1;
        while (i < this.matrix.numRows()) {
            j = 1;
            while (j < this.matrix.numCols()) {
                double prevPointEdge;
                double rowDelta = Double.MAX_VALUE;
                double colDelta = Double.MAX_VALUE;
                double rowCoverage1 = Double.MAX_VALUE;
                double rowCoverage2 = Double.MAX_VALUE;
                double colCoverage1 = Double.MAX_VALUE;
                double colCoverage2 = Double.MAX_VALUE;
                double rowSpatialScore = Double.MAX_VALUE;
                double colSpatialScore = Double.MAX_VALUE;
                STpoint t2Insert = null;
                Box t1Insert = null;
                Point t2Edit = null;
                Point t1Edit = null;
                if (i > 1) {
                    t1Edit = this.matrix.rowEdits[i - 1][j];
                    t2Edit = this.matrix.colEdits[i - 1][j];
                    prevPointEdge = t1Edit.euclidean(s1.getBox(i - 1));
                    t2Insert = this.lineMap(t2Edit, t2.getPoint(j), s1.getBox(i - 1));
                    double rowEditDistance = t2Insert.euclidean(s1.getBox(i - 1));
                    double rowEditEdge = t2Edit.euclidean(t2Insert);
                    rowCoverage1 = (rowEditEdge + prevPointEdge) / this.totalLength;
                    rowCoverage2 = (t2.getPoint(j).euclidean(t2Insert) + s1.edgeLength(i - 1)) / this.totalLength;
                    rowDelta = this.matrix.value[i - 1][j] - this.matrix.delta[i - 1][j] + (rowEditDistance + t1Edit.euclidean(t2Edit)) * rowCoverage1;
                    rowSpatialScore = rowDelta + (rowEditDistance + t2.getPoint(j).euclidean(s1.getBox(i))) * rowCoverage2;
                }
                if (j > 1) {
                    t1Edit = this.matrix.rowEdits[i][j - 1];
                    t2Edit = this.matrix.colEdits[i][j - 1];
                    prevPointEdge = t2Edit.euclidean(t2.getPoint(j - 1));
                    t1Insert = this.lineMap((Box)t1Edit, s1.getBox(i), t2.getPoint(j - 1));
                    double colEditDistance = t1Insert.euclidean(t2.getPoint(j - 1));
                    double colEditEdge = t1Edit.euclidean(t1Insert);
                    colCoverage1 = (colEditEdge + prevPointEdge) / this.totalLength;
                    colCoverage2 = (s1.getBox(i).euclidean(t1Insert) + t2.edgeLength(j - 1)) / this.totalLength;
                    colDelta = this.matrix.value[i][j - 1] - this.matrix.delta[i][j - 1] + (colEditDistance + t1Edit.euclidean(t2Edit)) * colCoverage1;
                    colSpatialScore = colDelta + (colEditDistance + s1.getBox(i).euclidean(t2.getPoint(j))) * colCoverage2;
                }
                double diagCoverage = (s1.edgeLength(i - 1) + t2.edgeLength(j - 1)) / this.totalLength;
                double subScore = (t2.getPoint(j).euclidean(s1.getBox(i)) + t2.getPoint(j - 1).euclidean(s1.getBox(i - 1))) * diagCoverage;
                double diagScore = this.matrix.value[i - 1][j - 1] + subScore;
                if (diagScore <= colSpatialScore && diagScore <= rowSpatialScore) {
                    this.matrix.add(i, j, diagScore, 1, t2.getPoint(j - 1), s1.getBox(i - 1));
                    this.matrix.delta[i][j] = diagScore - this.matrix.value[i - 1][j - 1];
                } else if (colSpatialScore < rowSpatialScore || colSpatialScore == rowSpatialScore && t2.edges.size() > s1.edges.size()) {
                    this.matrix.add(i, j, colSpatialScore, 2, t2.getPoint(j - 1), t1Insert);
                    this.matrix.delta[i][j] = colSpatialScore - colDelta;
                } else {
                    this.matrix.add(i, j, rowSpatialScore, 3, t2Insert, s1.getBox(i - 1));
                    this.matrix.delta[i][j] = rowSpatialScore - rowDelta;
                }
                ++j;
            }
            ++i;
        }
        i = this.matrix.numRows() - 1;
        j = this.matrix.numCols() - 1;
        ArrayList e1 = new ArrayList();
        ArrayList e2 = new ArrayList();
        double[] answer = new double[]{this.matrix.subScore(), this.time};
        return answer;
    }

    private void subInitializeMatrix() {
        int i = 0;
        while (i < this.matrix.value.length) {
            this.matrix.value[i][0] = 0.0;
            ++i;
        }
        int j = 1;
        while (j < this.matrix.value[0].length) {
            this.matrix.value[0][j] = Double.MAX_VALUE;
            ++j;
        }
        this.matrix.value[0][0] = 0.0;
    }

    private Box lineMap(Box b1, Box b2, STpoint p) {
        if (p.inBox(b1)) {
            return new Box(b1);
        }
        if (p.inBox(b2)) {
            return new Box(b2);
        }
        if (p.x < b1.x1 && p.x < b2.x1) {
            if (b1.x1 < b2.x1) {
                if (b1.y1 < b2.y1) {
                    return new Box(this.lineMap(b1.p3(), b2.p3(), p), this.lineMap(b1.p2(), b2.p2(), p));
                }
                return new Box(this.lineMap(b1.p4(), b2.p4(), p), this.lineMap(b1.p1(), b2.p1(), p));
            }
            if (b1.y1 < b2.y1) {
                return new Box(this.lineMap(b1.p4(), b2.p4(), p), this.lineMap(b1.p1(), b2.p1(), p));
            }
            return new Box(this.lineMap(b1.p3(), b2.p3(), p), this.lineMap(b1.p2(), b2.p2(), p));
        }
        if (p.x > b1.x2 && p.x > b2.x2) {
            if (b1.x2 > b2.x2) {
                if (b1.y1 < b2.y1) {
                    return new Box(this.lineMap(b1.p1(), b2.p1(), p), this.lineMap(b1.p4(), b2.p4(), p));
                }
                return new Box(this.lineMap(b1.p2(), b2.p2(), p), this.lineMap(b1.p3(), b2.p3(), p));
            }
            if (b1.y1 < b2.y1) {
                return new Box(this.lineMap(b1.p2(), b2.p2(), p), this.lineMap(b1.p3(), b2.p3(), p));
            }
            return new Box(this.lineMap(b1.p1(), b2.p1(), p), this.lineMap(b1.p4(), b2.p4(), p));
        }
        if (p.y <= b1.y1 && p.x <= b2.y1) {
            if (b1.y1 < b2.y1) {
                return new Box(b1);
            }
            return new Box(b2);
        }
        if (p.y >= b1.y2 && p.x >= b2.y2) {
            if (b1.y1 > b2.y1) {
                return new Box(b1);
            }
            return new Box(b2);
        }
        return new Box(p);
    }

    public STpoint intersect(STpoint p1, STpoint p2, STpoint p3, STpoint p4) {
        double d = (p1.x - p2.x) * (p3.y - p4.y) - (p1.y - p2.y) * (p3.x - p4.x);
        if (d == 0.0) {
            return this.lineMap(p1, p2, p3);
        }
        double xi = ((p3.x - p4.x) * (p1.x * p2.y - p1.y * p2.x) - (p1.x - p2.x) * (p3.x * p4.y - p3.y * p4.x)) / d;
        double yi = ((p3.y - p4.y) * (p1.x * p2.y - p1.y * p2.x) - (p1.y - p2.y) * (p3.x * p4.y - p3.y * p4.x)) / d;
        return new STpoint(xi, yi, -1.0);
    }

    private void initializeMatrix() {
        int i = 1;
        while (i < this.matrix.value.length) {
            this.matrix.value[i][0] = Double.MAX_VALUE;
            ++i;
        }
        int j = 1;
        while (j < this.matrix.value[0].length) {
            this.matrix.value[0][j] = Double.MAX_VALUE;
            ++j;
        }
        this.matrix.value[0][0] = 0.0;
    }

    @Override
    public void printPath() {
        this.matrix.printPath();
    }

    private void computeTemporalScore(Trajectory t1, Trajectory t2, int i, int j, ArrayList<Edge> e1, ArrayList<Edge> e2) {
        if (i == 0 || j == 0) {
            return;
        }
        if (Launch.DEBUG) {
            System.out.println("Time: " + this.time + " Parent: " + this.matrix.parent[i][j] + " Score: " + this.matrix.value[i][j] + " Delta: " + this.matrix.delta[i][j]);
        }
        if (this.matrix.parent[i][j] == 1) {
            if (e1.size() > 0) {
                e1.add(new Edge(t1.getEdge((int)(i - 1)).p1, e1.get((int)(e1.size() - 1)).p1));
                e2.add(new Edge(t2.getEdge((int)(j - 1)).p1, e2.get((int)(e2.size() - 1)).p1));
            } else {
                e1.add(t1.getEdge(i - 1));
                e2.add(t2.getEdge(j - 1));
            }
            --i;
            --j;
        } else if (this.matrix.parent[i][j] == 2) {
            STpoint insert = (STpoint)this.matrix.rowEdits[i][j];
            Edge e = null;
            e = e1.size() > 0 ? new Edge(insert, e1.get((int)(e1.size() - 1)).p1) : new Edge(insert, t1.getEdge((int)(i - 1)).p2);
            e1.add(e);
            if (e2.size() > 0) {
                e2.add(new Edge(t2.getEdge((int)(j - 1)).p1, e2.get((int)(e2.size() - 1)).p1));
            } else {
                e2.add(t2.getEdge(j - 1));
            }
            --j;
        } else {
            STpoint insert = (STpoint)this.matrix.colEdits[i][j];
            Edge e = null;
            e = e2.size() > 0 ? new Edge(insert, e2.get((int)(e2.size() - 1)).p1) : new Edge(insert, t2.getEdge((int)(j - 1)).p2);
            e2.add(e);
            if (e1.size() > 0) {
                e1.add(new Edge(t1.getEdge((int)(i - 1)).p1, e1.get((int)(e1.size() - 1)).p1));
            } else {
                e1.add(t1.getEdge(i - 1));
            }
            --i;
        }
        this.time += this.temporalDistance(e1.get(e1.size() - 1), e2.get(e2.size() - 1));
        if (Launch.DEBUG) {
            System.out.println(e1.get(e1.size() - 1));
            System.out.println(e2.get(e2.size() - 1));
        }
        this.computeTemporalScore(t1, t2, i, j, e1, e2);
    }

    private double temporalDistance(Edge e1, Edge e2) {
        return Math.abs(e1.speed - e2.speed) * (e1.length + e2.length) / this.totalLength;
    }

    public STpoint lineMap(STpoint point1, STpoint point2, STpoint point) {
        double l2 = (point1.x - point2.x) * (point1.x - point2.x) + (point1.y - point2.y) * (point1.y - point2.y);
        if (l2 == 0.0) {
            return new STpoint(point.x, point.y, point.time);
        }
        double t = ((point.x - point1.x) * (point2.x - point1.x) + (point.y - point1.y) * (point2.y - point1.y)) / l2;
        if (t < 0.0) {
            return new STpoint(point1.x, point1.y, point1.time);
        }
        if (t > 1.0) {
            return new STpoint(point2.x, point2.y, point2.time);
        }
        return new STpoint(point1.x + t * (point2.x - point1.x), point1.y + t * (point2.y - point1.y), point1.time);
    }

    private STpoint lineMap(Point t2Edit1, STpoint p2, Box box) {
        double minv;
        double maxv;
        STpoint min;
        STpoint t2Edit = (STpoint)t2Edit1;
        double a = t2Edit.y - p2.y;
        double b = p2.x - t2Edit.x;
        double c = t2Edit.x * p2.y - p2.x * t2Edit.y;
        double s1 = a * box.x1 + b * box.y1 + c;
        double s2 = a * box.x1 + b * box.y2 + c;
        double s3 = a * box.x2 + b * box.y1 + c;
        double s4 = a * box.x2 + b * box.y2 + c;
        if (s1 <= s2 && s1 <= s3 && s1 <= s4) {
            min = box.p1();
            minv = s1;
        } else if (s2 <= s3 && s2 <= 4.0) {
            min = box.p2();
            minv = s2;
        } else if (s3 <= s4) {
            min = box.p3();
            minv = s3;
        } else {
            min = box.p4();
            minv = s4;
        }
        if (s1 >= s2 && s1 >= s3 && s1 >= s4) {
            STpoint max = box.p1();
            maxv = s1;
        } else if (s2 >= s3 && s2 >= 4.0) {
            STpoint max = box.p2();
            maxv = s2;
        } else if (s3 >= s4) {
            STpoint max = box.p3();
            maxv = s3;
        } else {
            STpoint max = box.p4();
            maxv = s4;
        }
        if (maxv < 0.0 || minv > 0.0) {
            double slope = (p2.y - t2Edit.y) / (p2.x - t2Edit.x) * box.width / 2.0;
            if ((- box.height) / 2.0 <= slope && slope <= box.height / 2.0) {
                if (t2Edit.x > p2.x) {
                    return this.intersect(t2Edit, p2, box.p3(), box.p4());
                }
                return this.intersect(t2Edit, p2, box.p1(), box.p2());
            }
            if (t2Edit.y > p2.y) {
                return this.intersect(t2Edit, p2, box.p1(), box.p3());
            }
            return this.intersect(t2Edit, p2, box.p2(), box.p4());
        }
        return this.lineMap(t2Edit, p2, min);
    }

//    public static void main(String[] args) throws NumberFormatException, IOException {
//        ArrayList<Trajectory> trajectories = Launch.readTrajectories("EDwP-data.txt", 0);
//        EditDistance d = new EditDistance();
//        System.out.println(Arrays.toString(d.getDistance(trajectories.get(0), trajectories.get(1))));
//        System.out.println(Arrays.toString(d.getDistance(trajectories.get(2), trajectories.get(0))));
//        d.matrix.print();
//    }
    
    public static void main(String[] args) throws NumberFormatException, IOException {
        ArrayList<Trajectory> trajectories = Launch.readTrajectories("filename", 0);
        System.out.println(" there are total " + (trajectories.size()) + " trajectories");
        
        System.out.println(trajectories.get(0)); //[(1.0,9.0,0.0);(2.0,2.0,2.0);(3.0,4.0,3.0);(4.0,5.0,3.5);
        long begin = System.currentTimeMillis();
        EditDistance d = new EditDistance( );
        
        int i = 0;
        while(i < 10) 	
        {
        	for(int j =0 ; j <89;j++)
        		{
        			System.out.println(d.getDistance(trajectories.get(i), trajectories.get(j))[0]);
        			
        		}
        	i ++;
        }	
        System.out.println(d.getDistance(trajectories.get(0), trajectories.get(2))[0]);
        long end = System.currentTimeMillis();
        System.out.println("duration£º" + (end - begin));
        //d.matrix.print();
    }
}

