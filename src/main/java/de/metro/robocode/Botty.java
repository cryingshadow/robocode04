package de.metro.robocode;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import robocode.HitByBulletEvent;
import robocode.Robot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

public class Botty extends Robot {

	private static final String INTERNAL_VERSION = "v1626";

	private static final Color METRO_BLUE = new Color(0, 61, 122);
	private static final Color METRO_YELLOW = new Color(255, 229, 0);

	private final Map<String, OtherBot> knownBots = new ConcurrentHashMap<String, OtherBot>();
	private OtherBot target = null;

	private BotState state = BotState.PREPARING;

	private final Random RANDOM = new Random();

	private static final int CEASE_FIRE_THRESHOLD = 100;
	private static final int SWITCH_TARGET_THRESHOLD = 200;
	private static final int FORGET_TARGET_THRESHOLD = 300;
	private static final int CLEANUP_INTERVAL = 100;
	private static final int BORDER_THRESHOLD = 20;
	
	private long lastCleanUpTick = 0;

	@Override
	public void run() {
		say("Hello there! My name is Botty (" + INTERNAL_VERSION + ") and I'm going to kill you :-)");
		paintBot();
		initialSetup();

		while (true) {
			switch (state) {
			case PREPARING:
				handlePreparingState();
				break;
			case SEARCHING:
				handleSearchingState();
				break;
			case DESTROYING:
				handleDestroyingState();
				break;
			}

			cleanUp();
		}
	}

	private void paintBot() {
		setBodyColor(METRO_BLUE);
		setGunColor(METRO_YELLOW);
		setRadarColor(METRO_BLUE);
		setScanColor(Color.white);
		setBulletColor(METRO_YELLOW);
	}

	private synchronized void cleanUp() {
		final long delta = getTime() - lastCleanUpTick;

		if (delta > CLEANUP_INTERVAL) {
			final List<String> names = new LinkedList<String>();
			for (final OtherBot bot : knownBots.values()) {
				if (getTime() - bot.lastSeenTime > FORGET_TARGET_THRESHOLD) {
					names.add(bot.name);
				}
			}

			for (final String name : names) {
				knownBots.remove(name);
			}
		}
	}

	private void initialSetup() {
		turnLeft(1);
	}

	private synchronized Optional<OtherBot> getNextTarget() {
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
			say("I'm off to kill " + this.target.name);
			changeState(BotState.DESTROYING);
		} else {
			// SEARCH!
			ahead(RANDOM.nextDouble() * 400);
			turnRadarLeft(90);
			turnRadarRight(90);

			if (getY() < BORDER_THRESHOLD || getY() > getBattleFieldHeight() - BORDER_THRESHOLD) {
				turnLeft(RANDOM.nextInt(90) + 1);
			}

			if (getX() < BORDER_THRESHOLD || getX() > getBattleFieldWidth() - BORDER_THRESHOLD) {
				turnLeft(RANDOM.nextInt(90) + 1);
			}
		}
	}

	private void handleDestroyingState() {
		// check if still valid target
		final long delta = getTime() - target.lastSeenTime;
		if (delta > SWITCH_TARGET_THRESHOLD) {
			say("I think I'll give up chasing " + target.name);
			changeState(BotState.SEARCHING);
		} else {
			// turn to target
			if (target.bearing > 0) {
				turnRight(2);
			} else {
				turnLeft(-2);
			}

			turnRadarLeft(45);
			turnRadarRight(45);

			if (target.bearing <= 3) {
				// move to target location
				ahead(RANDOM.nextDouble() * 100 + 1.0);
			}

			if (delta < CEASE_FIRE_THRESHOLD) {
				fire(1.5);
			}
		}
	}

	public synchronized void onScannedRobot(ScannedRobotEvent e) {
		final OtherBot otherBot = OtherBot.fromScannedRobotEvent(e);

		knownBots.put(otherBot.name, otherBot);

		if (target != null && otherBot.name.equals(target.name)) {
			say("updated info on target");
			target = otherBot;
		}
	}

	public synchronized void onRobotDeath(RobotDeathEvent event) {
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

	public synchronized void onHitByBullet(HitByBulletEvent e) {
	}

	private synchronized void say(final String message) {
		System.out.println(message);
	}
}
