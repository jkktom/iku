package org.mtvs.backend.riot.dto;

import java.util.List;

public class MatchSummaryDto {
    private int kills;
    private int deaths;
    private int assists;
    private int cs;
    private int gold;
    private List<String> items;
    private String result;

    public MatchSummaryDto() {}

    public MatchSummaryDto(int kills, int deaths, int assists, int cs, int gold, List<String> items, String result) {
        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;
        this.cs = cs;
        this.gold = gold;
        this.items = items;
        this.result = result;
    }

    public int getKills() { return kills; }
    public void setKills(int kills) { this.kills = kills; }

    public int getDeaths() { return deaths; }
    public void setDeaths(int deaths) { this.deaths = deaths; }

    public int getAssists() { return assists; }
    public void setAssists(int assists) { this.assists = assists; }

    public int getCs() { return cs; }
    public void setCs(int cs) { this.cs = cs; }

    public int getGold() { return gold; }
    public void setGold(int gold) { this.gold = gold; }

    public List<String> getItems() { return items; }
    public void setItems(List<String> items) { this.items = items; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
}