// app/routes/duo-comparison.tsx
import { useState } from 'react'

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
    const [step, setStep] = useState(1)
    const [player1, setPlayer1] = useState({ name: '', tag: '' })
    const [player2, setPlayer2] = useState({ name: '', tag: '' })
    const [commonMatches, setCommonMatches] = useState<string[]>([])
    const [selectedMatch, setSelectedMatch] = useState('')
    const [loading, setLoading] = useState(false)
    const [analysisResult, setAnalysisResult] = useState<AnalysisResult | null>(null)

    // 공통 매치 찾기
    const handleSearchPlayers = async () => {
        if (!player1.name || !player2.name) {
            alert('두 플레이어의 이름을 모두 입력해주세요.')
            return
        }

        setLoading(true)
        try {
            const response = await fetch(
                `http://localhost:8080/api/analysis/duo/common-matches?player1Name=${encodeURIComponent(player1.name)}&player1Tag=${player1.tag}&player2Name=${encodeURIComponent(player2.name)}&player2Tag=${player2.tag}`
            )

            if (!response.ok) {
                throw new Error('공통 매치 조회 실패')
            }

            const data = await response.json()
            setCommonMatches(data.commonMatches || [])
            if (data.commonMatches && data.commonMatches.length > 0) {
                setStep(2)
            } else {
                alert('공통 매치가 없습니다. 다른 플레이어를 시도해보세요.')
            }

        } catch (error) {
            console.error('Error:', error)
            const errorMessage = error instanceof Error ? error.message : '알 수 없는 오류가 발생했습니다'
            alert('플레이어 정보 조회에 실패했습니다: ' + errorMessage)
        } finally {
            setLoading(false)
        }
    }

    // 듀오 분석 수행
    const handleAnalyzeDuo = async () => {
        if (!selectedMatch) {
            alert('분석할 매치를 선택해주세요.')
            return
        }

        setLoading(true)
        try {
            const response = await fetch('http://localhost:8080/api/analysis/duo/analyze', {
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

            if (!response.ok) {
                throw new Error('듀오 분석 실패')
            }

            const data = await response.json()
            setAnalysisResult(data)
            setStep(3)

        } catch (error) {
            console.error('분석 실행 중 오류:', String(error))
            const errorMessage = error instanceof Error ? error.message : '분석 실행에 실패했습니다'
            alert('분석 실행에 실패했습니다: ' + errorMessage)
        } finally {
            setLoading(false)
        }
    }

    const resetAnalysis = () => {
        setStep(1)
        setPlayer1({ name: '', tag: '' })
        setPlayer2({ name: '', tag: '' })
        setCommonMatches([])
        setSelectedMatch('')
        setAnalysisResult(null)
    }

    return (
            <div className="max-w-4xl mx-auto p-6">
                {/* 헤더 */}
                <div className="mb-8">
                    <h1 className="text-3xl font-bold text-gray-900 mb-2">친구와 비교</h1>
                    <p className="text-gray-600">함께 플레이한 게임을 AI로 분석해보세요</p>
                </div>

                {/* 1단계: 플레이어 정보 입력 */}
                <div className="bg-white rounded-lg shadow-md p-6 mb-6">
                    <div className="flex items-center mb-4">
                        <div className="flex items-center justify-center w-8 h-8 bg-blue-600 text-white rounded-full mr-3">
                            1
                        </div>
                        <h2 className="text-xl font-semibold">플레이어 정보 입력</h2>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                플레이어 1
                            </label>
                            <div className="flex gap-2">
                                <input
                                    type="text"
                                    placeholder="플레이어명"
                                    value={player1.name}
                                    onChange={(e) => setPlayer1({...player1, name: e.target.value})}
                                    disabled={step > 1}
                                    className="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100"
                                />
                                <input
                                    type = "text"
                                    placeholder= "태그"
                                    value={player1.tag}
                                    onChange={(e) => setPlayer1({...player1, tag: e.target.value})}
                                    disabled={step > 1}
                                    className="w-20 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100"
                                />
                            </div>
                            <p className="text-xs text-gray-500 mt-1">
                            </p>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                플레이어 2
                            </label>
                            <div className="flex gap-2">
                                <input
                                    type="text"
                                    placeholder="플레이어명"
                                    value={player2.name}
                                    onChange={(e) => setPlayer2({...player2, name: e.target.value})}
                                    disabled={step > 1}
                                    className="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100"
                                />
                                <input
                                    type="text"
                                    placeholder="태그"
                                    value={player2.tag}
                                    onChange={(e) => setPlayer2({...player2, tag: e.target.value})}
                                    disabled={step > 1}
                                    className="w-20 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100"
                                />
                            </div>
                            <p className="text-xs text-gray-500 mt-1">
                            </p>
                        </div>
                    </div>

                    {step === 1 && (
                        <button
                            onClick={handleSearchPlayers}
                            disabled={loading || !player1.name || !player2.name}
                            className="bg-blue-600 text-white px-6 py-2 rounded-md hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
                        >
                            {loading ? '조회 중...' : '공통 매치 찾기'}
                        </button>
                    )}

                    {step > 1 && (
                        <div className="bg-green-50 border border-green-200 rounded-md p-4">
                            <div className="flex items-center">
                                <div className="text-green-600 mr-3">✅</div>
                                <div>
                                    <div className="font-medium text-green-800">플레이어 조회 완료</div>
                                    <div className="text-green-700">공통 매치 {commonMatches.length}개 발견</div>
                                </div>
                            </div>
                        </div>
                    )}
                </div>

                {/* 2단계: 매치 선택 */}
                {step >= 2 && (
                    <div className="bg-white rounded-lg shadow-md p-6 mb-6">
                        <div className="flex items-center mb-4">
                            <div className={`flex items-center justify-center w-8 h-8 rounded-full mr-3 ${step >= 2 ? 'bg-blue-600 text-white' : 'bg-gray-300 text-gray-600'}`}>
                                2
                            </div>
                            <h2 className="text-xl font-semibold">매치 선택</h2>
                        </div>

                        {step === 2 && (
                            <div className="space-y-4">
                                <select
                                    value={selectedMatch}
                                    onChange={(e) => setSelectedMatch(e.target.value)}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                >
                                    <option value="">분석할 매치를 선택하세요</option>
                                    {commonMatches.map((matchId, index) => (
                                        <option key={matchId} value={matchId}>
                                            매치 {index + 1}: {matchId}
                                        </option>
                                    ))}
                                </select>

                                <button
                                    onClick={handleAnalyzeDuo}
                                    disabled={!selectedMatch || loading}
                                    className="bg-green-600 text-white px-6 py-2 rounded-md hover:bg-green-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
                                >
                                    {loading ? '분석 중...' : '듀오 분석 시작'}
                                </button>
                            </div>
                        )}

                        {step > 2 && (
                            <div className="bg-green-50 border border-green-200 rounded-md p-4">
                                <div className="flex items-center">
                                    <div className="text-green-600 mr-3">✅</div>
                                    <div>
                                        <div className="font-medium text-green-800">매치 선택 완료</div>
                                        <div className="text-green-700">선택된 매치: {selectedMatch}</div>
                                    </div>
                                </div>
                            </div>
                        )}
                    </div>
                )}

                {/* 3단계: 분석 결과 */}
                {step >= 3 && analysisResult && (
                    <div className="bg-white rounded-lg shadow-md p-6">
                        <div className="flex items-center mb-4">
                            <div className="flex items-center justify-center w-8 h-8 bg-blue-600 text-white rounded-full mr-3">
                                3
                            </div>
                            <h2 className="text-xl font-semibold">분석 결과</h2>
                        </div>

                        <div className="space-y-6">
                            <div className="bg-gray-50 rounded-lg p-4">
                                <h3 className="font-semibold mb-2">매치 정보</h3>
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
                                    <div>
                                        <span className="font-medium">매치 ID:</span> {analysisResult.analysisRecord.matchId}
                                    </div>
                                    <div>
                                        <span className="font-medium">분석 상태:</span> {analysisResult.analysisRecord.status}
                                    </div>
                                    <div>
                                        <span className="font-medium">플레이어 1:</span> {analysisResult.analysisRecord.player1Name}
                                    </div>
                                    <div>
                                        <span className="font-medium">플레이어 2:</span> {analysisResult.analysisRecord.player2Name}
                                    </div>
                                    <div>
                                        <span className="font-medium">챔피언 1:</span> {analysisResult.analysisRecord.player1Champion}
                                    </div>
                                    <div>
                                        <span className="font-medium">챔피언 2:</span> {analysisResult.analysisRecord.player2Champion}
                                    </div>
                                    <div>
                                        <span className="font-medium">분석 시간:</span> {new Date(analysisResult.analysisRecord.createdAt).toLocaleString('ko-KR')}
                                    </div>
                                    <div>
                                        <span className="font-medium">업데이트:</span> {new Date(analysisResult.analysisRecord.updatedAt).toLocaleString('ko-KR')}
                                    </div>
                                </div>
                            </div>

                            <div>
                                <h3 className="font-semibold mb-2">AI 분석 결과</h3>
                                <div className="bg-gray-50 rounded-lg p-4 text-sm whitespace-pre-wrap">
                                    {analysisResult.analysisRecord.analysisSummary || '분석 결과가 없습니다.'}
                                </div>
                            </div>

                            {analysisResult.analysisRecord.comparisonResult && (
                                <div>
                                    <h3 className="font-semibold mb-2">비교 분석 상세</h3>
                                    <div className="bg-blue-50 rounded-lg p-4 text-sm">
                                        <pre className="whitespace-pre-wrap">
                                            {JSON.stringify(analysisResult.analysisRecord.comparisonResult, null, 2)}
                                        </pre>
                                    </div>
                                </div>
                            )}

                            <div className="flex gap-2 pt-4">
                                <button
                                    onClick={resetAnalysis}
                                    className="bg-blue-600 text-white px-6 py-2 rounded-md hover:bg-blue-700"
                                >
                                    새로운 듀오 분석 시작
                                </button>
                                <button
                                    onClick={() => setStep(2)}
                                    className="bg-gray-600 text-white px-6 py-2 rounded-md hover:bg-gray-700"
                                >
                                    다른 매치 분석
                                </button>
                            </div>
                        </div>
                    </div>
                )}
            </div>
    )
}