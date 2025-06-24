import { useState, useEffect } from "react"
import { Button } from "./ui/button"
import { Input } from "./ui/input"
import { Card, CardContent, CardHeader } from "./ui/card"
import { Badge } from "./ui/badge"
import { CheckCircle, XCircle, Key } from "lucide-react"

export function AuthStatus() {
  const [token, setToken] = useState<string>("")
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false)

  useEffect(() => {
    const storedToken = localStorage.getItem('auth_token')
    setIsAuthenticated(!!storedToken)
    if (storedToken) {
      setToken(storedToken)
    }
  }, [])

  const handleSetToken = () => {
    if (token.trim()) {
      localStorage.setItem('auth_token', token.trim())
      setIsAuthenticated(true)
      alert('토큰이 설정되었습니다!')
    } else {
      alert('토큰을 입력해주세요.')
    }
  }

  const handleClearToken = () => {
    localStorage.removeItem('auth_token')
    setToken("")
    setIsAuthenticated(false)
    alert('토큰이 제거되었습니다.')
  }

  return (
    <Card className="bg-slate-800 border-slate-700 mb-6">
      <CardHeader>
        <div className="flex items-center gap-2">
          <Key className="w-5 h-5 text-slate-300" />
          <h3 className="text-lg font-semibold text-white">인증 상태</h3>
          <Badge 
            className={isAuthenticated ? "bg-green-600" : "bg-red-600"}
          >
            {isAuthenticated ? "인증됨" : "미인증"}
          </Badge>
        </div>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          <div className="flex items-center gap-2">
            {isAuthenticated ? (
              <CheckCircle className="w-5 h-5 text-green-500" />
            ) : (
              <XCircle className="w-5 h-5 text-red-500" />
            )}
            <span className="text-slate-300">
              {isAuthenticated ? "관리자 권한으로 공지사항을 작성/수정/삭제할 수 있습니다." : "관리자 권한이 필요합니다."}
            </span>
          </div>
          
          <div className="space-y-2">
            <Input
              type="password"
              placeholder="JWT 토큰을 입력하세요"
              value={token}
              onChange={(e) => setToken(e.target.value)}
              className="bg-slate-700 border-slate-600 text-white"
            />
            <div className="flex gap-2">
              <Button 
                onClick={handleSetToken}
                className="bg-blue-600 hover:bg-blue-700"
              >
                토큰 설정
              </Button>
              {isAuthenticated && (
                <Button 
                  onClick={handleClearToken}
                  variant="outline"
                  className="border-red-600 text-red-400 hover:bg-red-900/20"
                >
                  토큰 제거
                </Button>
              )}
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  )
} 