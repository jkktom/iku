-- DuoMatchAnalysis 테이블의 기존 데이터에 puuid 값 설정
-- 듀오 분석은 두 플레이어가 있으므로 player1_puuid 값을 puuid로 사용

-- 먼저 puuid가 null인 레코드들을 확인
-- SELECT id, match_id, player1_puuid, player2_puuid, puuid FROM duo_match_analysis WHERE puuid IS NULL;

-- player1_puuid 값을 puuid로 설정 (기존 데이터 마이그레이션)
UPDATE duo_match_analysis 
SET puuid = player1_puuid 
WHERE puuid IS NULL AND player1_puuid IS NOT NULL;

-- 만약 player1_puuid도 null이면 player2_puuid 사용
UPDATE duo_match_analysis 
SET puuid = player2_puuid 
WHERE puuid IS NULL AND player2_puuid IS NOT NULL;

-- 마이그레이션 후 결과 확인용 쿼리 (주석 해제해서 사용)
-- SELECT id, match_id, player1_puuid, player2_puuid, puuid FROM duo_match_analysis WHERE puuid IS NULL;
-- SELECT COUNT(*) as total_records, COUNT(puuid) as records_with_puuid FROM duo_match_analysis;