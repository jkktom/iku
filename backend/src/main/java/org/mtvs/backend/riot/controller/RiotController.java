package org.mtvs.backend.riot.controller;

import org.mtvs.backend.riot.dto.AccountDto;
import org.mtvs.backend.riot.dto.MatchDetailDto;
import org.mtvs.backend.riot.dto.MatchTimelineDto;
import org.mtvs.backend.riot.service.RiotService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/riot")
public class RiotController {

    private final RiotService riotService;

    public RiotController(RiotService riotService) {
        this.riotService = riotService;
    }

    //puuid 구하기
    @GetMapping("/account/{gameName}/{tagLine}")
    public AccountDto getAccountInfo(
            @PathVariable String gameName,
            @PathVariable String tagLine){
        return riotService.getAccountInfo(gameName,tagLine);
    }
    //얻은 puuid를 통해 matchId 구하기
    @GetMapping("/matches/{puuid}")
    public List<String> getMatchIds(
            @PathVariable String puuid,
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "1") int count){
        return riotService.getMatchIds(puuid, start, count);
    }
    //게임 참여인원에 대한 정보(인게임 닉네임, 라이엇태그, 플레이한 챔피언 이름, 참여자Id, 시야점수, 킬뎃 등) 출력
    @GetMapping("/matches/{matchId}/detail")
    public MatchDetailDto getMatchDetail(@PathVariable String matchId){
        return riotService.getMatchDetail(matchId);
    }
    //타임라인별로 게임 데이터 출력
    @GetMapping("/matches/{matchId}/timeline")
    public MatchTimelineDto getMatchTimeline(@PathVariable String matchId) {
        return riotService.getMatchTimeline(matchId);
    }

} 