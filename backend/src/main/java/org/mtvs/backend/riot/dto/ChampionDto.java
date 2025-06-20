package org.mtvs.backend.riot.dto;

import org.mtvs.backend.riot.entity.Champion;

public class ChampionDto {
    private String id;
    private String name;
    private int key;
    private String version;

    public ChampionDto() {
    }

    public ChampionDto(String id, String name, int key, String version) {
        this.id = id;
        this.name = name;
        this.key = key;
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    //변환 메서드
    //Entity -> DTO
    public static ChampionDto fromEntity(Champion champion){
        return new ChampionDto(
                champion.getId(),
                champion.getName(),
                champion.getKey(),
                champion.getVersion()
        );
    }

    //DTO를 Entity로 변환하는 메서드
    public Champion toEntity(){
        Champion champion = new Champion();
        champion.setId(this.id); //this -> DTO
        champion.setName(this.name);
        champion.setKey(this.key);
        champion.setVersion(this.version);
        return champion;
    }

    @Override
    public String toString() {
        return "ChampionDto{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", key=" + key +
                ", version='" + version + '\'' +
                '}';
    }
}
