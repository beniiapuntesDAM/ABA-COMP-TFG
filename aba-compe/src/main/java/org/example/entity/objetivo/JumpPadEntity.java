package org.example.entity.objetivo;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.example.entity.mapa.RegionEntity;

import java.util.List;

public class JumpPadEntity {

    private final List<Location> blocks;
    private final double force;
    private final Vector direction;

    public JumpPadEntity(List<Location> blocks, double force, Vector direction) {
        this.blocks = blocks;
        this.force = force;
        this.direction = direction;
    }

    public boolean contains(Location loc) {
        for (Location b : blocks) {
            if (b.getBlockX() == loc.getBlockX()
                    && b.getBlockY() == loc.getBlockY()
                    && b.getBlockZ() == loc.getBlockZ()) {
                return true;
            }
        }
        return false;
    }

    public double getForce() { return force; }
    public Vector getDirection() { return direction; }
}