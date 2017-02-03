import trajectory.Trajectory;
import java.io.*;
import java.util.ArrayList;
import distanceRankers.*;
import Launcher.Launch;

class EDwP {
	public static void main(String[] args) throws IOException {
		ArrayList<Trajectory> trajectories = Launch.readTrajectories("EDwP-data.txt", 0);
		TrajectoryDistance d = new EditDistance(false);
		d = new EDR(0, 10.0);
		System.out.println(d.getDistance(trajectories.get(0), trajectories.get(2))[0]);
	}
}
