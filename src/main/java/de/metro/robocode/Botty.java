package de.metro.robocode;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import robocode.HitByBulletEvent;
import robocode.Robot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

public class Botty extends Robot {

	private static final Color METRO_BLUE = new Color(0, 61, 122);
	private static final Color METRO_YELLOW = new Color(255, 229, 0);

	private Map<String, OtherBot> knownBots = new HashMap<String, OtherBot>();
	private OtherBot target = null;

	private BotState state = BotState.PREPARING;

	private final Random RANDOM = new Random();

	private static final int SWITCH_TARGET_THRESHOLD = 30;
	
	@Override
	public void run() {
		say("Hello there! My name is Botty and I'm going to kill you :-)");
		paintBot();
		initialSetup();

		while (true) {
			switch (state) {
			case PREPARING:
				handlePreparingState();
			case SEARCHING:
				handleSearchingState();
			case DESTROYING:
				handleDestroyingState();
			}
		}
	}

	private void paintBot() {
		setBodyColor(METRO_BLUE);
		setGunColor(METRO_YELLOW);
		setRadarColor(METRO_BLUE);
		setScanColor(Color.white);
		setBulletColor(METRO_YELLOW);
	}

	private void initialSetup() {
		turnLeft(1);
	}

	private Optional<OtherBot> getNextTarget() {
		if (knownBots.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(knownBots.values().iterator().next());
	}

	private void handlePreparingState() {
		changeState(BotState.SEARCHING);
	}

	private void handleSearchingState() {
		final Optional<OtherBot> possibleNextTarget = getNextTarget();

		if (possibleNextTarget.isPresent()) {
			this.target = possibleNextTarget.get();
			changeState(BotState.DESTROYING);
			say("I'm off to kill " + this.target.name);
		} else {
			// SEARCH!
			fire(1);	
			ahead(RANDOM.nextDouble() * 30);
			turnLeft(90);
			fire(1);	
		}
	}

	private void handleDestroyingState() {
		// check if still valid target
		if(target.lastSeenTime > SWITCH_TARGET_THRESHOLD) {
			say("I think I'll give up chasing " + target.name);
			changeState(BotState.SEARCHING);
		} else {
			// turn to target
			
			// move to target location		
			ahead(RANDOM.nextDouble() * 15);
			turnLeft(45);
			fire(1);	
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		final OtherBot otherBot = OtherBot.fromScannedRobotEvent(e);

		knownBots.put(otherBot.name, otherBot);

		if (target != null && target.name.equals(otherBot)) {
			target = otherBot;
		}
	}

	public void onRobotDeath(RobotDeathEvent event) {
		final String name = event.getName();

		if (knownBots.containsKey(name)) {
			knownBots.remove(knownBots.get(name));
		}

		if (target != null && target.name.equals(name)) {
			this.target = null;
			changeState(BotState.SEARCHING);
			say(name + " is dead. What a welcome sight!");
		}
	}

	private void changeState(final BotState state) {
		say(String.format("Switching state: %s -> %s", this.state, state));
		this.state = state;
	}

	public void onHitByBullet(HitByBulletEvent e) {
	}

	private void say(final String message) {
		System.out.println(message);
	}
}
