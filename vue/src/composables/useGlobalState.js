import { reactive } from 'vue'
import { useI18n } from 'vue-i18n'

/**
 * 전역 애플리케이션 상태를 관리하는 객체
 * loading: 로딩 중 여부
 * error: 사용자에게 표시할 에러 메시지
 * message: 사용자에게 표시할 일반 알림 메시지
 * successMessage: 성공 페이지에서 표시할 메시지
 * adminAccess: 관리자 페이지 접근 권한 상태 (null: 확인 전, true: 허용, false: 거부)
 */
const state = reactive({
  loading: false,
  error: '',
  message: '',
  successMessage: '',
  adminAccess: null,
  loginResult: null,
})

/**
 * 전역 상태 관리를 위한 Composable 함수
 */
export function useGlobalState() {
  const { t } = useI18n()

  /**
   * 발생한 에러를 안전하게 상태에 기록하는 함수
   * @param {Error} error - 발생한 에러 객체
   */
  const setSafeError = (error) => {
    // exposeToUser 플래그가 있는 경우만 메시지를 그대로 노출, 아니면 공통 실패 메시지 사용
    state.error = error?.exposeToUser ? error.message : t('common.requestFailed')
  }

  /**
   * 사용자 정의 에러 객체를 생성하는 함수
   * @param {string} message - 표시할 에러 메시지
   */
  const userError = (message) => {
    const error = new Error(message)
    error.exposeToUser = true
    return error
  }

  /**
   * 작업 성공 후 성공 메시지를 저장하고 성공 페이지로 리다이렉트하는 함수
   * @param {string} message - 성공 메시지
   * @param {function} pageHref - 경로 생성 헬퍼 함수
   */
  const goToSuccessPage = (message, pageHref) => {
    const successMessageStorageKey = 'zdo.successMessage'
    try {
      // 세션 스토리지를 통해 메시지 전달
      sessionStorage.setItem(successMessageStorageKey, message)
    } catch (_) {}
    
    // 성공 페이지로 이동 (pageHref가 함수가 아닐 경우를 대비한 안전 장치)
    if (typeof pageHref === 'function') {
      window.location.href = pageHref('/success')
    } else {
      window.location.href = '/success'
    }
  }

  return {
    state,
    setSafeError,
    userError,
    goToSuccessPage
  }
}
