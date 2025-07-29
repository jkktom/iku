import { useState } from 'react'
import ReactMarkdown from "react-markdown";
import { useApi } from '~/utils/api';
import { Button } from "~/components/ui/button";
import { Input } from "~/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle } from "~/components/ui/card";
import { Badge } from "~/components/ui/badge";
import {
  Users,
  Search,
  CheckCircle2,
  Play,
  Target,
  Brain,
  BarChart3,
  Sparkles,
  Gamepad2,
  Star,
  TrendingUp,
  UserPlus,
  Trophy,
  Clock,
  ArrowLeft,
  AlertTriangle,
} from "lucide-react"

interface Player {
    name: string
    tag: string
}

interface AnalysisRecord {
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

interface AnalysisResult {
    analysisRecord: AnalysisRecord
    message: string
}

interface CommonMatchResponse {
    commonMatches: string[]
    count: number
    message: string
}

export default function DuoComparisonPage() {
    const apiFetch = useApi()
    const [analysisStep, setAnalysisStep] = useState<"input" | "select" | "analyzing" | "results">("input")
    const [player1, setPlayer1] = useState({ name: '', tag: '' })
    const [player2, setPlayer2] = useState({ name: '', tag: '' })
    const [commonMatches, setCommonMatches] = useState<string[]>([])
    const [selectedMatch, setSelectedMatch] = useState('')
    const [loading, setLoading] = useState(false)
    const [analysisResult, setAnalysisResult] = useState<AnalysisResult | null>(null)
    const [apiError, setApiError] = useState<string>("")

    // 공통 매치 찾기
    const handleSearchPlayers = async () => {
        if (!player1.name || !player2.name) {
            setApiError('두 플레이어의 이름을 모두 입력해주세요.')
            return
        }

        setLoading(true)
        setApiError("")
        try {
            const params = new URLSearchParams({
                player1Name: player1.name,
                player1Tag: player1.tag,
                player2Name: player2.name,
                player2Tag: player2.tag
            });
            const data = await apiFetch(`/api/analysis/duo/common-matches?${params}`)
            setCommonMatches(data.commonMatches || [])
            if (data.commonMatches && data.commonMatches.length > 0) {
                setAnalysisStep("select")
            } else {
                setApiError('공통 매치가 없습니다. 다른 플레이어를 시도해보세요.')
            }

        } catch (error) {
            console.error('Error:', error)
            const errorMessage = error instanceof Error ? error.message : '알 수 없는 오류가 발생했습니다'
            setApiError('플레이어 정보 조회에 실패했습니다: ' + errorMessage)
        } finally {
            setLoading(false)
        }
    }

    // 듀오 분석 수행
    const handleAnalyzeDuo = async () => {
        if (!selectedMatch) {
            setApiError('분석할 매치를 선택해주세요.')
            return
        }

        setAnalysisStep("analyzing")
        setLoading(true)
        setApiError("")
        try {
            const data = await apiFetch(`/api/analysis/duo/analyze`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: new URLSearchParams({
                    player1Name: player1.name,
                    player1Tag: player1.tag,
                    player2Name: player2.name,
                    player2Tag: player2.tag,
                    matchId: selectedMatch
                })
            })
            setAnalysisResult(data)
            setAnalysisStep("results")

        } catch (error) {
            console.error('분석 실행 중 오류:', String(error))
            const errorMessage = error instanceof Error ? error.message : '분석 실행에 실패했습니다'
            setApiError('분석 실행에 실패했습니다: ' + errorMessage)
            setAnalysisStep("select")
        } finally {
            setLoading(false)
        }
    }

    const resetAnalysis = () => {
        setAnalysisStep("input")
        setPlayer1({ name: '', tag: '' })
        setPlayer2({ name: '', tag: '' })
        setCommonMatches([])
        setSelectedMatch('')
        setAnalysisResult(null)
        setApiError("")
    }

    const handleBackToInput = () => {
        setAnalysisStep("input")
        setApiError("")
    }

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50 relative overflow-hidden">
            {/* Animated Background Elements */}
            <div className="absolute inset-0 overflow-hidden pointer-events-none">
                <div className="absolute -top-40 -right-40 w-80 h-80 bg-gradient-to-br from-blue-400/20 to-purple-600/20 rounded-full blur-3xl animate-pulse" />
                <div className="absolute -bottom-40 -left-40 w-80 h-80 bg-gradient-to-tr from-cyan-400/20 to-blue-600/20 rounded-full blur-3xl animate-pulse delay-1000" />
                <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-gradient-to-r from-purple-400/10 to-pink-400/10 rounded-full blur-3xl animate-pulse delay-500" />
            </div>

            <div className="container mx-auto px-4 py-8 max-w-6xl relative z-10">
                {/* Header */}
                <div className="text-center mb-16">
                    <div className="flex items-center justify-center gap-4 mb-6">
                        <div className="relative group">
                            <div className="absolute inset-0 bg-gradient-to-r from-blue-600 to-purple-600 rounded-full blur-lg opacity-75 group-hover:opacity-100 transition-opacity animate-pulse" />
                            <div className="relative bg-white p-4 rounded-full shadow-2xl">
                                <Users className="w-12 h-12 text-blue-600" />
                            </div>
                            <div className="absolute -top-2 -right-2 w-6 h-6 bg-gradient-to-r from-pink-500 to-rose-500 rounded-full animate-bounce">
                                <Star className="w-4 h-4 text-white m-1" />
                            </div>
                        </div>
                        <div>
                            <h1 className="text-5xl font-black bg-gradient-to-r from-blue-600 via-purple-600 to-indigo-600 bg-clip-text text-transparent mb-2">
                                친구와 비교
                            </h1>
                            <div className="flex items-center justify-center gap-2">
                                <div className="w-2 h-2 bg-blue-500 rounded-full animate-pulse" />
                                <div className="w-2 h-2 bg-purple-500 rounded-full animate-pulse delay-100" />
                                <div className="w-2 h-2 bg-indigo-500 rounded-full animate-pulse delay-200" />
                            </div>
                        </div>
                    </div>
                    <p className="text-xl text-slate-600 max-w-2xl mx-auto leading-relaxed">
                        함께 플레이한 게임을 AI로 분석해보세요
                    </p>
                    <div className="mt-4 flex items-center justify-center gap-2 text-sm text-slate-500">
                        <Gamepad2 className="w-4 h-4" />
                        <span>듀오 플레이 전용 AI 분석 시스템</span>
                    </div>
                </div>

                {/* Error Display */}
                {apiError && (
                    <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-6">
                        <div className="flex items-center">
                            <AlertTriangle className="w-5 h-5 mr-2" />
                            {apiError}
                        </div>
                    </div>
                )}

                {/* Main Analysis Section */}
                <Card className="mb-12 overflow-hidden border-0 shadow-2xl backdrop-blur-xl bg-white/10 hover:shadow-3xl transition-all duration-500">
                    <div className="bg-gradient-to-br from-blue-500 via-purple-600 to-indigo-700 p-10 text-white relative overflow-hidden">
                        {/* Animated Background Patterns */}
                        <div className="absolute inset-0 opacity-20">
                            <div className="absolute top-0 right-0 w-64 h-64 bg-white/20 rounded-full -translate-y-32 translate-x-32 animate-pulse" />
                            <div className="absolute bottom-0 left-0 w-48 h-48 bg-white/10 rounded-full translate-y-24 -translate-x-24 animate-pulse delay-1000" />
                            <div className="absolute top-1/2 right-1/4 w-32 h-32 bg-white/15 rounded-full animate-bounce delay-500" />
                        </div>

                        <div className="relative z-10">
                            {analysisStep === "input" && (
                                <div className="animate-in fade-in-0 slide-in-from-bottom-4 duration-500">
                                    <div className="flex items-center gap-4 mb-6">
                                        <div className="p-3 bg-white/20 rounded-2xl backdrop-blur-sm">
                                            <UserPlus className="w-8 h-8" />
                                        </div>
                                        <div>
                                            <h2 className="text-4xl font-bold mb-1">플레이어 정보 입력</h2>
                                            <p className="text-blue-100">비교할 두 플레이어의 정보를 입력해주세요</p>
                                        </div>
                                    </div>

                                    <div className="grid md:grid-cols-2 gap-8 mb-10">
                                        <div className="space-y-4">
                                            <label className="text-sm font-semibold text-blue-100 uppercase tracking-wider">플레이어 1</label>
                                            <div className="flex gap-4">
                                                <div className="relative group flex-1">
                                                    <Input
                                                        value={player1.name}
                                                        onChange={(e) => setPlayer1({...player1, name: e.target.value})}
                                                        placeholder="플레이어명"
                                                        className="bg-white/20 border-white/30 text-white placeholder:text-blue-200 focus:bg-white/30 transition-all duration-300 h-14 text-lg rounded-2xl backdrop-blur-sm group-hover:bg-white/25"
                                                    />
                                                    <div className="absolute inset-0 rounded-2xl bg-gradient-to-r from-white/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none" />
                                                </div>
                                                <div className="relative group w-24">
                                                    <Input
                                                        value={player1.tag}
                                                        onChange={(e) => setPlayer1({...player1, tag: e.target.value})}
                                                        placeholder="태그"
                                                        className="bg-white/20 border-white/30 text-white placeholder:text-blue-200 focus:bg-white/30 transition-all duration-300 h-14 text-lg rounded-2xl backdrop-blur-sm group-hover:bg-white/25"
                                                    />
                                                    <div className="absolute inset-0 rounded-2xl bg-gradient-to-r from-white/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none" />
                                                </div>
                                            </div>
                                        </div>

                                        <div className="space-y-4">
                                            <label className="text-sm font-semibold text-blue-100 uppercase tracking-wider">플레이어 2</label>
                                            <div className="flex gap-4">
                                                <div className="relative group flex-1">
                                                    <Input
                                                        value={player2.name}
                                                        onChange={(e) => setPlayer2({...player2, name: e.target.value})}
                                                        placeholder="플레이어명"
                                                        className="bg-white/20 border-white/30 text-white placeholder:text-blue-200 focus:bg-white/30 transition-all duration-300 h-14 text-lg rounded-2xl backdrop-blur-sm group-hover:bg-white/25"
                                                    />
                                                    <div className="absolute inset-0 rounded-2xl bg-gradient-to-r from-white/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none" />
                                                </div>
                                                <div className="relative group w-24">
                                                    <Input
                                                        value={player2.tag}
                                                        onChange={(e) => setPlayer2({...player2, tag: e.target.value})}
                                                        placeholder="태그"
                                                        className="bg-white/20 border-white/30 text-white placeholder:text-blue-200 focus:bg-white/30 transition-all duration-300 h-14 text-lg rounded-2xl backdrop-blur-sm group-hover:bg-white/25"
                                                    />
                                                    <div className="absolute inset-0 rounded-2xl bg-gradient-to-r from-white/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none" />
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                    <Button
                                        onClick={handleSearchPlayers}
                                        disabled={loading || !player1.name || !player2.name}
                                        className="bg-white text-blue-600 hover:bg-blue-50 font-bold px-10 py-4 rounded-2xl shadow-2xl hover:shadow-3xl transition-all duration-300 disabled:opacity-50 text-lg group"
                                    >
                                        {loading ? (
                                            <>
                                                <div className="w-6 h-6 border-2 border-blue-600 border-t-transparent rounded-full animate-spin mr-3" />
                                                조회 중...
                                            </>
                                        ) : (
                                            <>
                                                <Search className="w-6 h-6 mr-3 group-hover:scale-110 transition-transform" />
                                                공통 매치 찾기
                                            </>
                                        )}
                                    </Button>
                                </div>
                            )}

                            {analysisStep === "select" && (
                                <div className="animate-in fade-in-0 slide-in-from-right-4 duration-500">
                                    <div className="flex items-center gap-4 mb-6">
                                        <div className="p-3 bg-white/20 rounded-2xl backdrop-blur-sm">
                                            <Target className="w-8 h-8" />
                                        </div>
                                        <div>
                                            <h2 className="text-4xl font-bold mb-1">매치 선택</h2>
                                            <p className="text-blue-100">
                                                공통 매치 <span className="font-semibold text-yellow-300">{commonMatches.length}</span>개가 발견되었습니다
                                            </p>
                                        </div>
                                    </div>

                                    <div className="bg-green-500/20 border border-green-400/30 rounded-2xl p-6 backdrop-blur-sm mb-8">
                                        <div className="flex items-center gap-3">
                                            <CheckCircle2 className="w-6 h-6 text-green-300" />
                                            <span className="text-green-100 font-semibold text-lg">
                                                <span className="text-yellow-300">{player1.name}#{player1.tag}</span> 와 <span className="text-yellow-300">{player2.name}#{player2.tag}</span> 의 공통 매치 찾기 완료
                                            </span>
                                        </div>
                                    </div>

                                    <div className="space-y-4 mb-8">
                                        {commonMatches.map((matchId, index) => (
                                            <button
                                                key={matchId}
                                                onClick={() => setSelectedMatch(matchId)}
                                                className={`w-full p-6 rounded-2xl border-2 transition-all duration-300 text-left group relative overflow-hidden ${
                                                    selectedMatch === matchId
                                                        ? "border-white bg-white/30 shadow-2xl scale-105"
                                                        : "border-white/30 hover:border-white/60 hover:bg-white/20 hover:scale-102"
                                                }`}
                                            >
                                                <div className="absolute inset-0 bg-gradient-to-r from-white/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity" />
                                                <div className="flex items-center justify-between relative z-10">
                                                    <div className="flex items-center gap-4">
                                                        <div className="w-12 h-12 bg-gradient-to-r from-blue-500 to-purple-500 rounded-2xl flex items-center justify-center text-white font-bold">
                                                            {index + 1}
                                                        </div>
                                                        <div>
                                                            <h4 className="font-bold text-lg text-white">매치 {index + 1}</h4>
                                                            <p className="text-blue-100 font-medium">매치 ID: {matchId}</p>
                                                        </div>
                                                    </div>
                                                    <div className="flex items-center gap-2 text-blue-100">
                                                        <Clock className="w-4 h-4" />
                                                        <span className="text-sm">최신순</span>
                                                    </div>
                                                </div>
                                            </button>
                                        ))}
                                    </div>

                                    <div className="flex gap-6">
                                        <Button
                                            onClick={handleBackToInput}
                                            variant="outline"
                                            className="bg-white/20 border-white/40 text-white hover:bg-white/30 font-semibold px-8 py-4 rounded-2xl backdrop-blur-sm transition-all duration-300"
                                        >
                                            <ArrowLeft className="w-5 h-5 mr-2" />
                                            이전
                                        </Button>
                                        <Button
                                            onClick={handleAnalyzeDuo}
                                            disabled={!selectedMatch || loading}
                                            className="bg-white text-blue-600 hover:bg-blue-50 font-bold px-10 py-4 rounded-2xl shadow-2xl hover:shadow-3xl transition-all duration-300 disabled:opacity-50 text-lg group"
                                        >
                                            <Play className="w-6 h-6 mr-3 group-hover:scale-110 transition-transform" />
                                            듀오 분석 시작
                                        </Button>
                                    </div>
                                </div>
                            )}

                            {analysisStep === "analyzing" && (
                                <div className="animate-in fade-in-0 slide-in-from-left-4 duration-500 text-center">
                                    <div className="flex items-center justify-center gap-4 mb-6">
                                        <div className="relative">
                                            <div className="w-16 h-16 border-4 border-white/30 border-t-white rounded-full animate-spin" />
                                            <div className="absolute inset-2 bg-white/20 rounded-full animate-pulse" />
                                        </div>
                                        <div>
                                            <h2 className="text-4xl font-bold mb-1">듀오 분석 진행 중</h2>
                                            <p className="text-blue-100">AI가 두 플레이어의 협력 패턴을 분석하고 있어요</p>
                                        </div>
                                    </div>

                                    <div className="bg-white/20 rounded-3xl p-8 backdrop-blur-sm mb-8">
                                        <p className="text-xl mb-4">
                                            <span className="font-bold text-yellow-300">{player1.name}#{player1.tag}</span> 와{" "}
                                            <span className="font-bold text-yellow-300">{player2.name}#{player2.tag}</span> 의{" "}
                                            듀오 플레이 분석을 진행하고 있습니다
                                        </p>

                                        <div className="flex items-center justify-center gap-3 text-blue-100">
                                            <div className="w-3 h-3 bg-blue-400 rounded-full animate-bounce" />
                                            <div className="w-3 h-3 bg-purple-400 rounded-full animate-bounce delay-100" />
                                            <div className="w-3 h-3 bg-indigo-400 rounded-full animate-bounce delay-200" />
                                            <span className="ml-4">시너지와 협력 패턴을 분석하고 있습니다</span>
                                        </div>
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>
                </Card>

                {/* Results Section */}
                {analysisStep === "results" && analysisResult && (
                    <div className="space-y-8">
                        <Card className="border-0 shadow-2xl backdrop-blur-xl bg-white/80">
                            <CardHeader className="bg-gradient-to-r from-green-50 to-blue-50 border-b border-gray-100">
                                <CardTitle className="flex items-center text-2xl">
                                    <div className="w-10 h-10 bg-gradient-to-r from-green-500 to-blue-500 rounded-xl flex items-center justify-center mr-4">
                                        <Trophy className="w-6 h-6 text-white" />
                                    </div>
                                    듀오 분석 결과
                                </CardTitle>
                                <p className="text-gray-600 font-medium">AI가 분석한 협력 패턴과 개선점</p>
                            </CardHeader>
                            <CardContent className="p-8">
                                <div className="bg-gradient-to-r from-green-50 to-blue-50 border border-green-200 p-6 rounded-2xl mb-6">
                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                        <div>
                                            <h4 className="font-bold text-lg text-gray-900 mb-3">매치 정보</h4>
                                            <div className="space-y-2 text-sm">
                                                <p><strong>매치 ID:</strong> {analysisResult.analysisRecord.matchId}</p>
                                                <p><strong>분석 상태:</strong> <span className="text-green-600">{analysisResult.analysisRecord.status}</span></p>
                                                <p><strong>분석 시간:</strong> {new Date(analysisResult.analysisRecord.createdAt).toLocaleString('ko-KR')}</p>
                                            </div>
                                        </div>
                                        <div>
                                            <h4 className="font-bold text-lg text-gray-900 mb-3">플레이어 정보</h4>
                                            <div className="space-y-2 text-sm">
                                                <p><strong>플레이어 1:</strong> {analysisResult.analysisRecord.player1Name} ({analysisResult.analysisRecord.player1Champion})</p>
                                                <p><strong>플레이어 2:</strong> {analysisResult.analysisRecord.player2Name} ({analysisResult.analysisRecord.player2Champion})</p>
                                                <p><strong>업데이트:</strong> {new Date(analysisResult.analysisRecord.updatedAt).toLocaleString('ko-KR')}</p>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div>
                                    <h4 className="font-bold text-xl text-gray-900 mb-4 flex items-center">
                                        <Brain className="w-6 h-6 mr-3 text-purple-600" />
                                        AI 듀오 분석 결과
                                    </h4>
                                    <div className="bg-white p-6 rounded-2xl border-2 border-gray-100 min-h-32">
                                        <div className="prose prose-sm max-w-none">
                                            <ReactMarkdown>
                                                {analysisResult.analysisRecord.analysisSummary || '분석 결과가 없습니다.'}
                                            </ReactMarkdown>
                                        </div>
                                    </div>
                                </div>
                            </CardContent>
                        </Card>

                        {/* New Analysis Button */}
                        <div className="text-center">
                            <Button
                                onClick={resetAnalysis}
                                className="bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 font-bold px-10 py-4 rounded-2xl shadow-2xl hover:shadow-3xl transition-all duration-300 text-lg"
                            >
                                새로운 듀오 분석 시작
                            </Button>
                        </div>
                    </div>
                )}

                {/* Analysis Features - Only show when in input step */}
                {analysisStep === "input" && (
                    <div className="grid lg:grid-cols-3 gap-8 mb-16">
                        <Card className="border-0 shadow-2xl hover:shadow-3xl transition-all duration-500 group backdrop-blur-xl bg-white/80 hover:bg-white/90 overflow-hidden">
                            <div className="absolute inset-0 bg-gradient-to-br from-blue-500/5 to-cyan-500/5 opacity-0 group-hover:opacity-100 transition-opacity" />
                            <CardContent className="p-8 text-center relative z-10">
                                <div className="mb-6 flex justify-center group-hover:scale-110 transition-transform duration-300">
                                    <div className="p-4 bg-gradient-to-br from-blue-500 to-cyan-500 rounded-2xl shadow-lg">
                                        <BarChart3 className="w-10 h-10 text-white" />
                                    </div>
                                </div>
                                <h4 className="text-2xl font-bold text-slate-800 mb-4">성능 비교</h4>
                                <p className="text-slate-600 leading-relaxed text-lg">KDA, CS, 데미지 등 핵심 지표를 상세 비교 분석</p>
                            </CardContent>
                        </Card>

                        <Card className="border-0 shadow-2xl hover:shadow-3xl transition-all duration-500 group backdrop-blur-xl bg-white/80 hover:bg-white/90 overflow-hidden">
                            <div className="absolute inset-0 bg-gradient-to-br from-purple-500/5 to-indigo-500/5 opacity-0 group-hover:opacity-100 transition-opacity" />
                            <CardContent className="p-8 text-center relative z-10">
                                <div className="mb-6 flex justify-center group-hover:scale-110 transition-transform duration-300">
                                    <div className="p-4 bg-gradient-to-br from-purple-500 to-indigo-500 rounded-2xl shadow-lg">
                                        <Brain className="w-10 h-10 text-white" />
                                    </div>
                                </div>
                                <h4 className="text-2xl font-bold text-slate-800 mb-4">시너지 분석</h4>
                                <p className="text-slate-600 leading-relaxed text-lg">두 플레이어 간의 협력 패턴과 시너지 효과 분석</p>
                            </CardContent>
                        </Card>

                        <Card className="border-0 shadow-2xl hover:shadow-3xl transition-all duration-500 group backdrop-blur-xl bg-white/80 hover:bg-white/90 overflow-hidden">
                            <div className="absolute inset-0 bg-gradient-to-br from-green-500/5 to-emerald-500/5 opacity-0 group-hover:opacity-100 transition-opacity" />
                            <CardContent className="p-8 text-center relative z-10">
                                <div className="mb-6 flex justify-center group-hover:scale-110 transition-transform duration-300">
                                    <div className="p-4 bg-gradient-to-br from-green-500 to-emerald-500 rounded-2xl shadow-lg">
                                        <TrendingUp className="w-10 h-10 text-white" />
                                    </div>
                                </div>
                                <h4 className="text-2xl font-bold text-slate-800 mb-4">개선 제안</h4>
                                <p className="text-slate-600 leading-relaxed text-lg">듀오 플레이 향상을 위한 맞춤형 개선 방안 제시</p>
                            </CardContent>
                        </Card>
                    </div>
                )}

                {/* Features Section */}
                {analysisStep === "input" && (
                    <div className="text-center">
                        <h3 className="text-4xl font-bold text-slate-800 mb-4">듀오 분석의 특별한 기능들</h3>
                        <p className="text-lg text-slate-600 mb-12">친구와 함께하는 게임을 더욱 효과적으로 분석하세요</p>
                        <div className="grid md:grid-cols-3 gap-8">
                            <FeatureCard
                                icon={<Users className="w-10 h-10 text-blue-500" />}
                                title="협력 패턴 분석"
                                description="두 플레이어의 협력 방식과 팀워크를 상세 분석합니다"
                                gradient="from-blue-400 to-cyan-500"
                            />
                            <FeatureCard
                                icon={<Trophy className="w-10 h-10 text-yellow-500" />}
                                title="승률 최적화"
                                description="듀오 플레이 승률 향상을 위한 전략을 제공합니다"
                                gradient="from-yellow-400 to-orange-500"
                            />
                            <FeatureCard
                                icon={<Sparkles className="w-10 h-10 text-purple-500" />}
                                title="실시간 코칭"
                                description="게임 중 실시간으로 듀오 전략을 제안합니다"
                                gradient="from-purple-400 to-pink-500"
                            />
                        </div>
                    </div>
                )}
            </div>
        </div>
    )
}

function FeatureCard({
    icon,
    title,
    description,
    gradient,
}: { icon: React.ReactNode; title: string; description: string; gradient: string }) {
    return (
        <Card className="border-0 shadow-2xl hover:shadow-3xl transition-all duration-500 group backdrop-blur-xl bg-white/80 hover:bg-white/90 overflow-hidden">
            <div
                className={`absolute inset-0 bg-gradient-to-br ${gradient} opacity-0 group-hover:opacity-5 transition-opacity`}
            />
            <CardContent className="p-8 text-center relative z-10">
                <div className="mb-6 flex justify-center group-hover:scale-110 transition-transform duration-300">
                    <div className={`p-4 bg-gradient-to-br ${gradient} rounded-2xl shadow-lg`}>
                        <div className="text-white">{icon}</div>
                    </div>
                </div>
                <h4 className="text-2xl font-bold text-slate-800 mb-4">{title}</h4>
                <p className="text-slate-600 leading-relaxed text-lg">{description}</p>
            </CardContent>
        </Card>
    )
}