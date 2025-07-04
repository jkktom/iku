package org.mtvs.backend.riot.config;

import org.mtvs.backend.riot.Repository.LineRepository;
import org.mtvs.backend.riot.entity.Line;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RiotConfig {
    @Bean
    public CommandLineRunner riotDataLoader(LineRepository lineRepository){
        return args -> {
            if(lineRepository.count() == 0){ //비어있을 때
                lineRepository.saveAll(List.of(
                    new Line(1,"TOP"),
                    new Line(2,"JUNGLE"),
                    new Line(3,"MID"),
                    new Line(4,"ADC"),
                    new Line(5,"SUPPORT")
                ));
            }
        };
    }

}
