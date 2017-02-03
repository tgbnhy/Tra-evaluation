/*
 * Decompiled with CFR 0_114.
 * 
 * Could not load the following classes:
 *  org.apache.commons.math3.linear.BlockRealMatrix
 *  org.apache.commons.math3.linear.RealMatrix
 *  org.apache.commons.math3.stat.correlation.SpearmansCorrelation
 */
package Testers;

import distanceRankers.TrajectoryDistance;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import query.Topk;
import query.TrajScore;
import trajectory.Trajectory;

public class CompareDistanceRankers {
    TrajectoryDistance[] distanceRankers;
    HashSet<Integer> dict;
    HashMap<Integer, double[]> alldict;

    public CompareDistanceRankers(TrajectoryDistance[] distanceRankers) {
        this.distanceRankers = distanceRankers;
        this.dict = new HashSet();
        this.alldict = new HashMap();
    }

    public CompareDistanceRankers() {
        this.distanceRankers = new TrajectoryDistance[2];
        this.dict = new HashSet();
        this.alldict = new HashMap();
    }

    public void compareDistance(Trajectory t1, Trajectory t2) {
        double[][] scores = new double[this.distanceRankers.length][2];
        int i = 0;
        System.out.println("-----------------------");
        TrajectoryDistance[] arrtrajectoryDistance = this.distanceRankers;
        int n = arrtrajectoryDistance.length;
        int n2 = 0;
        while (n2 < n) {
            TrajectoryDistance d = arrtrajectoryDistance[n2];
            scores[i] = d.getDistance(t1, t2);
            System.out.println(String.valueOf(d.getName()) + ": " + Arrays.toString(scores[i]));
            ++n2;
        }
        System.out.println("-----------------------");
    }

    public RealMatrix correlationBetweenDistanceRankers(Trajectory query, ArrayList<Trajectory> db, int k) {
        int i = 0;
        Topk tk = new Topk(db);
        TrajectoryDistance[] arrtrajectoryDistance = this.distanceRankers;
        int n = arrtrajectoryDistance.length;
        int n2 = 0;
        while (n2 < n) {
            TrajectoryDistance d = arrtrajectoryDistance[n2];
            this.addtoDictionary(tk.getTopK(query, d, db.size()), i++, k, this.distanceRankers.length);
            ++n2;
        }
        RealMatrix r = this.getRankMatrix();
        SpearmansCorrelation sc = new SpearmansCorrelation(r);
        return sc.computeCorrelationMatrix(r);
    }

    public double[] correlationWithNoise(Trajectory query, ArrayList<Trajectory> db, ArrayList<Trajectory> noisydb, int k) {
        int i = 0;
        double[] corr = new double[this.distanceRankers.length];
        Topk tk = new Topk(db);
        Topk noisytk = new Topk(noisydb);
        TrajectoryDistance[] arrtrajectoryDistance = this.distanceRankers;
        int n = arrtrajectoryDistance.length;
        int n2 = 0;
        while (n2 < n) {
            TrajectoryDistance d = arrtrajectoryDistance[n2];
            TrajScore[] topk = tk.getTopK(query, d, db.size());
            TrajScore[] noisyTopK = noisytk.getTopK(query, d, db.size());
            this.addtoDictionary(topk, 0, k, this.distanceRankers.length);
            this.addtoDictionary(noisyTopK, 1, k, this.distanceRankers.length);
            RealMatrix r = this.getRankMatrix();
            SpearmansCorrelation sc = new SpearmansCorrelation(r);
            corr[i++] = sc.computeCorrelationMatrix(r).getEntry(0, 1);
            this.dict.clear();
            this.alldict.clear();
            ++n2;
        }
        return corr;
    }

    public RealMatrix getRankMatrix() {
        BlockRealMatrix r = new BlockRealMatrix(this.dict.size(), this.distanceRankers.length);
        int i = 0;
        Iterator<Integer> iterator = this.dict.iterator();
        while (iterator.hasNext()) {
            int key = iterator.next();
            r.setRow(i++, this.alldict.get(key));
        }
        return r;
    }

    public void addtoDictionary(TrajScore[] topK, int col, int k, int size) {
        int i = 1;
        while (i < topK.length) {
            double[] rank;
            if (i <= k) {
                this.dict.add(topK[i].index);
            }
            if (this.alldict.containsKey(topK[i].index)) {
                rank = this.alldict.get(topK[i].index);
                rank[col] = i;
                this.alldict.put(topK[i].index, rank);
            } else {
                rank = new double[size];
                rank[col] = i;
                this.alldict.put(topK[i].index, rank);
            }
            ++i;
        }
    }
}

