/*
 * Decompiled with CFR 0_114.
 */
package Utilities;

import IO.FileRead;
import IO.FileWrite;
import java.io.IOException;
import java.util.ArrayList;
import trajectory.Edge;
import trajectory.STpoint;
import trajectory.Trajectory;

public class PyPlot {
    String template;
    int count;

    public PyPlot(String fileName) {
        try {
            this.template = FileRead.readFile(fileName);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        this.count = 0;
    }

    public void addTrajectory(Trajectory t, String code, String label) {
        String x = "";
        String y = "";
        int i = 0;
        while (i < t.edges.size() + 1) {
            x = String.valueOf(x) + t.getPoint((int)i).x + ",";
            y = String.valueOf(y) + t.getPoint((int)i).y + ",";
            ++i;
        }
        x = "[" + x + "]";
        y = "[" + y + "]";
        this.template = String.valueOf(this.template) + "x" + this.count + "=" + x + "\n";
        this.template = String.valueOf(this.template) + "y" + this.count + "=" + y + "\n";
        this.template = String.valueOf(this.template) + "plt.plot(x" + this.count + ", y" + this.count + ", \"" + code + "-\", label=\"" + label + "\")\n";
        ++this.count;
    }

    public void addTrajectory(Trajectory t) {
        String x = "";
        String y = "";
        int i = 0;
        while (i < t.edges.size() + 1) {
            x = String.valueOf(x) + t.getPoint((int)i).x + ",";
            y = String.valueOf(y) + t.getPoint((int)i).y + ",";
            ++i;
        }
        x = "[" + x + "]";
        y = "[" + y + "]";
        this.template = String.valueOf(this.template) + "x" + this.count + "=" + x + "\n";
        this.template = String.valueOf(this.template) + "y" + this.count + "=" + y + "\n";
        this.template = String.valueOf(this.template) + "plt.plot(x" + this.count + ", y" + this.count + ", label=\"" + this.count + "\")\n";
        ++this.count;
    }

    public void plotTrajectories(ArrayList<Trajectory> trajectories) {
        int i = 0;
        while (i < trajectories.size()) {
            this.addTrajectory(trajectories.get(i));
            ++i;
        }
        this.plot();
    }

    public void plot() {
        this.template = String.valueOf(this.template) + "plt.legend()\n";
        this.template = String.valueOf(this.template) + "plt.show()\n";
        try {
            FileWrite.write("pyplot.py", this.template);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Runtime.getRuntime().exec("python pyplot.py");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

