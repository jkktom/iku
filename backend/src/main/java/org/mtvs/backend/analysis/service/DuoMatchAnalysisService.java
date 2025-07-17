package org.mtvs.backend.analysis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mtvs.backend.analysis.entity.DuoMatchAnalysis;
import org.mtvs.backend.analysis.repository.DuoMatchAnalysisRepository;
import org.mtvs.backend.gemini.service.GameAnalysisService;
import org.mtvs.backend.riot.dto.AccountDto;
import org.mtvs.backend.riot.dto.MatchDetailDto;
import org.mtvs.backend.riot.dto.ParticipantDto;
import org.mtvs.backend.riot.service.RiotService;
import org.mtvs.backend.analysis.entity.AnalysisStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service
@Transactional
public class DuoMatchAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(DuoMatchAnalysisService.class);

    private final DuoMatchAnalysisRepository duoMatchAnalysisRepository;
    private final GameAnalysisService gameAnalysisService;
    private final RiotService riotService;
    private final ObjectMapper objectMapper;

    @Autowired
    public DuoMatchAnalysisService(DuoMatchAnalysisRepository duoMatchAnalysisRepository,
                                   GameAnalysisService gameAnalysisService,
                                   RiotService riotService,
                                   ObjectMapper objectMapper){
        this.duoMatchAnalysisRepository = duoMatchAnalysisRepository;
        this.gameAnalysisService = gameAnalysisService;
        this.riotService = riotService;
        this.objectMapper = objectMapper;
    }
    /*
    * 공통 매치 찾기
    * */

    public List<String> findCommonMatches(String player1Name, String player1Tag,
                                          String player2Name, String player2Tag){
        logger.info("두 플레이어의 공통 매치 찾기 {}#{} and {}#{}",
                player1Name,player1Tag,player2Name,player2Tag);

        try{
            // 1. 두 플레이어의 계정 정보 조회
            AccountDto player1Account = riotService.getAccountInfo(player1Name,player1Tag);
            AccountDto player2Account = riotService.getAccountInfo(player2Name,player2Tag);

            // 2. 각 플레이어의 최근 매치 목록 조회 (최근 게임 10개 중 겹치는 매치 추출)
            List<String> player1Matches = riotService.getMatchIds(player1Account.getPuuid(), 0, 10);
            List<String> player2Matches = riotService.getMatchIds(player2Account.getPuuid(), 0, 10);

            // 3. 공통 매치  중 같은 팀인 경우만 필터링
            List<String> sameTeamMatches = player1Matches.stream()
                    .filter(player2Matches::contains)
                    .filter(matchId -> arePlayersInSameTeam(matchId, player1Account.getPuuid(),player2Account.getPuuid()))
                    .collect(Collectors.toList());

            logger.info("Find {} common matches", sameTeamMatches.size());
            return sameTeamMatches;
        }catch (Exception e){
            logger.error("Failed to find common matches", e);
            throw new RuntimeException("공통 매치 찾기 실패: " + e.getMessage());
        }
    }

    /*
    * 듀오 비교 분석 수행 및 응답 반환
    * */
    public Map<String,Object> compareDuoPlayersAndGetResponse(String player1Name, String player1Tag,
                                                              String player2Name, String player2Tag,
                                                              String matchId){
        logger.info("Performing duo analysis for match: {} with players: {}#{} vs {}#{}",
                matchId, player1Name, player1Tag, player2Name, player2Tag);

        System.out.println("=== 입력값 확인 ===");
        System.out.println("player1Name: " + player1Name);
        System.out.println("player1Tag: " + player1Tag);
        System.out.println("player2Name: " + player2Name);
        System.out.println("player2Tag: " + player2Tag);
        System.out.println("matchId: " + matchId);

        try{
            // 1. 플레이어 계정 정보 조회
            AccountDto player1Account = riotService.getAccountInfo(player1Name,player1Tag);
            AccountDto player2Account = riotService.getAccountInfo(player2Name,player2Tag);

            // 2. 중복 분석 방지
            Optional<DuoMatchAnalysis> existingAnalysis = duoMatchAnalysisRepository
                    .findByMatchIdAndPlayer1PuuidAndPlayer2Puuid(matchId, player1Account.getPuuid(),player2Account.getPuuid());

            // 순서가 바뀐 경우도 확인
            if (!existingAnalysis.isPresent()) {
                existingAnalysis = duoMatchAnalysisRepository
                        .findByMatchIdAndPlayer1PuuidAndPlayer2Puuid(matchId, player2Account.getPuuid(), player1Account.getPuuid());
            }

            if(existingAnalysis.isPresent()&&
            existingAnalysis.get().getAnalysisStatus() == AnalysisStatus.COMPLETED){
                logger.info("Analysis already exists for this duo match");
                return buildResponseData(existingAnalysis.get());
            }

            // 3. 분석 레코드 생성 또는 업데이트
            DuoMatchAnalysis analysis = existingAnalysis.orElseGet(() -> {
                DuoMatchAnalysis newAnalysis = new DuoMatchAnalysis();
                newAnalysis.setMatchId(matchId);
                newAnalysis.setPlayer1Puuid(player1Account.getPuuid());
                newAnalysis.setPlayer2Puuid(player2Account.getPuuid());

                newAnalysis.setPlayer1Name(player1Name + " #" + player1Tag);
                newAnalysis.setPlayer2Name(player2Name + " #" + player2Tag);
                newAnalysis.setAnalysisStatus(AnalysisStatus.REQUESTED);

                logger.info("새 분석 생성: Player1={}, Player2={}",
                        newAnalysis.getPlayer1Name(), newAnalysis.getPlayer2Name());

                return newAnalysis;
            });

            // 4. 처리 상태로 변경
            analysis.setAnalysisStatus(AnalysisStatus.PROCESSING);
            analysis = duoMatchAnalysisRepository.save(analysis);

            // 5. 매치 데이터 조회
            MatchDetailDto matchDetail = riotService.getMatchDetail(matchId);

            // 6. 두 플레이어의 데이터 추출
            Map<String, Object> player1Data = extractPlayerDataFromMatch(matchDetail, player1Account.getPuuid());
            Map<String, Object> player2Data = extractPlayerDataFromMatch(matchDetail, player2Account.getPuuid());

            // 7. 챔피언 정보 설정
            String player1Champion = (String) player1Data.get("championName");
            String player2Champion = (String) player2Data.get("championName");

            analysis.setPlayer1Champion(player1Champion);
            analysis.setPlayer2Champion(player2Champion);

            logger.info("챔피언 정보: Player1={} ({}), Player2={} ({})",
                    analysis.getPlayer1Name(), player1Champion,
                    analysis.getPlayer2Name(), player2Champion);


            // 8. 성과 비교 분석
            Map<String, Object> comparisonResult = performComparison(player1Data,player2Data);

            // 9. AI 분석 수행
            String aiResponse = gameAnalysisService.analyzeDuoMatch(
                    player1Name, player1Tag, player2Name, player2Tag,matchId
            );

            // 10. 결과 저장
            analysis.setComparisonResult(comparisonResult);
            analysis.setAnalysisSummary(aiResponse);
            analysis.setAnalysisStatus(AnalysisStatus.COMPLETED);

            DuoMatchAnalysis savedAnalysis = duoMatchAnalysisRepository.save(analysis);

            logger.info("분석 완료: Player1={}, Player2={}, Champion1={}, Champion2={}",
                    savedAnalysis.getPlayer1Name(), savedAnalysis.getPlayer2Name(),
                    savedAnalysis.getPlayer1Champion(), savedAnalysis.getPlayer2Champion());

            return buildResponseData(savedAnalysis);

        }catch (Exception e){
            logger.error("Duo analysis failed for match: {}", matchId, e);
            throw new RuntimeException("듀오 분석 실패: "+e.getMessage());
        }
    }
    /*
    * 매치에서 특정 플레이어의 데이터 추출 */
    private Map<String, Object> extractPlayerDataFromMatch(MatchDetailDto matchDetail, String puuid){
        if (matchDetail == null || matchDetail.getInfo() == null || matchDetail.getInfo().getParticipants() == null){
            throw new IllegalArgumentException("매치 데이터가 유효하지 않습니다.");
        }

        ParticipantDto participant = matchDetail.getInfo().getParticipants().stream()
                .filter(p -> puuid.equals(p.getPuuid()))
                .findFirst()
                .orElseThrow(()-> new IllegalArgumentException("매치에서 플레이어를 찾을 수 없습니다." +puuid));

        Map<String, Object> playerData = new HashMap<>();
        playerData.put("puuid", participant.getPuuid());
        playerData.put("championName", participant.getChampionName());
        playerData.put("kills", participant.getKills());
        playerData.put("deaths", participant.getDeaths());
        playerData.put("assists", participant.getAssists());
        playerData.put("totalCS", participant.getTotalMinionsKilled() + participant.getNeutralMinionsKilled());
        playerData.put("goldEarned", participant.getGoldEarned());
        playerData.put("damageDealt", participant.getTotalDamageDealtToChampions());
        playerData.put("damageTaken", participant.getTotalDamageTaken());
        playerData.put("visionScore", participant.getVisionScore());
        playerData.put("win", participant.isWin());

        return playerData;
    }

    /*
    * 두 플레이어의 성과 비교 분석
    * */

    private Map<String, Object> performComparison(Map<String, Object> player1Data, Map<String, Object> player2Data){
        Map<String, Object> comparison = new HashMap<>();

        //KDA 비교
        comparison.put("kdaComparison", compareKDA(player1Data,player2Data));

        //딜량 비교
        comparison.put("damageComparison", compareDamage(player1Data,player2Data));

        // 골드 비교
        comparison.put("goldComparison", compareGold(player1Data, player2Data));

        // 시야 점수 비교
        comparison.put("visionComparison", compareVision(player1Data, player2Data));

        // CS 비교
        comparison.put("csComparison", compareCS(player1Data, player2Data));

        return comparison;
    }

    /*
     * KDA 비교
     **/
    private Map<String, Object> compareKDA(Map<String, Object> player1Data, Map<String, Object> player2Data) {
        int p1Kills = (Integer) player1Data.get("kills");
        int p1Deaths = (Integer) player1Data.get("deaths");
        int p1Assists = (Integer) player1Data.get("assists");

        int p2Kills = (Integer) player2Data.get("kills");
        int p2Deaths = (Integer) player2Data.get("deaths");
        int p2Assists = (Integer) player2Data.get("assists");

        double p1KDA = p1Deaths == 0 ? (p1Kills + p1Assists) : (double)(p1Kills + p1Assists) / p1Deaths;
        double p2KDA = p2Deaths == 0 ? (p2Kills + p2Assists) : (double)(p2Kills + p2Assists) / p2Deaths;

        return Map.of(
                "player1", Map.of("kills", p1Kills, "deaths", p1Deaths, "assists", p1Assists, "kda", p1KDA),
                "player2", Map.of("kills", p2Kills, "deaths", p2Deaths, "assists", p2Assists, "kda", p2KDA),
                "winner", p1KDA > p2KDA ? "player1" : "player2",
                "kdaDifference", Math.abs(p1KDA - p2KDA)
        );
    }

    /*
     * 딜량 비교
     **/
    private Map<String, Object> compareDamage(Map<String, Object> player1Data, Map<String, Object> player2Data) {
        int p1Damage = (Integer) player1Data.get("damageDealt");
        int p2Damage = (Integer) player2Data.get("damageDealt");

        return Map.of(
                "player1", p1Damage,
                "player2", p2Damage,
                "winner", p1Damage > p2Damage ? "player1" : "player2",
                "difference", Math.abs(p1Damage - p2Damage)
        );
    }

    /*
     * 골드 비교
     **/
    private Map<String, Object> compareGold(Map<String, Object> player1Data, Map<String, Object> player2Data) {
        int p1Gold = (Integer) player1Data.get("goldEarned");
        int p2Gold = (Integer) player2Data.get("goldEarned");

        return Map.of(
                "player1", p1Gold,
                "player2", p2Gold,
                "winner", p1Gold > p2Gold ? "player1" : "player2",
                "difference", Math.abs(p1Gold - p2Gold)
        );
    }

    /*
     * 시야 점수 비교
     **/
    private Map<String, Object> compareVision(Map<String, Object> player1Data, Map<String, Object> player2Data) {
        int p1Vision = (Integer) player1Data.get("visionScore");
        int p2Vision = (Integer) player2Data.get("visionScore");

        return Map.of(
                "player1", p1Vision,
                "player2", p2Vision,
                "winner", p1Vision > p2Vision ? "player1" : "player2",
                "difference", Math.abs(p1Vision - p2Vision)
        );
    }

    /*
     * CS 비교
     **/
    private Map<String, Object> compareCS(Map<String, Object> player1Data, Map<String, Object> player2Data) {
        int p1CS = (Integer) player1Data.get("totalCS");
        int p2CS = (Integer) player2Data.get("totalCS");

        return Map.of(
                "player1", p1CS,
                "player2", p2CS,
                "winner", p1CS > p2CS ? "player1" : "player2",
                "difference", Math.abs(p1CS - p2CS)
        );
    }

    /*
     * 응답 데이터 구성
     **/
    private Map<String, Object> buildResponseData(DuoMatchAnalysis analysis) {
        Map<String, Object> response = new HashMap<>();

        logger.info("응답 구성: Player1={}, Player2={}, Champion1={}, Champion2={}",
                analysis.getPlayer1Name(), analysis.getPlayer2Name(),
                analysis.getPlayer1Champion(), analysis.getPlayer2Champion());

        Map<String,Object> analysisRecord = new HashMap<>();
        response.put("id", analysis.getId());
        response.put("matchId", analysis.getMatchId());
        response.put("player1Name", analysis.getPlayer1Name());
        response.put("player2Name", analysis.getPlayer2Name());
        response.put("player1Champion", analysis.getPlayer1Champion());
        response.put("player2Champion", analysis.getPlayer2Champion());
        response.put("status", analysis.getAnalysisStatus().name());
        response.put("analysisSummary", analysis.getAnalysisSummary());
        response.put("comparisonResult", analysis.getComparisonResult());
        response.put("updatedAt", analysis.getUpdatedAt());
        response.put("createdAt", analysis.getCreatedAt());

        response.put("analysisRecord", analysisRecord);
        response.put("message", "듀오 분석 완료");

        return response;
    }

    /*
    두 플레이어가 같은 팀인지 확인
    */
    private boolean arePlayersInSameTeam(String matchId, String player1puuid,String player2puuid) {
        try {
            MatchDetailDto matchDetail = riotService.getMatchDetail(matchId);

            if (matchDetail == null || matchDetail.getInfo() == null || matchDetail.getInfo().getParticipants() == null) {
                logger.warn("Invalid match data for matchId: {}", matchId);
                return false;
            }

            //각 플레이어 팀의 ID 찾기
            Integer player1TeamId = null;
            Integer player2TeamId = null;

            for (ParticipantDto participant : matchDetail.getInfo().getParticipants()) {
                if (player1puuid.equals(participant.getPuuid())) {
                    player1TeamId = participant.getTeamId();
                }
                if (player2puuid.equals(participant.getPuuid())) {
                    player2TeamId = participant.getTeamId();
                }
            }

            //둘 다 팀 ID가 있고 같은 팀인지 확인
            boolean sameTeam = player1TeamId != null && player2TeamId != null && player1TeamId.equals(player2TeamId);

            logger.debug("Match {}: Player1 Team={}, Player2 Team={}, Same Team={}",
                    matchId, player1TeamId, player2TeamId, sameTeam);

            return sameTeam;

        } catch (Exception e) {
            logger.error("Failed to check team for match: {}", matchId, e);
            return false;
        }
    }

    /**
     * 매치 ID로 분석 조회
     */
    public List<DuoMatchAnalysis> getAnalysisByMatchId(String matchId) {
        return duoMatchAnalysisRepository.findByMatchIdOrderByCreatedAtDesc(matchId);
    }

    /**
     * 플레이어로 분석 조회 (player1 또는 player2)
     */
    public List<DuoMatchAnalysis> getAnalysisByPlayer(String puuid) {
        return duoMatchAnalysisRepository.findByPlayer1PuuidOrPlayer2PuuidOrderByCreatedAtDesc(puuid, puuid);
    }

    /**
     * 두 플레이어 조합으로 분석 조회
     */
    public List<DuoMatchAnalysis> getAnalysisByPlayers(String player1Puuid, String player2Puuid) {
        return duoMatchAnalysisRepository.findByPlayer1PuuidAndPlayer2PuuidOrPlayer1PuuidAndPlayer2PuuidOrderByCreatedAtDesc(
                player1Puuid, player2Puuid, player2Puuid, player1Puuid
        );
    }

    /**
     * 상태별 분석 조회
     */
    public List<DuoMatchAnalysis> getAnalysisByStatus(AnalysisStatus status) {
        return duoMatchAnalysisRepository.findByAnalysisStatusOrderByCreatedAtDesc(status);
    }

    /**
     * 상태별 페이징 조회
     */
    public List<DuoMatchAnalysis> getAnalysisByStatusWithPaging(AnalysisStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return duoMatchAnalysisRepository.findByAnalysisStatusOrderByCreatedAtDesc(status, pageable);
    }

    /**
     * 전체 분석 개수
     */
    public long getTotalCount() {
        return duoMatchAnalysisRepository.count();
    }

    /**
     * 완료된 분석 개수
     */
    public long getCompletedCount() {
        return duoMatchAnalysisRepository.countByAnalysisStatus(AnalysisStatus.COMPLETED);
    }

    /**
     * 대기 중인 분석 조회
     */
    public List<DuoMatchAnalysis> getPendingAnalysis() {
        return duoMatchAnalysisRepository.findByAnalysisStatusInOrderByCreatedAtAsc(
                Arrays.asList(AnalysisStatus.REQUESTED, AnalysisStatus.PROCESSING)
        );
    }

    /**
     * 실패한 분석 조회
     */
    public List<DuoMatchAnalysis> getFailedAnalysis() {
        return duoMatchAnalysisRepository.findByAnalysisStatusOrderByUpdatedAtDesc(AnalysisStatus.FAILED);
    }

    /**
     * 상태별 듀오 분석 개수 조회
     */
    public long countByAnalysisStatus(AnalysisStatus status) {
        return duoMatchAnalysisRepository.countByAnalysisStatus(status);
    }

}
