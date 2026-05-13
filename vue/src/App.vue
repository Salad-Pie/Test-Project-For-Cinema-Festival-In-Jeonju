<script setup>
/**
 * App.vue - 애플리케이션의 최상위 엔트리 컴포넌트
 * 라우팅 처리, 전역 상태 관리 및 컨텍스트 주입을 담당합니다.
 */
import { onMounted, provide } from 'vue'
import { useI18n } from 'vue-i18n'
import { i18n } from './i18n'
import { createApiFetch, apiFetchWithBase } from './api/client'
import { apiBase, apiRoot, ideaAuthApiBase } from './config/api'
import { getIdeaContestAuthToken, isIdeaContestLoggedIn, saveIdeaContestAuthToken, saveIdeaContestLogin, getGlobalRedirectPath, clearGlobalRedirectPath } from './utils/authStorage'
import artistMeetingPosterUrl from './assets/artist-meeting-poster.png'

// --- 비즈니스 로직 분리를 위한 Composables 임포트 ---
import { useGlobalState } from './composables/useGlobalState'
import { useRouting } from './composables/useRouting'
import { useAuth } from './composables/useAuth'
import { useAddress } from './composables/useAddress'

// --- 공통 UI 구성 컴포넌트 임포트 ---
import LanguageSelector from './components/LanguageSelector.vue'
import GlobalNotifications from './components/GlobalNotifications.vue'

// --- 개별 화면(View) 컴포넌트 임포트 ---
import HomeView from './views/HomeView.vue'
import BootstrapHomeView from './views/BootstrapHomeView.vue'
import SuccessView from './views/SuccessView.vue'
import SponsorshipView from './views/SponsorshipView.vue'
import ArtistMeetingView from './views/ArtistMeetingView.vue'
import StreetCollaborationView from './views/StreetCollaborationView.vue'
import ExhibitionSurveyView from './views/ExhibitionSurveyView.vue'
import ExperienceZoneSurveyView from './views/ExperienceZoneSurveyView.vue'
import EndingCreditsView from './views/EndingCreditsView.vue'
import IdeaContestLoginView from './views/IdeaContestLoginView.vue'
import BootstrapLoginView from './views/BootstrapLoginView.vue'
import IdeaContestView from './views/IdeaContestView.vue'
import SponsorshipThanksView from './views/SponsorshipThanksView.vue'
import ExhibitionSurveySuccessView from './views/ExhibitionSurveySuccessView.vue'
import ExperienceZoneSurveySuccessView from './views/ExperienceZoneSurveySuccessView.vue'
import ProjectParticipantView from './views/ProjectParticipantView.vue'
import StreamingRecruitView from './views/StreamingRecruitView.vue'
import LocationView from './views/LocationView.vue'
import AxSpaceView from './views/AxSpaceView.vue'
import KartAxView from './views/KartAxView.vue'
import AxShopShopView from './views/AxShopShopView.vue'
import PdRecruitView from './views/PdRecruitView.vue'
import IdentifierCodeReissueView from './views/IdentifierCodeReissueView.vue'
import EmailLoginView from './views/EmailLoginView.vue'
import EmailSignupView from './views/EmailSignupView.vue'
import EndingCreditsChineseView from './views/EndingCreditsChineseView.vue'
import AdminDashboardView from './views/AdminDashboardView.vue'
import AdminAccessDeniedView from './views/AdminAccessDeniedView.vue'
import AdminReservationsView from './views/AdminReservationsView.vue'
import AdminSignaturesView from './views/AdminSignaturesView.vue'
import AdminStatisticsView from './views/AdminStatisticsView.vue'
import AdminOCRReviewView from './views/AdminOCRReviewView.vue'
import AdminUsersView from './views/AdminUsersView.vue'
import AdminConfigView from './views/AdminConfigView.vue'
import AdminExportView from './views/AdminExportView.vue'
import AdminI18nEditorView from './views/AdminI18nEditorView.vue'
import OauthCallbackView from './views/OauthCallbackView.vue'
import CertificateDownloadView from './views/CertificateDownloadView.vue'
import TabletView from './views/TabletView.vue'

// 1. i18n 및 커스텀 로직 모듈 초기화
const { t, locale } = useI18n()
const globalState = useGlobalState()
const routing = useRouting()
const auth = useAuth(routing, globalState)
const address = useAddress(globalState)

// 2. 초기화된 모듈에서 필요한 변수 및 함수 추출
const { state, setSafeError, userError, goToSuccessPage } = globalState
const { pageHref, isHome, isBootstrapHome, isEndingCreditsPage, isEndingCreditsChinesePage, isSuccessPage, isIdeaContestPage, isArtistMeetingPage, isAxSpacePage, isKartAxPage, isAxShopShopPage, isPdRecruitPage, isAdminDashboardPage, isAdminReservationsPage, isAdminSignaturesPage, isAdminStatisticsPage, isAdminOCRReviewPage, isAdminUsersPage, isAdminConfigPage, isAdminExportPage, isAdminI18nEditorPage, isOriginalRoute, isBootstrapRoute, isIdentifierCodeReissuePage, isEmailLoginPage, isBootstrapLoginPage, isIdeaContestLoginPage, isSponsorshipPage, isSponsorshipThanksPage, isStreetCollaborationPage, isExhibitionSurveyPage, isExhibitionSurveySuccessPage, isExperienceZoneSurveyPage, isExperienceZoneSurveySuccessPage, isProjectParticipantPage, isStreamingRecruitPage, isLocationPage, isEmailSignupPage, isOauthCallbackPage, isCertificateDownloadPage, isTabletPage } = routing
const { startOAuth, redirectToLogin, goEmailLoginPage } = auth
const { openAddressSearch } = address

// 3. API 요청을 위한 Fetcher 생성
const apiFetch = createApiFetch(apiBase, () => t('common.requestFailed'))

// 부트스트랩 모드에서 사용할 공개 페이지 목록
const bootstrapPages = [
  { key: 'nav.loginPage', href: '/login-page' },
  { key: 'auth.emailLogin', href: '/login/email' },
  { key: 'auth.emailSignup', href: '/signup/email' },
  { key: 'identifierReissue.title', href: '/identifier-code-reissue' },
  { key: 'tablet.verifyTitle', href: '/tablet' },
  { key: 'certificateDownload.title', href: '/certificate-download' },
  { href: '/ending-credits', label: 'Indonesia Familiarization Tour Ending Credit' },
  { href: '/ending-credits-cn', label: 'China Tourist Ending Credits' },
  { key: 'nav.ideaContest', href: '/idea-contest' },
  { key: 'nav.sponsorshipApplication', href: '/sponsorship-application' },
  { key: 'nav.streetCollaboration', href: '/street-collaboration' },
  { key: 'nav.exhibitionArtistMeeting', href: '/artist-meet' },
  { key: 'nav.exhibitionSurvey', href: '/exhibition-survey' },
  { key: 'nav.experienceZoneSurvey', href: '/experience-zone-survey' },
  { key: 'nav.projectParticipant', href: '/project-participant' },
  { key: 'project.axSpaceTitle', href: '/ax-space' },
  { key: 'project.kArtAxTitle', href: '/k-art-ax' },
  { key: 'nav.streamingRecruit', href: '/streaming-recruit' },
  { key: 'project.axShopShopTitle', href: '/ax-shop-shop' },
  { key: 'project.pdRecruitTitle', href: '/pd-recruit' },
  { key: 'nav.location', href: '/location' },
]
const ideaPosterUrl = 'https://zdo.co.kr/theme/home/html/image/top_logo_m.png'

/**
 * 4. 애플리케이션 마운트 시 초기화 로직
 * - 언어 설정 로드
 * - 페이지별 접근 권한 체크
 * - 관리자 세션 유효성 검사
 */
/**
 * 백엔드로부터 동적 다국어 데이터를 로드하여 반영 (대표님 보고용 실시간 UI 관리 기능)
 */
async function loadDynamicI18n(localeCode) {
  try {
    const res = await fetch(`${apiRoot}/i18n/messages/${localeCode}`)
    if (res.ok) {
      const messages = await res.json()
      if (Object.keys(messages).length > 0) {
        // 기존 로컬 메시지와 병합
        const currentMessages = i18n.global.getLocaleMessage(localeCode)
        i18n.global.setLocaleMessage(localeCode, { ...currentMessages, ...messages })
      }
    }
  } catch (e) {
    console.warn('Failed to load dynamic i18n:', e)
  }
}

onMounted(async () => {
  const localeStorageKey = 'zdo.locale'
  const successMessageStorageKey = 'zdo.successMessage'

  // 1. 동적 다국어 데이터 로드
  await loadDynamicI18n(locale.value)
  
  // 브라우저 저장소에서 언어 설정 복원
  try {
    const savedLocale = localStorage.getItem(localeStorageKey)
    if (savedLocale && ['ko', 'en', 'zh', 'ja'].includes(savedLocale)) {
      locale.value = savedLocale
    }
  } catch (_) {}

  // 성공 페이지 메시지 로드
  if (isSuccessPage.value) {
    try {
      state.successMessage = sessionStorage.getItem(successMessageStorageKey) || ''
    } catch (_) {
      state.successMessage = ''
    }
  }

  // 로컬 호스트 테스트용 리다이렉트 (IP 기반 접근 유도)
  if (window.location.hostname === 'localhost') {
    const canonicalUrl = new URL(window.location.href)
    canonicalUrl.hostname = '127.0.0.1'
    window.location.replace(canonicalUrl.toString())
    return
  }

  // --- OAuth 콜백 처리 ---
  if (isOauthCallbackPage.value) {
    const params = new URLSearchParams(window.location.search)
    const code = params.get('code')
    const stateVal = params.get('state')
    
    if (code && stateVal) {
      try {
        state.loading = true
        // 직접 로컬 스토리지 확인하여 베이스 URL 결정 (함수 호출 에러 방지)
        const savedRedirect = localStorage.getItem('zdoPostLoginRedirectPath') || ''
        const authBase = savedRedirect === '/idea-contest' ? ideaAuthApiBase : apiBase
        
        console.log('Starting OAuth exchange...', { authBase, code, state: stateVal })
        
        const res = await apiFetchWithBase(authBase, '/oauth/exchange', {
          method: 'POST',
          body: JSON.stringify({ code, state: stateVal })
        })
        
        console.log('OAuth exchange success:', res)
        
        if (res && res.registerToken) {
          saveIdeaContestAuthToken(res.registerToken)
          if (res.userId) {
            saveIdeaContestLogin(res.userId)
          }
          // 리다이렉트 경로 복구
          const redirectPath = getGlobalRedirectPath() || '/'
          clearGlobalRedirectPath()
          window.location.href = redirectPath
          return
        } else {
          throw new Error('Invalid response: missing registerToken')
        }
      } catch (e) {
        console.error('OAuth Exchange Error:', e)
        setSafeError(e)
      } finally {
        state.loading = false
      }
    }
  }

  // --- 접근 제어 (Access Control) 로직 ---
  
  // 로그인이 필요한 일반 페이지 목록
  const loginProtectedPages = [
    isIdentifierCodeReissuePage, isIdeaContestPage, isArtistMeetingPage, 
    isAxSpacePage, isKartAxPage, isAxShopShopPage, isPdRecruitPage
  ]
  
  // 로그인 여부 확인 및 리다이렉트
  if (loginProtectedPages.some(p => p.value) && !isIdeaContestLoggedIn()) {
    redirectToLogin(window.location.pathname)
    return
  }

  // 관리자 전용 페이지 목록
  const adminPages = [
    isAdminDashboardPage, isAdminReservationsPage, isAdminSignaturesPage,
    isAdminStatisticsPage, isAdminOCRReviewPage, isAdminUsersPage,
    isAdminConfigPage, isAdminExportPage, isAdminI18nEditorPage
  ]

  // 관리자 페이지 진입 시 권한 확인
  if (adminPages.some(p => p.value)) {
    if (!isIdeaContestLoggedIn()) {
      redirectToLogin(window.location.pathname)
      return
    }
    try {
      const authToken = getIdeaContestAuthToken()
      // 실제 어드민 API 호출을 통해 토큰 유효성 및 권한 확인
      const res = await fetch(`${apiRoot}/admin/reservations/types`, {
        headers: { Authorization: `Bearer ${authToken}` },
      })
      if (!res.ok) throw new Error()
      state.adminAccess = true
    } catch (e) {
      state.adminAccess = false
    }
  }
})

/**
 * 5. 하위 컴포넌트에 전역 컨텍스트 제공 (Provide)
 * 이를 통해 분리된 View 컴포넌트들이 부모의 상태와 함수를 Inject하여 사용할 수 있습니다.
 */
provide('appContext', {
  t,
  locale,
  state,
  setSafeError,
  userError,
  goToSuccessPage,
  apiFetch,
  startOAuth,
  goEmailLoginPage,
  pageHref,
  openAddressSearch,
  artistMeetingPosterUrl,
  ideaPosterUrl,
  bootstrapPages,
  isEndingCreditsPage,
})
</script>

<template>
  <main :class="['container', { 'bootstrap-mode': isBootstrapRoute, 'ending-credits-layout': isEndingCreditsPage || isEndingCreditsChinesePage }]">
    <!-- 상단 언어 선택기 -->
    <LanguageSelector />

    <!-- 경로에 따른 화면 렌더링 영역 -->
    <HomeView v-if="isHome" />
    <BootstrapHomeView v-if="isBootstrapHome" />
    <EndingCreditsView v-if="isEndingCreditsPage" />
    <EndingCreditsChineseView v-if="isEndingCreditsChinesePage" />
    <SuccessView v-if="isSuccessPage" />
    <ArtistMeetingView v-if="isArtistMeetingPage" />
    <IdeaContestLoginView v-if="isIdeaContestLoginPage" />
    <BootstrapLoginView v-if="isBootstrapLoginPage" />
    <StreetCollaborationView v-if="isStreetCollaborationPage" />
    <SponsorshipView v-if="isSponsorshipPage" />
    <IdeaContestView v-if="isIdeaContestPage" />
    <SponsorshipThanksView v-if="isSponsorshipThanksPage" />
    <ExhibitionSurveyView v-if="isExhibitionSurveyPage" />
    <ExhibitionSurveySuccessView v-if="isExhibitionSurveySuccessPage" />
    <ExperienceZoneSurveyView v-if="isExperienceZoneSurveyPage" />
    <ExperienceZoneSurveySuccessView v-if="isExperienceZoneSurveySuccessPage" />
    <ProjectParticipantView v-if="isProjectParticipantPage" />
    <StreamingRecruitView v-if="isStreamingRecruitPage" />
    <LocationView v-if="isLocationPage" />
    <AxSpaceView v-if="isAxSpacePage" />
    <KartAxView v-if="isKartAxPage" />
    <AxShopShopView v-if="isAxShopShopPage" />
    <PdRecruitView v-if="isPdRecruitPage" />
    <IdentifierCodeReissueView v-if="isIdentifierCodeReissuePage" />
    <EmailLoginView v-if="isEmailLoginPage" />
    
    <!-- 관리자 관련 화면 -->
    <AdminDashboardView v-if="isAdminDashboardPage && state.adminAccess === true" />
    <AdminAccessDeniedView v-if="(isAdminDashboardPage || isAdminReservationsPage || isAdminSignaturesPage || isAdminStatisticsPage || isAdminOCRReviewPage || isAdminUsersPage || isAdminConfigPage || isAdminExportPage || isAdminI18nEditorPage) && state.adminAccess === false" />
    <AdminReservationsView v-if="isAdminReservationsPage && state.adminAccess === true" />
    <AdminSignaturesView v-if="isAdminSignaturesPage && state.adminAccess === true" />
    <AdminStatisticsView v-if="isAdminStatisticsPage && state.adminAccess === true" />
    <AdminOCRReviewView v-if="isAdminOCRReviewPage && state.adminAccess === true" />
    <AdminUsersView v-if="isAdminUsersPage && state.adminAccess === true" />
    <AdminConfigView v-if="isAdminConfigPage && state.adminAccess === true" />
    <AdminExportView v-if="isAdminExportPage && state.adminAccess === true" />
    <AdminI18nEditorView v-if="isAdminI18nEditorPage && state.adminAccess === true" />

    <EmailSignupView v-if="isEmailSignupPage" />
    <OauthCallbackView v-if="isOauthCallbackPage" />
    <CertificateDownloadView v-if="isCertificateDownloadPage" />
    <TabletView v-if="isTabletPage" />

    <!-- 전역 알림 표시 -->
    <GlobalNotifications />
  </main>
</template>
