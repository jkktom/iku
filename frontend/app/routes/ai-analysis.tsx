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

interface SingleAnalysisResult {
  analysisRecord: {
    id: number;
    puuid: string;
    matchId: string;
    targetPlayerName: string;
    targetChampion?: string;
    matchDuration?: number;
    gameMode?: string;
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
    matchId: null;
    targetPlayerName: string;
    matchCount: number;
    analyzedMatchIds: string[];
    analysisPeriod: string;
    totalGamesFound: number;
    status: string;
    analysisSummary: string;
    updatedAt: string;
    aiResponseData: any;
  };
  message: string;
}

export default function AIAnalysis() {
  const apiFetch = useApi();
  const API_BASE_URL = typeof window !== 'undefined' 
    ? (window as any).ENV?.VITE_API_BASE_URL || process.env.VITE_API_BASE_URL || process.env.VITE_DEFAULT_API_URL || 'http://localhost:8081'
    : process.env.VITE_API_BASE_URL || process.env.VITE_DEFAULT_API_URL || 'http://localhost:8081';
  
  // Step 1: Get PUUID
  const [playerName, setPlayerName] = useState("");
  const [tagLine, setTagLine] = useState("");
  const [accountInfo, setAccountInfo] = useState<AccountInfo | null>(null);
  const [isLoadingAccount, setIsLoadingAccount] = useState(false);
  
  // Step 2: Get Match ID
  const [matchInfo, setMatchInfo] = useState<MatchInfo | null>(null);
  const [isLoadingMatches, setIsLoadingMatches] = useState(false);
  
  // Step 3-1: Single Match Analysis
  const [singleAnalysisResult, setSingleAnalysisResult] = useState<SingleAnalysisResult | null>(null);
  const [isLoadingSingleAnalysis, setIsLoadingSingleAnalysis] = useState(false);
  
  // Step 3-2: Multiple Match Analysis (5게임 고정)
  const [multipleAnalysisResult, setMultipleAnalysisResult] = useState<MultipleAnalysisResult | null>(null);
  const [isLoadingMultipleAnalysis, setIsLoadingMultipleAnalysis] = useState(false);
  
  const [error, setError] = useState<string>("");

  // 1단계: 계정 정보 조회 및 분석 초기화
  const handleGetAccount = async () => {
    if (!playerName || !tagLine) {
      setError("플레이어명과 태그를 모두 입력해주세요.");
      return;
    }

    setIsLoadingAccount(true);
    setError("");
    
    try {
      // 1. Riot API로 계정 정보 조회
      const accountResponse = await apiFetch(`${API_BASE_URL}/api/riot/account/${playerName}/${tagLine}`);
      
      if (accountResponse.account) {
        setAccountInfo(accountResponse.account);
        
        // 2. Analysis API로 초기 레코드 생성
        await apiFetch(`${API_BASE_URL}/api/analysis/init`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(accountResponse.account)
        });
        
        // 기존 결과 초기화
        setMatchInfo(null);
        setSingleAnalysisResult(null);
        setMultipleAnalysisResult(null);
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

  // 2단계: 최신 매치 조회 및 매치 ID 업데이트
  const handleGetMatches = async () => {
    if (!accountInfo) return;

    setIsLoadingMatches(true);
    setError("");

    try {
      // 1. Riot API로 매치 ID 조회
      const matchResponse = await apiFetch(`${API_BASE_URL}/api/riot/matches/${accountInfo.puuid}`);
      
      if (matchResponse.selectedMatchId) {
        // 2. Analysis API로 매치 ID 업데이트 (단일 분석용)
        await apiFetch(`/api/analysis/match/${accountInfo.puuid}/${matchResponse.selectedMatchId}`, {
          method: 'PUT'
        });
        
        setMatchInfo({
          matchIds: matchResponse.matchIds,
          selectedMatchId: matchResponse.selectedMatchId
        });
        
        // 기존 분석 결과 초기화
        setSingleAnalysisResult(null);
        setMultipleAnalysisResult(null);
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

  // 3-1단계: 단일 게임 AI 분석
  const handleSingleAIAnalysis = async () => {
    if (!accountInfo || !matchInfo) return;

    setIsLoadingSingleAnalysis(true);
    setError("");

    try {
      const response = await apiFetch(
        `/api/analysis/analyze/${accountInfo.puuid}/${matchInfo.selectedMatchId}`,
        { method: 'POST' }
      );
      
      setSingleAnalysisResult(response);
      setMultipleAnalysisResult(null); // 다중 분석 결과 초기화
    } catch (err) {
      setError("단일 게임 AI 분석에 실패했습니다.");
      console.error(err);
    } finally {
      setIsLoadingSingleAnalysis(false);
    }
  };

  // 3-2단계: 다중 게임 AI 분석 (5게임 고정)
  const handleMultipleAIAnalysis = async () => {
    if (!accountInfo) return;

    setIsLoadingMultipleAnalysis(true);
    setError("");

    try {
      const response = await apiFetch(
        `/api/analysis/analyze-multiple/${accountInfo.puuid}?matchCount=5`,
        { method: 'POST' }
      );
      
      setMultipleAnalysisResult(response);
      setSingleAnalysisResult(null); // 단일 분석 결과 초기화
    } catch (err) {
      setError("5게임 종합 분석에 실패했습니다.");
      console.error(err);
    } finally {
      setIsLoadingMultipleAnalysis(false);
    }
  };

  return (
    <div className="h-full bg-gray-50">
      <div className="p-6">
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-900">AI 게임 분석</h1>
          <p className="text-gray-600 mt-2">리그 오브 레전드 게임 플레이를 AI로 분석해보세요</p>
        </div>
        
        <div className="space-y-6">

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
          {error}
        </div>
      )}

      {/* Step 1: 계정 정보 조회 */}
      <Card>
        <CardHeader>
          <CardTitle>1단계: 계정 정보 조회</CardTitle>
          <CardDescription>플레이어명과 태그를 입력하여 분석을 시작합니다</CardDescription>
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
            {isLoadingAccount ? "조회 중..." : "1단계: 계정 조회 및 분석 초기화"}
          </Button>
          
          {accountInfo && (
            <div className="bg-green-50 border border-green-200 p-4 rounded">
              <p><strong>✅ 계정 조회 완료</strong></p>
              <p><strong>플레이어:</strong> {accountInfo.gameName}#{accountInfo.tagLine}</p>
              <p><strong>PUUID:</strong> {accountInfo.puuid}</p>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Step 2: 최신 게임 조회 */}
      {accountInfo && (
        <Card>
          <CardHeader>
            <CardTitle>2단계: 최신 게임 조회</CardTitle>
            <CardDescription>가장 최신 게임의 매치 정보를 가져옵니다</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <Button 
              onClick={handleGetMatches} 
              disabled={isLoadingMatches}
              className="w-full"
            >
              {isLoadingMatches ? "조회 중..." : "2단계: 최신 게임 조회"}
            </Button>
            
            {matchInfo && (
              <div className="bg-blue-50 border border-blue-200 p-4 rounded">
                <p><strong>✅ 매치 조회 완료</strong></p>
                <p><strong>최신 매치 ID:</strong> {matchInfo.selectedMatchId}</p>
                <p><strong>총 매치 수:</strong> {matchInfo.matchIds.length}개</p>
              </div>
            )}
          </CardContent>
        </Card>
      )}

      {/* Step 3: AI 분석 선택 */}
      {matchInfo && (
        <Card>
          <CardHeader>
            <CardTitle>3단계: AI 분석 선택</CardTitle>
            <CardDescription>단일 게임 분석 또는 5게임 종합 분석을 선택하세요</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {/* 3-1: 단일 게임 분석 */}
              <div className="space-y-3">
                <h4 className="font-semibold text-gray-900">3-1. 단일 게임 분석</h4>
                <p className="text-sm text-gray-600">최신 게임 1개에 대한 상세 분석</p>
                <Button 
                  onClick={handleSingleAIAnalysis} 
                  disabled={isLoadingSingleAnalysis}
                  className="w-full bg-blue-600 hover:bg-blue-700"
                >
                  {isLoadingSingleAnalysis ? "분석 중..." : "단일 게임 AI 분석"}
                </Button>
              </div>

              {/* 3-2: 다중 게임 분석 */}
              <div className="space-y-3">
                <h4 className="font-semibold text-gray-900">3-2. 종합 게임 분석</h4>
                <p className="text-sm text-gray-600">최근 5게임에 대한 종합 분석</p>
                <Button 
                  onClick={handleMultipleAIAnalysis} 
                  disabled={isLoadingMultipleAnalysis}
                  className="w-full bg-green-600 hover:bg-green-700"
                >
                  {isLoadingMultipleAnalysis ? "분석 중..." : "5게임 종합 AI 분석"}
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      {/* 단일 게임 분석 결과 */}
      {singleAnalysisResult && (
        <Card>
          <CardHeader>
            <CardTitle className="text-blue-700">단일 게임 분석 결과</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="bg-blue-50 border border-blue-200 p-4 rounded">
              <div className="space-y-3">
                <div className="flex justify-between items-start">
                  <div>
                    <p><strong>플레이어:</strong> {singleAnalysisResult.analysisRecord.targetPlayerName}</p>
                    <p><strong>매치 ID:</strong> {singleAnalysisResult.analysisRecord.matchId}</p>
                    {singleAnalysisResult.analysisRecord.targetChampion && (
                      <p><strong>사용 챔피언:</strong> {singleAnalysisResult.analysisRecord.targetChampion}</p>
                    )}
                    {singleAnalysisResult.analysisRecord.gameMode && (
                      <p><strong>게임 모드:</strong> {singleAnalysisResult.analysisRecord.gameMode}</p>
                    )}
                    {singleAnalysisResult.analysisRecord.matchDuration && (
                      <p><strong>게임 시간:</strong> {Math.floor(singleAnalysisResult.analysisRecord.matchDuration / 60)}분 {singleAnalysisResult.analysisRecord.matchDuration % 60}초</p>
                    )}
                  </div>
                  <div className="text-right">
                    <p><strong>상태:</strong> <span className="text-green-600">{singleAnalysisResult.analysisRecord.status}</span></p>
                    <p><strong>분석 완료:</strong> {new Date(singleAnalysisResult.analysisRecord.updatedAt).toLocaleString('ko-KR')}</p>
                  </div>
                </div>
                
                <div className="mt-4">
                  <strong>분석 결과:</strong>
                  <div className="bg-white p-4 rounded border mt-2 min-h-32 max-h-none w-full">
                    <div className="prose prose-sm max-w-none">
                      <ReactMarkdown>
                        {singleAnalysisResult.analysisRecord.aiResponseData?.analysisResult || 
                         singleAnalysisResult.analysisRecord.analysisSummary}
                      </ReactMarkdown>
                    </div>
                  </div>
                </div>
                
                {singleAnalysisResult.analysisRecord.aiResponseData && (
                  <div className="mt-4">
                    <strong>상세 데이터:</strong>
                    <div className="bg-gray-50 p-3 rounded border mt-2 max-h-60 overflow-y-auto">
                      <pre className="whitespace-pre-wrap text-xs break-words leading-relaxed">
                        {JSON.stringify(singleAnalysisResult.analysisRecord.aiResponseData, null, 2)}
                      </pre>
                    </div>
                  </div>
                )}
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      {/* 다중 게임 분석 결과 */}
      {multipleAnalysisResult && (
        <Card>
          <CardHeader>
            <CardTitle className="text-green-700">5게임 종합 분석 결과</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="bg-green-50 border border-green-200 p-4 rounded">
              <div className="space-y-3">
                <div className="flex justify-between items-start">
                  <div>
                    <p><strong>플레이어:</strong> {multipleAnalysisResult.analysisRecord.targetPlayerName}</p>
                    <p><strong>분석 기간:</strong> {multipleAnalysisResult.analysisRecord.analysisPeriod}</p>
                    <p><strong>분석된 게임 수:</strong> {multipleAnalysisResult.analysisRecord.matchCount}개</p>
                    <p><strong>총 조회된 게임:</strong> {multipleAnalysisResult.analysisRecord.totalGamesFound}개</p>
                  </div>
                  <div className="text-right">
                    <p><strong>상태:</strong> <span className="text-green-600">{multipleAnalysisResult.analysisRecord.status}</span></p>
                    <p><strong>분석 완료:</strong> {new Date(multipleAnalysisResult.analysisRecord.updatedAt).toLocaleString('ko-KR')}</p>
                  </div>
                </div>
                
                <div className="mt-4">
                  <strong>분석된 매치 ID 목록:</strong>
                  <div className="bg-white p-3 rounded border mt-2">
                    <div className="flex flex-wrap gap-2">
                      {multipleAnalysisResult.analysisRecord.analyzedMatchIds.map((matchId, index) => (
                        <span key={index} className="bg-gray-100 px-2 py-1 rounded text-xs font-mono">
                          {matchId}
                        </span>
                      ))}
                    </div>
                  </div>
                </div>
                
                <div className="mt-4">
                  <strong>종합 분석 결과:</strong>
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
          </CardContent>
        </Card>
      )}
        </div>
      </div>
    </div>
  );
}