/*
 * Decompiled with CFR 0_114.
 */
package query;

import distanceRankers.TrajectoryDistance;
import java.util.ArrayList;
import java.util.PriorityQueue;
import query.TrajScore;
import trajectory.Trajectory;

public class Topk {
    ArrayList<Trajectory> db;

    public Topk(ArrayList<Trajectory> db) {
        this.db = db;
    }

    public TrajScore[] getTopK(Trajectory query, TrajectoryDistance d, int k) {
        PriorityQueue<TrajScore> pq = new PriorityQueue<TrajScore>(k + 1);
        int i = 0;
        for (Trajectory t : this.db) {
            pq.add(new TrajScore(i++, d.getDistance(query, t)));
            if (pq.size() <= k) continue;
            pq.poll();
        }
        TrajScore[] a = new TrajScore[k];
        i = k - 1;
        while (!pq.isEmpty()) {
            a[i--] = (TrajScore)pq.poll();
        }
        return a;
    }
}

