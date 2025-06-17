package org.mtvs.backend.riot.service;

import org.mtvs.backend.riot.dto.AccountDto;
import org.mtvs.backend.riot.dto.MatchDetailDto;
import org.mtvs.backend.riot.dto.MatchTimelineDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.support.UriComponentsContributor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
public class RiotService {
    private final RestTemplate restTemplate;

    @Value("${riot.api.key}")
    private String apikey;

    private static final String ASIA_BASE_URL = "https://asia.api.riotgames.com";
    private static final Map<Integer, String> QUEUE_NAMES = Map.of(
            400, "일반 게임 (드래프트 픽)",
            420, "솔로 랭크",
            430, "일반 게임 (무작위 총력전)",
            440, "자유 랭크",
            450, "칼바람 나락",
            490, "빠른 대전",
            700, "격전"
    );

    public RiotService(RestTemplate restTemplate, UriComponentsContributor uriComponentsContributor) {
        this.restTemplate = restTemplate;
    }

    public AccountDto getAccountInfo(String gameName, String tagLine) {
        String url = UriComponentsBuilder.fromHttpUrl(ASIA_BASE_URL)
                .path("/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}")
                .buildAndExpand(gameName, tagLine)
                .toUriString();

        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                createHttpEntity(), //이게 머지
                AccountDto.class
        ).getBody();
    }

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

    public MatchDetailDto getMatchDetail(String matchId) {
        String url = UriComponentsBuilder.fromHttpUrl(ASIA_BASE_URL)
                .path("/lol/match/v5/matches/{matchId}")
                .buildAndExpand(matchId)
                .toUriString();

        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                createHttpEntity(),
                MatchDetailDto.class
        ).getBody();
    }

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

    private HttpEntity<String> createHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Riot-Token", apikey);
        return new HttpEntity<>(headers);
    }

    public String getQueueName(int queueId) {
        return QUEUE_NAMES.getOrDefault(queueId, "알 수 없는 큐 (" + queueId + ")");
    }
}
