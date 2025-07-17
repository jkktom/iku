import { useState, useEffect } from "react";
import { Link } from "@remix-run/react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "~/components/ui/card";
import { Button } from "~/components/ui/button";
import { Input } from "~/components/ui/input";
import { Label } from "~/components/ui/label";
import { Badge } from "~/components/ui/badge";
import { useApi } from "~/utils/api";
import ReactMarkdown from "react-markdown";
import { Clock, User, Trophy, AlertCircle, CheckCircle, Loader, FileText, Target, Users } from "lucide-react";

interface SingleAnalysis {
  id: number;
  puuid: string;
  matchId: string;
  targetPlayerName: string;
  targetChampion?: string;
  matchDuration?: number;
  gameMode?: string;
  analysisStatus: 'REQUESTED' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  analysisSummary: string;
  aiResponseData: any;
  createdAt: string;
  updatedAt: string;
  errorMessage?: string;
}

interface MultipleAnalysis {
  id: number;
  puuid: string;
  targetPlayerName: string;
  matchCount: number;
  analyzedMatchIds: string[];
  analysisPeriod: string;
  totalGamesFound: number;
  analysisStatus: 'REQUESTED' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  analysisSummary: string;
  aiResponseData: any;
  createdAt: string;
  updatedAt: string;
  errorMessage?: string;
}

// 듀오 분석 인터페이스
interface DuoAnalysis {
  id: number;
  matchId: string;
  player1Name: string;
  player2Name: string;
  player1Champion: string;
  player2Champion: string;
  analysisStatus: 'REQUESTED' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  analysisSummary: string;
  createdAt: string;
  updatedAt: string;
  errorMessage?: string;
}

type AnalysisType = 'SINGLE' | 'MULTIPLE' | 'DUO';

export default function MatchHistory() {
  const apiFetch = useApi();
  const [activeTab, setActiveTab] = useState<AnalysisType>('SINGLE');
  const [singleAnalyses, setSingleAnalyses] = useState<SingleAnalysis[]>([]);
  const [multipleAnalyses, setMultipleAnalyses] = useState<MultipleAnalysis[]>([]);
  const [duoAnalyses, setDuoAnalyses] = useState<DuoAnalysis[]>([]);
  const [filteredSingleAnalyses, setFilteredSingleAnalyses] = useState<SingleAnalysis[]>([]);
  const [filteredMultipleAnalyses, setFilteredMultipleAnalyses] = useState<MultipleAnalysis[]>([]);
  const [filteredDuoAnalyses, setFilteredDuoAnalyses] = useState<DuoAnalysis[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedSingleAnalysis, setSelectedSingleAnalysis] = useState<SingleAnalysis | null>(null);
  const [selectedMultipleAnalysis, setSelectedMultipleAnalysis] = useState<MultipleAnalysis | null>(null);
  const [selectedDuoAnalysis, setSelectedDuoAnalysis] = useState<DuoAnalysis | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');

  // 단일 분석 기록 로드
  const loadSingleAnalyses = async () => {
    try {
      const response = await apiFetch('/api/analysis/status/COMPLETED?page=0&size=20');
      const singleResults = response.content?.filter((item: any) => item.analysisType === 'SINGLE') || [];
      setSingleAnalyses(singleResults);
      setFilteredSingleAnalyses(singleResults);
    } catch (err) {
      console.error('단일 분석 기록 로드 실패:', err);
    }
  };

  // 다중 분석 기록 로드
  const loadMultipleAnalyses = async () => {
    try {
      const response = await apiFetch('/api/analysis/status/COMPLETED?page=0&size=20');
      const multipleResults = response.content?.filter((item: any) => item.analysisType === 'MULTIPLE') || [];
      setMultipleAnalyses(multipleResults);
      setFilteredMultipleAnalyses(multipleResults);
    } catch (err) {
      console.error('다중 분석 기록 로드 실패:', err);
    }
  };

  // 듀오 분석 기록 로드
  const loadDuoAnalyses = async () => {
    try {
      const response = await apiFetch('/api/analysis/duo/status/COMPLETED?page=0&size=20');
      const duoResults = response.content || [];
      setDuoAnalyses(duoResults);
      setFilteredDuoAnalyses(duoResults);
    } catch (err) {
      console.error('듀오 분석 기록 로드 실패:', err);
    }
  };

  // 전체 분석 기록 로드
  const loadAllAnalyses = async () => {
    console.log('loadAllAnalyses 함수 호출됨');
    setIsLoading(true);
    setError(null);
    try {
      console.log('API 호출 시작');
      await Promise.all([loadSingleAnalyses(), loadMultipleAnalyses(), loadDuoAnalyses()]); // 듀오 분석 로드 추가
      console.log('API 호출 완료');
    } catch (err) {
      console.error('API 호출 오류:', err);
      setError('분석 기록을 불러오는데 실패했습니다.');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    console.log('컴포넌트 마운트됨, API 호출 시작');
    loadAllAnalyses();
  }, []);

  // 단일 분석 필터링
  useEffect(() => {
    let filtered = singleAnalyses;

    if (statusFilter !== 'ALL') {
      filtered = filtered.filter(analysis => analysis.analysisStatus === statusFilter);
    }

    if (searchTerm) {
      filtered = filtered.filter(analysis =>
          analysis.targetPlayerName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
          analysis.targetChampion?.toLowerCase().includes(searchTerm.toLowerCase()) ||
          analysis.matchId?.includes(searchTerm)
      );
    }

    setFilteredSingleAnalyses(filtered);
  }, [singleAnalyses, statusFilter, searchTerm]);

  // 다중 분석 필터링
  useEffect(() => {
    let filtered = multipleAnalyses;

    if (statusFilter !== 'ALL') {
      filtered = filtered.filter(analysis => analysis.analysisStatus === statusFilter);
    }

    if (searchTerm) {
      filtered = filtered.filter(analysis =>
          analysis.targetPlayerName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
          analysis.analysisPeriod?.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    setFilteredMultipleAnalyses(filtered);
  }, [multipleAnalyses, statusFilter, searchTerm]);

  //
  // 듀오 분석 필터링
  useEffect(() => {
    let filtered = duoAnalyses;

    if (statusFilter !== 'ALL') {
      filtered = filtered.filter(analysis => analysis.analysisStatus === statusFilter);
    }

    if (searchTerm) {
      filtered = filtered.filter(analysis =>
          analysis.player1Name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
          analysis.player2Name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
          analysis.player1Champion?.toLowerCase().includes(searchTerm.toLowerCase()) ||
          analysis.player2Champion?.toLowerCase().includes(searchTerm.toLowerCase()) ||
          analysis.matchId?.includes(searchTerm)
      );
    }

    setFilteredDuoAnalyses(filtered);
  }, [duoAnalyses, statusFilter, searchTerm]);

  // 탭 변경 시 선택된 분석 초기화
  useEffect(() => {
    setSelectedSingleAnalysis(null);
    setSelectedMultipleAnalysis(null);
    setSelectedDuoAnalysis(null);
  }, [activeTab]);

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'COMPLETED':
        return <Badge className="bg-green-100 text-green-800"><CheckCircle className="h-3 w-3 mr-1" />완료</Badge>;
      case 'PROCESSING':
        return <Badge className="bg-blue-100 text-blue-800"><Loader className="h-3 w-3 mr-1" />처리중</Badge>;
      case 'FAILED':
        return <Badge className="bg-red-100 text-red-800"><AlertCircle className="h-3 w-3 mr-1" />실패</Badge>;
      case 'REQUESTED':
        return <Badge className="bg-yellow-100 text-yellow-800"><Clock className="h-3 w-3 mr-1" />대기</Badge>;
      default:
        return <Badge>{status}</Badge>;
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const formatGameDuration = (seconds: number) => {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes}분 ${remainingSeconds}초`;
  };

  // 현재 분석 목록 결정
  const getCurrentAnalyses = () => {
    switch (activeTab) {
      case 'SINGLE':
        return filteredSingleAnalyses;
      case 'MULTIPLE':
        return filteredMultipleAnalyses;
      case 'DUO':
        return filteredDuoAnalyses;
      default:
        return [];
    }
  };

  // 현재 선택된 분석 결정
  const getSelectedAnalysis = () => {
    switch (activeTab) {
      case 'SINGLE':
        return selectedSingleAnalysis;
      case 'MULTIPLE':
        return selectedMultipleAnalysis;
      case 'DUO':
        return selectedDuoAnalysis;
      default:
        return null;
    }
  };

  const currentAnalyses = getCurrentAnalyses();
  const selectedAnalysis = getSelectedAnalysis();

  return (
      <div className="h-full bg-gray-50">
        <div className="p-6">
          <div className="mb-6">
            <h1 className="text-2xl font-bold text-gray-900">분석 기록</h1>
            <p className="text-gray-600 mt-2">저장된 게임 분석 기록을 확인하세요</p>
          </div>

          {/* 탭 메뉴 - 친구와 비교 탭 */}
          <div className="mb-6">
            <div className="border-b border-gray-200">
              <nav className="-mb-px flex space-x-8">
                <button
                    onClick={() => setActiveTab('SINGLE')}
                    className={`py-2 px-1 border-b-2 font-medium text-sm ${
                        activeTab === 'SINGLE'
                            ? 'border-blue-500 text-blue-600'
                            : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                    }`}
                >
                  <FileText className="h-4 w-4 inline mr-2" />
                  단일 게임 분석 ({filteredSingleAnalyses.length})
                </button>
                <button
                    onClick={() => setActiveTab('MULTIPLE')}
                    className={`py-2 px-1 border-b-2 font-medium text-sm ${
                        activeTab === 'MULTIPLE'
                            ? 'border-green-500 text-green-600'
                            : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                    }`}
                >
                  <Target className="h-4 w-4 inline mr-2" />
                  다중 게임 분석 ({filteredMultipleAnalyses.length})
                </button>
                {/* 친구와 비교 탭 */}
                <button
                    onClick={() => setActiveTab('DUO')}
                    className={`py-2 px-1 border-b-2 font-medium text-sm ${
                        activeTab === 'DUO'
                            ? 'border-purple-500 text-purple-600'
                            : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                    }`}
                >
                  <Users className="h-4 w-4 inline mr-2" />
                  친구와 비교 ({filteredDuoAnalyses.length})
                </button>
              </nav>
            </div>
          </div>

          {/* 필터 */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
            <div>
              <Label htmlFor="search">검색</Label>
              <Input
                  id="search"
                  placeholder={
                    activeTab === 'SINGLE' ? "플레이어명, 챔피언, 매치ID 검색..." :
                        activeTab === 'MULTIPLE' ? "플레이어명, 분석 기간 검색..." :
                            "플레이어명, 챔피언, 매치ID 검색..."
                  }
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
            <div>
              <Label htmlFor="status">상태 필터</Label>
              <select
                  id="status"
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="ALL">모든 상태</option>
                <option value="COMPLETED">완료</option>
                <option value="PROCESSING">처리중</option>
                <option value="FAILED">실패</option>
                <option value="REQUESTED">대기</option>
              </select>
            </div>
            <div className="flex items-end">
              <Button onClick={loadAllAnalyses} disabled={isLoading}>
                {isLoading ? "새로고침 중..." : "새로고침"}
              </Button>
            </div>
          </div>

          {error && (
              <div className="mb-6 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
                {error}
              </div>
          )}

          {isLoading ? (
              <div className="text-center py-12">
                <Loader className="h-8 w-8 animate-spin mx-auto mb-4" />
                <p>분석 기록을 불러오는 중...</p>
              </div>
          ) : (
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {/* 분석 목록 */}
                <div className="space-y-4">
                  <h2 className="text-lg font-semibold">
                    {activeTab === 'SINGLE' ? '단일 게임 분석' :
                        activeTab === 'MULTIPLE' ? '다중 게임 분석' :
                            '친구와 비교'} ({currentAnalyses.length}개)
                  </h2>

                  {currentAnalyses.length === 0 ? (
                      <Card>
                        <CardContent className="py-8 text-center">
                          <p className="text-gray-500">분석 기록이 없습니다.</p>
                          <Link to={activeTab === 'DUO' ? "/duo-comparison" : "/ai-analysis"} className="mt-4 inline-block">
                            <Button>새 분석 시작하기</Button>
                          </Link>
                        </CardContent>
                      </Card>
                  ) : (
                      currentAnalyses.map((analysis) => (
                          <Card
                              key={analysis.id}
                              className={`cursor-pointer transition-all hover:shadow-md ${
                                  selectedAnalysis?.id === analysis.id ? 'ring-2 ring-blue-500' : ''
                              } ${
                                  activeTab === 'SINGLE' ? 'border-l-4 border-l-blue-500' :
                                      activeTab === 'MULTIPLE' ? 'border-l-4 border-l-green-500' :
                                          'border-l-4 border-l-purple-500'
                              }`}
                              onClick={() => {
                                if (activeTab === 'SINGLE') {
                                  setSelectedSingleAnalysis(analysis as SingleAnalysis);
                                } else if (activeTab === 'MULTIPLE') {
                                  setSelectedMultipleAnalysis(analysis as MultipleAnalysis);
                                } else if (activeTab === 'DUO') {
                                  setSelectedDuoAnalysis(analysis as DuoAnalysis);
                                }
                              }}
                          >
                            <CardHeader>
                              <div className="flex justify-between items-start">
                                <div>
                                  <CardTitle className="text-base">
                                    {activeTab === 'DUO' ?
                                        `${(analysis as DuoAnalysis).player1Name} vs ${(analysis as DuoAnalysis).player2Name}` :
                                        (analysis as SingleAnalysis | MultipleAnalysis).targetPlayerName || '알 수 없는 플레이어'
                                    }
                                  </CardTitle>
                                  <CardDescription className="flex items-center gap-2 mt-1">
                                    {activeTab === 'SINGLE' ? (
                                        <>
                                          {(analysis as SingleAnalysis).targetChampion && (
                                              <span className="flex items-center">
                                                <Trophy className="h-3 w-3 mr-1" />
                                                {(analysis as SingleAnalysis).targetChampion}
                                              </span>
                                          )}
                                          {(analysis as SingleAnalysis).matchId && (
                                              <span className="text-xs text-gray-500">
                                                {(analysis as SingleAnalysis).matchId}
                                              </span>
                                          )}
                                        </>
                                    ) : activeTab === 'MULTIPLE' ? (
                                        <>
                                          <span className="flex items-center">
                                            <Target className="h-3 w-3 mr-1" />
                                            {(analysis as MultipleAnalysis).analysisPeriod}
                                          </span>
                                          <span className="text-xs text-gray-500">
                                            {(analysis as MultipleAnalysis).matchCount}게임
                                          </span>
                                        </>
                                    ) : (
                                        <>
                                          <span className="flex items-center">
                                            <Users className="h-3 w-3 mr-1" />
                                            {(analysis as DuoAnalysis).player1Champion} vs {(analysis as DuoAnalysis).player2Champion}
                                          </span>
                                          <span className="text-xs text-gray-500">
                                            {(analysis as DuoAnalysis).matchId}
                                          </span>
                                        </>
                                    )}
                                  </CardDescription>
                                </div>
                                {getStatusBadge(analysis.analysisStatus)}
                              </div>
                            </CardHeader>
                            <CardContent>
                              <div className="flex items-center text-sm text-gray-500">
                                <Clock className="h-3 w-3 mr-1" />
                                {formatDate(analysis.createdAt)}
                              </div>
                              {analysis.errorMessage && (
                                  <div className="mt-2 text-sm text-red-600">
                                    오류: {analysis.errorMessage}
                                  </div>
                              )}
                            </CardContent>
                          </Card>
                      ))
                  )}
                </div>

                {/* 분석 상세 */}
                <div className="space-y-4">
                  <h2 className="text-lg font-semibold mb-4">분석 상세</h2>

                  {selectedAnalysis ? (
                      <Card>
                        <CardHeader>
                          <div className="flex justify-between items-start">
                            <div>
                              <CardTitle>
                                {activeTab === 'DUO' ?
                                    `${(selectedAnalysis as DuoAnalysis).player1Name} vs ${(selectedAnalysis as DuoAnalysis).player2Name}` :
                                    (selectedAnalysis as SingleAnalysis | MultipleAnalysis).targetPlayerName
                                }
                              </CardTitle>
                              <CardDescription>
                                {activeTab === 'SINGLE' && (selectedAnalysis as SingleAnalysis).targetChampion &&
                                    `${(selectedAnalysis as SingleAnalysis).targetChampion} • `
                                }
                                {activeTab === 'MULTIPLE' &&
                                    `${(selectedAnalysis as MultipleAnalysis).analysisPeriod} • `
                                }
                                {activeTab === 'DUO' &&
                                    `${(selectedAnalysis as DuoAnalysis).player1Champion} vs ${(selectedAnalysis as DuoAnalysis).player2Champion} • `
                                }
                                {formatDate(selectedAnalysis.createdAt)}
                              </CardDescription>
                            </div>
                            {getStatusBadge(selectedAnalysis.analysisStatus)}
                          </div>
                        </CardHeader>
                        <CardContent>
                          {/* 단일 분석 정보 */}
                          {activeTab === 'SINGLE' && (
                              <div className="space-y-3 mb-4">
                                <div className="p-3 bg-blue-50 rounded">
                                  <Label>매치 정보</Label>
                                  <div className="mt-2 space-y-1">
                                    <p className="font-mono text-sm">{(selectedAnalysis as SingleAnalysis).matchId}</p>
                                    {(selectedAnalysis as SingleAnalysis).gameMode && (
                                        <p className="text-sm text-gray-600">
                                          게임 모드: {(selectedAnalysis as SingleAnalysis).gameMode}
                                        </p>
                                    )}
                                    {(selectedAnalysis as SingleAnalysis).matchDuration && (
                                        <p className="text-sm text-gray-600">
                                          플레이 시간: {formatGameDuration((selectedAnalysis as SingleAnalysis).matchDuration!)}
                                        </p>
                                    )}
                                  </div>
                                </div>
                              </div>
                          )}

                          {/* 다중 분석 정보 */}
                          {activeTab === 'MULTIPLE' && (
                              <div className="space-y-3 mb-4">
                                <div className="p-3 bg-green-50 rounded">
                                  <Label>분석 정보</Label>
                                  <div className="mt-2 space-y-1">
                                    <p className="text-sm">
                                      <strong>분석 기간:</strong> {(selectedAnalysis as MultipleAnalysis).analysisPeriod}
                                    </p>
                                    <p className="text-sm">
                                      <strong>분석된 게임:</strong> {(selectedAnalysis as MultipleAnalysis).matchCount}개
                                    </p>
                                    <p className="text-sm">
                                      <strong>총 조회된 게임:</strong> {(selectedAnalysis as MultipleAnalysis).totalGamesFound}개
                                    </p>
                                  </div>
                                </div>

                                <div className="p-3 bg-gray-50 rounded">
                                  <Label>분석된 매치 ID</Label>
                                  <div className="mt-2 flex flex-wrap gap-2">
                                    {(selectedAnalysis as MultipleAnalysis).analyzedMatchIds?.map((matchId, index) => (
                                        <span key={index} className="bg-white px-2 py-1 rounded text-xs font-mono border">
                                          {matchId}
                                        </span>
                                    ))}
                                  </div>
                                </div>
                              </div>
                          )}

                          {/* 듀오 분석 정보 */}
                          {activeTab === 'DUO' && (
                              <div className="space-y-3 mb-4">
                                <div className="p-3 bg-purple-50 rounded">
                                  <Label>듀오 매치 정보</Label>
                                  <div className="mt-2 space-y-1">
                                    <p className="font-mono text-sm">{(selectedAnalysis as DuoAnalysis).matchId}</p>
                                    <div className="grid grid-cols-2 gap-4 mt-2">
                                      <div>
                                        <p className="text-sm font-medium text-purple-700">플레이어 1</p>
                                        <p className="text-sm">{(selectedAnalysis as DuoAnalysis).player1Name}</p>
                                        <p className="text-sm text-gray-600">{(selectedAnalysis as DuoAnalysis).player1Champion}</p>
                                      </div>
                                      <div>
                                        <p className="text-sm font-medium text-purple-700">플레이어 2</p>
                                        <p className="text-sm">{(selectedAnalysis as DuoAnalysis).player2Name}</p>
                                        <p className="text-sm text-gray-600">{(selectedAnalysis as DuoAnalysis).player2Champion}</p>
                                      </div>
                                    </div>
                                  </div>
                                </div>
                              </div>
                          )}

                          {/* 분석 결과 */}
                          {selectedAnalysis.analysisStatus === 'COMPLETED' ? (
                              <div>
                                <Label>AI 분석 결과</Label>
                                <div className="mt-2 prose prose-sm max-w-none bg-white p-4 rounded border">
                                  <ReactMarkdown>{selectedAnalysis.analysisSummary}</ReactMarkdown>
                                </div>
                              </div>
                          ) : selectedAnalysis.analysisStatus === 'FAILED' ? (
                              <div className="text-red-600">
                                <Label>오류 메시지</Label>
                                <p className="mt-2 p-3 bg-red-50 rounded">{selectedAnalysis.errorMessage}</p>
                              </div>
                          ) : (
                              <div className="text-center py-8 text-gray-500">
                                <Loader className="h-6 w-6 animate-spin mx-auto mb-2" />
                                <p>분석이 진행 중입니다...</p>
                              </div>
                          )}
                        </CardContent>
                      </Card>
                  ) : (
                      <Card>
                        <CardContent className="py-8 text-center text-gray-500">
                          분석 기록을 선택하면 상세 내용을 확인할 수 있습니다.
                        </CardContent>
                      </Card>
                  )}
                </div>
              </div>
          )}
        </div>
      </div>
  );
}