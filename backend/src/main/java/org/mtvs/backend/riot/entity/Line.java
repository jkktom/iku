package org.mtvs.backend.riot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/*라인(TOP,JUNGLE,MID,ADC,SUPPORT) 저장용 엔티티*/
@Entity
@Table(name = "lines")

public class Line{
    @Id
    private Integer id; // 1.TOP, 2.JUNGLE, 3.MID, 4. ADC, 5.SUPPORT

    @Column(nullable = false, unique = true)
    private String name; //"TOP", "JUNGLE", "MID", "ADC", "SUPPORT"

    public Line() {
    }

    public Line(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    /*Line.top 이런식으로 쓰기 가능*/

    public static final Line JUNGLE = new Line(2,"JUNGLE");
    public static final Line MID = new Line(3,"MID");
    public static final Line ADC = new Line(4,"ADC");
    public static final Line SUPPORT = new Line(5,"SUPPORT");

}
