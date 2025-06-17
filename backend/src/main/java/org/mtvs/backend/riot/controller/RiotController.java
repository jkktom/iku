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

    @GetMapping("/account/{gameName}/{tagLine}")
    public AccountDto getAccountInfo(
            @PathVariable String gameName,
            @PathVariable String tagLine){
        return riotService.getAccountInfo(gameName,tagLine);
    }
    @GetMapping("/matches/{puuid}")
    public List<String> getMatchIds(
            @PathVariable String puuid,
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "1") int count){
        return riotService.getMatchIds(puuid, start, count);
    }
    @GetMapping("/matches/{matchId}/detail")
    public MatchDetailDto getMatchDetail(@PathVariable String matchId){
        return riotService.getMatchDetail(matchId);
    }
    @GetMapping("/matches/{matchId}/timeline")
    public MatchTimelineDto getMatchTimeline(@PathVariable String matchId) {
        return riotService.getMatchTimeline(matchId);
    }

}
