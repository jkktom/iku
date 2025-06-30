package org.mtvs.backend.riot.dto;

public class MatchDetailDto {
    private InfoDto info;

    public MatchDetailDto() {
    }

    public InfoDto getInfo() {
        return info;
    }

    public void setInfo(InfoDto info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return "MatchDetailDto{" +
                "info=" + info +
                '}';
    }


}
