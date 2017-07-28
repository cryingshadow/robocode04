package de.metro.robocode;

import robocode.ScannedRobotEvent;

/**
 * @author martin
 *
 */
public class OtherBot {

	public final String name;
	public final long lastSeenTime;
	public final double energy;
	public final double bearing;
	public final double distance;

	public OtherBot(String name, long lastSeenTime, double energy, double bearing, double distance) {
		this.name = name;
		this.lastSeenTime = lastSeenTime;
		this.energy = energy;
		this.bearing = bearing;
		this.distance = distance;
	}

	public static OtherBot fromScannedRobotEvent(final ScannedRobotEvent event) {
		return new OtherBot(event.getName(), event.getTime(), event.getEnergy(), event.getBearing(), event.getDistance());
	}

	@Override
	public String toString() {
		return "BotInfo [name=" + name + ", lastSeenTime=" + lastSeenTime + ", energy=" + energy + "]";
	}

}
