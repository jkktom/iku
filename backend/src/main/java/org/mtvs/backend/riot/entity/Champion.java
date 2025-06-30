package org.mtvs.backend.riot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "champions")
public class Champion {
    @Id
    private String id; //챔피언 이름 (영어)

    @Column(nullable = false)
    private String name; //챔피언 이름(한국어)

    @Column(nullable = false)
    private int key; //챔피언 번호 ex) 아트록스 = 266번

    @Column(nullable = false)
    private String version; //게임 버전

    public Champion() {
    }

    public Champion(String id, String name, int key, String version) {
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
}
