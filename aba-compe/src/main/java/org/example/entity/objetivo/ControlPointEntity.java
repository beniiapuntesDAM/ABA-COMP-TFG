package org.example.entity.objetivo;

import org.bukkit.Material;
import org.example.entity.mapa.RegionEntity;

import java.util.HashMap;
import java.util.Map;

public class ControlPointEntity {

    public enum State {
        NEUTRAL,
        CAPTURING,
        CAPTURED,
        CONTESTED
    }

    private final String name;
    private final int pointsPerTick;
    private final RegionEntity captureRegion;
    private final RegionEntity fillRegion;
    private final RegionEntity floorRegion;
    private final Material floorMaterial;
    private final int captureTime;
    private State state = State.NEUTRAL;
    private String ownerTeam = null;
    private double captureProgress = 0;

    private final Map<String, Material> originalBlocks = new HashMap<>();
    private final Map<String, Material> originalFloorBlocks = new HashMap<>();

    public ControlPointEntity(String name, int pointsPerTick, RegionEntity captureRegion,
                              RegionEntity fillRegion, RegionEntity floorRegion,
                              String floorMaterial, int captureTime) {
        this.name = name;
        this.pointsPerTick = pointsPerTick;
        this.captureRegion = captureRegion;
        this.fillRegion = fillRegion;
        this.floorRegion = floorRegion;
        this.floorMaterial = floorMaterial != null ? Material.matchMaterial(floorMaterial) : Material.WHITE_TERRACOTTA;
        this.captureTime = captureTime;
    }

    public String getName() { return name; }
    public int getPointsPerTick() { return pointsPerTick; }
    public RegionEntity getCaptureRegion() { return captureRegion; }
    public RegionEntity getFillRegion() { return fillRegion; }
    public RegionEntity getFloorRegion() { return floorRegion; }
    public Material getFloorMaterial() { return floorMaterial; }
    public int getCaptureTime() { return captureTime; }
    public State getState() { return state; }
    public void setState(State state) { this.state = state; }
    public String getOwnerTeam() { return ownerTeam; }
    public void setOwnerTeam(String ownerTeam) { this.ownerTeam = ownerTeam; }
    public double getCaptureProgress() { return captureProgress; }
    public void setCaptureProgress(double captureProgress) {
        this.captureProgress = Math.max(0, Math.min(captureTime, captureProgress));
    }
    public double getCapturePercent() { return captureProgress / captureTime; }
    public Map<String, Material> getOriginalBlocks() { return originalBlocks; }
    public Map<String, Material> getOriginalFloorBlocks() { return originalFloorBlocks; }
}