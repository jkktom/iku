import { useState } from "react";
import { Button } from "~/components/ui/button";
import { Input } from "~/components/ui/input";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "~/components/ui/card";
import { Label } from "~/components/ui/label";
import { useApi } from "~/utils/api";

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

export default function AIAnalysis() {
  const apiFetch = useApi();
  
  // Step 1: Get PUUID
  const [playerName, setPlayerName] = useState("");
  const [tagLine, setTagLine] = useState("");
  const [accountInfo, setAccountInfo] = useState<AccountInfo | null>(null);
  const [isLoadingAccount, setIsLoadingAccount] = useState(false);
  
  // Step 2: Get Match ID
  const [matchInfo, setMatchInfo] = useState<MatchInfo | null>(null);
  const [isLoadingMatches, setIsLoadingMatches] = useState(false);
  
  // Step 3: AI Analysis
  const [analysisResult, setAnalysisResult] = useState<AnalysisResult | null>(null);
  const [isLoadingAnalysis, setIsLoadingAnalysis] = useState(false);
  
  const [error, setError] = useState<string>("");

  const handleGetAccount = async () => {
    if (!playerName || !tagLine) {
      setError("플레이어명과 태그를 모두 입력해주세요.");
      return;
    }

    setIsLoadingAccount(true);
    setError("");
    
    try {
      const response = await apiFetch(`http://localhost:8080/api/riot/account/${playerName}/${tagLine}`);
      
      if (response.account) {
        setAccountInfo(response.account);
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
      const response = await apiFetch(`http://localhost:8080/api/riot/matches/${accountInfo.puuid}`);
      
      if (response.selectedMatchId) {
        setMatchInfo({
          matchIds: response.matchIds,
          selectedMatchId: response.selectedMatchId
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
        `http://localhost:8080/api/riot/analyze/${accountInfo.puuid}/${matchInfo.selectedMatchId}`,
        { method: 'POST' }
      );
      
      setAnalysisResult(response);
    } catch (err) {
      setError("AI 분석에 실패했습니다.");
      console.error(err);
    } finally {
      setIsLoadingAnalysis(false);
    }
  };

  return (
    <div className="container mx-auto p-6 space-y-6">
      <div className="text-center mb-8">
        <h1 className="text-3xl font-bold">AI 게임 분석</h1>
        <p className="text-gray-600 mt-2">리그 오브 레전드 게임 플레이를 AI로 분석해보세요</p>
      </div>

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
            <CardTitle>3단계: AI 분석</CardTitle>
            <CardDescription>AI가 게임 플레이를 분석합니다</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <Button 
              onClick={handleAIAnalysis} 
              disabled={isLoadingAnalysis}
              className="w-full"
            >
              {isLoadingAnalysis ? "분석 중..." : "AI 분석 시작"}
            </Button>
            
            {analysisResult && (
              <div className="bg-purple-50 border border-purple-200 p-4 rounded">
                <h3 className="font-bold text-lg mb-2">분석 결과</h3>
                <div className="space-y-2">
                  <p><strong>플레이어:</strong> {analysisResult.analysisRecord.targetPlayerName}</p>
                  <p><strong>상태:</strong> {analysisResult.analysisRecord.status}</p>
                  <p><strong>AI 응답 시간:</strong> {new Date(analysisResult.analysisRecord.updatedAt).toLocaleString('ko-KR')}</p>
                  <div className="mt-4">
                    <strong>분석 요약:</strong>
                    <div className="bg-white p-4 rounded border mt-2 min-h-32 max-h-none w-full">
                      <div className="whitespace-pre-wrap text-sm break-words leading-relaxed">
                        {analysisResult.analysisRecord.aiResponseData?.analysisResult || 
                         analysisResult.analysisRecord.analysisSummary}
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
    </div>
  );
}