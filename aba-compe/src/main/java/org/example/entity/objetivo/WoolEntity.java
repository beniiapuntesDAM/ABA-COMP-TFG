package org.example.entity.objetivo;

import org.bukkit.Material;

/**
 * Representa una lana objetivo del mapa. Almacena el equipo al que pertenece,
 * su color, su posición original en el mapa y la posición del monumento
 * donde debe colocarse para puntuar.
 */
public class WoolEntity {

    private final String team;
    private final Material color;

    private final double locX, locY, locZ;
    private final double monX, monY, monZ;

    private boolean captured = false;

    public WoolEntity(String team, Material color,
                      double locX, double locY, double locZ,
                      double monX, double monY, double monZ) {

        this.team = team;
        this.color = color;
        this.locX = locX;
        this.locY = locY;
        this.locZ = locZ;
        this.monX = monX;
        this.monY = monY;
        this.monZ = monZ;
    }

    public boolean isCaptured() { return captured; }
    public void setCaptured(boolean captured) { this.captured = captured; }

    public double getMonX() { return monX; }
    public double getMonY() { return monY; }
    public double getMonZ() { return monZ; }

    public double getLocX() { return locX; }
    public double getLocY() { return locY; }
    public double getLocZ() { return locZ; }

    public Material getColor() { return color; }
    public String getTeam() { return team; }
}
