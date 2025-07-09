package org.mtvs.backend.analysis.service;

import org.mtvs.backend.riot.Repository.*;
import org.mtvs.backend.riot.dto.*;
import org.mtvs.backend.riot.entity.*;
import org.mtvs.backend.common.constants.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@Transactional
public class MatchDataPersister {
    
    private static final Logger logger = LoggerFactory.getLogger(MatchDataPersister.class);
    
    private final MatchRepository matchRepository;
    private final ParticipantRepository participantRepository;
    private final MatchTimelineRepository matchTimelineRepository;
    private final ParticipantFrameRepository participantFrameRepository;
    private final MatchEventRepository matchEventRepository;
    
    public MatchDataPersister(
            MatchRepository matchRepository,
            ParticipantRepository participantRepository,
            MatchTimelineRepository matchTimelineRepository,
            ParticipantFrameRepository participantFrameRepository,
            MatchEventRepository matchEventRepository) {
        this.matchRepository = matchRepository;
        this.participantRepository = participantRepository;
        this.matchTimelineRepository = matchTimelineRepository;
        this.participantFrameRepository = participantFrameRepository;
        this.matchEventRepository = matchEventRepository;
    }
    
    public void saveMatchData(String matchId, MatchDetailDto matchDetail, MatchTimelineDto matchTimeline) {
        try {
            logger.info("Saving match data for match ID: {}", matchId);
            
            if (matchRepository.existsById(matchId)) {
                logger.info("Match data already exists for ID: {}", matchId);
                return;
            }
            
            Match match = createMatchEntity(matchId, matchDetail);
            matchRepository.save(match);
            
            saveParticipants(match, matchDetail);
            saveMatchTimeline(match, matchTimeline);
            
            logger.info("Successfully saved match data for ID: {}", matchId);
            
        } catch (Exception e) {
            logger.error("Error saving match data for ID {}: {}", matchId, e.getMessage(), e);
            throw new RuntimeException("Failed to save match data: " + e.getMessage());
        }
    }
    
    private Match createMatchEntity(String matchId, MatchDetailDto matchDetail) {
        Match match = new Match();
        match.setMatchId(matchId);
        
        if (matchDetail.getInfo() != null) {
            InfoDto info = matchDetail.getInfo();
            match.setGameDuration(info.getGameDuration());
            match.setGameMode(info.getGameMode());
            match.setGameVersion(info.getGameVersion());
            match.setQueueId(info.getQueueId());
            
            // Note: InfoDto doesn't have gameStartTimestamp or gameEndTimestamp fields
            // These would need to be added to InfoDto if timestamp tracking is required
        }
        
        return match;
    }
    
    private void saveParticipants(Match match, MatchDetailDto matchDetail) {
        if (matchDetail.getInfo() == null || matchDetail.getInfo().getParticipants() == null) {
            return;
        }
        
        for (ParticipantDto participantDto : matchDetail.getInfo().getParticipants()) {
            Participant participant = createParticipantEntity(match, participantDto);
            participantRepository.save(participant);
        }
    }
    
    private Participant createParticipantEntity(Match match, ParticipantDto dto) {
        Participant participant = new Participant();
        participant.setMatch(match);
        participant.setPuuid(dto.getPuuid());
        participant.setParticipantId(dto.getParticipantId());
        participant.setChampionName(dto.getChampionName());
        participant.setSummonerName(dto.getSummonerName());
        participant.setRiotIdGameName(dto.getRiotIdGameName());
        participant.setRiotIdTagline(dto.getRiotIdTagline());
        participant.setKills(dto.getKills());
        participant.setDeaths(dto.getDeaths());
        participant.setAssists(dto.getAssists());
        participant.setTotalMinionsKilled(dto.getTotalMinionsKilled());
        participant.setNeutralMinionsKilled(dto.getNeutralMinionsKilled());
        participant.setGoldEarned(dto.getGoldEarned());
        participant.setTotalDamageDealtToChampions(dto.getTotalDamageDealtToChampions());
        participant.setVisionScore(dto.getVisionScore());
        // Note: ParticipantDto doesn't have wardsPlaced field, only visionScore
        // participant.setWardsPlaced() would need wardsPlaced field in ParticipantDto
        participant.setWin(dto.isWin());
        
        return participant;
    }
    
    private void saveMatchTimeline(Match match, MatchTimelineDto matchTimeline) {
        if (matchTimeline.getInfo() == null || matchTimeline.getInfo().getFrames() == null) {
            return;
        }
        
        for (FrameDto frameDto : matchTimeline.getInfo().getFrames()) {
            // Create MatchTimeline for each frame with composite key
            MatchTimeline timeline = new MatchTimeline();
            timeline.setMatch(match);
            timeline.setMatchId(match.getMatchId());
            timeline.setTimestamp(frameDto.getTimestamp());
            
            timeline = matchTimelineRepository.save(timeline);
            saveFrameData(timeline, frameDto);
        }
    }
    
    private void saveFrameData(MatchTimeline timeline, FrameDto frameDto) {
        if (frameDto.getParticipantFrames() != null) {
            frameDto.getParticipantFrames().forEach((participantId, frameData) -> {
                ParticipantFrame participantFrame = new ParticipantFrame();
                participantFrame.setTimeline(timeline);
                
                // Set composite key fields
                participantFrame.setMatchId(timeline.getMatchId());
                participantFrame.setTimestamp(timeline.getTimestamp());
                participantFrame.setParticipantId((byte) Integer.parseInt(participantId));
                
                if (frameData.getPosition() != null) {
                    participantFrame.setX(frameData.getPosition().getX());
                    participantFrame.setY(frameData.getPosition().getY());
                }
                
                // Note: ParticipantFrame entity has totalGold but not currentGold
                // participantFrame.setCurrentGold(frameData.getCurrentGold());
                participantFrame.setTotalGold(frameData.getTotalGold());
                participantFrame.setLevel(frameData.getLevel());
                // Note: ParticipantFrame entity doesn't have xp field
                // participantFrame.setXp(frameData.getXp());
                participantFrame.setMinionsKilled(frameData.getMinionsKilled());
                participantFrame.setJungleMinionsKilled(frameData.getJungleMinionsKilled());
                
                participantFrameRepository.save(participantFrame);
            });
        }
        
        if (frameDto.getEvents() != null) {
            for (int eventIndex = 0; eventIndex < frameDto.getEvents().size(); eventIndex++) {
                EventDto eventDto = frameDto.getEvents().get(eventIndex);
                saveEventData(timeline, eventDto, (short) eventIndex);
            }
        }
    }
    
    private void saveEventData(MatchTimeline timeline, EventDto eventDto, short sequenceId) {
        MatchEvent event = new MatchEvent();
        event.setTimeline(timeline);
        
        // Set composite key fields
        event.setMatchId(timeline.getMatchId());
        event.setTimestamp(timeline.getTimestamp()); // Use timeline timestamp for consistency
        event.setSequenceId(sequenceId);
        // Convert event type string to byte ID
        event.setEventTypeId(EventType.getEventTypeId(eventDto.getType()));
        event.setParticipantId(eventDto.getParticipantId() != null ? eventDto.getParticipantId() : 0);
        event.setKillerId(eventDto.getKillerId() != null ? eventDto.getKillerId() : 0);
        event.setVictimId(eventDto.getVictimId() != null ? eventDto.getVictimId() : 0);
        
        if (eventDto.getAssistingParticipantIds() != null) {
            event.setAssistingParticipantIds(eventDto.getAssistingParticipantIds());
        }
        
        // Note: MatchEvent entity doesn't have position fields
        // if (eventDto.getPosition() != null) {
        //     event.setPositionX(eventDto.getPosition().getX());
        //     event.setPositionY(eventDto.getPosition().getY());
        // }
        
        event.setItemId(eventDto.getItemId() != null ? eventDto.getItemId() : 0);
        // Note: MatchEvent entity doesn't have these fields
        // event.setSkillSlot(eventDto.getSkillSlot());
        // event.setLevelUpType(eventDto.getLevelUpType());
        // event.setWardType(eventDto.getWardType());
        // event.setCreatorId(eventDto.getCreatorId());
        
        matchEventRepository.save(event);
    }
    
    public boolean isMatchDataExists(String matchId) {
        return matchRepository.existsById(matchId);
    }
}