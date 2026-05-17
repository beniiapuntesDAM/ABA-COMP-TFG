package org.example.entity.mapa;

/**
 * Representa una región tridimensional del mapa definida por dos esquinas opuestas.
 * Soporta los tipos {@code cuboid} (comprueba las tres coordenadas) y
 * {@code rectangle} (ignora el eje Y).
 */
public class RegionEntity {

    /** Identificador único de la región. */
    private final String id;

    /** Tipo de región: {@code "cuboid"} o {@code "rectangle"}. */
    private final String type;

    /** Coordenadas mínimas de la región. */
    private final double minX, minY, minZ;

    /** Coordenadas máximas de la región. */
    private final double maxX, maxY, maxZ;

    /**
     * Crea una nueva región. Las coordenadas mínimas y máximas se calculan
     * automáticamente a partir de los dos puntos, independientemente del orden en que se pasen.
     *
     * @param id   identificador de la región
     * @param type tipo de región ({@code "cuboid"} o {@code "rectangle"})
     * @param minX coordenada X del primer punto
     * @param minY coordenada Y del primer punto
     * @param minZ coordenada Z del primer punto
     * @param maxX coordenada X del segundo punto
     * @param maxY coordenada Y del segundo punto
     * @param maxZ coordenada Z del segundo punto
     */
    public RegionEntity(String id, String type, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.id = id;
        this.type = type;
        this.minX = Math.min(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxX = Math.max(minX, maxX);
        this.maxY = Math.max(minY, maxY);
        this.maxZ = Math.max(minZ, maxZ);
    }

    /**
     * Comprueba si el punto dado está dentro de la región.
     * Para regiones de tipo {@code rectangle} se ignora la coordenada Y.
     *
     * @param x coordenada X del punto
     * @param y coordenada Y del punto
     * @param z coordenada Z del punto
     * @return {@code true} si el punto está dentro de la región
     */
    public boolean contains(double x, double y, double z) {
        if (type.equals("rectangle")) {
            return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
        } else {
            return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
        }
    }

    /** @return identificador de la región */
    public String getId() { return id; }

    /** @return tipo de región ({@code "cuboid"} o {@code "rectangle"}) */
    public String getType() { return type; }

    public double getMinX() { return minX; }
    public double getMaxX() { return maxX; }
    public double getMinY() { return minY; }
    public double getMaxY() { return maxY; }
    public double getMinZ() { return minZ; }
    public double getMaxZ() { return maxZ; }
}