/*
 * Decompiled with CFR 0_114.
 * 
 * Could not load the following classes:
 *  org.apache.commons.cli.CommandLine
 *  org.apache.commons.cli.GnuParser
 *  org.apache.commons.cli.Options
 *  org.apache.commons.cli.ParseException
 */
package IO;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CmdParser {
    private CommandLine cmd;

    public CmdParser(String[] args) {
        Options options = new Options();
        options.addOption("d", "debug", false, "Debug Mode");
        options.addOption("sThresh", true, "Spatial distance threshold for mappability of regions in LCSS");
        options.addOption("l", "minLength", true, "Minimum trajectory Length");
        options.addOption("f", "filename", true, "filename of the trajetcory database");
        options.addOption("n", "noise", true, "Correlation with noise addition");
        options.addOption("p", "perturbation", true, "Perturbation threshold for noise addition in minutes");
        options.addOption("r", "correlation", false, "Correlation between the distance functions");
        options.addOption("v", "visual", false, "visual comparison of top-1 between distance rankers");
        options.addOption("k", true, "k value for top-k");
        options.addOption("bf", true, "branching factor");
        options.addOption("vp", true, "number of vantage points");
        options.addOption("s", "sample", true, "sample size");
        options.addOption("compute", false, "Compute Distance between given indices");
        options.addOption("symmetry", false, "Test symmetricity of the database");
        options.addOption("plot", false, "Plot top 1 for a random query trajectory");
        options.addOption("t1", true, "ID 1");
        options.addOption("t2", true, "ID 1");
        GnuParser parser = new GnuParser();
        try {
            this.cmd = parser.parse(options, args);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public boolean hasOption(String s) {
        return this.cmd.hasOption(s);
    }

    public String getString(String s) {
        return this.cmd.getOptionValue(s);
    }

    public int getInteger(String s) {
        return new Integer(this.cmd.getOptionValue(s));
    }

    public int getInteger(String s, int def) {
        if (this.cmd.hasOption(s)) {
            return new Integer(this.cmd.getOptionValue(s));
        }
        return def;
    }

    public double getDouble(String s) {
        return new Double(this.cmd.getOptionValue(s));
    }

    public double getDouble(String s, double def) {
        if (this.cmd.hasOption(s)) {
            return new Double(this.cmd.getOptionValue(s));
        }
        return def;
    }
}

