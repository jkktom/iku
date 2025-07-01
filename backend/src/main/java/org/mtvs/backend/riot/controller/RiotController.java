package org.mtvs.backend.riot.controller;

import org.mtvs.backend.riot.dto.AccountDto;
import org.mtvs.backend.riot.dto.MatchDetailDto;
import org.mtvs.backend.riot.dto.MatchTimelineDto;
import org.mtvs.backend.riot.service.RiotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/riot")
@CrossOrigin(origins = "*")
public class RiotController {

    private final RiotService riotService;

    public RiotController(RiotService riotService) {
        this.riotService = riotService;
    }

    /**
     * 계정 정보 조회 (순수 Riot API)
     */
    @GetMapping("/account/{gameName}/{tagLine}")
    public ResponseEntity<Map<String, Object>> getAccountInfo(
            @PathVariable String gameName,
            @PathVariable String tagLine) {
        
        try {
            // Riot API로 계정 정보 조회
            AccountDto account = riotService.getAccountInfo(gameName, tagLine);
            
            // 응답 구성 (순수 계정 정보만)
            Map<String, Object> response = new HashMap<>();
            response.put("account", account);
            response.put("message", "계정 정보 조회 완료");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 매치 ID 목록 조회 (순수 Riot API)
     */
    @GetMapping("/matches/{puuid}")
    public ResponseEntity<Map<String, Object>> getMatchIds(
            @PathVariable String puuid,
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "1") int count) {
        
        try {
            // Riot API로 매치 ID 목록 조회
            List<String> matchIds = riotService.getMatchIds(puuid, start, count);
            
            // 응답 구성 (순수 매치 데이터만)
            Map<String, Object> response = new HashMap<>();
            response.put("matchIds", matchIds);
            response.put("count", matchIds != null ? matchIds.size() : 0);
            
            if (matchIds == null || matchIds.isEmpty()) {
                response.put("message", "매치 데이터가 없습니다.");
            } else {
                response.put("selectedMatchId", matchIds.get(0)); // 첫 번째 매치 ID
                response.put("message", "매치 ID 목록 조회 완료");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 매치 상세 정보 조회 (순수 Riot API)
     */
    @GetMapping("/matches/{matchId}/detail")
    public MatchDetailDto getMatchDetail(@PathVariable String matchId){
        return riotService.getMatchDetail(matchId);
    }
    
    /**
     * 매치 타임라인 조회 (순수 Riot API)
     */
    @GetMapping("/matches/{matchId}/timeline")
    public MatchTimelineDto getMatchTimeline(@PathVariable String matchId) {
        return riotService.getMatchTimeline(matchId);
    }
}
