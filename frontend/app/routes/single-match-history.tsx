import { useState, useEffect } from "react";
import { Link } from "@remix-run/react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "~/components/ui/card";
import { Button } from "~/components/ui/button";
import { Input } from "~/components/ui/input";
import { Label } from "~/components/ui/label";
import { Badge } from "~/components/ui/badge";
import { useApi } from "~/utils/api";
import ReactMarkdown from "react-markdown";
import { Clock, User, Trophy, AlertCircle, CheckCircle, Loader, GamepadIcon } from "lucide-react";

interface SingleMatchAnalysis {
  id: number;
  puuid: string;
  matchId: string | null;
  targetPlayerName: string | null;
  targetChampion: string | null;
  analysisStatus: 'REQUESTED' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  analysisSummary: string;
  aiResponseData: any;
  createdAt: string;
  updatedAt: string;
  errorMessage: string | null;
}

export default function SingleMatchHistory() {
  const apiFetch = useApi();
  const [analyses, setAnalyses] = useState<SingleMatchAnalysis[]>([]);
  const [filteredAnalyses, setFilteredAnalyses] = useState<SingleMatchAnalysis[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedAnalysis, setSelectedAnalysis] = useState<SingleMatchAnalysis | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');

  const loadSingleMatchAnalyses = async () => {
    setIsLoading(true);
    setError(null);
    try {
      // Get all single match analyses from the new backend endpoint
      const response = await apiFetch('/api/singleanalysis/all');
      setAnalyses(response);
      setFilteredAnalyses(response);
    } catch (err) {
      setError('단일 경기 분석 기록을 불러오는데 실패했습니다.');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadSingleMatchAnalyses();
  }, []);

  useEffect(() => {
    let filtered = analyses;
    
    // Filter by status
    if (statusFilter !== 'ALL') {
      filtered = filtered.filter(analysis => analysis.analysisStatus === statusFilter);
    }
    
    // Filter by search term
    if (searchTerm) {
      filtered = filtered.filter(analysis => 
        analysis.targetPlayerName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        analysis.targetChampion?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        analysis.matchId?.includes(searchTerm)
      );
    }
    
    setFilteredAnalyses(filtered);
  }, [analyses, statusFilter, searchTerm]);

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

  return (
    <div className="h-full bg-gray-50">
      <div className="p-6">
        <div className="mb-6">
          <div className="flex items-center mb-2">
            <GamepadIcon className="h-6 w-6 mr-2 text-blue-600" />
            <h1 className="text-2xl font-bold text-gray-900">단일 경기 분석 기록</h1>
          </div>
          <p className="text-gray-600">개별 게임의 AI 분석 결과를 확인하세요</p>
        </div>

        {/* Filters */}
        <div className="mb-6 grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <Label htmlFor="search">검색</Label>
            <Input
              id="search"
              placeholder="플레이어명, 챔피언, 매치ID 검색..."
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
            <Button onClick={loadSingleMatchAnalyses} disabled={isLoading}>
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
            <p>단일 경기 분석 기록을 불러오는 중...</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Analysis List */}
            <div className="space-y-4">
              <h2 className="text-lg font-semibold">단일 경기 분석 목록 ({filteredAnalyses.length}개)</h2>
              
              {filteredAnalyses.length === 0 ? (
                <Card>
                  <CardContent className="py-8 text-center">
                    <GamepadIcon className="h-12 w-12 mx-auto mb-4 text-gray-400" />
                    <p className="text-gray-500 mb-4">단일 경기 분석 기록이 없습니다.</p>
                    <Link to="/ai-analysis" className="inline-block">
                      <Button>새 분석 요청하기</Button>
                    </Link>
                  </CardContent>
                </Card>
              ) : (
                filteredAnalyses.map((analysis) => (
                  <Card 
                    key={analysis.id} 
                    className={`cursor-pointer transition-all hover:shadow-md ${selectedAnalysis?.id === analysis.id ? 'ring-2 ring-blue-500' : ''}`}
                    onClick={() => setSelectedAnalysis(analysis)}
                  >
                    <CardHeader className="pb-3">
                      <div className="flex justify-between items-start">
                        <div>
                          <CardTitle className="text-base flex items-center">
                            <GamepadIcon className="h-4 w-4 mr-2 text-blue-600" />
                            {analysis.targetPlayerName || '알 수 없는 플레이어'}
                          </CardTitle>
                          <CardDescription className="flex items-center gap-2 mt-1">
                            {analysis.targetChampion && (
                              <span className="flex items-center">
                                <Trophy className="h-3 w-3 mr-1" />
                                {analysis.targetChampion}
                              </span>
                            )}
                            {analysis.matchId && (
                              <span className="text-xs text-gray-500 font-mono">
                                {analysis.matchId.substring(0, 15)}...
                              </span>
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

            {/* Analysis Detail */}
            <div>
              <h2 className="text-lg font-semibold mb-4">분석 상세</h2>
              
              {selectedAnalysis ? (
                <Card>
                  <CardHeader>
                    <div className="flex justify-between items-start">
                      <div>
                        <CardTitle className="flex items-center">
                          <GamepadIcon className="h-5 w-5 mr-2 text-blue-600" />
                          {selectedAnalysis.targetPlayerName}
                        </CardTitle>
                        <CardDescription>
                          {selectedAnalysis.targetChampion && `${selectedAnalysis.targetChampion} • `}
                          {formatDate(selectedAnalysis.createdAt)}
                        </CardDescription>
                      </div>
                      {getStatusBadge(selectedAnalysis.analysisStatus)}
                    </div>
                  </CardHeader>
                  <CardContent>
                    {selectedAnalysis.matchId && (
                      <div className="mb-4 p-3 bg-gray-50 rounded">
                        <Label>매치 ID</Label>
                        <p className="font-mono text-sm break-all">{selectedAnalysis.matchId}</p>
                      </div>
                    )}
                    
                    {selectedAnalysis.analysisStatus === 'COMPLETED' && selectedAnalysis.analysisSummary ? (
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
                        <p>단일 경기 분석이 진행 중입니다...</p>
                        <p className="text-sm">약 30초 정도 소요될 예정입니다.</p>
                      </div>
                    )}
                  </CardContent>
                </Card>
              ) : (
                <Card>
                  <CardContent className="py-8 text-center text-gray-500">
                    <GamepadIcon className="h-8 w-8 mx-auto mb-2 text-gray-400" />
                    <p>단일 경기 분석 기록을 선택하면 상세 내용을 확인할 수 있습니다.</p>
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