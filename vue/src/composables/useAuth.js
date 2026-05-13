import { computed } from 'vue'
import { apiBase, ideaAuthApiBase } from '../config/api'
import { apiFetchWithBase } from '../api/client'
import { getGlobalRedirectPath, setGlobalRedirectPath } from '../utils/authStorage'

/**
 * 인증(Authentication) 관련 로직을 관리하는 Composable 함수
 * @param {object} routing - useRouting에서 반환된 객체
 * @param {object} globalState - useGlobalState에서 반환된 객체
 */
export function useAuth(routing, globalState) {
  const { isIdeaContestLoginPage, isBootstrapLoginPage, isEmailLoginPage, isEmailSignupPage, isOriginalRoute, pageHref } = routing
  const { state, setSafeError } = globalState

  /**
   * 아이디어 공모전 전용 인증 API를 사용해야 하는지 판단하는 함수
   */
  const shouldUseIdeaContestAuthApi = () => {
    // 특정 로그인/가입 페이지이거나 리다이렉트 경로가 아이디어 공모인 경우 true 반환
    if (isIdeaContestLoginPage.value || isBootstrapLoginPage.value || isEmailLoginPage.value || isEmailSignupPage.value) {
      return true
    }
    const redirectPath = getGlobalRedirectPath()
    return redirectPath === '/idea-contest'
  }

  /**
   * OAuth 로그인을 시작하는 함수 (카카오, 구글 등)
   * @param {string} provider - 'kakao' 또는 'google'
   */
  const startOAuth = async (provider) => {
    state.loading = true
    state.error = ''
    try {
      const q = new URLSearchParams(window.location.search)
      const loginRedirectPath = q.get('redirect') || ''
      
      // 현재 리다이렉트 정보를 로컬 스토리지에 저장
      if (loginRedirectPath) {
        setGlobalRedirectPath(loginRedirectPath)
      }
      const redirectPath = getGlobalRedirectPath()
      if (redirectPath) {
        setGlobalRedirectPath(redirectPath)
      }
      
      // 적절한 인증 베이스 URL 결정 및 권한 부여 URL 요청
      const authBase = shouldUseIdeaContestAuthApi() ? ideaAuthApiBase : apiBase
      const redirectQuery = redirectPath ? `?redirect=${encodeURIComponent(redirectPath)}` : ''
      const result = await apiFetchWithBase(authBase, `/oauth/authorize-url/${provider}${redirectQuery}`)
      
      // 외부 인증 페이지로 이동
      window.location.href = result.authorizationUrl
    } catch (e) {
      setSafeError(e)
      state.loading = false
    }
  }

  /**
   * 로그인이 필요한 페이지 접근 시 로그인 페이지로 리다이렉트하는 함수
   * @param {string} redirectPath - 로그인 완료 후 돌아올 경로
   */
  const redirectToLogin = (redirectPath) => {
    setGlobalRedirectPath(redirectPath)
    const loginPath = isOriginalRoute ? '/original/login-page' : '/login-page'
    window.location.href = `${loginPath}?redirect=${encodeURIComponent(redirectPath)}`
  }

  /**
   * 이메일 로그인 페이지로 이동하는 함수
   */
  const goEmailLoginPage = () => {
    const redirectPath = getGlobalRedirectPath()
    const loginPath = pageHref('/login/email')
    const href = redirectPath ? `${loginPath}?redirect=${encodeURIComponent(redirectPath)}` : loginPath
    window.location.href = href
  }

  return {
    startOAuth,
    redirectToLogin,
    goEmailLoginPage,
    shouldUseIdeaContestAuthApi
  }
}
