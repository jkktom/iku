import { useLocation, useNavigate } from "@remix-run/react"
import { useEffect, useState } from "react"
import ReactMarkdown from "react-markdown"
import { Button } from "~/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "~/components/ui/card"
import { Badge } from "~/components/ui/badge"
import { ArrowLeft, Loader2, AlertTriangle, User, Trophy, Target, Brain, TrendingUp, CheckCircle, Eye, Shield, Zap, Clock, Star, ArrowRight, PlayCircle, BarChart3, Map, Sparkles, Crown, Flame, Users, Heart } from "lucide-react"
import DuoAnalysisReportPage from "./analysis-duo"

export default function AnalysisPage() {
  const location = useLocation()
  const navigate = useNavigate()
  
  const [currentStep, setCurrentStep] = useState(0)
  const [parsedData, setParsedData] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string>("")

  // state에서 분석 데이터 가져오기
  const analysisData = location.state?.analysisData
  const analysisType = location.state?.analysisType

  useEffect(() => {
    console.log("Received analysisData:", analysisData); // 데이터 확인용 콘솔 로그
    if (!analysisData) {
      setError("분석 데이터가 없습니다. 분석 기록 페이지에서 다시 시도해주세요.")
      setLoading(false)
      return
    }

    try {
      setLoading(true);

      try {
        let finalReportData = null;

        // 우선순위 1: analysisSummary에서 JSON 파싱 시도
        if (analysisData?.analysisSummary && typeof analysisData.analysisSummary === 'string') {
          try {
            const summaryData = JSON.parse(analysisData.analysisSummary);
            console.log("analysisSummary 파싱 결과:", summaryData);
            
            if (summaryData?.analysisResult) {
              finalReportData = summaryData.analysisResult;
              console.log("analysisSummary에서 analysisResult 추출:", finalReportData);
            } else if (summaryData?.playerOverview) {
              // analysisSummary 자체가 바로 분석 결과인 경우
              finalReportData = summaryData;
              console.log("analysisSummary 자체가 분석 결과:", finalReportData);
            }
          } catch (summaryError) {
            console.log("analysisSummary 파싱 실패:", summaryError);
          }
        }

        // 우선순위 2: aiResponseData.analysisResult에서 JSON 파싱 시도
        if (!finalReportData && analysisData?.aiResponseData?.analysisResult) {
          try {
            if (typeof analysisData.aiResponseData.analysisResult === 'string') {
              const analysisResultData = JSON.parse(analysisData.aiResponseData.analysisResult);
              console.log("aiResponseData.analysisResult 파싱 결과:", analysisResultData);
              
              if (analysisResultData?.analysisResult) {
                finalReportData = analysisResultData.analysisResult;
              } else if (analysisResultData?.playerOverview) {
                finalReportData = analysisResultData;
              }
            } else if (typeof analysisData.aiResponseData.analysisResult === 'object') {
              finalReportData = analysisData.aiResponseData.analysisResult;
            }
          } catch (analysisResultError) {
            console.log("aiResponseData.analysisResult 파싱 실패:", analysisResultError);
          }
        }

        // 최종 데이터 유효성 검사 및 구조 확인
        if (finalReportData) {
          console.log("최종 파싱된 데이터:", finalReportData);
          
          // 듀오 분석 데이터 구조 확인
          if (finalReportData.matchInfo && finalReportData.playerComparison) {
            console.log("✅ 듀오 분석 데이터 파싱 성공!");
            setParsedData(finalReportData);
          }
          // 단일/다중 게임 분석 데이터 구조 확인
          else if (finalReportData.playerOverview && 
              finalReportData.criticalMoments && 
              finalReportData.psychologyInsights && 
              finalReportData.actionPlans && 
              finalReportData.roadmapGoals) {
            console.log("✅ 단일/다중 게임 분석 데이터 파싱 성공!");
            setParsedData(finalReportData);
          } else {
            console.log("❌ 필수 필드 누락. 현재 필드들:", Object.keys(finalReportData));
            console.log("듀오 분석 필드:");
            console.log("- matchInfo:", !!finalReportData.matchInfo);
            console.log("- playerComparison:", !!finalReportData.playerComparison);
            console.log("단일/다중 게임 분석 필드:");
            console.log("- playerOverview:", !!finalReportData.playerOverview);
            console.log("- criticalMoments:", !!finalReportData.criticalMoments);
            console.log("- psychologyInsights:", !!finalReportData.psychologyInsights);
            console.log("- actionPlans:", !!finalReportData.actionPlans);
            console.log("- roadmapGoals:", !!finalReportData.roadmapGoals);
          }
        } else {
          console.log("❌ 파싱된 데이터가 없습니다");
        }
      } catch (e) {
        console.error("리포트 데이터 처리 중 심각한 오류 발생:", e);
      }
      
    } catch (err) {
      console.error('분석 데이터 처리 실패:', err)
      setError('분석 데이터를 처리할 수 없습니다.')
    } finally {
      setLoading(false)
    }
  }, [analysisData])

  if (!analysisData) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50 flex items-center justify-center p-4">
        <Card className="max-w-md w-full">
          <CardContent className="p-8 text-center">
            <AlertTriangle className="w-16 h-16 text-red-500 mx-auto mb-4" />
            <h2 className="text-xl font-bold text-gray-900 mb-2">데이터 없음</h2>
            <p className="text-gray-600 mb-6">분석 데이터가 없습니다. 분석 기록 페이지에서 다시 시도해주세요.</p>
            <Button 
              onClick={() => navigate('/match-history')} 
              className="bg-blue-600 hover:bg-blue-700"
            >
              <ArrowLeft className="w-4 h-4 mr-2" />
              분석 기록으로 돌아가기
            </Button>
          </CardContent>
        </Card>
      </div>
    )
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50 flex items-center justify-center">
        <Card className="p-8">
          <div className="flex items-center justify-center space-x-4">
            <Loader2 className="w-8 h-8 animate-spin text-blue-500" />
            <span className="text-lg text-gray-600">분석 결과를 불러오는 중...</span>
          </div>
        </Card>
      </div>
    )
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50 flex items-center justify-center p-4">
        <Card className="max-w-md w-full">
          <CardContent className="p-8 text-center">
            <AlertTriangle className="w-16 h-16 text-red-500 mx-auto mb-4" />
            <h2 className="text-xl font-bold text-gray-900 mb-2">오류 발생</h2>
            <p className="text-gray-600 mb-6">{error}</p>
            <Button 
              onClick={() => navigate('/match-history')} 
              className="bg-blue-600 hover:bg-blue-700"
            >
              <ArrowLeft className="w-4 h-4 mr-2" />
              분석 기록으로 돌아가기
            </Button>
          </CardContent>
        </Card>
      </div>
    )
  }

  // JSON 파싱된 데이터가 있으면 AICoachingReport 스타일로 렌더링
  if (parsedData) {
    return <AICoachingReportPage data={parsedData} analysisData={analysisData} analysisType={analysisType} onBack={() => navigate('/match-history')} />
  }

  // JSON 파싱 실패시 기존 마크다운 렌더링
  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50">
      <div className="container mx-auto px-4 py-6 max-w-6xl">
        <Button
          onClick={() => navigate('/match-history')}
          variant="outline"
          className="mb-6 bg-white/80 backdrop-blur-sm hover:bg-white/90"
        >
          <ArrowLeft className="w-4 h-4 mr-2" />
          분석 기록으로 돌아가기
        </Button>

        <Card className="border-0 shadow-2xl backdrop-blur-xl bg-white/80">
          <CardHeader>
            <CardTitle className="text-2xl">분석 결과</CardTitle>
            <div className="flex gap-2">
              <Badge variant="outline">
                {analysisType === 'single' ? '단일 게임 분석' : 
                 analysisType === 'multiple' ? '종합 게임 분석' : '듀오 게임 분석'}
              </Badge>
              {analysisData.targetPlayerName && (
                <Badge variant="secondary">{analysisData.targetPlayerName}</Badge>
              )}
              {analysisData.player1Name && analysisData.player2Name && (
                <Badge variant="secondary">{analysisData.player1Name} & {analysisData.player2Name}</Badge>
              )}
            </div>
          </CardHeader>
          <CardContent>
            <div className="prose prose-lg max-w-none">
              <ReactMarkdown>
                {analysisData.analysisSummary || '분석 결과를 표시할 수 없습니다.'}
              </ReactMarkdown>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

// AICoachingReport 스타일의 페이지 컴포넌트
function AICoachingReportPage({ data, onBack, analysisData, analysisType }: { data: any; onBack: () => void, analysisData: any, analysisType: string }) {
  // 듀오 분석인 경우 다른 컴포넌트 렌더링
  if (analysisType === 'duo') {
    console.log("듀오 분석 데이터:", data);
    // 듀오 분석 데이터가 다른 구조일 수 있으므로 두 가지 경우 모두 처리
    const duoData = data.analysisResult || data;
    if (duoData && (duoData.matchInfo || duoData.playerComparison)) {
      return <DuoAnalysisReportPage data={duoData} analysisData={analysisData} onBack={onBack} />
    }
  }
  const [currentStep, setCurrentStep] = useState(0)
  const [showOriginalReport, setShowOriginalReport] = useState(false)

  const playerData = data.playerOverview
  const criticalMoments = data.criticalMoments
  const psychologyInsights = data.psychologyInsights.map((insight: any, index: number) => ({
    ...insight,
    color: index === 0 ? "from-red-500 to-pink-500" : index === 1 ? "from-orange-500 to-yellow-500" : "from-yellow-500 to-amber-500",
    bgColor: index === 0 ? "bg-red-50 border-red-200" : index === 1 ? "bg-orange-50 border-orange-200" : "bg-yellow-50 border-yellow-200"
  }))

  const actionPlans = data.actionPlans.map((plan: any, index: number) => ({
    ...plan,
    icon: index === 0 ? Clock : index === 1 ? Eye : PlayCircle,
    gradient: index === 0 ? "from-blue-500 to-cyan-500" : index === 1 ? "from-purple-500 to-indigo-500" : "from-green-500 to-emerald-500"
  }))

  const roadmapGoals = data.roadmapGoals.map((goal: any, index: number) => ({
    ...goal,
    icon: index === 0 ? Eye : index === 1 ? Shield : index === 2 ? Brain : index === 3 ? Trophy : index === 4 ? Star : Map,
    color: index === 0 ? "from-blue-500 to-cyan-500" : index === 1 ? "from-green-500 to-emerald-500" : index === 2 ? "from-purple-500 to-indigo-500" : index === 3 ? "from-yellow-500 to-orange-500" : index === 4 ? "from-pink-500 to-rose-500" : "from-teal-500 to-cyan-500"
  }))

  const steps = [
    { id: 0, title: "플레이어 개요", icon: User, color: "from-blue-500 to-cyan-500" },
    { id: 1, title: "결정적 순간", icon: Target, color: "from-red-500 to-pink-500" },
    { id: 2, title: "심리적 패턴", icon: Brain, color: "from-purple-500 to-indigo-500" },
    { id: 3, title: "개선 액션", icon: Zap, color: "from-yellow-500 to-orange-500" },
    { id: 4, title: "성장 로드맵", icon: TrendingUp, color: "from-green-500 to-emerald-500" },
  ]

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50 relative overflow-hidden">
      {/* Animated Background Elements */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute -top-40 -right-40 w-80 h-80 bg-gradient-to-br from-blue-400/20 to-purple-600/20 rounded-full blur-3xl animate-pulse" />
        <div className="absolute -bottom-40 -left-40 w-80 h-80 bg-gradient-to-tr from-cyan-400/20 to-blue-600/20 rounded-full blur-3xl animate-pulse delay-1000" />
        <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-gradient-to-r from-purple-400/10 to-pink-400/10 rounded-full blur-3xl animate-pulse delay-500" />
      </div>

      {/* 헤더 */}
      <div className="bg-gradient-to-r from-blue-600 via-purple-600 to-indigo-700 text-white relative overflow-hidden">
        <div className="absolute inset-0 bg-black/10" />
        <div className="absolute top-0 right-0 w-64 h-64 bg-white/10 rounded-full -translate-y-32 translate-x-32" />
        <div className="absolute bottom-0 left-0 w-48 h-48 bg-white/5 rounded-full translate-y-24 -translate-x-24" />

        <div className="max-w-6xl mx-auto p-8 relative z-10">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-6">
              <div className="relative">
                <div className="w-20 h-20 bg-white/20 rounded-2xl backdrop-blur-sm flex items-center justify-center">
                  <Crown className="w-10 h-10 text-yellow-300" />
                </div>
                <div className="absolute -top-2 -right-2 w-6 h-6 bg-gradient-to-r from-yellow-400 to-orange-500 rounded-full flex items-center justify-center">
                  <Sparkles className="w-4 h-4 text-white" />
                </div>
              </div>
              <div>
                <h1 className="text-4xl font-black mb-2 bg-gradient-to-r from-white to-blue-100 bg-clip-text text-transparent">
                  🎯 {playerData.name}
                </h1>
                <h2 className="text-2xl font-bold mb-2">맞춤형 LoL AI 코칭 리포트</h2>
                <p className="text-blue-100 text-lg">
                  최근 {playerData.gamesAnalyzed}게임 분석을 통한 개인별 성장 로드맵
                </p>
              </div>
            </div>
            <div className="text-right">
              <div className="bg-white/20 rounded-2xl p-6 backdrop-blur-sm border border-white/30">
                <div className="text-3xl font-black text-yellow-300">{playerData.kda}</div>
                <div className="text-sm text-blue-100 font-medium">평균 KDA</div>
                <div className="flex items-center justify-center mt-2 gap-1">
                  <Star className="w-4 h-4 text-yellow-300 fill-current" />
                  <Star className="w-4 h-4 text-yellow-300 fill-current" />
                  <Star className="w-4 h-4 text-yellow-300 fill-current" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-6xl mx-auto p-6 relative z-10">
        {/* 뒤로가기 버튼 */}
        <Button
          onClick={onBack}
          variant="outline"
          className="mb-6 bg-white/80 backdrop-blur-sm hover:bg-white/90"
        >
          <ArrowLeft className="w-4 h-4 mr-2" />
          분석 기록으로 돌아가기
        </Button>

        {/* 네비게이션 탭 */}
        <div className="flex flex-wrap justify-center mb-12 bg-white/80 backdrop-blur-xl rounded-2xl shadow-2xl p-3 border border-white/20">
          {steps.map((step, index) => (
            <button
              key={step.id}
              onClick={() => setCurrentStep(step.id)}
              className={`flex items-center px-6 py-4 rounded-xl m-1 transition-all duration-300 group relative overflow-hidden ${
                currentStep === step.id
                  ? "bg-gradient-to-r text-white shadow-xl scale-105"
                  : "text-gray-600 hover:bg-gray-50 hover:scale-102"
              }`}
              style={{
                background:
                  currentStep === step.id
                    ? `linear-gradient(135deg, ${step.color.split(" ")[1]}, ${step.color.split(" ")[3]})`
                    : undefined,
              }}
            >
              <div className="absolute inset-0 bg-gradient-to-r from-white/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity" />
              <step.icon className="w-5 h-5 mr-3 relative z-10" />
              <span className="font-semibold relative z-10">{step.title}</span>
            </button>
          ))}
        </div>

        {/* 여기에 step별 컨텐츠가 들어갑니다 - 기존 AICoachingReport와 동일한 구조 */}
        {/* Step 0: 플레이어 개요 */}
        {currentStep === 0 && (
          <div className="space-y-8 animate-in fade-in-0 slide-in-from-bottom-4 duration-500">
            <Card className="border-0 shadow-2xl backdrop-blur-xl bg-white/80 overflow-hidden">
              <CardContent className="p-8 relative z-10">
                <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                  <div className="text-center group">
                    <div className="w-16 h-16 bg-white/20 rounded-2xl backdrop-blur-sm flex items-center justify-center mx-auto mb-4 group-hover:scale-110 transition-transform">
                      <BarChart3 className="w-8 h-8" />
                    </div>
                    <div className="text-3xl font-black mb-1">{playerData.winRate}</div>
                    <div className="text-gray-600 font-medium">승률</div>
                  </div>
                  <div className="text-center group">
                    <div className="w-16 h-16 bg-white/20 rounded-2xl backdrop-blur-sm flex items-center justify-center mx-auto mb-4 group-hover:scale-110 transition-transform">
                      <Star className="w-8 h-8" />
                    </div>
                    <div className="text-3xl font-black mb-1">{playerData.kda}</div>
                    <div className="text-gray-600 font-medium">평균 KDA</div>
                  </div>
                  <div className="text-center group">
                    <div className="w-16 h-16 bg-white/20 rounded-2xl backdrop-blur-sm flex items-center justify-center mx-auto mb-4 group-hover:scale-110 transition-transform">
                      <Target className="w-8 h-8" />
                    </div>
                    <div className="text-3xl font-black mb-1">{playerData.gamesAnalyzed}</div>
                    <div className="text-gray-600 font-medium">분석 게임수</div>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card className="border-0 shadow-2xl backdrop-blur-xl bg-white/80 overflow-hidden">
              <CardHeader className="bg-gradient-to-r from-blue-50 to-purple-50 border-b border-gray-100">
                <CardTitle className="flex items-center text-2xl">
                  <div className="w-10 h-10 bg-gradient-to-r from-blue-500 to-purple-500 rounded-xl flex items-center justify-center mr-4">
                    <Brain className="w-6 h-6 text-white" />
                  </div>
                  AI 분석 요약
                </CardTitle>
              </CardHeader>
              <CardContent className="p-8">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                  <div className="space-y-6">
                    <h3 className="font-bold text-xl text-green-700 flex items-center">
                      <div className="w-8 h-8 bg-green-100 rounded-xl flex items-center justify-center mr-3">
                        <CheckCircle className="w-5 h-5 text-green-600" />
                      </div>
                      강점 분석
                    </h3>
                    <ul className="space-y-4">
                      <li className="flex items-start group">
                        <div className="w-3 h-3 bg-gradient-to-r from-green-400 to-emerald-500 rounded-full mt-2 mr-4 group-hover:scale-125 transition-transform"></div>
                        <span className="text-gray-700 font-medium">공격적인 초반 라인전으로 킬 우위 확보</span>
                      </li>
                      <li className="flex items-start group">
                        <div className="w-3 h-3 bg-gradient-to-r from-green-400 to-emerald-500 rounded-full mt-2 mr-4 group-hover:scale-125 transition-transform"></div>
                        <span className="text-gray-700 font-medium">우수한 KDA와 안정적인 승률 유지</span>
                      </li>
                    </ul>
                  </div>
                  <div className="space-y-6">
                    <h3 className="font-bold text-xl text-orange-700 flex items-center">
                      <div className="w-8 h-8 bg-orange-100 rounded-xl flex items-center justify-center mr-3">
                        <AlertTriangle className="w-5 h-5 text-orange-600" />
                      </div>
                      개선 포인트
                    </h3>
                    <ul className="space-y-4">
                      <li className="flex items-start group">
                        <div className="w-3 h-3 bg-gradient-to-r from-orange-400 to-red-500 rounded-full mt-2 mr-4 group-hover:scale-125 transition-transform"></div>
                        <span className="text-gray-700 font-medium">킬 획득 후 안전 관리 소홀</span>
                      </li>
                      <li className="flex items-start group">
                        <div className="w-3 h-3 bg-gradient-to-r from-orange-400 to-red-500 rounded-full mt-2 mr-4 group-hover:scale-125 transition-transform"></div>
                        <span className="text-gray-700 font-medium">시야 확보 부족으로 갱킹 노출</span>
                      </li>
                    </ul>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        )}

        {/* Step 1: 결정적 순간 */}
        {currentStep === 1 && (
          <div className="space-y-8 animate-in fade-in-0 slide-in-from-right-4 duration-500">
            <Card className="border-0 shadow-2xl backdrop-blur-xl bg-white/80 overflow-hidden">
              <CardHeader className="bg-gradient-to-r from-red-50 to-pink-50 border-b border-gray-100">
                <CardTitle className="flex items-center text-2xl">
                  <div className="w-10 h-10 bg-gradient-to-r from-red-500 to-pink-500 rounded-xl flex items-center justify-center mr-4">
                    <Target className="w-6 h-6 text-white" />
                  </div>
                  결정적 순간 분석
                </CardTitle>
                <p className="text-gray-600 font-medium">{playerData.gamesAnalyzed}게임에서 발견된 핵심 실수 패턴</p>
              </CardHeader>
              <CardContent className="p-8">
                <div className="space-y-6">
                  {criticalMoments.map((moment: any, index: number) => (
                    <div key={index} className="relative group">
                      <div className="absolute left-0 top-0 bottom-0 w-1 bg-gradient-to-b from-red-400 to-pink-500 rounded-full"></div>
                      <div className="bg-gradient-to-r from-red-50 to-pink-50 border border-red-200 p-6 rounded-2xl ml-6 hover:shadow-lg transition-all duration-300 group-hover:scale-102">
                        <div className="flex items-start justify-between">
                          <div className="flex-1">
                            <div className="flex items-center space-x-4 mb-4">
                              <Badge className="bg-white border-red-200 text-red-700 font-semibold">
                                게임 {moment.game}
                              </Badge>
                              <Badge className="bg-gradient-to-r from-blue-500 to-purple-500 text-white">
                                {moment.champion}
                              </Badge>
                              <span className="text-sm font-mono bg-gray-100 px-3 py-1 rounded-full text-gray-600">
                                {moment.time}
                              </span>
                            </div>
                            <h4 className="font-bold text-gray-900 mb-2 text-lg">{moment.situation}</h4>
                            <p className="text-red-700 font-medium">실수: {moment.mistake}</p>
                          </div>
                          <div
                            className={`px-4 py-2 rounded-xl text-sm font-bold ${
                              moment.impact === "critical"
                                ? "bg-gradient-to-r from-red-500 to-pink-500 text-white"
                                : "bg-gradient-to-r from-orange-400 to-yellow-500 text-white"
                            }`}
                          >
                            {moment.impact === "critical" ? "치명적" : "중요"}
                          </div>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </div>
        )}

        {/* Step 2: 심리적 패턴 */}
        {currentStep === 2 && (
          <div className="space-y-8 animate-in fade-in-0 slide-in-from-left-4 duration-500">
            <Card className="border-0 shadow-2xl backdrop-blur-xl bg-white/80 overflow-hidden">
              <CardHeader className="bg-gradient-to-r from-purple-50 to-indigo-50 border-b border-gray-100">
                <CardTitle className="flex items-center text-2xl">
                  <div className="w-10 h-10 bg-gradient-to-r from-purple-500 to-indigo-500 rounded-xl flex items-center justify-center mr-4">
                    <Brain className="w-6 h-6 text-white" />
                  </div>
                  심리적 패턴 분석
                </CardTitle>
                <p className="text-gray-600 font-medium">플레이 스타일에서 발견된 심리적 경향</p>
              </CardHeader>
              <CardContent className="p-8">
                <div className="grid gap-6">
                  {psychologyInsights.map((insight: any, index: number) => (
                    <div
                      key={index}
                      className={`p-6 rounded-2xl border-2 ${insight.bgColor} hover:shadow-lg transition-all duration-300 group hover:scale-102`}
                    >
                      <div className="flex items-center justify-between mb-4">
                        <h4 className="font-bold text-xl text-gray-900">{insight.pattern}</h4>
                        <Badge className={`bg-gradient-to-r ${insight.color} text-white font-semibold px-4 py-2`}>
                          빈도: {insight.frequency}
                        </Badge>
                      </div>
                      <p className="text-gray-700 font-medium leading-relaxed">{insight.description}</p>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </div>
        )}

        {/* Step 3: 개선 액션 */}
        {currentStep === 3 && (
          <div className="space-y-8 animate-in fade-in-0 slide-in-from-bottom-4 duration-500">
            <Card className="border-0 shadow-2xl backdrop-blur-xl bg-white/80 overflow-hidden">
              <CardHeader className="bg-gradient-to-r from-yellow-50 to-orange-50 border-b border-gray-100">
                <CardTitle className="flex items-center text-2xl">
                  <div className="w-10 h-10 bg-gradient-to-r from-yellow-500 to-orange-500 rounded-xl flex items-center justify-center mr-4">
                    <Zap className="w-6 h-6 text-white" />
                  </div>
                  맞춤형 개선 액션 플랜
                </CardTitle>
                <p className="text-gray-600 font-medium">즉시 적용 가능한 구체적 실행 방안</p>
              </CardHeader>
              <CardContent className="p-8">
                <div className="space-y-8">
                  {actionPlans.map((plan: any, index: number) => (
                    <div
                      key={index}
                      className="border-2 border-gray-100 rounded-2xl p-6 hover:shadow-xl transition-all duration-300 group hover:scale-102 bg-gradient-to-r from-white to-gray-50"
                    >
                      <div className="flex items-start justify-between mb-6">
                        <div className="flex items-center">
                          <div
                            className={`w-12 h-12 bg-gradient-to-r ${plan.gradient} rounded-2xl flex items-center justify-center mr-4 group-hover:scale-110 transition-transform`}
                          >
                            <plan.icon className="w-7 h-7 text-white" />
                          </div>
                          <h4 className="font-bold text-xl text-gray-900">{plan.title}</h4>
                        </div>
                        <Badge
                          className={`${plan.priority === "높음" ? "bg-gradient-to-r from-red-500 to-pink-500" : "bg-gradient-to-r from-blue-500 to-purple-500"} text-white font-semibold px-4 py-2`}
                        >
                          {plan.priority} 우선순위
                        </Badge>
                      </div>

                      <p className="text-gray-700 mb-6 text-lg font-medium">{plan.description}</p>

                      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div className="bg-blue-50 border border-blue-200 rounded-xl p-4">
                          <h5 className="font-bold text-blue-900 mb-2 flex items-center">
                            <Clock className="w-4 h-4 mr-2" />📅 언제
                          </h5>
                          <p className="text-blue-700 font-medium">{plan.when}</p>
                        </div>
                        <div className="bg-green-50 border border-green-200 rounded-xl p-4">
                          <h5 className="font-bold text-green-900 mb-2 flex items-center">
                            <Zap className="w-4 h-4 mr-2" />🔧 어떻게
                          </h5>
                          <p className="text-green-700 font-medium">{plan.how}</p>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </div>
        )}

        {/* Step 4: 성장 로드맵 */}
        {currentStep === 4 && (
          <div className="space-y-8 animate-in fade-in-0 slide-in-from-top-4 duration-500">
            <Card className="border-0 shadow-2xl backdrop-blur-xl bg-white/80 overflow-hidden">
              <CardHeader className="bg-gradient-to-r from-green-50 to-emerald-50 border-b border-gray-100">
                <CardTitle className="flex items-center text-2xl">
                  <div className="w-10 h-10 bg-gradient-to-r from-green-500 to-emerald-500 rounded-xl flex items-center justify-center mr-4">
                    <TrendingUp className="w-6 h-6 text-white" />
                  </div>
                  성장 로드맵
                </CardTitle>
                <p className="text-gray-600 font-medium">단계별 목표와 달성 기준</p>
              </CardHeader>
              <CardContent className="p-8">
                <div className="space-y-6">
                  {roadmapGoals.map((goal: any, index: number) => (
                    <div
                      key={index}
                      className="flex items-center justify-between p-6 bg-gradient-to-r from-gray-50 to-white border-2 border-gray-100 rounded-2xl hover:shadow-lg transition-all duration-300 group hover:scale-102"
                    >
                      <div className="flex items-center">
                        <div
                          className={`w-12 h-12 bg-gradient-to-r ${goal.color} text-white rounded-2xl flex items-center justify-center text-lg font-bold mr-4 group-hover:scale-110 transition-transform`}
                        >
                          {index + 1}
                        </div>
                        <div
                          className={`w-10 h-10 bg-gradient-to-r ${goal.color} rounded-xl flex items-center justify-center mr-4`}
                        >
                          <goal.icon className="w-6 h-6 text-white" />
                        </div>
                        <div>
                          <h4 className="font-bold text-lg text-gray-900">{goal.category}</h4>
                          <p className="text-gray-600 font-medium">{goal.target}</p>
                        </div>
                      </div>
                      <Badge className="bg-gradient-to-r from-blue-500 to-purple-500 text-white font-semibold px-4 py-2 text-sm">
                        {goal.timeframe}
                      </Badge>
                    </div>
                  ))}
                </div>
                
                {/* 원본 AI 리포트 보기 버튼 */}
                <div className="mt-8 pt-6 border-t border-gray-200">
                  <Button
                    onClick={() => setShowOriginalReport(!showOriginalReport)}
                    variant="outline"
                    className="w-full bg-gradient-to-r from-gray-50 to-gray-100 hover:from-gray-100 hover:to-gray-200 border-2 border-gray-300 text-gray-700 font-semibold py-4 rounded-2xl transition-all duration-300 hover:scale-102"
                  >
                    <Brain className="w-5 h-5 mr-3" />
                    {showOriginalReport ? '원본 AI 리포트 숨기기' : '원본 AI 리포트 보기'}
                    <ArrowRight className={`w-5 h-5 ml-3 transition-transform duration-300 ${showOriginalReport ? 'rotate-90' : ''}`} />
                  </Button>
                  
                  {/* 원본 리포트 내용 */}
                  {showOriginalReport && (
                    <div className="mt-6 p-6 bg-white border-2 border-gray-200 rounded-2xl shadow-lg animate-in fade-in-0 slide-in-from-top-4 duration-500">
                      <div className="flex items-center mb-4">
                        <div className="w-8 h-8 bg-gradient-to-r from-indigo-500 to-purple-500 rounded-xl flex items-center justify-center mr-3">
                          <Brain className="w-5 h-5 text-white" />
                        </div>
                        <h3 className="text-xl font-bold text-gray-900">원본 AI 분석 리포트</h3>
                      </div>
                      <div className="prose prose-lg max-w-none">
                        {(() => {
                          // 원본 데이터 우선순위 체크
                          let originalContent = analysisData?.analysisFeedback;
                          
                          console.log("원본 리포트 데이터 확인:", {
                            analysisFeedback: analysisData?.analysisFeedback,
                            analysisSummary: analysisData?.analysisSummary,
                            analysisType: analysisType
                          });
                          
                          // 1. analysisFeedback이 있고 의미있는 내용인 경우
                          if (originalContent && originalContent !== '별도의 텍스트 피드백이 없습니다.') {
                            return <ReactMarkdown>{originalContent}</ReactMarkdown>;
                          }
                          
                          // 2. analysisSummary에서 데이터 추출 시도
                          if (analysisData?.analysisSummary) {
                            try {
                              const summaryData = JSON.parse(analysisData.analysisSummary);
                              console.log("analysisSummary 파싱 결과:", summaryData);
                              
                              // analysisFeedback 추출 시도
                              if (summaryData?.analysisFeedback && summaryData.analysisFeedback !== '별도의 텍스트 피드백이 없습니다.') {
                                originalContent = summaryData.analysisFeedback;
                                return <ReactMarkdown>{originalContent}</ReactMarkdown>;
                              }
                              
                              // 구조화된 데이터가 있는 경우 텍스트로 변환
                              const analysisResult = summaryData?.analysisResult;
                              if (analysisResult && analysisResult.playerOverview) {
                                originalContent = `# ${analysisResult.playerOverview?.name || '플레이어'} 분석 리포트

## 🎯 플레이어 개요
- **현재 티어**: ${analysisResult.playerOverview?.currentTier || 'N/A'}
- **승률**: ${analysisResult.playerOverview?.winRate || 'N/A'}
- **평균 KDA**: ${analysisResult.playerOverview?.kda || 'N/A'}
- **분석 게임수**: ${analysisResult.playerOverview?.gamesAnalyzed || 'N/A'}게임

## ⚡ 결정적 순간들
${analysisResult.criticalMoments?.map((moment: any, idx: number) => 
  `**${idx + 1}. 게임 ${moment.game} - ${moment.time}** (${moment.champion})
상황: ${moment.situation}
*실수*: ${moment.mistake}
*영향도*: ${moment.impact === 'critical' ? '치명적' : moment.impact === 'high' ? '높음' : '보통'}`
).join('\n\n') || '데이터 없음'}

## 🧠 심리적 패턴
${analysisResult.psychologyInsights?.map((insight: any, idx: number) => 
  `**${idx + 1}. ${insight.pattern}** (빈도: ${insight.frequency})
${insight.description}`
).join('\n\n') || '데이터 없음'}

## 🔧 개선 액션 플랜
${analysisResult.actionPlans?.map((plan: any, idx: number) => 
  `**${idx + 1}. ${plan.title}** (${plan.priority} 우선순위)
${plan.description}
- **언제**: ${plan.when}
- **방법**: ${plan.how}`
).join('\n\n') || '데이터 없음'}

## 🚀 성장 로드맵
${analysisResult.roadmapGoals?.map((goal: any, idx: number) => 
  `**${idx + 1}. ${goal.category}**
목표: ${goal.target} (기간: ${goal.timeframe})`
).join('\n\n') || '데이터 없음'}`;
                                return <ReactMarkdown>{originalContent}</ReactMarkdown>;
                              }
                              
                            } catch (error) {
                              console.error("analysisSummary JSON 파싱 실패:", error);
                            }
                          }
                          
                          // 3. analysisSummary 직접 파싱 시도 (다중 게임 분석용)
                          if (analysisData?.analysisSummary) {
                            try {
                              const parsed = JSON.parse(analysisData.analysisSummary);
                              if (parsed.playerOverview) {
                                const markdownContent = `# ${parsed.playerOverview?.name || '플레이어'} 분석 리포트

## 🎯 플레이어 개요
- **현재 티어**: ${parsed.playerOverview?.currentTier || 'N/A'}
- **승률**: ${parsed.playerOverview?.winRate || 'N/A'}
- **평균 KDA**: ${parsed.playerOverview?.kda || 'N/A'}
- **분석 게임수**: ${parsed.playerOverview?.gamesAnalyzed || 'N/A'}게임

## ⚡ 결정적 순간들
${parsed.criticalMoments?.map((moment: any, idx: number) => 
  `**${idx + 1}. 게임 ${moment.game} - ${moment.time}** (${moment.champion})
상황: ${moment.situation}
*실수*: ${moment.mistake}  
*영향도*: ${moment.impact === 'critical' ? '치명적' : moment.impact === 'high' ? '높음' : '보통'}`
).join('\n\n') || '데이터 없음'}

## 🧠 심리적 패턴
${parsed.psychologyInsights?.map((insight: any, idx: number) => 
  `**${idx + 1}. ${insight.pattern}** (빈도: ${insight.frequency})  
${insight.description}`
).join('\n\n') || '데이터 없음'}

## 🔧 개선 액션 플랜
${parsed.actionPlans?.map((plan: any, idx: number) => 
  `**${idx + 1}. ${plan.title}** (${plan.priority} 우선순위)  
${plan.description}  
- **언제**: ${plan.when}  
- **방법**: ${plan.how}`
).join('\n\n') || '데이터 없음'}

## 🚀 성장 로드맵
${parsed.roadmapGoals?.map((goal: any, idx: number) => 
  `**${idx + 1}. ${goal.category}**  
목표: ${goal.target} (기간: ${goal.timeframe})`
).join('\n\n') || '데이터 없음'}`;
                                return <ReactMarkdown>{markdownContent}</ReactMarkdown>;
                              }
                            } catch (parseError) {
                              console.log("analysisSummary 직접 파싱도 실패:", parseError);
                            }
                          }

                          return <p className="text-gray-500">원본 분석 결과가 없습니다.</p>;
                        })()}
                      </div>
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>
          </div>
        )}

        {/* 네비게이션 버튼 */}
        <div className="flex justify-between mt-12">
          <Button
            variant="outline"
            onClick={() => setCurrentStep(Math.max(0, currentStep - 1))}
            disabled={currentStep === 0}
            className="px-8 py-4 rounded-2xl font-semibold text-lg border-2 hover:scale-105 transition-all duration-300"
          >
            <ArrowLeft className="w-5 h-5 mr-2" />
            이전 단계
          </Button>
          <Button
            onClick={() => setCurrentStep(Math.min(4, currentStep + 1))}
            disabled={currentStep === 4}
            className="bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 px-8 py-4 rounded-2xl font-semibold text-lg hover:scale-105 transition-all duration-300"
          >
            다음 단계
            <ArrowRight className="w-5 h-5 ml-2" />
          </Button>
        </div>
      </div>
    </div>
  )
}