"use client"

import { useState, useEffect, useCallback } from "react"
import { useNavigate } from "@remix-run/react"
import { Button } from "~/components/ui/button"
import { Input } from "~/components/ui/input"
import { Card, CardContent, CardHeader, CardTitle } from "~/components/ui/card"
import { Badge } from "~/components/ui/badge"
import { useApi } from '~/utils/api'
import {
  History,
  Search,
  RefreshCw,
  User,
  Users,
  Trophy,
  Clock,
  CheckCircle2,
  Eye,
  Calendar,
  Star,
  Gamepad2,
  Sparkles,
  BarChart3,
  Target,
  Brain,
  AlertTriangle,
  Loader2,
} from "lucide-react"


interface SingleAnalysisRecord {
  id: number
  puuid: string
  matchId: string
  targetPlayerName: string
  targetChampion?: string
  matchDuration?: number
  gameMode?: string
  status: string
  analysisSummary: string
  updatedAt: string
  createdAt: string
  aiResponseData?: any
}

interface MultipleAnalysisRecord {
  id: number
  puuid: string
  targetPlayerName: string
  matchCount: number
  analyzedMatchIds: string[]
  analysisPeriod: string
  totalGamesFound: number
  status: string
  analysisSummary: string
  updatedAt: string
  createdAt: string
  aiResponseData?: any
}

interface DuoAnalysisRecord {
  id: number
  matchId: string
  player1Name: string
  player2Name: string
  player1Champion: string
  player2Champion: string
  status: string
  analysisSummary: string
  comparisonResult?: any
  updatedAt: string
  createdAt: string
}

const MatchHistory = () => {
  const apiFetch = useApi()
  const navigate = useNavigate()
  const [activeTab, setActiveTab] = useState("single")
  const [searchQuery, setSearchQuery] = useState("")
  const [selectedRecord, setSelectedRecord] = useState<any>(null)
  const [isRefreshing, setIsRefreshing] = useState(false)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string>("")
  
  // 데이터 상태
  const [singleRecords, setSingleRecords] = useState<SingleAnalysisRecord[]>([])
  const [multipleRecords, setMultipleRecords] = useState<MultipleAnalysisRecord[]>([])
  const [duoRecords, setDuoRecords] = useState<DuoAnalysisRecord[]>([])
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)

  // 전체 개수 상태
  const [singleCount, setSingleCount] = useState(0)
  const [multipleCount, setMultipleCount] = useState(0)
  const [duoCount, setDuoCount] = useState(0)

  const loadData = useCallback(async (tab: string, page: number) => {
    setLoading(true)
    setError("")
    try {
      let response;
      switch (tab) {
        case "single":
          response = await apiFetch(`/api/analysis/single/status/COMPLETED?page=${page}&size=20`)
          if (response.content) {
            setSingleRecords(response.content)
            setTotalPages(response.totalPages)
          }
          break
        case "multiple":
          response = await apiFetch(`/api/analysis/multiple/status/COMPLETED?page=${page}&size=20`)
          if (response.content) {
            setMultipleRecords(response.content)
            setTotalPages(response.totalPages)
          }
          break
        case "duo":
          response = await apiFetch(`/api/analysis/duo/status/COMPLETED?page=${page}&size=20`)
          if (response.content) {
            setDuoRecords(response.content)
            setTotalPages(response.totalPages)
          }
          break
      }
    } catch (err) {
      console.error('데이터 로드 실패:', err)
      setError('데이터를 불러오는데 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }, [apiFetch])

  const loadAllCounts = useCallback(async () => {
    try {
      const [single, multiple, duo] = await Promise.all([
        apiFetch(`/api/analysis/single/status/COMPLETED?page=0&size=1`),
        apiFetch(`/api/analysis/multiple/status/COMPLETED?page=0&size=1`),
        apiFetch(`/api/analysis/duo/status/COMPLETED?page=0&size=1`)
      ]);
      setSingleCount(single.totalElements || 0)
      setMultipleCount(multiple.totalElements || 0)
      setDuoCount(duo.totalElements || 0)
    } catch (err) {
      console.error('Failed to load counts:', err);
      setError('분석 기록 개수를 불러오는데 실패했습니다.');
    }
  }, [apiFetch]);

  useEffect(() => {
    loadAllCounts()
  }, [loadAllCounts]);

  useEffect(() => {
    loadData(activeTab, currentPage)
  }, [activeTab, currentPage, loadData])

  const handleRefresh = async () => {
    setIsRefreshing(true)
    setCurrentPage(0)
    await loadAllCounts()
    await loadData(activeTab, 0)
    setTimeout(() => {
      setIsRefreshing(false)
    }, 1000)
  }

  const formatDate = (dateString: string) => {
    const date = new Date(dateString)
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    })
  }

  const formatTime = (dateString: string) => {
    const date = new Date(dateString)
    return date.toLocaleTimeString('ko-KR', {
      hour: '2-digit',
      minute: '2-digit',
    })
  }

  const formatDuration = (seconds?: number) => {
    if (!seconds) return "알 수 없음"
    const minutes = Math.floor(seconds / 60)
    return `${minutes}분`
  }

  const tabs = [
    { 
      id: "single", 
      label: "단일 게임 분석", 
      count: singleCount, 
      icon: User, 
      color: "from-blue-500 to-cyan-500" 
    },
    { 
      id: "multiple", 
      label: "종합 게임 분석", 
      count: multipleCount, 
      icon: BarChart3, 
      color: "from-green-500 to-emerald-500" 
    },
    { 
      id: "duo", 
      label: "듀오 게임 분석", 
      count: duoCount, 
      icon: Users, 
      color: "from-purple-500 to-indigo-500" 
    },
  ]

  // 현재 탭의 레코드 가져오기
  const getCurrentRecords = () => {
    switch (activeTab) {
      case "single":
        return singleRecords
      case "multiple":
        return multipleRecords
      case "duo":
        return duoRecords
      default:
        return []
    }
  }

  // 검색 필터링
  const filteredRecords = getCurrentRecords().filter((record: any) => {
    const query = searchQuery.toLowerCase()
    
    switch (activeTab) {
      case "single":
        return record.targetPlayerName?.toLowerCase().includes(query) ||
               record.targetChampion?.toLowerCase().includes(query) ||
               record.matchId?.toLowerCase().includes(query)
      case "multiple":
        return record.targetPlayerName?.toLowerCase().includes(query)
      case "duo":
        return record.player1Name?.toLowerCase().includes(query) ||
               record.player2Name?.toLowerCase().includes(query) ||
               record.player1Champion?.toLowerCase().includes(query) ||
               record.player2Champion?.toLowerCase().includes(query) ||
               record.matchId?.toLowerCase().includes(query)
      default:
        return true
    }
  })

  const renderRecordCard = (record: any, index: number) => {
    const isSelected = selectedRecord?.id === record.id
    
    return (
      <div
        key={record.id}
        onClick={() => setSelectedRecord(record)}
        className={`p-6 rounded-2xl border-2 transition-all duration-300 cursor-pointer group hover:scale-102 ${
          isSelected
            ? "border-blue-500 bg-blue-50 shadow-lg"
            : "border-gray-200 hover:border-blue-300 hover:bg-blue-50/50"
        }`}
      >
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 bg-gradient-to-r from-blue-500 to-purple-500 rounded-2xl flex items-center justify-center text-white font-bold">
              {(currentPage * 20) + index + 1}
            </div>
            <div>
              {activeTab === "single" && (
                <>
                  <h4 className="font-bold text-lg text-gray-900">{record.targetPlayerName}</h4>
                  <div className="flex items-center gap-2 text-gray-600">
                    {record.targetChampion && (
                      <Badge className="bg-gradient-to-r from-purple-500 to-indigo-500 text-white">
                        {record.targetChampion}
                      </Badge>
                    )}
                    <span className="text-sm font-mono">{record.matchId}</span>
                  </div>
                </>
              )}
              
              {activeTab === "multiple" && (
                <>
                  <h4 className="font-bold text-lg text-gray-900">{record.targetPlayerName}</h4>
                  <div className="flex items-center gap-2 text-gray-600">
                    <Badge className="bg-gradient-to-r from-green-500 to-emerald-500 text-white">
                      {record.matchCount}게임 종합분석
                    </Badge>
                    <span className="text-sm text-gray-500">{record.analysisPeriod}</span>
                  </div>
                </>
              )}
              
              {activeTab === "duo" && (
                <>
                  <h4 className="font-bold text-lg text-gray-900">
                    {record.player1Name} & {record.player2Name}
                  </h4>
                  <div className="flex items-center gap-2 text-gray-600">
                    {record.player1Champion && (
                      <Badge className="bg-gradient-to-r from-blue-500 to-cyan-500 text-white">
                        {record.player1Champion}
                      </Badge>
                    )}
                    {record.player2Champion && (
                      <Badge className="bg-gradient-to-r from-green-500 to-emerald-500 text-white">
                        {record.player2Champion}
                      </Badge>
                    )}
                  </div>
                </>
              )}
            </div>
          </div>
          
          <div className="flex items-center gap-4">
            <div className="text-right">
              <div className="flex items-center gap-2 text-gray-500 mb-1">
                <Calendar className="w-4 h-4" />
                <span className="text-sm">{formatDate(record.createdAt)}</span>
                <Clock className="w-4 h-4" />
                <span className="text-sm">{formatTime(record.createdAt)}</span>
              </div>
              <div className="flex items-center gap-2">
                {activeTab === "single" && record.matchDuration && (
                  <Badge className="bg-gradient-to-r from-yellow-500 to-orange-500 text-white">
                    {formatDuration(record.matchDuration)}
                  </Badge>
                )}
                {activeTab === "multiple" && (
                  <Badge className="bg-gradient-to-r from-indigo-500 to-purple-500 text-white">
                    총 {record.totalGamesFound}게임 조회
                  </Badge>
                )}
              </div>
            </div>
            <div className="flex items-center gap-2">
              <CheckCircle2 className="w-5 h-5 text-green-500" />
              <Badge className="bg-green-100 text-green-700 font-semibold">
                {record.status === 'COMPLETED' ? '완료' : record.status}
              </Badge>
            </div>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50 relative overflow-hidden">
      {/* Animated Background Elements */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute -top-40 -right-40 w-80 h-80 bg-gradient-to-br from-blue-400/20 to-purple-600/20 rounded-full blur-3xl animate-pulse" />
        <div className="absolute -bottom-40 -left-40 w-80 h-80 bg-gradient-to-tr from-cyan-400/20 to-blue-600/20 rounded-full blur-3xl animate-pulse delay-1000" />
        <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-gradient-to-r from-purple-400/10 to-pink-400/10 rounded-full blur-3xl animate-pulse delay-500" />
      </div>

      <div className="container mx-auto px-4 py-8 max-w-7xl relative z-10">
        {/* Header */}
        <div className="text-center mb-16">
          <div className="flex items-center justify-center gap-4 mb-6">
            <div className="relative group">
              <div className="absolute inset-0 bg-gradient-to-r from-blue-600 to-purple-600 rounded-full blur-lg opacity-75 group-hover:opacity-100 transition-opacity animate-pulse" />
              <div className="relative bg-white p-4 rounded-full shadow-2xl">
                <History className="w-12 h-12 text-blue-600" />
              </div>
              <div className="absolute -top-2 -right-2 w-6 h-6 bg-gradient-to-r from-pink-500 to-rose-500 rounded-full animate-bounce">
                <Star className="w-4 h-4 text-white m-1" />
              </div>
            </div>
            <div>
              <h1 className="text-5xl font-black bg-gradient-to-r from-blue-600 via-purple-600 to-indigo-600 bg-clip-text text-transparent mb-2">
                분석 기록
              </h1>
              <div className="flex items-center justify-center gap-2">
                <div className="w-2 h-2 bg-blue-500 rounded-full animate-pulse" />
                <div className="w-2 h-2 bg-purple-500 rounded-full animate-pulse delay-100" />
                <div className="w-2 h-2 bg-indigo-500 rounded-full animate-pulse delay-200" />
              </div>
            </div>
          </div>
          <p className="text-xl text-slate-600 max-w-2xl mx-auto leading-relaxed">지금껏 게임 분석 기록을 확인하세요</p>
          <div className="mt-4 flex items-center justify-center gap-2 text-sm text-slate-500">
            <Gamepad2 className="w-4 h-4" />
            <span>모든 분석 기록을 한 곳에서 관리</span>
          </div>
        </div>

        {/* Error Display */}
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-6">
            <div className="flex items-center">
              <AlertTriangle className="w-5 h-5 mr-2" />
              {error}
            </div>
          </div>
        )}

        {/* Tab Navigation */}
        <div className="flex flex-wrap justify-center mb-12 bg-white/80 backdrop-blur-xl rounded-2xl shadow-2xl p-3 border border-white/20">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              onClick={() => {
                setActiveTab(tab.id)
                setCurrentPage(0)
                setSelectedRecord(null)
              }}
              className={`flex items-center px-6 py-4 rounded-xl m-1 transition-all duration-300 group relative overflow-hidden ${
                activeTab === tab.id
                  ? "bg-gradient-to-r text-white shadow-xl scale-105"
                  : "text-gray-600 hover:bg-gray-50 hover:scale-102"
              }`}
              style={{
                background:
                  activeTab === tab.id
                    ? `linear-gradient(135deg, ${tab.color.split(" ")[1]}, ${tab.color.split(" ")[3]})`
                    : undefined,
              }}
            >
              <div className="absolute inset-0 bg-gradient-to-r from-white/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity" />
              <tab.icon className="w-5 h-5 mr-3 relative z-10" />
              <span className="font-semibold relative z-10">{tab.label}</span>
              <Badge
                className={`ml-3 relative z-10 ${
                  activeTab === tab.id ? "bg-white/20 text-white" : "bg-gray-100 text-gray-600 group-hover:bg-gray-200"
                }`}
              >
                {tab.count}
              </Badge>
            </button>
          ))}
        </div>

        {/* Search and Filter Section */}
        <Card className="mb-8 overflow-hidden border-0 shadow-2xl backdrop-blur-xl bg-white/80">
          <CardContent className="p-6">
            <div className="flex flex-col md:flex-row gap-4 items-center">
              <div className="flex-1 relative group">
                <Search className="absolute left-4 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                <Input
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  placeholder="플레이어명, 챔피언, 매치ID 검색..."
                  className="pl-12 h-12 rounded-2xl border-2 border-gray-200 focus:border-blue-500 transition-all duration-300"
                />
              </div>
              <Button
                onClick={handleRefresh}
                disabled={isRefreshing}
                className="bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 px-6 py-3 rounded-2xl font-semibold transition-all duration-300 hover:scale-105"
              >
                {isRefreshing ? (
                  <Loader2 className="w-5 h-5 mr-2 animate-spin" />
                ) : (
                  <RefreshCw className="w-5 h-5 mr-2" />
                )}
                새로고침
              </Button>
            </div>
          </CardContent>
        </Card>

        {/* Main Content */}
        <div className="grid lg:grid-cols-3 gap-8">
          {/* Records List */}
          <div className="lg:col-span-2">
            <Card className="border-0 shadow-2xl backdrop-blur-xl bg-white/80 overflow-hidden">
              <CardHeader className="bg-gradient-to-r from-blue-50 to-purple-50 border-b border-gray-100">
                <CardTitle className="flex items-center text-2xl">
                  <div className="w-10 h-10 bg-gradient-to-r from-blue-500 to-purple-500 rounded-xl flex items-center justify-center mr-4">
                    <History className="w-6 h-6 text-white" />
                  </div>
                  {tabs.find((tab) => tab.id === activeTab)?.label} ({tabs.find((tab) => tab.id === activeTab)?.count || 0}개)
                </CardTitle>
              </CardHeader>
              <CardContent className="p-6">
                {loading ? (
                  <div className="flex items-center justify-center py-12">
                    <Loader2 className="w-8 h-8 animate-spin text-blue-500 mr-3" />
                    <span className="text-gray-500">분석 기록을 불러오는 중...</span>
                  </div>
                ) : filteredRecords.length === 0 ? (
                  <div className="text-center py-12">
                    <div className="w-16 h-16 bg-gray-100 rounded-2xl flex items-center justify-center mx-auto mb-4">
                      <Sparkles className="w-8 h-8 text-gray-400" />
                    </div>
                    <p className="text-gray-500 leading-relaxed">
                      {searchQuery ? '검색 결과가 없습니다.' : '분석 기록이 없습니다.'}
                    </p>
                  </div>
                ) : (
                  <div className="space-y-4 max-h-[600px] overflow-y-auto p-1">
                    {filteredRecords.map((record, index) => renderRecordCard(record, index))}
                  </div>
                )}
              </CardContent>
            </Card>
          </div>

          {/* Analysis Detail */}
          <div className="lg:col-span-1">
            <Card className="border-0 shadow-2xl backdrop-blur-xl bg-white/80 overflow-hidden sticky top-8">
              <CardHeader className="bg-gradient-to-r from-green-50 to-emerald-50 border-b border-gray-100">
                <CardTitle className="flex items-center text-2xl">
                  <div className="w-10 h-10 bg-gradient-to-r from-green-500 to-emerald-500 rounded-xl flex items-center justify-center mr-4">
                    <Eye className="w-6 h-6 text-white" />
                  </div>
                  분석 상세
                </CardTitle>
              </CardHeader>
              <CardContent className="p-8">
                {selectedRecord ? (
                  <div className="space-y-6">
                    <div className="text-center">
                      <div className="w-16 h-16 bg-gradient-to-r from-blue-500 to-purple-500 rounded-2xl flex items-center justify-center mx-auto mb-4">
                        <Target className="w-8 h-8 text-white" />
                      </div>
                      <h3 className="text-xl font-bold text-gray-900 mb-2">
                        {activeTab === "single" 
                          ? selectedRecord.targetPlayerName
                          : activeTab === "multiple"
                            ? selectedRecord.targetPlayerName
                            : `${selectedRecord.player1Name} & ${selectedRecord.player2Name}`
                        }
                      </h3>
                      <Badge className="bg-gradient-to-r from-green-500 to-emerald-500 text-white font-semibold px-4 py-2">
                        {selectedRecord.status === 'COMPLETED' ? '완료' : selectedRecord.status}
                      </Badge>
                    </div>

                    <div className="space-y-4">
                      <div className="bg-blue-50 border border-blue-200 rounded-xl p-4">
                        <h4 className="font-semibold text-blue-900 mb-2 flex items-center">
                          <Calendar className="w-4 h-4 mr-2" />
                          분석 일시
                        </h4>
                        <p className="text-blue-700">
                          {formatDate(selectedRecord.createdAt)} {formatTime(selectedRecord.createdAt)}
                        </p>
                      </div>

                      {activeTab === "single" && (
                        <>
                          {selectedRecord.targetChampion && (
                            <div className="bg-purple-50 border border-purple-200 rounded-xl p-4">
                              <h4 className="font-semibold text-purple-900 mb-2 flex items-center">
                                <Star className="w-4 h-4 mr-2" />
                                챔피언
                              </h4>
                              <Badge className="bg-gradient-to-r from-purple-500 to-indigo-500 text-white">
                                {selectedRecord.targetChampion}
                              </Badge>
                            </div>
                          )}
                          
                          {selectedRecord.matchDuration && (
                            <div className="bg-yellow-50 border border-yellow-200 rounded-xl p-4">
                              <h4 className="font-semibold text-yellow-900 mb-2 flex items-center">
                                <Clock className="w-4 h-4 mr-2" />
                                게임 시간
                              </h4>
                              <Badge className="bg-gradient-to-r from-yellow-500 to-orange-500 text-white">
                                {formatDuration(selectedRecord.matchDuration)}
                              </Badge>
                            </div>
                          )}
                        </>
                      )}

                      {activeTab === "multiple" && (
                        <div className="bg-green-50 border border-green-200 rounded-xl p-4">
                          <h4 className="font-semibold text-green-900 mb-2 flex items-center">
                            <BarChart3 className="w-4 h-4 mr-2" />
                            종합 분석 정보
                          </h4>
                          <div className="space-y-2">
                            <Badge className="bg-gradient-to-r from-green-500 to-emerald-500 text-white">
                              {selectedRecord.matchCount}게임 분석
                            </Badge>
                            <p className="text-green-700 text-sm">
                              총 {selectedRecord.totalGamesFound}게임 중 분석 완료
                            </p>
                          </div>
                        </div>
                      )}

                      {activeTab === "duo" && (
                        <div className="bg-indigo-50 border border-indigo-200 rounded-xl p-4">
                          <h4 className="font-semibold text-indigo-900 mb-2 flex items-center">
                            <Users className="w-4 h-4 mr-2" />
                            듀오 정보
                          </h4>
                          <div className="space-y-2">
                            {selectedRecord.player1Champion && (
                              <Badge className="bg-gradient-to-r from-blue-500 to-cyan-500 text-white mr-2">
                                {selectedRecord.player1Champion}
                              </Badge>
                            )}
                            {selectedRecord.player2Champion && (
                              <Badge className="bg-gradient-to-r from-green-500 to-emerald-500 text-white">
                                {selectedRecord.player2Champion}
                              </Badge>
                            )}
                          </div>
                        </div>
                      )}
                    </div>

                    {selectedRecord.analysisSummary && (
                      <div className="bg-gray-50 border border-gray-200 rounded-xl p-4 max-h-96 overflow-y-auto">
                        <h4 className="font-semibold text-gray-900 mb-4 flex items-center">
                          <Brain className="w-4 h-4 mr-2" />
                          AI 분석 결과 (미리보기)
                        </h4>
                        <div className="prose prose-sm max-w-none text-gray-700 line-clamp-6">
                          <div className="whitespace-pre-wrap">
                            {selectedRecord.analysisSummary.substring(0, 300) + 
                             (selectedRecord.analysisSummary.length > 300 ? '...' : '')}
                          </div>
                        </div>
                      </div>
                    )}

                    <Button 
                      onClick={() => {
                        if (selectedRecord) {
                          // 상태로 분석 데이터 전달
                          navigate('/analysis', {
                            state: {
                              analysisData: selectedRecord,
                              analysisType: activeTab
                            }
                          })
                        }
                      }}
                      className="w-full bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 font-semibold py-3 rounded-2xl transition-all duration-300 hover:scale-105"
                    >
                      <Brain className="w-5 h-5 mr-2" />
                      상세 분석 보기
                    </Button>
                  </div>
                ) : (
                  <div className="text-center py-12">
                    <div className="w-16 h-16 bg-gray-100 rounded-2xl flex items-center justify-center mx-auto mb-4">
                      <Sparkles className="w-8 h-8 text-gray-400" />
                    </div>
                    <p className="text-gray-500 leading-relaxed">
                      분석 기록을 선택하면
                      <br />
                      상세 내용을 확인할 수 있습니다
                    </p>
                  </div>
                )}
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  )
}

export default MatchHistory