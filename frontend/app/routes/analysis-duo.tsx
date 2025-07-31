import { useState } from "react"
import ReactMarkdown from "react-markdown"
import { Button } from "~/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "~/components/ui/card"
import { Badge } from "~/components/ui/badge"
import { ArrowLeft, Trophy, Target, Brain, TrendingUp, CheckCircle, Clock, Star, ArrowRight, BarChart3, Sparkles, Crown, Users, Heart, Plus, Minus } from "lucide-react"

// 듀오 분석 전용 컴포넌트
export default function DuoAnalysisReportPage({ data, analysisData, onBack }: { data: any; analysisData: any; onBack: () => void }) {
  const [currentStep, setCurrentStep] = useState(0)
  const [showOriginalReport, setShowOriginalReport] = useState(false)

  const duoData = {
    matchInfo: data.matchInfo,
    player1: data.playerComparison.player1,
    player2: data.playerComparison.player2,
    synergy: data.synergyAnalysis,
    improvements: data.improvementPoints,
    mvp: data.mvp
  }

  const steps = [
    { id: 0, title: "게임 결과", icon: Trophy, color: "from-green-500 to-emerald-500" },
    { id: 1, title: "플레이어 비교", icon: BarChart3, color: "from-blue-500 to-cyan-500" },
    { id: 2, title: "시너지 분석", icon: Heart, color: "from-pink-500 to-rose-500" },
    { id: 3, title: "개선 방안", icon: TrendingUp, color: "from-yellow-500 to-orange-500" },
  ]

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50 relative overflow-hidden">
      {/* 배경 요소들 */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute -top-40 -right-40 w-80 h-80 bg-gradient-to-br from-blue-400/20 to-purple-600/20 rounded-full blur-3xl animate-pulse" />
        <div className="absolute -bottom-40 -left-40 w-80 h-80 bg-gradient-to-tr from-cyan-400/20 to-blue-600/20 rounded-full blur-3xl animate-pulse delay-1000" />
      </div>

      {/* 헤더 */}
      <div className="bg-gradient-to-r from-blue-600 via-purple-600 to-indigo-700 text-white relative overflow-hidden">
        <div className="absolute inset-0 bg-black/10" />
        <div className="max-w-6xl mx-auto p-8 relative z-10">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-6">
              <div className="relative">
                <div className="w-20 h-20 bg-white/20 rounded-2xl backdrop-blur-sm flex items-center justify-center">
                  <Users className="w-10 h-10 text-cyan-300" />
                </div>
                <div className="absolute -top-2 -right-2 w-6 h-6 bg-gradient-to-r from-cyan-400 to-blue-500 rounded-full flex items-center justify-center">
                  <Sparkles className="w-4 h-4 text-white" />
                </div>
              </div>
              <div>
                <h1 className="text-4xl font-black mb-2 bg-gradient-to-r from-white to-blue-100 bg-clip-text text-transparent">
                  🤝 {duoData.player1.name} & {duoData.player2.name}
                </h1>
                <h2 className="text-2xl font-bold mb-2">듀오 게임 분석 리포트</h2>
                <p className="text-blue-100 text-lg">
                  게임 결과: {duoData.matchInfo.gameResult} • 시간: {duoData.matchInfo.gameDuration}
                </p>
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
              <step.icon className="w-5 h-5 mr-3 relative z-10" />
              <span className="font-semibold relative z-10">{step.title}</span>
            </button>
          ))}
        </div>

        {/* Step 0: 게임 결과 */}
        {currentStep === 0 && (
          <div className="space-y-8 animate-in fade-in-0 slide-in-from-bottom-4 duration-500">
            <Card className="border-0 shadow-2xl backdrop-blur-xl bg-white/80 overflow-hidden">
              <CardHeader className="bg-gradient-to-r from-green-50 to-emerald-50 border-b border-gray-100">
                <CardTitle className="flex items-center text-2xl">
                  <div className="w-10 h-10 bg-gradient-to-r from-green-500 to-emerald-500 rounded-xl flex items-center justify-center mr-4">
                    <Trophy className="w-6 h-6 text-white" />
                  </div>
                  게임 결과
                </CardTitle>
              </CardHeader>
              <CardContent className="p-8">
                <div className="text-center mb-8">
                  <div className={`w-32 h-32 ${duoData.matchInfo.gameResult === '승리' ? 'bg-gradient-to-r from-green-500 to-emerald-500' : 'bg-gradient-to-r from-red-500 to-rose-500'} rounded-full flex items-center justify-center mx-auto mb-6`}>
                    <Trophy className="w-16 h-16 text-white" />
                  </div>
                  <h3 className="text-4xl font-bold text-gray-900 mb-2">{duoData.matchInfo.gameResult}</h3>
                  <p className="text-xl text-gray-600">게임 시간: {duoData.matchInfo.gameDuration}</p>
                </div>
                
                <div className="bg-gradient-to-r from-yellow-50 to-orange-50 border-2 border-yellow-200 rounded-2xl p-6">
                  <div className="flex items-center mb-4">
                    <Crown className="w-8 h-8 text-yellow-600 mr-3" />
                    <h4 className="text-xl font-bold text-yellow-900">경기 MVP</h4>
                  </div>
                  <div className="mb-4">
                    <h5 className="text-lg font-bold text-gray-900">{duoData.mvp.playerName}</h5>
                  </div>
                  <p className="text-gray-700">{duoData.mvp.reason}</p>
                </div>
              </CardContent>
            </Card>
          </div>
        )}

        {/* Step 1: 플레이어 비교 */}
        {currentStep === 1 && (
          <div className="space-y-8 animate-in fade-in-0 slide-in-from-right-4 duration-500">
            <div className="grid md:grid-cols-2 gap-8">
              {/* Player 1 */}
              <Card className="border-0 shadow-2xl backdrop-blur-xl bg-white/80 overflow-hidden">
                <CardHeader className="bg-gradient-to-r from-blue-50 to-cyan-50 border-b border-gray-100">
                  <CardTitle className="flex items-center justify-between">
                    <div className="flex items-center">
                      <div className="w-10 h-10 bg-gradient-to-r from-blue-500 to-cyan-500 rounded-xl flex items-center justify-center mr-4">
                        <span className="text-white font-bold">1</span>
                      </div>
                      <div>
                        <h3 className="text-xl font-bold">{duoData.player1.name}</h3>
                        <Badge variant="outline" className="bg-blue-50 mt-1">
                          {duoData.player1.champion}
                        </Badge>
                      </div>
                    </div>
                  </CardTitle>
                </CardHeader>
                <CardContent className="p-6">
                  <div className="space-y-4">
                    <div className="flex items-center justify-between">
                      <span className="font-semibold">KDA</span>
                      <span className="font-bold text-blue-600">{duoData.player1.kda}</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="font-semibold">데미지</span>
                      <span className="font-bold text-blue-600">{duoData.player1.damage.toLocaleString()}</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="font-semibold">골드</span>
                      <span className="font-bold text-blue-600">{duoData.player1.gold.toLocaleString()}</span>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {/* Player 2 */}
              <Card className="border-0 shadow-2xl backdrop-blur-xl bg-white/80 overflow-hidden">
                <CardHeader className="bg-gradient-to-r from-purple-50 to-indigo-50 border-b border-gray-100">
                  <CardTitle className="flex items-center justify-between">
                    <div className="flex items-center">
                      <div className="w-10 h-10 bg-gradient-to-r from-purple-500 to-indigo-500 rounded-xl flex items-center justify-center mr-4">
                        <span className="text-white font-bold">2</span>
                      </div>
                      <div>
                        <h3 className="text-xl font-bold">{duoData.player2.name}</h3>
                        <Badge variant="outline" className="bg-purple-50 mt-1">
                          {duoData.player2.champion}
                        </Badge>
                      </div>
                    </div>
                  </CardTitle>
                </CardHeader>
                <CardContent className="p-6">
                  <div className="space-y-4">
                    <div className="flex items-center justify-between">
                      <span className="font-semibold">KDA</span>
                      <span className="font-bold text-purple-600">{duoData.player2.kda}</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="font-semibold">데미지</span>
                      <span className="font-bold text-purple-600">{duoData.player2.damage.toLocaleString()}</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="font-semibold">골드</span>
                      <span className="font-bold text-purple-600">{duoData.player2.gold.toLocaleString()}</span>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>
          </div>
        )}

        {/* Step 2: 시너지 분석 */}
        {currentStep === 2 && (
          <div className="space-y-8 animate-in fade-in-0 slide-in-from-left-4 duration-500">
            <Card className="border-0 shadow-2xl backdrop-blur-xl bg-white/80 overflow-hidden">
              <CardHeader className="bg-gradient-to-r from-pink-50 to-rose-50 border-b border-gray-100">
                <CardTitle className="flex items-center text-2xl">
                  <div className="w-10 h-10 bg-gradient-to-r from-pink-500 to-rose-500 rounded-xl flex items-center justify-center mr-4">
                    <Heart className="w-6 h-6 text-white" />
                  </div>
                  듀오 시너지 분석
                </CardTitle>
              </CardHeader>
              <CardContent className="p-8">
                <div className="grid md:grid-cols-2 gap-8">
                  <div className="space-y-6">
                    <h4 className="font-bold text-xl text-green-700 flex items-center">
                      <div className="w-8 h-8 bg-green-100 rounded-xl flex items-center justify-center mr-3">
                        <CheckCircle className="w-5 h-5 text-green-600" />
                      </div>
                      시너지 강점
                    </h4>
                    <ul className="space-y-4">
                      {duoData.synergy.strengths.map((strength: string, index: number) => (
                        <li key={index} className="flex items-start group">
                          <div className="w-3 h-3 bg-gradient-to-r from-green-400 to-emerald-500 rounded-full mt-2 mr-4 group-hover:scale-125 transition-transform"></div>
                          <span className="text-gray-700 font-medium">{strength}</span>
                        </li>
                      ))}
                    </ul>
                  </div>
                  <div className="space-y-6">
                    <h4 className="font-bold text-xl text-orange-700 flex items-center">
                      <div className="w-8 h-8 bg-orange-100 rounded-xl flex items-center justify-center mr-3">
                        <TrendingUp className="w-5 h-5 text-orange-600" />
                      </div>
                      개선 포인트
                    </h4>
                    <ul className="space-y-4">
                      {duoData.synergy.weaknesses.map((weakness: string, index: number) => (
                        <li key={index} className="flex items-start group">
                          <div className="w-3 h-3 bg-gradient-to-r from-orange-400 to-red-500 rounded-full mt-2 mr-4 group-hover:scale-125 transition-transform"></div>
                          <span className="text-gray-700 font-medium">{weakness}</span>
                        </li>
                      ))}
                    </ul>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        )}

        {/* Step 3: 개선 방안 */}
        {currentStep === 3 && (
          <div className="space-y-8 animate-in fade-in-0 slide-in-from-bottom-4 duration-500">
            <div className="grid md:grid-cols-2 gap-8">
              {/* Player 1 개선 방안 */}
              <Card className="border-0 shadow-2xl backdrop-blur-xl bg-white/80 overflow-hidden">
                <CardHeader className="bg-gradient-to-r from-blue-50 to-cyan-50 border-b border-gray-100">
                  <CardTitle className="flex items-center">
                    <div className="w-10 h-10 bg-gradient-to-r from-blue-500 to-cyan-500 rounded-xl flex items-center justify-center mr-4">
                      <span className="text-white font-bold">1</span>
                    </div>
                    {duoData.player1.name} 개선방안
                  </CardTitle>
                </CardHeader>
                <CardContent className="p-6">
                  <ul className="space-y-3">
                    {duoData.improvements.forPlayer1.map((improvement: string, index: number) => (
                      <li key={index} className="flex items-start">
                        <ArrowRight className="w-4 h-4 text-blue-500 mr-2 mt-1 flex-shrink-0" />
                        <span className="text-sm text-gray-700">{improvement}</span>
                      </li>
                    ))}
                  </ul>
                </CardContent>
              </Card>

              {/* Player 2 개선 방안 */}
              <Card className="border-0 shadow-2xl backdrop-blur-xl bg-white/80 overflow-hidden">
                <CardHeader className="bg-gradient-to-r from-purple-50 to-indigo-50 border-b border-gray-100">
                  <CardTitle className="flex items-center">
                    <div className="w-10 h-10 bg-gradient-to-r from-purple-500 to-indigo-500 rounded-xl flex items-center justify-center mr-4">
                      <span className="text-white font-bold">2</span>
                    </div>
                    {duoData.player2.name} 개선방안
                  </CardTitle>
                </CardHeader>
                <CardContent className="p-6">
                  <ul className="space-y-3">
                    {duoData.improvements.forPlayer2.map((improvement: string, index: number) => (
                      <li key={index} className="flex items-start">
                        <ArrowRight className="w-4 h-4 text-purple-500 mr-2 mt-1 flex-shrink-0" />
                        <span className="text-sm text-gray-700">{improvement}</span>
                      </li>
                    ))}
                  </ul>
                </CardContent>
              </Card>
            </div>

            {/* 듀오 종합 개선 방안 */}
            <Card className="border-0 shadow-2xl backdrop-blur-xl bg-white/80 overflow-hidden">
              <CardHeader className="bg-gradient-to-r from-yellow-50 to-orange-50 border-b border-gray-100">
                <CardTitle className="flex items-center text-2xl">
                  <div className="w-10 h-10 bg-gradient-to-r from-yellow-500 to-orange-500 rounded-xl flex items-center justify-center mr-4">
                    <Users className="w-6 h-6 text-white" />
                  </div>
                  듀오 종합 개선 방안
                </CardTitle>
              </CardHeader>
              <CardContent className="p-8">
                <ul className="space-y-4">
                  {duoData.improvements.forDuo.map((improvement: string, index: number) => (
                    <li key={index} className="flex items-start">
                      <Heart className="w-5 h-5 text-yellow-600 mr-3 mt-1 flex-shrink-0" />
                      <span className="text-gray-700 font-medium">{improvement}</span>
                    </li>
                  ))}
                </ul>

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
                          console.log("듀오 원본 리포트 데이터:", {
                            analysisFeedback: analysisData?.analysisFeedback,
                            analysisSummary: analysisData?.analysisSummary,
                            fullAnalysisData: analysisData
                          });

                          // 1. analysisFeedback이 있는 경우
                          if (analysisData?.analysisFeedback && analysisData.analysisFeedback !== '별도의 텍스트 피드백이 없습니다.') {
                            return <ReactMarkdown>{analysisData.analysisFeedback}</ReactMarkdown>;
                          }

                          // 2. analysisSummary에서 analysisFeedback 추출 시도
                          if (analysisData?.analysisSummary) {
                            try {
                              const summaryData = JSON.parse(analysisData.analysisSummary);
                              if (summaryData?.analysisFeedback && summaryData.analysisFeedback !== '별도의 텍스트 피드백이 없습니다.') {
                                return <ReactMarkdown>{summaryData.analysisFeedback}</ReactMarkdown>;
                              }
                            } catch (error) {
                              console.log("analysisSummary 파싱 실패:", error);
                            }
                          }

                          // 3. 듀오 분석의 경우 aiResponseData에서 analysisFeedback 찾기
                          if (analysisData?.aiResponseData?.analysisFeedback) {
                            return <ReactMarkdown>{analysisData.aiResponseData.analysisFeedback}</ReactMarkdown>;
                          }

                          // 4. 전체 데이터에서 analysisFeedback 찾기
                          if (analysisData?.aiResponseData && typeof analysisData.aiResponseData === 'object') {
                            const findFeedback = (obj: any): string | null => {
                              if (typeof obj === 'object' && obj !== null) {
                                if (obj.analysisFeedback && obj.analysisFeedback !== '별도의 텍스트 피드백이 없습니다.') {
                                  return obj.analysisFeedback;
                                }
                                for (const key in obj) {
                                  const result = findFeedback(obj[key]);
                                  if (result) return result;
                                }
                              }
                              return null;
                            };
                            
                            const feedback = findFeedback(analysisData.aiResponseData);
                            if (feedback) {
                              return <ReactMarkdown>{feedback}</ReactMarkdown>;
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
            onClick={() => setCurrentStep(Math.min(3, currentStep + 1))}
            disabled={currentStep === 3}
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