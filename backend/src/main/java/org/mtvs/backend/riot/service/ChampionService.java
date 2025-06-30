package org.mtvs.backend.riot.service;

import org.mtvs.backend.riot.Repository.ChampionRepository;
import org.mtvs.backend.riot.dto.ChampionDto;
import org.mtvs.backend.riot.entity.Champion;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChampionService {
    private final ChampionRepository championRepository;
    private final RestTemplate restTemplate;

    public ChampionService(ChampionRepository championRepository, RestTemplate restTemplate) {
        this.championRepository = championRepository;
        this.restTemplate = restTemplate;
    }

    //최신 버전 가져오기
    public String getLatestVersion(){
        String[] versions = restTemplate.getForObject(
                "https://ddragon.leagueoflegends.com/api/versions.json",
                String[].class
        );
        return versions[0]; //가장 앞에 작성된게 최신
    }

    //전체 챔피언 목록 조회
    public List<ChampionDto> getAllChampions(){
        List<Champion> champions = championRepository.findAll();
        return champions.stream()
                .map(champion -> {
                    ChampionDto dto = new ChampionDto();
                    dto.setId(champion.getId());
                    dto.setName(champion.getName());
                    dto.setKey(champion.getKey());
                    dto.setVersion(champion.getVersion());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    //특정 챔피언 조회
    public ChampionDto getChampionById(String id){
        Champion champion = championRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("챔피언을 찾을 수 없습니다." +id));

        ChampionDto dto = new ChampionDto();
        dto.setId(champion.getId());
        dto.setName(champion.getName());
        dto.setKey(champion.getKey());
        dto.setVersion(champion.getVersion());
        return dto;
    }

    //전체 챔피언 정보 동기화
    public void syncChampions() {
        try {
            String latestVersion = getLatestVersion();

            //기존 버전 정보 삭제
            championRepository.deleteByVersion(latestVersion);

            //전체 챔피언 목록 가져오기
            String championListUrl = "https://ddragon.leagueoflegends.com/cdn/" + latestVersion + "/data/ko_KR/champion.json";

            //Json 응답을 Map으로 파싱
            Map<String, Object> response = restTemplate.getForObject(championListUrl, Map.class);
            Map<String, Object> data = (Map<String, Object>) response.get("data");

            //각 챔피언 데이터를 엔티티로 변환하여 저장
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String championId = entry.getKey();
                Map<String, Object> championData = (Map<String, Object>) entry.getValue();

                Champion champion = new Champion();
                champion.setId(championId);
                champion.setName((String) championData.get("name"));
                champion.setKey(Integer.parseInt((String) championData.get("key")));
                champion.setVersion(latestVersion);

                championRepository.save(champion);
            }
        } catch (Exception ex) {
            throw new RuntimeException("챔피언 동기화 중 오류 발생: " + ex.getMessage());
        }
    }
}
