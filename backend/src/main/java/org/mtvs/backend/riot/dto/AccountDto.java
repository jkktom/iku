package org.mtvs.backend.riot.dto;

//라이엇 계정의 기본 정보
public class AccountDto {
    private String puuid; //내부적으로 계정 식별에 사용되는 고유값
    private String gameName; //인게임 닉네임
    private String tagLine; //라이엇 태크

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
