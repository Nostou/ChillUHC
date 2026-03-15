package fr.Nosta.ChillUHC.Managers;

import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Tasks.WorldBorderTask;
import org.bukkit.World;
import org.bukkit.WorldBorder;

public class BorderManager {

    private static final int DEFAULT_START_RADIUS = 1000;
    private static final int DEFAULT_TARGET_RADIUS = 50;
    private static final long DEFAULT_MEETUP_DURATION_SECONDS = 3600;
    private static final long DEFAULT_SHRINK_DURATION_SECONDS = 1800;
    private static final int BORDER_WARNING_DISTANCE = 0;
    private static final long BORDER_DIAMETER_MULTIPLIER = 2L;

    private final Main plugin;

    private WorldBorder worldBorder;
    private WorldBorderTask activeTask;

    private int startRadius = DEFAULT_START_RADIUS;
    public int getStartRadius() { return this.startRadius; }
    public int getCurrentRadius() { return (int)worldBorder.getSize()/2; }

    private int targetRadius = DEFAULT_TARGET_RADIUS;
    public int getTargetRadius() { return this.targetRadius; }
    public void setTargetRadius(int inRadius) { this.targetRadius = inRadius; }

    private long meetupDuration = DEFAULT_MEETUP_DURATION_SECONDS;
    public long getMeetupDuration() { return this.meetupDuration; }
    public void setMeetupDuration(long inDuration) { this.meetupDuration = inDuration; }

    private long shrinkDuration = DEFAULT_SHRINK_DURATION_SECONDS;
    public long getShrinkDuration() { return this.shrinkDuration; }
    public void setShrinkDuration(long inDuration) { this.shrinkDuration = inDuration; }

    private long meetupEndTimestamp;
    public long getMeetupEnd() {
        return Math.max(0, (meetupEndTimestamp - System.currentTimeMillis()) / 1000);
    }

    public BorderManager(Main plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        World world = plugin.getWorld();
        worldBorder = world.getWorldBorder();
        worldBorder.setWarningDistance(BORDER_WARNING_DISTANCE);
        worldBorder.setCenter(world.getSpawnLocation());
        worldBorder.setSize(startRadius * BORDER_DIAMETER_MULTIPLIER);
    }

    public void reset() {
        cancelShrink();
        worldBorder.setSize(startRadius * BORDER_DIAMETER_MULTIPLIER);
    }

    public void setStartRadius(int radius) {
        startRadius = radius;
        worldBorder.setSize(radius * BORDER_DIAMETER_MULTIPLIER);
    }

    public void changeRadius() {
        worldBorder.changeSize(targetRadius * BORDER_DIAMETER_MULTIPLIER, shrinkDuration * 20L);
    }

    public boolean isShrinking() { return worldBorder.getSize() < startRadius * BORDER_DIAMETER_MULTIPLIER; }

    public void startShrink() {
        cancelShrink();
        meetupEndTimestamp = System.currentTimeMillis() + (meetupDuration * 1000);
        activeTask = new WorldBorderTask(plugin, this);
        activeTask.start();
        activeTask.onCompleted.addListener((runnable) -> changeRadius());
    }

    public void cancelShrink() {
        if (activeTask == null) return;
        activeTask.cancel();
        activeTask = null;
    }
}
