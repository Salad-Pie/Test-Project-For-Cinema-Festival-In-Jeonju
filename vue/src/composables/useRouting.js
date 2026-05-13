import { computed, ref, onMounted, onUnmounted } from 'vue'

/**
 * 라우팅 및 경로 관련 로직을 관리하는 Composable 함수
 */
export function useRouting() {
  // 현재 URL 경로 정보를 반응형으로 관리
  const currentPath = ref(window.location.pathname.toLowerCase())
  
  // 'original' 접두사 경로 여부 판단
  const isOriginalRoute = computed(() => currentPath.value === '/original' || currentPath.value.startsWith('/original/'))
  
  // 레거시 부트스트랩 경로 여부 판단
  const isLegacyBootstrapRoute = computed(() => currentPath.value === '/boot-strap' || currentPath.value.startsWith('/boot-strap/'))
  
  // 접두사를 제외한 순수 경로 추출
  const routePath = computed(() => {
    return isOriginalRoute.value
      ? currentPath.value.replace('/original', '') || '/'
      : isLegacyBootstrapRoute.value
        ? currentPath.value.replace('/boot-strap', '') || '/'
        : currentPath.value
  })
      
  // 부트스트랩 모드 여부
  const isBootstrapRoute = computed(() => !isOriginalRoute.value)

  /**
   * 브라우저 히스토리를 유지하며 페이지를 이동하는 함수 (대표님 보고용 고도화)
   */
  const navigate = (pathname) => {
    const fullPath = isOriginalRoute.value && !pathname.startsWith('/original')
      ? `/original${pathname}`
      : pathname
    
    window.history.pushState({}, '', fullPath)
    currentPath.value = fullPath.toLowerCase()
  }

  /**
   * 접두사를 포함한 전체 경로를 생성하는 함수 (기존 호환성 유지)
   */
  const pageHref = (pathname) => {
    return isOriginalRoute.value ? `/original${pathname}` : pathname
  }

  // 브라우저 뒤로가기/앞으로가기 이벤트 처리
  const handlePopState = () => {
    currentPath.value = window.location.pathname.toLowerCase()
  }

  onMounted(() => {
    window.addEventListener('popstate', handlePopState)
  })

  onUnmounted(() => {
    window.removeEventListener('popstate', handlePopState)
  })

  // --- 각 페이지별 활성화 여부를 판단하는 Computed 속성들 ---
  const isHome = computed(() => isOriginalRoute.value && (routePath.value === '/' || routePath.value === '/index.html'))
  const isBootstrapHome = computed(() => !isOriginalRoute.value && (routePath.value === '/' || routePath.value === '/index.html'))
  
  const isEmailLoginPage = computed(() => routePath.value.startsWith('/login/email'))
  const isEmailSignupPage = computed(() => routePath.value.startsWith('/signup/email'))
  const isOauthCallbackPage = computed(() => routePath.value.startsWith('/oauth/callback'))
  
  const isTabletPage = computed(() => routePath.value.startsWith('/tablet'))
  const isCertificateDownloadPage = computed(() => routePath.value.startsWith('/certificate-download'))
  const isEndingCreditsPage = computed(() => routePath.value.startsWith('/ending-credits') && !routePath.value.startsWith('/ending-credits-cn'))
  const isEndingCreditsChinesePage = computed(() => routePath.value.startsWith('/ending-credits-cn'))
  const isIdentifierCodeReissuePage = computed(() => routePath.value.startsWith('/identifier-code-reissue'))
  const isSuccessPage = computed(() => routePath.value === '/success')
  
  const isBootstrapLoginPage = computed(() => !isOriginalRoute.value && routePath.value.startsWith('/login-page'))
  const isIdeaContestLoginPage = computed(() => isOriginalRoute.value && routePath.value.startsWith('/login-page'))
  const isIdeaContestPage = computed(() => routePath.value.startsWith('/idea-contest') && !isIdeaContestLoginPage.value)
  
  const isSponsorshipPage = computed(() => routePath.value.startsWith('/sponsorship-application'))
  const isSponsorshipThanksPage = computed(() => routePath.value.startsWith('/sponsorship-thanks'))
  const isStreetCollaborationPage = computed(() => routePath.value.startsWith('/street-collaboration'))
  const isArtistMeetingPage = computed(() => routePath.value.startsWith('/artist-meet'))
  const isExhibitionSurveyPage = computed(() => routePath.value === '/exhibition-survey')
  const isExhibitionSurveySuccessPage = computed(() => routePath.value === '/exhibition-survey-success')
  const isExperienceZoneSurveyPage = computed(() => routePath.value === '/experience-zone-survey')
  const isExperienceZoneSurveySuccessPage = computed(() => routePath.value === '/experience-zone-survey-success')
  const isProjectParticipantPage = computed(() => routePath.value.startsWith('/project-participant'))
  const isAxSpacePage = computed(() => routePath.value.startsWith('/ax-space'))
  const isKartAxPage = computed(() => routePath.value.startsWith('/k-art-ax'))
  const isStreamingRecruitPage = computed(() => routePath.value.startsWith('/streaming-recruit'))
  const isAxShopShopPage = computed(() => routePath.value.startsWith('/ax-shop-shop'))
  const isPdRecruitPage = computed(() => routePath.value.startsWith('/pd-recruit'))
  const isLocationPage = computed(() => routePath.value.startsWith('/location'))
  
  const isAdminDashboardPage = computed(() => routePath.value === '/admin' || routePath.value === '/admin/')
  const isAdminReservationsPage = computed(() => routePath.value.startsWith('/admin/reservations'))
  const isAdminSignaturesPage = computed(() => routePath.value.startsWith('/admin/signatures'))
  const isAdminStatisticsPage = computed(() => routePath.value.startsWith('/admin/statistics'))
  const isAdminOCRReviewPage = computed(() => routePath.value.startsWith('/admin/ocr-review'))
  const isAdminUsersPage = computed(() => routePath.value.startsWith('/admin/users'))
  const isAdminConfigPage = computed(() => routePath.value.startsWith('/admin/config'))
  const isAdminExportPage = computed(() => routePath.value.startsWith('/admin/export'))
  const isAdminI18nEditorPage = computed(() => routePath.value.startsWith('/admin/i18n-editor'))
  const isAdminDataPage = computed(() => routePath.value.startsWith('/admin/data'))

  return {
    isOriginalRoute,
    isBootstrapRoute,
    routePath,
    pageHref,
    navigate,
    isHome,
    isBootstrapHome,
    isEmailLoginPage,
    isEmailSignupPage,
    isOauthCallbackPage,
    isTabletPage,
    isCertificateDownloadPage,
    isEndingCreditsPage,
    isEndingCreditsChinesePage,
    isIdentifierCodeReissuePage,
    isSuccessPage,
    isBootstrapLoginPage,
    isIdeaContestLoginPage,
    isIdeaContestPage,
    isSponsorshipPage,
    isSponsorshipThanksPage,
    isStreetCollaborationPage,
    isArtistMeetingPage,
    isExhibitionSurveyPage,
    isExhibitionSurveySuccessPage,
    isExperienceZoneSurveyPage,
    isExperienceZoneSurveySuccessPage,
    isProjectParticipantPage,
    isAxSpacePage,
    isKartAxPage,
    isStreamingRecruitPage,
    isAxShopShopPage,
    isPdRecruitPage,
    isLocationPage,
    isAdminDashboardPage,
    isAdminReservationsPage,
    isAdminSignaturesPage,
    isAdminStatisticsPage,
    isAdminOCRReviewPage,
    isAdminUsersPage,
    isAdminConfigPage,
    isAdminExportPage,
    isAdminI18nEditorPage,
    isAdminDataPage,
  }
}
