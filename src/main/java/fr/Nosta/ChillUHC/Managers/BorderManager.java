package fr.Nosta.ChillUHC.Managers;

import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Tasks.WorldBorderTask;
import org.bukkit.World;
import org.bukkit.WorldBorder;

public class BorderManager {

    private final Main plugin;

    private WorldBorder worldBorder;
    private WorldBorderTask activeTask;

    private int startRadius = 1000;
    public int getStartRadius() { return this.startRadius; }
    public int getCurrentRadius() { return (int)worldBorder.getSize()/2; }

    private int targetRadius = 50; // Final border 50x50
    public int getTargetRadius() { return this.targetRadius; }
    public void setTargetRadius(int inRadius) { this.targetRadius = inRadius; }

    private long meetupDuration = 3600; //60m meetup
    public long getMeetupDuration() { return this.meetupDuration; }
    public void setMeetupDuration(long inDuration) { this.meetupDuration = inDuration; }

    private long shrinkDuration = 1800; //30m shrink
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
        worldBorder.setWarningDistance(0);
        worldBorder.setCenter(world.getSpawnLocation());
        worldBorder.setSize(startRadius*2);
    }

    public void reset() {
        cancelShrink();
        worldBorder.setSize(startRadius*2);
    }

    public void setStartRadius(int radius) {
        startRadius = radius;
        worldBorder.setSize(radius*2);
    }

    public void changeRadius() {
        worldBorder.changeSize(targetRadius*2, shrinkDuration * 20L);
    }

    public boolean isShrinking() { return worldBorder.getSize() < startRadius * 2; }

    public void startShrink() {
        cancelShrink();
        meetupEndTimestamp = System.currentTimeMillis() + (meetupDuration * 1000);
        activeTask = new WorldBorderTask(plugin, this);
        activeTask.start();
        activeTask.OnCompleted.addListener((runnable) -> changeRadius());
    }

    public void cancelShrink() {
        if (activeTask == null) return;
        activeTask.cancel();
        activeTask = null;
    }
}