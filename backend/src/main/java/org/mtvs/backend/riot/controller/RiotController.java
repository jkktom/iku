package org.mtvs.backend.riot.controller;

import org.mtvs.backend.riot.dto.AccountDto;
import org.mtvs.backend.riot.dto.MatchDetailDto;
import org.mtvs.backend.riot.dto.MatchTimelineDto;
import org.mtvs.backend.riot.dto.RiotUserRequestDto;
import org.mtvs.backend.riot.entity.RiotUser;
import org.mtvs.backend.riot.service.RiotService;
import org.mtvs.backend.riot.service.RiotUserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/riot")
public class RiotController {

    private final RiotService riotService;
    private final RiotUserService riotUserService;

    public RiotController(RiotService riotService, RiotUserService riotUserService) {
        this.riotService = riotService;
        this.riotUserService = riotUserService;
    }

    @PostMapping
    public RiotUser saveRiotUser(@RequestBody RiotUserRequestDto dto){
        //1. 닉네임과 태그로 puuid 조회
        AccountDto account = riotService.getAccountInfo(dto.getGameName(), dto.getTagLine());
        if(account == null || account.getPuuid() == null){
            throw new IllegalArgumentException("해당 유저를 찾을 수 없습니다.");
        }
        //2. DB에 저장
        return riotUserService.saveOrUpdate(account.getPuuid(), dto.getGameName(), dto.getTagLine());
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