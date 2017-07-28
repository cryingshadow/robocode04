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

	public OtherBot(String name, long lastSeenTime, double energy) {
		this.name = name;
		this.lastSeenTime = lastSeenTime;
		this.energy = energy;
	}

	public static OtherBot fromScannedRobotEvent(final ScannedRobotEvent event) {
		return new OtherBot(event.getName(), event.getTime(), event.getEnergy());
	}
	
	@Override
	public String toString() {
		return "BotInfo [name=" + name + ", lastSeenTime=" + lastSeenTime + ", energy=" + energy + "]";
	}

}
