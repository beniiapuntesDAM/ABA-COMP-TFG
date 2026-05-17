package org.example.entity.mapa;

public class EnterRuleEntity {

    private final String teamId;
    private final String regionId;
    private RegionEntity region;
    private final String message;

    public EnterRuleEntity(String teamId, String regionId, String message) {
        this.teamId = teamId;
        this.regionId = regionId;
        this.message = message;
    }

    public String getTeamId() { return teamId; }
    public String getRegionId() { return regionId; }
    public RegionEntity getRegion() { return region; }
    public void setRegion(RegionEntity region) { this.region = region; }
    public String getMessage() { return message; }
}