package org.mtvs.backend.riot.dto;

public class AccountDto {
    private String puuid;
    private String gameName;
    private String tagLine;

    public AccountDto() {
    }

    public AccountDto(String puuid, String tagLine, String gameName) {
        this.puuid = puuid;
        this.tagLine = tagLine;
        this.gameName = gameName;
    }

    public String getPuuid() {
        return puuid;
    }

    public void setPuuid(String puuid) {
        this.puuid = puuid;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getTagLine() {
        return tagLine;
    }

    public void setTagLine(String tagLine) {
        this.tagLine = tagLine;
    }

    @Override
    public String toString() {
        return "AccountDto{" +
                "puuid='" + puuid + '\'' +
                ", gameName='" + gameName + '\'' +
                ", tagLine='" + tagLine + '\'' +
                '}';
    }
}
