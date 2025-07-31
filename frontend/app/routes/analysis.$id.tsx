import { useParams, useNavigate } from "@remix-run/react"
import { useEffect, useState } from "react"
import { useApi } from "~/utils/api"
import ReactMarkdown from "react-markdown"
import { Button } from "~/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "~/components/ui/card"
import { Badge } from "~/components/ui/badge"
import { ArrowLeft, Loader2, AlertTriangle, User, Trophy, Target, Brain, TrendingUp, CheckCircle, Eye, Shield, Zap, Clock, Star, ArrowRight, PlayCircle, BarChart3, Map, Sparkles, Crown, Flame } from "lucide-react"

export default function AnalysisDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const apiFetch = useApi()
  
  const [analysisData, setAnalysisData] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string>("")
  const [currentStep, setCurrentStep] = useState(0)
  const [parsedData, setParsedData] = useState<any>(null)

  useEffect(() => {
    if (!id) return

    const loadAnalysisData = async () => {
      try {
        setLoading(true)
        setError("")
        
        // ID 형태에 따라 다른 API 호출
        let response
        if (id.startsWith('single-')) {
          const analysisId = id.replace('single-', '')
          response = await apiFetch(`/api/analysis/single/${analysisId}`)
        } else if (id.startsWith('multiple-')) {
          const analysisId = id.replace('multiple-', '')
          response = await apiFetch(`/api/analysis/multiple/${analysisId}`)
        } else if (id.startsWith('duo-')) {
          const analysisId = id.replace('duo-', '')
          response = await apiFetch(`/api/analysis/duo/${analysisId}`)
        } else {
          // 기본적으로 단일 분석으로 시도
          response = await apiFetch(`/api/analysis/single/${id}`)
        }
        
        setAnalysisData(response)

        // JSON 파싱 시도
        const aiResult = response.aiResponseData?.analysisResult
        const summary = response.analysisSummary
        const textToParse = (typeof aiResult === 'string' ? aiResult : summary) || ''

        try {
          const parsed = JSON.parse(textToParse)
          if (parsed.playerOverview && parsed.criticalMoments && 
              parsed.psychologyInsights && parsed.actionPlans && 
              parsed.roadmapGoals) {
            setParsedData(parsed)
          }
        } catch (parseError) {
          console.log('JSON 파싱 실패:', parseError)
        }
        
      } catch (err) {
        console.error('분석 데이터 로드 실패:', err)
        setError('분석 데이터를 불러올 수 없습니다.')
      } finally {
        setLoading(false)
      }
    }

    loadAnalysisData()
  }, [id, apiFetch])

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
              onClick={() => navigate('/')} 
              className="bg-blue-600 hover:bg-blue-700"
            >
              <ArrowLeft className="w-4 h-4 mr-2" />
              홈으로 돌아가기
            </Button>
          </CardContent>
        </Card>
      </div>
    )
  }

  // JSON 파싱된 데이터가 있으면 AICoachingReport 스타일로 렌더링
  if (parsedData) {
    return <AICoachingReportPage data={parsedData} onBack={() => navigate(-1)} />
  }

  // JSON 파싱 실패시 기존 마크다운 렌더링
  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50">
      <div className="container mx-auto px-4 py-6 max-w-6xl">
        <Button
          onClick={() => navigate(-1)}
          variant="outline"
          className="mb-6 bg-white/80 backdrop-blur-sm hover:bg-white/90"
        >
          <ArrowLeft className="w-4 h-4 mr-2" />
          이전 페이지로
        </Button>

        <Card className="border-0 shadow-2xl backdrop-blur-xl bg-white/80">
          <CardHeader>
            <CardTitle className="text-2xl">분석 결과</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="prose prose-lg max-w-none">
              <ReactMarkdown>
                {analysisData?.analysisSummary || '분석 결과를 표시할 수 없습니다.'}
              </ReactMarkdown>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

// AICoachingReport 스타일의 페이지 컴포넌트
function AICoachingReportPage({ data, onBack }: { data: any; onBack: () => void }) {
  const [currentStep, setCurrentStep] = useState(0)

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
          이전 페이지로
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

        {/* Step 0: 플레이어 개요 */}
        {currentStep === 0 && (
          <div className="space-y-8 animate-in fade-in-0 slide-in-from-bottom-4 duration-500">
            <Card className="border-0 shadow-2xl backdrop-blur-xl bg-white/80 overflow-hidden">
              <CardContent className="p-8 relative z-10">
                <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
                  <div className="text-center group">
                    <div className="w-16 h-16 bg-white/20 rounded-2xl backdrop-blur-sm flex items-center justify-center mx-auto mb-4 group-hover:scale-110 transition-transform">
                      <Trophy className="w-8 h-8" />
                    </div>
                    <div className="text-3xl font-black mb-1">{playerData.currentTier}</div>
                    <div className="text-gray-600 font-medium">현재 티어</div>
                  </div>
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