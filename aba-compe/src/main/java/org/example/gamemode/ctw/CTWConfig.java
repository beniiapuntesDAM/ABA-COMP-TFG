package org.example.gamemode.ctw;


import org.example.entity.objetivo.WoolEntity;
import java.util.List;

/**
 * Configuración del modo CTW (Capture The Wool).
 * Contiene únicamente datos, sin lógica.
 */
public class CTWConfig {

    /** Lanas del mapa (cada una pertenece a un equipo). */
    private final List<WoolEntity> wools;

    /** Puntos que otorga capturar una lana. */
    private final int pointsPerWool;

    /** Si se permite recuperar lana enemiga del suelo. */
    private final boolean allowRecover;

    /** Si se permite colocar lana en cualquier bloque (no solo monumento). */
    private final boolean allowPlaceAnywhere;

    public CTWConfig(List<WoolEntity> wools, int pointsPerWool, boolean allowRecover, boolean allowPlaceAnywhere) {
        this.wools = wools;
        this.pointsPerWool = pointsPerWool;
        this.allowRecover = allowRecover;
        this.allowPlaceAnywhere = allowPlaceAnywhere;
    }

    public List<WoolEntity> getWools() { return wools; }
    public int getPointsPerWool() { return pointsPerWool; }
    public boolean isAllowRecover() { return allowRecover; }
    public boolean isAllowPlaceAnywhere() { return allowPlaceAnywhere; }
}

