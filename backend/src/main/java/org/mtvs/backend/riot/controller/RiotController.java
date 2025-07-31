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
            
            // Riot API 404 에러인 경우 더 친화적인 메시지 제공
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("404")) {
                errorResponse.put("error", "해당 소환사명을 찾을 수 없습니다. 게임명과 태그를 정확히 입력해주세요.");
                errorResponse.put("detail", "입력하신 '" + gameName + "#" + tagLine + "' 계정이 존재하지 않습니다.");
                errorResponse.put("suggestion", "태그는 보통 'KR1', '1234' 등의 형태입니다. 게임 내에서 확인해주세요.");
            } else if (errorMessage != null && errorMessage.contains("403")) {
                errorResponse.put("error", "API 키에 문제가 있습니다. 관리자에게 문의해주세요.");
            } else if (errorMessage != null && errorMessage.contains("429")) {
                errorResponse.put("error", "요청이 너무 많습니다. 잠시 후 다시 시도해주세요.");
            } else {
                errorResponse.put("error", "계정 정보를 조회할 수 없습니다: " + errorMessage);
            }
            
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
     * 게임 참여인원에 대한 정보(인게임 닉네임, 라이엇태그, 플레이한 챔피언 이름, 참여자Id, 시야점수, 킬뎃 등)
     */
    @GetMapping("/matches/{matchId}/detail")
    public MatchDetailDto getMatchDetail(@PathVariable String matchId){
        return riotService.getMatchDetail(matchId);
    }

    /**
     * 매치 타임라인 조회 (순수 Riot API)
     * 타임라인별로 게임 데이터 조회
     */
    @GetMapping("/matches/{matchId}/timeline")
    public MatchTimelineDto getMatchTimeline(@PathVariable String matchId) {
        return riotService.getMatchTimeline(matchId);
    }
}
