import { SignedIn, SignedOut, UserButton, useAuth } from '@clerk/remix'
import { Link, useNavigate } from '@remix-run/react'
import type { MetaFunction } from "@remix-run/node";
import { useApi } from '~/utils/api'
import { useEffect, useState } from 'react'
import { Button } from "~/components/ui/button";
import { Input } from "~/components/ui/input";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "~/components/ui/card";
import { Badge } from "~/components/ui/badge";
import {
  Target,
  Play,
  CheckCircle2,
  BarChart3,
  Zap,
  Brain,
  TrendingUp,
  Users,
  Award,
  Sparkles,
  ArrowLeft,
  Star,
  Gamepad2,
} from "lucide-react"

interface AIResponseData {
  analysisResult?: string;
  [key: string]: unknown;
}

interface AccountInfo {
  puuid: string;
  gameName: string;
  tagLine: string;
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
    aiResponseData: AIResponseData;
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
    aiResponseData: AIResponseData;
  };
  message: string;
}

export const meta: MetaFunction = () => {
  return [
    { title: "IKU AI 분석 시스템" },
    { name: "description", content: "AI 게임 분석 시스템" },
  ];
};

export default function Index() {
  const apiFetch = useApi()
  const { isSignedIn } = useAuth()
  const navigate = useNavigate()
  const [user, setUser] = useState<any>(null)
  const [error, setError] = useState<string | null>(null)
  
  // 새로운 UI 상태 관리
  const [playerName, setPlayerName] = useState("")
  const [tagLine, setTagLine] = useState("")
  const [analysisStep, setAnalysisStep] = useState<"input" | "select" | "analyzing">("input")
  const [selectedAnalysisType, setSelectedAnalysisType] = useState<"single" | "comprehensive" | null>(null)
  const [isAnalyzing, setIsAnalyzing] = useState(false)
  
  // 기존 상태들
  const [accountInfo, setAccountInfo] = useState<AccountInfo | null>(null);
  const [singleAnalysisResult, setSingleAnalysisResult] = useState<SingleAnalysisResult | null>(null);
  const [multipleAnalysisResult, setMultipleAnalysisResult] = useState<MultipleAnalysisResult | null>(null);
  const [apiError, setApiError] = useState<string>("");

  useEffect(() => {
    if (!isSignedIn) {
      setUser(null)
      setError(null)
      return
    }

    const fetchUser = async () => {
      try {
        const userData = await apiFetch('/api/users/me')
        setUser(userData)
        setError(null)
      } catch (err) {
        if (err instanceof Error) {
          setError(err.message)
        } else {
          setError('An unknown error occurred')
        }
      }
    }

    fetchUser()
  }, [apiFetch, isSignedIn])

  const handleStartAnalysis = () => {
    if (analysisStep === "input") {
      setAnalysisStep("select")
    } else if (analysisStep === "select" && selectedAnalysisType) {
      performAnalysis()
    }
  }

  const handleAnalysisTypeSelect = (type: "single" | "comprehensive") => {
    setSelectedAnalysisType(type)
  }

  const handleBackToInput = () => {
    setAnalysisStep("input")
    setSelectedAnalysisType(null)
  }

  const resetToStart = () => {
    setAnalysisStep("input")
    setSelectedAnalysisType(null)
    setPlayerName("")
    setTagLine("")
    setAccountInfo(null)
    setSingleAnalysisResult(null)
    setMultipleAnalysisResult(null)
    setApiError("")
  }

  const performAnalysis = async () => {
    setAnalysisStep("analyzing")
    setIsAnalyzing(true)
    setApiError("")

    try {
      // 1. 계정 정보 조회 (trim spaces)
      const trimmedPlayerName = playerName.trim()
      const trimmedTagLine = tagLine.trim()
      
      if (!trimmedPlayerName || !trimmedTagLine) {
        setApiError("플레이어명과 태그를 모두 입력해주세요.");
        setAnalysisStep("select")
        setIsAnalyzing(false)
        return;
      }
      
      const accountResponse = await apiFetch(`/api/riot/account/${encodeURIComponent(trimmedPlayerName)}/${encodeURIComponent(trimmedTagLine)}`);
      
      if (!accountResponse.account) {
        setApiError("계정 정보를 찾을 수 없습니다.");
        setAnalysisStep("select")
        setIsAnalyzing(false)
        return;
      }

      const account = accountResponse.account;
      setAccountInfo(account);

      if (selectedAnalysisType === "single") {
        // 단일 게임 분석
        const matchResponse = await apiFetch(`/api/riot/matches/${account.puuid}`);
        
        if (!matchResponse.selectedMatchId) {
          setApiError("매치 정보를 찾을 수 없습니다.");
          setAnalysisStep("select")
          setIsAnalyzing(false)
          return;
        }

        const analysisResponse = await apiFetch(
          `/api/analysis/analyze/${account.puuid}/${matchResponse.selectedMatchId}`,
          { method: 'POST' }
        );
        
        setSingleAnalysisResult(analysisResponse);
        setMultipleAnalysisResult(null);
        
        // 단일 분석 완료 후 분석 페이지로 리다이렉트
        navigate('/analysis', {
          state: {
            analysisData: analysisResponse.analysisRecord,
            analysisType: 'single'
          }
        });
        return;
      } else {
        // 다중 게임 분석
        const analysisResponse = await apiFetch(
          `/api/analysis/analyze-multiple/${account.puuid}?matchCount=5`,
          { method: 'POST' }
        );
        
        setMultipleAnalysisResult(analysisResponse);
        setSingleAnalysisResult(null);
        
        // 다중 분석 완료 후 분석 페이지로 리다이렉트
        navigate('/analysis', {
          state: {
            analysisData: analysisResponse.analysisRecord,
            analysisType: 'multiple'
          }
        });
        return;
      }
    } catch (err) {
      console.error("분석 오류:", err);
      setApiError("분석에 실패했습니다. 다시 시도해주세요.");
      setAnalysisStep("select")
    } finally {
      setIsAnalyzing(false)
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-cyan-50 relative overflow-hidden">
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
                <Target className="w-12 h-12 text-blue-600" />
              </div>
              <div className="absolute -top-2 -right-2 w-6 h-6 bg-gradient-to-r from-pink-500 to-rose-500 rounded-full animate-bounce">
                <Star className="w-4 h-4 text-white m-1" />
              </div>
            </div>
            <div>
              <h1 className="text-5xl font-black bg-gradient-to-r from-blue-600 via-purple-600 to-indigo-600 bg-clip-text text-transparent mb-2">
                AI 게임 분석
              </h1>
              <div className="flex items-center justify-center gap-2">
                <div className="w-2 h-2 bg-blue-500 rounded-full animate-pulse" />
                <div className="w-2 h-2 bg-purple-500 rounded-full animate-pulse delay-100" />
                <div className="w-2 h-2 bg-indigo-500 rounded-full animate-pulse delay-200" />
              </div>
            </div>
          </div>
          <p className="text-xl text-slate-600 max-w-2xl mx-auto leading-relaxed">
            플레이어명만 입력하면 최신 게임을 자동으로 분석해드려요
          </p>
          <div className="mt-4 flex items-center justify-center gap-2 text-sm text-slate-500">
            <Gamepad2 className="w-4 h-4" />
            <span>AI 기반 실시간 게임 분석 플랫폼</span>
          </div>
          <SignedIn>
            {user && (
              <div className="mt-2 text-sm text-gray-500">
                환영합니다, {user.email}
              </div>
            )}
            {error && (
              <div className="mt-2 text-sm text-red-600">
                오류: {error}
              </div>
            )}
          </SignedIn>
          <SignedOut>
            <div className="mt-2 text-sm text-blue-600">
              베타 서비스 - 로그인 없이도 이용 가능합니다
            </div>
          </SignedOut>
        </div>

        {/* Error Display */}
        {apiError && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-6">
            {apiError}
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
                      <Zap className="w-8 h-8" />
                    </div>
                    <div>
                      <h2 className="text-4xl font-bold mb-1">빠른 AI 분석</h2>
                      <p className="text-blue-100">계정 조회부터 분석까지 원클릭으로 완료</p>
                    </div>
                  </div>

                  <div className="grid md:grid-cols-2 gap-8 mb-10">
                    <div className="space-y-3">
                      <label className="text-sm font-semibold text-blue-100 uppercase tracking-wider">플레이어명</label>
                      <div className="relative group">
                        <Input
                          value={playerName}
                          onChange={(e) => setPlayerName(e.target.value)}
                          placeholder="예: Hide on bush"
                          className="bg-white/20 border-white/30 text-white placeholder:text-blue-200 focus:bg-white/30 transition-all duration-300 h-14 text-lg rounded-2xl backdrop-blur-sm group-hover:bg-white/25"
                        />
                        <div className="absolute inset-0 rounded-2xl bg-gradient-to-r from-white/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none" />
                      </div>
                    </div>
                    <div className="space-y-3">
                      <label className="text-sm font-semibold text-blue-100 uppercase tracking-wider">태그</label>
                      <div className="relative group">
                        <Input
                          value={tagLine}
                          onChange={(e) => setTagLine(e.target.value)}
                          placeholder="예: KR1"
                          className="bg-white/20 border-white/30 text-white placeholder:text-blue-200 focus:bg-white/30 transition-all duration-300 h-14 text-lg rounded-2xl backdrop-blur-sm group-hover:bg-white/25"
                        />
                        <div className="absolute inset-0 rounded-2xl bg-gradient-to-r from-white/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none" />
                      </div>
                    </div>
                  </div>

                  <Button
                    onClick={handleStartAnalysis}
                    disabled={!playerName.trim() || !tagLine.trim()}
                    className="bg-white text-blue-600 hover:bg-blue-50 font-bold px-10 py-4 rounded-2xl shadow-2xl hover:shadow-3xl transition-all duration-300 disabled:opacity-50 text-lg group"
                  >
                    <Play className="w-6 h-6 mr-3 group-hover:scale-110 transition-transform" />
                    다음 단계
                    <div className="absolute inset-0 rounded-2xl bg-gradient-to-r from-blue-600/20 to-purple-600/20 opacity-0 group-hover:opacity-100 transition-opacity" />
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
                      <h2 className="text-4xl font-bold mb-1">분석 타입 선택</h2>
                      <p className="text-blue-100">
                        <span className="font-semibold text-yellow-300">{playerName}</span>님의 게임 분석 방식을
                        선택해주세요
                      </p>
                    </div>
                  </div>

                  <div className="grid md:grid-cols-2 gap-8 mb-10">
                    <button
                      onClick={() => handleAnalysisTypeSelect("single")}
                      className={`p-8 rounded-3xl border-2 transition-all duration-500 text-left group relative overflow-hidden ${
                        selectedAnalysisType === "single"
                          ? "border-white bg-white/30 shadow-2xl scale-105"
                          : "border-white/30 hover:border-white/60 hover:bg-white/20 hover:scale-102"
                      }`}
                    >
                      <div className="absolute inset-0 bg-gradient-to-br from-white/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity" />
                      <div className="relative z-10">
                        <div className="flex items-center gap-4 mb-4">
                          <div className="p-3 bg-blue-500/30 rounded-2xl backdrop-blur-sm">
                            <Play className="w-8 h-8" />
                          </div>
                          <h3 className="text-2xl font-bold">단일 게임 분석</h3>
                        </div>
                        <p className="text-blue-100 mb-6 text-lg">최신 게임 1개를 상세히 분석합니다</p>
                        <div className="space-y-3">
                          <div className="flex items-center gap-3">
                            <CheckCircle2 className="w-5 h-5 text-green-400" />
                            <span className="text-sm">핵심 순간별 플레이 분석</span>
                          </div>
                          <div className="flex items-center gap-3">
                            <CheckCircle2 className="w-5 h-5 text-green-400" />
                            <span className="text-sm">즉시 개선 가능한 팁 제공</span>
                          </div>
                        </div>
                      </div>
                    </button>

                    <button
                      onClick={() => handleAnalysisTypeSelect("comprehensive")}
                      className={`p-8 rounded-3xl border-2 transition-all duration-500 text-left group relative overflow-hidden ${
                        selectedAnalysisType === "comprehensive"
                          ? "border-white bg-white/30 shadow-2xl scale-105"
                          : "border-white/30 hover:border-white/60 hover:bg-white/20 hover:scale-102"
                      }`}
                    >
                      <div className="absolute inset-0 bg-gradient-to-br from-white/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity" />
                      <div className="relative z-10">
                        <div className="flex items-center gap-4 mb-4">
                          <div className="p-3 bg-emerald-500/30 rounded-2xl backdrop-blur-sm">
                            <BarChart3 className="w-8 h-8" />
                          </div>
                          <h3 className="text-2xl font-bold">종합 게임 분석</h3>
                        </div>
                        <p className="text-blue-100 mb-6 text-lg">최근 5게임의 패턴을 종합 분석합니다</p>
                        <div className="space-y-3">
                          <div className="flex items-center gap-3">
                            <CheckCircle2 className="w-5 h-5 text-green-400" />
                            <span className="text-sm">심리적 플레이 성향 파악</span>
                          </div>
                          <div className="flex items-center gap-3">
                            <CheckCircle2 className="w-5 h-5 text-green-400" />
                            <span className="text-sm">맞춤형 성장 로드맵 제공</span>
                          </div>
                        </div>
                      </div>
                    </button>
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
                      onClick={handleStartAnalysis}
                      disabled={!selectedAnalysisType}
                      className="bg-white text-blue-600 hover:bg-blue-50 font-bold px-10 py-4 rounded-2xl shadow-2xl hover:shadow-3xl transition-all duration-300 disabled:opacity-50 text-lg group"
                    >
                      <Play className="w-6 h-6 mr-3 group-hover:scale-110 transition-transform" />
                      분석 시작
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
                      <h2 className="text-4xl font-bold mb-1">분석 진행 중</h2>
                      <p className="text-blue-100">AI가 열심히 분석하고 있어요</p>
                    </div>
                  </div>

                  <div className="bg-white/20 rounded-3xl p-8 backdrop-blur-sm mb-8">
                    <p className="text-xl mb-4">
                      <span className="font-bold text-yellow-300">{playerName}</span>님의{" "}
                      <span className="font-semibold">
                        {selectedAnalysisType === "single" ? "단일 게임" : "종합 게임"}
                      </span>{" "}
                      분석을 진행하고 있습니다
                    </p>

                    <div className="flex items-center justify-center gap-3 text-blue-100">
                      <div className="w-3 h-3 bg-blue-400 rounded-full animate-bounce" />
                      <div className="w-3 h-3 bg-purple-400 rounded-full animate-bounce delay-100" />
                      <div className="w-3 h-3 bg-indigo-400 rounded-full animate-bounce delay-200" />
                      <span className="ml-4">AI가 게임 데이터를 분석하고 있습니다</span>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        </Card>


        {/* Analysis Types - Only show when in input step */}
        {analysisStep === "input" && (
          <div className="grid lg:grid-cols-2 gap-8 mb-16">
            {/* Single Game Analysis */}
            <Card className="border-0 shadow-2xl hover:shadow-3xl transition-all duration-500 group backdrop-blur-xl bg-white/80 hover:bg-white/90 overflow-hidden">
              <div className="absolute inset-0 bg-gradient-to-br from-blue-500/5 to-purple-500/5 opacity-0 group-hover:opacity-100 transition-opacity" />
              <CardHeader className="pb-6 relative z-10">
                <div className="flex items-center gap-4">
                  <div className="p-4 bg-gradient-to-br from-blue-500 to-blue-600 rounded-2xl shadow-lg group-hover:scale-110 transition-transform">
                    <Play className="w-8 h-8 text-white" />
                  </div>
                  <CardTitle className="text-3xl font-bold text-slate-800">단일 게임 분석</CardTitle>
                </div>
              </CardHeader>
              <CardContent className="space-y-5 relative z-10">
                <AnalysisFeature
                  icon={<CheckCircle2 className="w-6 h-6 text-emerald-500" />}
                  text="최신 게임 1개 상세 분석"
                  badge="실시간"
                />
                <AnalysisFeature
                  icon={<Brain className="w-6 h-6 text-emerald-500" />}
                  text="핵심 순간별 플레이 분석"
                  badge="AI 분석"
                />
                <AnalysisFeature
                  icon={<TrendingUp className="w-6 h-6 text-emerald-500" />}
                  text="즉시 개선 가능한 팁 제공"
                  badge="맞춤형"
                />
              </CardContent>
            </Card>

            {/* Comprehensive Analysis */}
            <Card className="border-0 shadow-2xl hover:shadow-3xl transition-all duration-500 group backdrop-blur-xl bg-white/80 hover:bg-white/90 overflow-hidden">
              <div className="absolute inset-0 bg-gradient-to-br from-emerald-500/5 to-teal-500/5 opacity-0 group-hover:opacity-100 transition-opacity" />
              <CardHeader className="pb-6 relative z-10">
                <div className="flex items-center gap-4">
                  <div className="p-4 bg-gradient-to-br from-emerald-500 to-emerald-600 rounded-2xl shadow-lg group-hover:scale-110 transition-transform">
                    <BarChart3 className="w-8 h-8 text-white" />
                  </div>
                  <CardTitle className="text-3xl font-bold text-slate-800">종합 게임 분석</CardTitle>
                </div>
              </CardHeader>
              <CardContent className="space-y-5 relative z-10">
                <AnalysisFeature
                  icon={<CheckCircle2 className="w-6 h-6 text-emerald-500" />}
                  text="최근 5게임 패턴 분석"
                  badge="트렌드"
                />
                <AnalysisFeature
                  icon={<Users className="w-6 h-6 text-emerald-500" />}
                  text="심리적 플레이 성향 파악"
                  badge="심층 분석"
                />
                <AnalysisFeature
                  icon={<Award className="w-6 h-6 text-emerald-500" />}
                  text="맞춤형 성장 로드맵 제공"
                  badge="개인화"
                />
              </CardContent>
            </Card>
          </div>
        )}

        {/* Features Section */}
        {analysisStep === "input" && (
          <div className="text-center">
            <h3 className="text-4xl font-bold text-slate-800 mb-4">AI 분석의 특별한 기능들</h3>
            <p className="text-lg text-slate-600 mb-12">최첨단 AI 기술로 제공하는 프리미엄 게임 분석 서비스</p>
            <div className="grid md:grid-cols-3 gap-8">
              <FeatureCard
                icon={<Sparkles className="w-10 h-10 text-yellow-500" />}
                title="실시간 분석"
                description="게임 종료 즉시 상세한 분석 결과를 제공합니다"
                gradient="from-yellow-400 to-orange-500"
              />
              <FeatureCard
                icon={<Brain className="w-10 h-10 text-purple-500" />}
                title="AI 기반 인사이트"
                description="머신러닝으로 숨겨진 패턴과 개선점을 발견합니다"
                gradient="from-purple-400 to-pink-500"
              />
              <FeatureCard
                icon={<TrendingUp className="w-10 h-10 text-green-500" />}
                title="성장 추적"
                description="시간에 따른 실력 향상을 시각적으로 확인하세요"
                gradient="from-green-400 to-teal-500"
              />
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

function AnalysisFeature({ icon, text, badge }: { icon: React.ReactNode; text: string; badge: string }) {
  return (
    <div className="flex items-center gap-4 p-4 rounded-2xl hover:bg-slate-50/80 transition-all duration-300 group">
      <div className="group-hover:scale-110 transition-transform">{icon}</div>
      <span className="flex-1 text-slate-700 font-semibold text-lg">{text}</span>
      <Badge
        variant="secondary"
        className="bg-gradient-to-r from-blue-100 to-purple-100 text-blue-700 hover:from-blue-200 hover:to-purple-200 px-3 py-1 rounded-full font-medium"
      >
        {badge}
      </Badge>
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