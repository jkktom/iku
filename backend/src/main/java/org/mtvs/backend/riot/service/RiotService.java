package org.mtvs.backend.riot.service;

import org.mtvs.backend.riot.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.support.UriComponentsContributor;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* 게임 매치 관련 */
@Service
public class RiotService {
    private static final Logger logger = LoggerFactory.getLogger(RiotService.class);
    private final RestTemplate restTemplate;

    @Value("${riot.api.key}")
    private String apikey;

    private static final String ASIA_BASE_URL = "https://asia.api.riotgames.com";
    private static final String KR_BASE_URL = "https://kr.api.riotgames.com";
    private static final Map<Integer, String> QUEUE_NAMES = Map.of(
            400, "일반 게임 (드래프트 픽)",
            420, "솔로 랭크",
            430, "일반 게임 (무작위 총력전)",
            440, "자유 랭크",
            450, "칼바람 나락",
            480, "신속대전",
            490, "빠른 대전",
            700, "격전"
    );

    public RiotService(RestTemplate restTemplate, UriComponentsContributor uriComponentsContributor) {
        this.restTemplate = restTemplate;
    }
    /* 유저의 정보 (puuid, gameName, tagLine 출력) */
    @Cacheable(value = "riotAccountInfo", key = "#gameName + '_' + #tagLine")
    public AccountDto getAccountInfo(String gameName, String tagLine) {
        String url = UriComponentsBuilder.fromHttpUrl(ASIA_BASE_URL)
                .path("/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}")
                .buildAndExpand(gameName, tagLine)
                .toUriString();

        return restTemplate.exchange(
                url,
                HttpMethod.GET, //API에 get 요청
                createHttpEntity(), //Riot API 키가 포함된 헤더 생성
                AccountDto.class //puuid, gameName, tagLine 출력
        ).getBody();
    }
    /* 매치의 고유 ID 조회 */
    public List<String> getMatchIds(String puuid, int start, int count) {
        String url = UriComponentsBuilder.fromHttpUrl(ASIA_BASE_URL)
                .path("/lol/match/v5/matches/by-puuid/{puuid}/ids")
                .queryParam("start", start)
                .queryParam("count", count)
                .buildAndExpand(puuid)
                .toUriString();
        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                createHttpEntity(),
                new ParameterizedTypeReference<List<String>>() {
                }
        ).getBody();
    }
    /* 해당 매치의 상세 정보*/
    @Cacheable(value = "riotMatchData", key = "#matchId")
    public MatchDetailDto getMatchDetail(String matchId) {
        String url = UriComponentsBuilder.fromHttpUrl(ASIA_BASE_URL)
                .path("/lol/match/v5/matches/{matchId}")
                .buildAndExpand(matchId)
                .toUriString();

        // First get the raw response to log participant data
        ResponseEntity<String> rawResponse = restTemplate.exchange(
                url,
                HttpMethod.GET,
                createHttpEntity(),
                String.class
        );
        
        // Log a sample of participant data to debug field mappings
        String responseBody = rawResponse.getBody();
        if (responseBody != null && responseBody.contains("participants")) {
            // Extract and log first participant data for debugging
            try {
                int participantsStart = responseBody.indexOf("\"participants\":[{");
                if (participantsStart != -1) {
                    int firstParticipantEnd = responseBody.indexOf("},", participantsStart);
                    if (firstParticipantEnd != -1) {
                        String firstParticipant = responseBody.substring(participantsStart, firstParticipantEnd + 1);
                        logger.info("=== DEBUG: First Participant JSON Sample ===");
                        logger.info(firstParticipant.substring(0, Math.min(500, firstParticipant.length())));
                        
                        // 포지션 관련 필드 확인
                        boolean hasTeamPosition = firstParticipant.contains("teamPosition");
                        boolean hasIndividualPosition = firstParticipant.contains("individualPosition");
                        boolean hasLane = firstParticipant.contains("lane");
                        boolean hasRole = firstParticipant.contains("role");
                        
                        logger.info("Position fields - teamPosition: {}, individualPosition: {}, lane: {}, role: {}", 
                            hasTeamPosition, hasIndividualPosition, hasLane, hasRole);
                        logger.info("=== END DEBUG ===");
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to extract participant debug info", e);
            }
        }

        // Now get the properly deserialized response
        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                createHttpEntity(),
                MatchDetailDto.class
        ).getBody();
    }
    /* 해당 매치의 타임라인별 정보*/
    @Cacheable(value = "riotMatchTimeline", key = "#matchId")
    public MatchTimelineDto getMatchTimeline(String matchId) {
        String url = UriComponentsBuilder.fromHttpUrl(ASIA_BASE_URL)
                .path("/lol/match/v5/matches/{matchId}/timeline")
                .buildAndExpand(matchId)
                .toUriString();

        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                createHttpEntity(),
                MatchTimelineDto.class
        ).getBody();
    }
    /* API 호출 시 필요한 인증 헤더 */
    private HttpEntity<String> createHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Riot-Token", apikey);
        
        // API 키 검증 로깅 (키의 처음 4자리와 마지막 4자리만 로깅)
        if (apikey != null && apikey.length() > 8) {
            String maskedKey = apikey.substring(0, 4) + "..." + apikey.substring(apikey.length() - 4);
            logger.debug("Using Riot API key: {}", maskedKey);
        } else {
            logger.error("Riot API key is null or too short! Current value: {}", apikey);
        }
        
        return new HttpEntity<>(headers);
    }

    public String getQueueName(int queueId) {
        return QUEUE_NAMES.getOrDefault(queueId, "알 수 없는 큐 (" + queueId + ")");
    }

    /* PUUID로 소환사 정보 조회 */
    @Cacheable(value = "summonerInfo", key = "#puuid")
    public SummonerDto getSummonerByPuuid(String puuid) {
        logger.info("Fetching summoner info for PUUID: {}", puuid);
        
        String url = UriComponentsBuilder.fromHttpUrl(KR_BASE_URL)
                .path("/lol/summoner/v4/summoners/by-puuid/{puuid}")
                .buildAndExpand(puuid)
                .toUriString();

        try {
            // Raw JSON 응답 먼저 확인
            ResponseEntity<String> rawResponse = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    createHttpEntity(),
                    String.class
            );
            
            String jsonResponse = rawResponse.getBody();
            System.out.println("=== RAW SUMMONER API RESPONSE ===");
            System.out.println("Response Status: " + rawResponse.getStatusCode());
            System.out.println("Response Body: " + jsonResponse);
            System.out.println("=== END RAW RESPONSE ===");
            
            // JSON에서 특정 필드 확인
            if (jsonResponse != null) {
                System.out.println("Contains 'id'?: " + jsonResponse.contains("\"id\""));
                System.out.println("Contains 'name'?: " + jsonResponse.contains("\"name\""));
                System.out.println("Contains 'summonerId'?: " + jsonResponse.contains("\"summonerId\""));
            }
            
            SummonerDto summoner = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    createHttpEntity(),
                    SummonerDto.class
            ).getBody();
            
            logger.info("Successfully fetched summoner info for PUUID: {}, SummonerId: {}", puuid, summoner.getId());
            logger.info("Summoner details - ID: {}, Name: {}, AccountId: {}, Level: {}", 
                summoner.getId(), summoner.getName(), summoner.getAccountId(), summoner.getSummonerLevel());
            
            // Riot API 변경으로 인해 더 이상 ID가 제공되지 않음
            logger.warn("Riot API no longer provides summoner ID, name, or accountId fields");
            
            return summoner;
        } catch (Exception e) {
            logger.error("Failed to fetch summoner info for PUUID: {}", puuid, e);
            throw new RuntimeException("소환사 정보 조회 실패: " + e.getMessage(), e);
        }
    }

    /* 소환사 ID로 랭크 정보 조회 */
    @Cacheable(value = "rankedInfo", key = "#summonerId")
    public List<LeagueEntryDto> getRankedData(String summonerId) {
        logger.info("Fetching ranked data for SummonerId: {}", summonerId);
        
        String url = UriComponentsBuilder.fromHttpUrl(KR_BASE_URL)
                .path("/lol/league/v4/entries/by-summoner/{summonerId}")
                .buildAndExpand(summonerId)
                .toUriString();

        try {
            ResponseEntity<List<LeagueEntryDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    createHttpEntity(),
                    new ParameterizedTypeReference<List<LeagueEntryDto>>() {}
            );
            
            List<LeagueEntryDto> rankedData = response.getBody();
            logger.info("Successfully fetched ranked data for SummonerId: {}, Entries: {}", 
                       summonerId, rankedData != null ? rankedData.size() : 0);
            return rankedData;
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            logger.warn("Failed to fetch ranked data for SummonerId: {} - {}", summonerId, errorMessage);
            
            // 403 Forbidden 또는 기타 권한 오류인 경우 빈 리스트 반환 (언랭크 처리)
            if (errorMessage.contains("403") || errorMessage.contains("Forbidden") || 
                e.getClass().getSimpleName().contains("Forbidden") ||
                e.getClass().getSimpleName().contains("HttpClientErrorException")) {
                logger.info("Rank API access denied, treating as unranked for SummonerId: {}", summonerId);
                return new ArrayList<>(); // 빈 리스트 반환하여 언랭크로 처리
            }
            
            // 다른 오류는 예외 발생
            throw new RuntimeException("랭크 정보 조회 실패: " + errorMessage, e);
        }
    }

    /* PUUID로 소환사 + 랭크 정보 통합 조회 */
    public Map<String, Object> getPlayerRankInfo(String puuid) {
        logger.info("=== Starting rank info lookup for PUUID: {} ===", puuid);
        try {
            // Riot API 변경으로 인해 summoner ID가 더 이상 제공되지 않음
            // PUUID를 사용해서 직접 랭크 정보 조회 시도
            logger.warn("Riot API no longer provides summoner ID. Attempting alternative rank lookup methods.");
            
            // 1. 소환사 정보 조회 (기본 정보만)
            SummonerDto summoner = getSummonerByPuuidSafe(puuid);
            logger.info("Summoner Level: {}", summoner.getSummonerLevel());
            
            // 2. PUUID로 직접 랭크 정보 조회 시도 (새로운 방법)
            List<LeagueEntryDto> rankedEntries = getRankedDataByPuuid(puuid);
            logger.info("Ranked entries count: {}", rankedEntries != null ? rankedEntries.size() : 0);
            
            // 랭크 엔트리 상세 로깅
            if (rankedEntries != null && !rankedEntries.isEmpty()) {
                for (int i = 0; i < rankedEntries.size(); i++) {
                    LeagueEntryDto entry = rankedEntries.get(i);
                    logger.info("Rank Entry {}: QueueType={}, Tier={}, Rank={}", 
                        i, entry.getQueueType(), entry.getTier(), entry.getRank());
                }
            } else {
                logger.warn("No ranked entries found for summoner: {}", summoner.getId());
            }
            
            // 3. 솔로랭크와 자유랭크 분리
            LeagueEntryDto soloRank = rankedEntries.stream()
                    .filter(entry -> "RANKED_SOLO_5x5".equals(entry.getQueueType()))
                    .findFirst()
                    .orElse(null);
            
            LeagueEntryDto flexRank = rankedEntries.stream()
                    .filter(entry -> "RANKED_FLEX_SR".equals(entry.getQueueType()))
                    .findFirst()
                    .orElse(null);
            
            // 4. 통합 결과 반환
            Map<String, Object> result = Map.of(
                "summoner", summoner,
                "soloRank", soloRank != null ? soloRank : new LeagueEntryDto(),
                "flexRank", flexRank != null ? flexRank : new LeagueEntryDto(),
                "soloTier", soloRank != null ? soloRank.getTier() : null,
                "soloRankDivision", soloRank != null ? soloRank.getRank() : null,
                "flexTier", flexRank != null ? flexRank.getTier() : null,
                "flexRankDivision", flexRank != null ? flexRank.getRank() : null,
                "hasRankInfo", soloRank != null || flexRank != null
            );
            
            logger.info("Successfully compiled rank info for PUUID: {}, Solo: {}, Flex: {}", 
                       puuid, 
                       soloRank != null ? soloRank.getTier() + " " + soloRank.getRank() : "UNRANKED",
                       flexRank != null ? flexRank.getTier() + " " + flexRank.getRank() : "UNRANKED");
            
            return result;
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            logger.warn("Failed to get player rank info for PUUID: {}, treating as unranked - {}", puuid, errorMessage);
            
            // 오류 발생 시 언랭크로 처리
            Map<String, Object> unrankedResult = new HashMap<>();
            unrankedResult.put("summoner", new SummonerDto()); // 빈 소환사 정보
            unrankedResult.put("soloRank", new LeagueEntryDto());
            unrankedResult.put("flexRank", new LeagueEntryDto());
            unrankedResult.put("soloTier", "UNRANKED");
            unrankedResult.put("soloRankDivision", "");
            unrankedResult.put("flexTier", "UNRANKED");
            unrankedResult.put("flexRankDivision", "");
            unrankedResult.put("hasRankInfo", false);
            
            return unrankedResult;
        }
    }

    /* Riot API 변경에 대응한 안전한 소환사 정보 조회 */
    public SummonerDto getSummonerByPuuidSafe(String puuid) {
        logger.info("Fetching summoner info (safe mode) for PUUID: {}", puuid);
        
        String url = UriComponentsBuilder.fromHttpUrl(KR_BASE_URL)
                .path("/lol/summoner/v4/summoners/by-puuid/{puuid}")
                .buildAndExpand(puuid)
                .toUriString();

        try {
            SummonerDto summoner = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    createHttpEntity(),
                    SummonerDto.class
            ).getBody();
            
            logger.info("Successfully fetched summoner info (safe mode) for PUUID: {}, Level: {}", 
                puuid, summoner.getSummonerLevel());
            
            return summoner;
        } catch (Exception e) {
            logger.error("Failed to fetch summoner info (safe mode) for PUUID: {}", puuid, e);
            throw new RuntimeException("소환사 정보 조회 실패: " + e.getMessage(), e);
        }
    }

    /* PUUID로 직접 랭크 정보 조회 시도 */
    public List<LeagueEntryDto> getRankedDataByPuuid(String puuid) {
        logger.info("Attempting to fetch ranked data directly by PUUID: {}", puuid);
        
        // 방법 1: 기존 매치 데이터에서 랭크 정보 추출 시도
        try {
            List<String> recentMatches = getMatchIds(puuid, 0, 1);
            if (!recentMatches.isEmpty()) {
                MatchDetailDto matchDetail = getMatchDetail(recentMatches.get(0));
                // 매치에서 플레이어 찾기
                ParticipantDto player = matchDetail.getInfo().getParticipants().stream()
                    .filter(p -> puuid.equals(p.getPuuid()))
                    .findFirst()
                    .orElse(null);
                
                if (player != null) {
                    logger.info("Found player in recent match, but cannot extract rank from match data");
                }
            }
        } catch (Exception e) {
            logger.warn("Could not extract rank from match data: {}", e.getMessage());
        }
        
        // 현재로서는 PUUID만으로는 랭크 정보를 직접 조회할 수 없음
        logger.warn("Unable to fetch rank data with current Riot API limitations");
        return new ArrayList<>(); // 빈 리스트 반환하여 언랭크로 처리
    }
}