public interface SupabaseClientInterface {
    List<PlayerStats> getAllPlayers();

    PlayerStats getByUuid(String uuid);

    PlayerStats getByUsername(String username);

    boolean loginComprobarContrasenia(String username, String password);

    boolean updateContra(String username, String newPassword);

    List<PlayerStats> getJugadoresByNombreClan(String nombreClan);

    boolean actualizarClanJugador(String nombreJugador, String nombreClan);

    List<String> getAllClanes();

    boolean crearClan(String nombreClan);

    String getNombreClanByJugador(String nombreJugador);
}