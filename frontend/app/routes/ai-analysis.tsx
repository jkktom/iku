import { useState } from "react";
import { Button } from "~/components/ui/button";
import { Input } from "~/components/ui/input";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "~/components/ui/card";
import { Label } from "~/components/ui/label";
import { useApi } from "~/utils/api";
import ReactMarkdown from "react-markdown";

interface AccountInfo {
  puuid: string;
  gameName: string;
  tagLine: string;
}

interface MatchInfo {
  matchIds: string[];
  selectedMatchId: string;
}

interface AnalysisResult {
  analysisRecord: {
    id: number;
    puuid: string;
    matchId: string;
    targetPlayerName: string;
    status: string;
    analysisSummary: string;
    updatedAt: string;
    aiResponseData: any;
  };
  message: string;
}

interface MultipleAnalysisResult {
  analysisRecord: {
    id: number;
    puuid: string;
    matchId: string | null;  // 다중 매치 분석에서는 null
    targetPlayerName: string;
    status: string;
    analysisSummary: string;
    updatedAt: string;
    aiResponseData: any;
  };
  message: string;
}

export default function AIAnalysis() {
  const apiFetch = useApi();
  
  // Step 1: Get PUUID
  const [playerName, setPlayerName] = useState("");
  const [tagLine, setTagLine] = useState("");
  const [accountInfo, setAccountInfo] = useState<AccountInfo | null>(null);
  const [isLoadingAccount, setIsLoadingAccount] = useState(false);
  const [analysisId, setAnalysisId] = useState<number | null>(null);
  
  // Step 2: Get Match ID
  const [matchInfo, setMatchInfo] = useState<MatchInfo | null>(null);
  const [isLoadingMatches, setIsLoadingMatches] = useState(false);
  
  // Step 3: AI Analysis
  const [analysisResult, setAnalysisResult] = useState<AnalysisResult | null>(null);
  const [isLoadingAnalysis, setIsLoadingAnalysis] = useState(false);
  
  // Step 4: Multiple Match Analysis
  const [multipleAnalysisResult, setMultipleAnalysisResult] = useState<MultipleAnalysisResult | null>(null);
  const [isLoadingMultipleAnalysis, setIsLoadingMultipleAnalysis] = useState(false);
  const [matchCount, setMatchCount] = useState<number>(5);
  
  const [error, setError] = useState<string>("");

  const handleGetAccount = async () => {
    if (!playerName || !tagLine) {
      setError("플레이어명과 태그를 모두 입력해주세요.");
      return;
    }

    setIsLoadingAccount(true);
    setError("");
    
    try {
      // 1. Riot API로 계정 정보 조회 (순수 API)
      const accountResponse = await apiFetch(`http://localhost:8080/api/riot/account/${playerName}/${tagLine}`);
      
      if (accountResponse.account) {
        setAccountInfo(accountResponse.account);
        
        // 2. Single Match Analysis API로 초기 레코드 생성
        const createResponse = await apiFetch(`http://localhost:8080/api/singleanalysis/create/${accountResponse.account.puuid}`, {
          method: 'POST'
        });
        
        if (createResponse.analysisId) {
          setAnalysisId(createResponse.analysisId);
        }
        
        setMatchInfo(null);
        setAnalysisResult(null);
      } else {
        setError("계정 정보를 찾을 수 없습니다.");
      }
    } catch (err) {
      setError("계정 정보 조회에 실패했습니다.");
      console.error(err);
    } finally {
      setIsLoadingAccount(false);
    }
  };

  const handleGetMatches = async () => {
    if (!accountInfo) return;

    setIsLoadingMatches(true);
    setError("");

    try {
      // 1. Riot API로 매치 ID 조회 (순수 API)
      const matchResponse = await apiFetch(`http://localhost:8080/api/riot/matches/${accountInfo.puuid}`);
      
      if (matchResponse.selectedMatchId) {
        // 2. Single Match Analysis API로 매치 ID 업데이트
        if (analysisId) {
          await apiFetch(`http://localhost:8080/api/singleanalysis/match/${analysisId}?matchId=${matchResponse.selectedMatchId}`, {
            method: 'PUT'
          });
        }
        
        setMatchInfo({
          matchIds: matchResponse.matchIds,
          selectedMatchId: matchResponse.selectedMatchId
        });
        setAnalysisResult(null);
      } else {
        setError("매치 정보를 찾을 수 없습니다.");
      }
    } catch (err) {
      setError("매치 정보 조회에 실패했습니다.");
      console.error(err);
    } finally {
      setIsLoadingMatches(false);
    }
  };

  const handleAIAnalysis = async () => {
    if (!accountInfo || !matchInfo) return;

    setIsLoadingAnalysis(true);
    setError("");

    try {
      const response = await apiFetch(
        `http://localhost:8080/api/singleanalysis/analyze/${accountInfo.puuid}/${matchInfo.selectedMatchId}`,
        { method: 'POST' }
      );
      
      setAnalysisResult(response);
      setMultipleAnalysisResult(null); // 단일 분석 시 다중 분석 결과 초기화
    } catch (err) {
      setError("AI 분석에 실패했습니다.");
      console.error(err);
    } finally {
      setIsLoadingAnalysis(false);
    }
  };

  const handleMultipleAIAnalysis = async () => {
    if (!accountInfo) return;

    // 매치 개수 검증
    if (matchCount < 1 || matchCount > 5) {
      setError("매치 개수는 1~5개만 선택 가능합니다.");
      return;
    }

    setIsLoadingMultipleAnalysis(true);
    setError("");

    try {
      const response = await apiFetch(
        `http://localhost:8080/api/multianalysis/analyze/${accountInfo.puuid}?matchCount=${matchCount}`,
        { method: 'POST' }
      );
      
      setMultipleAnalysisResult(response);
      setAnalysisResult(null); // 다중 분석 시 단일 분석 결과 초기화
    } catch (err) {
      setError(`${matchCount}개 게임 종합 분석에 실패했습니다.`);
      console.error(err);
    } finally {
      setIsLoadingMultipleAnalysis(false);
    }
  };

  return (
    <div className="h-full bg-gray-50">
      <div className="p-6">
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-900">새로운 분석 요청하기</h1>
          <p className="text-gray-600 mt-2">리그 오브 레전드 게임 플레이를 AI로 분석해보세요</p>
        </div>
        
        <div className="space-y-6">

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
          {error}
        </div>
      )}

      {/* Step 1: Get PUUID */}
      <Card>
        <CardHeader>
          <CardTitle>1단계: 계정 정보 조회</CardTitle>
          <CardDescription>플레이어명과 태그를 입력하여 PUUID를 조회합니다</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <Label htmlFor="playerName">플레이어명</Label>
              <Input
                id="playerName"
                value={playerName}
                onChange={(e) => setPlayerName(e.target.value)}
                placeholder="예: Hide on bush"
              />
            </div>
            <div>
              <Label htmlFor="tagLine">태그</Label>
              <Input
                id="tagLine"
                value={tagLine}
                onChange={(e) => setTagLine(e.target.value)}
                placeholder="예: KR1"
              />
            </div>
          </div>
          <Button 
            onClick={handleGetAccount} 
            disabled={isLoadingAccount}
            className="w-full"
          >
            {isLoadingAccount ? "조회 중..." : "PUUID 조회"}
          </Button>
          
          {accountInfo && (
            <div className="bg-green-50 border border-green-200 p-4 rounded">
              <p><strong>PUUID:</strong> {accountInfo.puuid}</p>
              <p><strong>플레이어명:</strong> {accountInfo.gameName}#{accountInfo.tagLine}</p>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Step 2: Get Match ID */}
      {accountInfo && (
        <Card>
          <CardHeader>
            <CardTitle>2단계: 최신 게임 조회</CardTitle>
            <CardDescription>가장 최신 게임의 매치 ID를 조회합니다</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <Button 
              onClick={handleGetMatches} 
              disabled={isLoadingMatches}
              className="w-full"
            >
              {isLoadingMatches ? "조회 중..." : "최신 게임 조회"}
            </Button>
            
            {matchInfo && (
              <div className="bg-blue-50 border border-blue-200 p-4 rounded">
                <p><strong>매치 ID:</strong> {matchInfo.selectedMatchId}</p>
                <p><strong>총 매치 수:</strong> {matchInfo.matchIds.length}개</p>
              </div>
            )}
          </CardContent>
        </Card>
      )}

      {/* Step 3: AI Analysis */}
      {matchInfo && (
        <Card>
          <CardHeader>
            <CardTitle>3단계: 단일 게임 AI 분석</CardTitle>
            <CardDescription>선택된 최신 게임의 상세 분석을 수행합니다</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <Button 
              onClick={handleAIAnalysis} 
              disabled={isLoadingAnalysis}
              className="w-full"
            >
              {isLoadingAnalysis ? "분석 중..." : "단일 게임 AI 분석"}
            </Button>
            
            {analysisResult && (
              <div className="bg-purple-50 border border-purple-200 p-4 rounded">
                <h3 className="font-bold text-lg mb-2">단일 게임 분석 결과</h3>
                <div className="space-y-2">
                  <p><strong>플레이어:</strong> {analysisResult.analysisRecord.targetPlayerName}</p>
                  <p><strong>상태:</strong> {analysisResult.analysisRecord.status}</p>
                  <p><strong>AI 응답 시간:</strong> {new Date(analysisResult.analysisRecord.updatedAt).toLocaleString('ko-KR')}</p>
                  <div className="mt-4">
                    <strong>분석 요약:</strong>
                    <div className="bg-white p-4 rounded border mt-2 min-h-32 max-h-none w-full">
                      <div className="prose prose-sm max-w-none">
                        <ReactMarkdown>
                          {analysisResult.analysisRecord.aiResponseData?.analysisResult || 
                           analysisResult.analysisRecord.analysisSummary}
                        </ReactMarkdown>
                      </div>
                    </div>
                  </div>
                  
                  {analysisResult.analysisRecord.aiResponseData && (
                    <div className="mt-4">
                      <strong>AI 응답 데이터:</strong>
                      <div className="bg-gray-50 p-3 rounded border mt-2 min-h-32 max-h-none">
                        <pre className="whitespace-pre-wrap text-xs break-words leading-relaxed overflow-x-auto">
                          {JSON.stringify(analysisResult.analysisRecord.aiResponseData, null, 2)}
                        </pre>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      )}

      {/* Step 4: Multiple Match Analysis */}
      {accountInfo && (
        <Card>
          <CardHeader>
            <CardTitle>4단계: 다중 게임 종합 분석</CardTitle>
            <CardDescription>최근 여러 게임을 종합하여 상세 분석합니다 (1~5개)</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <Label htmlFor="matchCount">분석할 게임 수</Label>
              <select 
                id="matchCount"
                value={matchCount} 
                onChange={(e) => setMatchCount(Number(e.target.value))}
                className="w-full p-2 border border-gray-300 rounded mt-1"
              >
                <option value={1}>1개 게임</option>
                <option value={2}>2개 게임</option>
                <option value={3}>3개 게임</option>
                <option value={4}>4개 게임</option>
                <option value={5}>5개 게임 (추천)</option>
              </select>
            </div>
            
            <Button 
              onClick={handleMultipleAIAnalysis} 
              disabled={isLoadingMultipleAnalysis}
              className="w-full bg-green-600 hover:bg-green-700"
            >
              {isLoadingMultipleAnalysis ? `${matchCount}개 게임 분석 중...` : `${matchCount}개 게임 종합 분석`}
            </Button>
            
            {multipleAnalysisResult && (
              <div className="bg-green-50 border border-green-200 p-4 rounded">
                <h3 className="font-bold text-lg mb-2">종합 분석 결과</h3>
                <div className="space-y-2">
                  <p><strong>플레이어:</strong> {multipleAnalysisResult.analysisRecord.targetPlayerName}</p>
                  <p><strong>상태:</strong> {multipleAnalysisResult.analysisRecord.status}</p>
                  <p><strong>분석 완료 시간:</strong> {new Date(multipleAnalysisResult.analysisRecord.updatedAt).toLocaleString('ko-KR')}</p>
                  <p><strong>메시지:</strong> {multipleAnalysisResult.message}</p>
                  <div className="mt-4">
                    <strong>종합 분석 요약:</strong>
                    <div className="bg-white p-4 rounded border mt-2 min-h-32 max-h-none w-full">
                      <div className="prose prose-sm max-w-none">
                        <ReactMarkdown>
                          {multipleAnalysisResult.analysisRecord.aiResponseData?.analysisResult || 
                           multipleAnalysisResult.analysisRecord.analysisSummary}
                        </ReactMarkdown>
                      </div>
                    </div>
                  </div>
                  
                  {multipleAnalysisResult.analysisRecord.aiResponseData && (
                    <div className="mt-4">
                      <strong>상세 분석 데이터:</strong>
                      <div className="bg-gray-50 p-3 rounded border mt-2 max-h-60 overflow-y-auto">
                        <pre className="whitespace-pre-wrap text-xs break-words leading-relaxed">
                          {JSON.stringify(multipleAnalysisResult.analysisRecord.aiResponseData, null, 2)}
                        </pre>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      )}
        </div>
      </div>
    </div>
  );
}