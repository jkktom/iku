import { useState, useEffect } from "react";
import { Link } from "@remix-run/react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "~/components/ui/card";
import { Button } from "~/components/ui/button";
import { Input } from "~/components/ui/input";
import { Label } from "~/components/ui/label";
import { Badge } from "~/components/ui/badge";
import { useApi } from "~/utils/api";
import ReactMarkdown from "react-markdown";
import { Clock, User, Trophy, AlertCircle, CheckCircle, Loader, BarChart3, Users } from "lucide-react";

interface MultiMatchAnalysis {
  id: number;
  puuid: string;
  gameName: string;
  tagLine: string;
  matchCount: number;
  matchIds: string;
  status: 'REQUESTED' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  aiRequest: string | null;
  aiResponse: string | null;
  errorMessage: string | null;
  analysisDurationSeconds: number | null;
  completedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export default function MultiMatchHistory() {
  const apiFetch = useApi();
  const [analyses, setAnalyses] = useState<MultiMatchAnalysis[]>([]);
  const [filteredAnalyses, setFilteredAnalyses] = useState<MultiMatchAnalysis[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedAnalysis, setSelectedAnalysis] = useState<MultiMatchAnalysis | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');

  const loadMultiMatchAnalyses = async () => {
    setIsLoading(true);
    setError(null);
    try {
      // Get all multi match analyses from the new backend endpoint
      const response = await apiFetch('/api/multianalysis/all');
      setAnalyses(response);
      setFilteredAnalyses(response);
    } catch (err) {
      setError('다수 경기 분석 기록을 불러오는데 실패했습니다.');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadMultiMatchAnalyses();
  }, []);

  useEffect(() => {
    let filtered = analyses;
    
    // Filter by status
    if (statusFilter !== 'ALL') {
      filtered = filtered.filter(analysis => analysis.status === statusFilter);
    }
    
    // Filter by search term
    if (searchTerm) {
      filtered = filtered.filter(analysis => 
        analysis.gameName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        analysis.tagLine?.toLowerCase().includes(searchTerm.toLowerCase())
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

  const formatDuration = (seconds: number | null) => {
    if (!seconds) return '알 수 없음';
    if (seconds < 60) return `${seconds}초`;
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes}분 ${remainingSeconds}초`;
  };

  const getMatchIdsList = (matchIds: string) => {
    if (!matchIds) return [];
    return matchIds.split(',').filter(id => id.trim());
  };

  return (
    <div className="h-full bg-gray-50">
      <div className="p-6">
        <div className="mb-6">
          <div className="flex items-center mb-2">
            <BarChart3 className="h-6 w-6 mr-2 text-green-600" />
            <h1 className="text-2xl font-bold text-gray-900">다수 경기 분석 기록</h1>
          </div>
          <p className="text-gray-600">여러 게임을 종합한 AI 분석 결과를 확인하세요</p>
        </div>

        {/* Filters */}
        <div className="mb-6 grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <Label htmlFor="search">검색</Label>
            <Input
              id="search"
              placeholder="플레이어명, 태그 검색..."
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
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500"
            >
              <option value="ALL">모든 상태</option>
              <option value="COMPLETED">완료</option>
              <option value="PROCESSING">처리중</option>
              <option value="FAILED">실패</option>
              <option value="REQUESTED">대기</option>
            </select>
          </div>
          <div className="flex items-end">
            <Button onClick={loadMultiMatchAnalyses} disabled={isLoading} className="bg-green-600 hover:bg-green-700">
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
            <p>다수 경기 분석 기록을 불러오는 중...</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Analysis List */}
            <div className="space-y-4">
              <h2 className="text-lg font-semibold">다수 경기 분석 목록 ({filteredAnalyses.length}개)</h2>
              
              {filteredAnalyses.length === 0 ? (
                <Card>
                  <CardContent className="py-8 text-center">
                    <BarChart3 className="h-12 w-12 mx-auto mb-4 text-gray-400" />
                    <p className="text-gray-500 mb-4">다수 경기 분석 기록이 없습니다.</p>
                    <Link to="/ai-analysis" className="inline-block">
                      <Button className="bg-green-600 hover:bg-green-700">새 분석 요청하기</Button>
                    </Link>
                  </CardContent>
                </Card>
              ) : (
                filteredAnalyses.map((analysis) => (
                  <Card 
                    key={analysis.id} 
                    className={`cursor-pointer transition-all hover:shadow-md ${selectedAnalysis?.id === analysis.id ? 'ring-2 ring-green-500' : ''}`}
                    onClick={() => setSelectedAnalysis(analysis)}
                  >
                    <CardHeader className="pb-3">
                      <div className="flex justify-between items-start">
                        <div>
                          <CardTitle className="text-base flex items-center">
                            <BarChart3 className="h-4 w-4 mr-2 text-green-600" />
                            {analysis.gameName}#{analysis.tagLine}
                          </CardTitle>
                          <CardDescription className="flex items-center gap-2 mt-1">
                            <span className="flex items-center">
                              <Users className="h-3 w-3 mr-1" />
                              {analysis.matchCount}개 경기 종합
                            </span>
                            {analysis.analysisDurationSeconds && (
                              <span className="text-xs text-gray-500">
                                소요시간: {formatDuration(analysis.analysisDurationSeconds)}
                              </span>
                            )}
                          </CardDescription>
                        </div>
                        {getStatusBadge(analysis.status)}
                      </div>
                    </CardHeader>
                    <CardContent>
                      <div className="flex items-center text-sm text-gray-500">
                        <Clock className="h-3 w-3 mr-1" />
                        {formatDate(analysis.createdAt)}
                      </div>
                      {analysis.completedAt && (
                        <div className="flex items-center text-sm text-green-600 mt-1">
                          <CheckCircle className="h-3 w-3 mr-1" />
                          완료: {formatDate(analysis.completedAt)}
                        </div>
                      )}
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
                          <BarChart3 className="h-5 w-5 mr-2 text-green-600" />
                          {selectedAnalysis.gameName}#{selectedAnalysis.tagLine}
                        </CardTitle>
                        <CardDescription>
                          {selectedAnalysis.matchCount}개 경기 종합 분석 • {formatDate(selectedAnalysis.createdAt)}
                        </CardDescription>
                      </div>
                      {getStatusBadge(selectedAnalysis.status)}
                    </div>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="grid grid-cols-2 gap-4">
                      <div className="p-3 bg-gray-50 rounded">
                        <Label>분석 게임 수</Label>
                        <p className="font-semibold text-lg">{selectedAnalysis.matchCount}개</p>
                      </div>
                      {selectedAnalysis.analysisDurationSeconds && (
                        <div className="p-3 bg-gray-50 rounded">
                          <Label>분석 소요시간</Label>
                          <p className="font-semibold text-lg">{formatDuration(selectedAnalysis.analysisDurationSeconds)}</p>
                        </div>
                      )}
                    </div>

                    {selectedAnalysis.matchIds && (
                      <div className="p-3 bg-gray-50 rounded">
                        <Label>분석된 매치 ID 목록</Label>
                        <div className="mt-2 space-y-1">
                          {getMatchIdsList(selectedAnalysis.matchIds).map((matchId, index) => (
                            <p key={index} className="font-mono text-sm text-gray-600">
                              {index + 1}. {matchId}
                            </p>
                          ))}
                        </div>
                      </div>
                    )}
                    
                    {selectedAnalysis.status === 'COMPLETED' && selectedAnalysis.aiResponse ? (
                      <div>
                        <Label>종합 AI 분석 결과</Label>
                        <div className="mt-2 prose prose-sm max-w-none bg-white p-4 rounded border">
                          <ReactMarkdown>{selectedAnalysis.aiResponse}</ReactMarkdown>
                        </div>
                      </div>
                    ) : selectedAnalysis.status === 'FAILED' ? (
                      <div className="text-red-600">
                        <Label>오류 메시지</Label>
                        <p className="mt-2 p-3 bg-red-50 rounded">{selectedAnalysis.errorMessage}</p>
                      </div>
                    ) : (
                      <div className="text-center py-8 text-gray-500">
                        <Loader className="h-6 w-6 animate-spin mx-auto mb-2" />
                        <p>다수 경기 종합 분석이 진행 중입니다...</p>
                        <p className="text-sm">약 30초 정도 소요될 예정입니다.</p>
                      </div>
                    )}
                  </CardContent>
                </Card>
              ) : (
                <Card>
                  <CardContent className="py-8 text-center text-gray-500">
                    <BarChart3 className="h-8 w-8 mx-auto mb-2 text-gray-400" />
                    <p>다수 경기 분석 기록을 선택하면 상세 내용을 확인할 수 있습니다.</p>
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