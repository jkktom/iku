package org.mtvs.backend.riot.dto;

public class RiotUserRequestDto {
    private String gameName;
    private String tagLine;

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
        return "RiotUserRequestDto{" +
                "gameName='" + gameName + '\'' +
                ", tagLine='" + tagLine + '\'' +
                '}';
    }
}
