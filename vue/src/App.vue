<script setup>
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { apiFetchWithBase, createApiFetch, parseErrorResponse } from './api/client'
import { apiBase, apiRoot, ideaAuthApiBase } from './config/api'
import {
  clearGlobalRedirectPath,
  getGlobalRedirectPath,
  getIdeaContestAuthToken,
  isIdeaContestLoggedIn,
  saveIdeaContestAuthToken,
  saveIdeaContestLogin,
  setGlobalRedirectPath,
} from './utils/authStorage'
import { combineAddress, formatPhoneInput } from './utils/format'
import { parseJwtPayload } from './utils/jwt'
import artistMeetingPosterUrl from './assets/artist-meeting-poster.png'

const { t, locale } = useI18n()
const path = window.location.pathname.toLowerCase()
const isOriginalRoute = path === '/original' || path.startsWith('/original/')
const isLegacyBootstrapRoute = path === '/boot-strap' || path.startsWith('/boot-strap/')
const routePath = isOriginalRoute
  ? path.replace('/original', '') || '/'
  : isLegacyBootstrapRoute
    ? path.replace('/boot-strap', '') || '/'
    : path
const isBootstrapRoute = !isOriginalRoute
const localeDisplayNames = { ko: '\uD55C\uAD6D\uC5B4', en: 'English', zh: '\u4E2D\u6587', ja: '\u65E5\u672C\u8A9E' }

const isHome = computed(() => isOriginalRoute && (routePath === '/' || routePath === '/index.html'))
const isBootstrapHome = computed(() => !isOriginalRoute && (routePath === '/' || routePath === '/index.html'))
const isEmailLoginPage = computed(() => routePath.startsWith('/login/email'))
const isEmailSignupPage = computed(() => routePath.startsWith('/signup/email'))
const isOauthCallbackPage = computed(() => routePath.startsWith('/oauth/callback'))
const isTabletPage = computed(() => routePath.startsWith('/tablet'))
const isCertificateDownloadPage = computed(() => routePath.startsWith('/certificate-download'))
const isEndingCreditsPage = computed(() => routePath.startsWith('/ending-credits'))
const isIdentifierCodeReissuePage = computed(() => routePath.startsWith('/identifier-code-reissue'))
const isSuccessPage = computed(() => routePath === '/success')
const isBootstrapLoginPage = computed(() => !isOriginalRoute && routePath.startsWith('/login-page'))
const isIdeaContestLoginPage = computed(() => isOriginalRoute && routePath.startsWith('/login-page'))
const isIdeaContestPage = computed(() => routePath.startsWith('/idea-contest') && !isIdeaContestLoginPage.value)
const isSponsorshipPage = computed(() => routePath.startsWith('/sponsorship-application'))
const isSponsorshipThanksPage = computed(() => routePath.startsWith('/sponsorship-thanks'))
const isStreetCollaborationPage = computed(() => routePath.startsWith('/street-collaboration'))
const isArtistMeetingPage = computed(() => routePath.startsWith('/artist-meet'))
const isExhibitionSurveyPage = computed(() => routePath === '/exhibition-survey')
const isExhibitionSurveySuccessPage = computed(() => routePath === '/exhibition-survey-success')
const isExperienceZoneSurveyPage = computed(() => routePath === '/experience-zone-survey')
const isExperienceZoneSurveySuccessPage = computed(() => routePath === '/experience-zone-survey-success')
const isProjectParticipantPage = computed(() => routePath.startsWith('/project-participant'))
const isAxSpacePage = computed(() => routePath.startsWith('/ax-space'))
const isKartAxPage = computed(() => routePath.startsWith('/k-art-ax'))
const isStreamingRecruitPage = computed(() => routePath.startsWith('/streaming-recruit'))
const isAxShopShopPage = computed(() => routePath.startsWith('/ax-shop-shop'))
const isPdRecruitPage = computed(() => routePath.startsWith('/pd-recruit'))
const isLocationPage = computed(() => routePath.startsWith('/location'))
const isAdminDashboardPage = computed(() => routePath === '/admin' || routePath === '/admin/')
const isAdminReservationsPage = computed(() => routePath.startsWith('/admin/reservations'))
const isAdminSignaturesPage = computed(() => routePath.startsWith('/admin/signatures'))
const publicRoutePages = [
  { key: 'nav.loginPage', href: '/login-page' },
  { key: 'auth.emailLogin', href: '/login/email' },
  { key: 'auth.emailSignup', href: '/signup/email' },
  { key: 'identifierReissue.title', href: '/identifier-code-reissue' },
  { key: 'tablet.verifyTitle', href: '/tablet' },
  { key: 'certificateDownload.title', href: '/certificate-download' },
  { href: '/ending-credits', label: 'Indonesia Familiarization Tour Ending Credit' },
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
const bootstrapPages = publicRoutePages
const streetHourOptions = [17, 18, 19, 20, 21, 22]
const artistMeetingDateOptions = ['2026-05-02', '2026-05-04']
const artistMeetingHourMap = {
  '2026-05-02': [17, 19],
  '2026-05-04': [19, 21],
}
const axSpaceDate = '2026-05-08'
const axSpaceHour = '20'
const kArtAxDate = '2026-05-09'
const kArtAxHour = '16'
const axShopShopDate = '2026-05-09'
const axShopShopHour = '14'
const pdRecruitDate = '2026-05-09'
const pdRecruitHour = '19'
const locationAddress = '??????????諛몃마嶺뚮?????????????硫λ젒?????????곕츥???????????ш끽維뽳쭩?뱀땡???얩맪??????????????????????????????諛몃마嶺뚮?????????????硫λ젒?????????????諛몃마嶺뚮?????????????硫λ젒?????????????諛몃마嶺뚮?????????????硫λ젒????????⑥レ뿥??????????????????????????67'
const kakaoDirectionsUrl = `https://map.kakao.com/link/search/${encodeURIComponent(locationAddress)}`
const naverDirectionsUrl = `https://map.naver.com/p/search/${encodeURIComponent(locationAddress)}`
const ideaPosterUrl = 'https://zdo.co.kr/theme/home/html/image/top_logo_m.png'
const customPaymentProviderValue = '__CUSTOM__'
const endingCreditsLoadButtonLabelKo = '\uC5D4\uB529 \uD06C\uB808\uB51F \uBD88\uB7EC\uC624\uAE30'
const endingCreditsViewButtonLabelKo = '\uC5D4\uB529 \uD06C\uB808\uB51F \uBCF4\uAE30'
const endingCreditsLeadPreset1 = `\uC81C\uBAA9
AX \uC735\uBCF5\uD569 \uD55C \uCF54\uB4DC \uD504\uB85C\uC81D\uD2B8
\uC81C\uC791 
\uC778\uC0AC\uC774\uD2B8\uD22C\uC5B4 
\uD6C4\uC6D0
\uC804\uC8FC\uBB38\uD654\uACF5\uD310\uC7A5\uC7AC\uB2E8
\uAE30\uD68D  \uC6B4\uC601
\uC0D0\uB7EC\uB4DC\uD30C\uC774 \uC8FC\uC2DD\uD68C\uC0AC 
\uC2A4\uD0ED
\uBC15\uB3C4\uACB8
\uD1B5\uC5ED
\uAC15\uBD09\uC8FC
\uCD9C\uC5F0`
const endingCreditsTailPreset1 = `\u201C\uC774\uC81C \uC6B0\uB9AC\uB294 \uC190\uB2D8\uACFC \uC8FC\uCD5C\uC790\uAC00 \uC544\uB2C8\uB77C\u2026 \uCE5C\uAD6C\uAC00 \uB418\uC5C8\uC2B5\uB2C8\uB2E4!\u201D
\u201CSekarang kita bukan hanya tamu dan tuan rumah\u2026
kita sudah menjadi teman!\u201D

\uC624\uB298 \uC804\uC8FC\uC5D0\uC11C \uD568\uAED8\uD55C \uC2DC\uAC04\uC774
\uC5EC\uB7EC\uBD84\uC5D0\uAC8C \uB530\uB73B\uD55C \uAE30\uC5B5\uC73C\uB85C
\uB0A8\uAE30\uB97C \uBC14\uB78D\uB2C8\uB2E4.
Kami berharap waktu yang kita
habiskan bersama di Jeonju hari
ini
menjadi kenangan hangat bagi
Anda semua.

\uC5EC\uB7EC\uBD84\uACFC \uD568\uAED8 \uD55C\uAD6D\uC758 \uBB38\uD654\uC640
\uB9C8\uC74C\uC744 \uB098\uB20C \uC218 \uC788\uC5B4 \uC815\uB9D0 \uD589
\uBCF5\uD588\uC2B5\uB2C8\uB2E4.
Kami sangat bahagia dapat
berbagi budaya dan hati Korea
bersama Anda.

\uB2E4\uC74C\uC5D0\uB294 \uC5EC\uD589\uC790\uAC00 \uC544\uB2C8\uB77C
\uCE5C\uAD6C\uB85C\uB9CC\uB098\uAE30\uB97C\uAE30\uB300\uD558\uACA0\uC2B5\uB2C8\uB2E4
Semoga di lain waktu kita bisa
bertemu kembali,
bukan hanya sebagai tamu, tetapi
sebagai teman.

\uAC10\uC0AC\uD569\uB2C8\uB2E4.
Terima kasih banyak.`
const sponsorshipPaymentProviderOptionsByType = {
  BANK: ['KB', 'SHINHAN', 'WOORI', 'HANA', 'NH', 'IBK', 'KAKAO BANK', 'TOSS BANK', 'K BANK'],
  PAYMENT: ['KAKAO PAY', 'NAVER PAY', 'TOSS PAY', 'PAYCO', 'PayPal'],
  CARD: ['SHINHAN CARD', 'SAMSUNG CARD', 'KB CARD', 'HYUNDAI CARD', 'LOTTE CARD', 'WOORI CARD', 'HANA CARD', 'BC CARD', 'Visa', 'Mastercard'],
}

const q = new URLSearchParams(window.location.search)
const callbackCode = q.get('code') || ''
const callbackState = q.get('state') || ''
const loginRedirectPath = q.get('redirect') || ''
const localeStorageKey = 'zdo.locale'
const successMessageStorageKey = 'zdo.successMessage'

const state = reactive({
  loading: false,
  error: '',
  message: '',
  emailLogin: {
    email: '',
    code: '',
  },
  loginResult: null,
  ideaContest: {
    files: [],
  },
  sponsorship: {
    name: '',
    phoneNumber: '',
    bankAccount: '',
    paymentMethodType: 'BANK',
    paymentProviderPreset: '',
    paymentProviderName: '',
    amount: '',
    address: '',
    addressDetail: '',
  },
  street: {
    reservationDate: '',
    reservationHour: '17',
    name: '',
    phoneNumber: '',
    availability: null,
    checkingAvailability: false,
  },
  artistMeeting: {
    date: '2026-05-02',
    hour: '17',
  },
  exhibitionSurvey: {
    name: '',
    phoneNumber: '',
    address: '',
    addressDetail: '',
    identifierCode: '',
    impressivePoint: '',
    improvementNeeded: '',
    desiredGenre: '',
    invitedArtist: '',
    feedback: '',
  },
  experienceSurvey: {
    name: '',
    phoneNumber: '',
    address: '',
    addressDetail: '',
    impressiveSpace: '',
    improvementIdeaSpace: '',
    streamingParticipation: '',
    desiredGoods: '',
    feedback: '',
  },
  axSpace: { phoneNumber: '', date: axSpaceDate, hour: axSpaceHour },
  kArtAx: { phoneNumber: '', date: kArtAxDate, hour: kArtAxHour },
  axShopShop: { phoneNumber: '', date: axShopShopDate, hour: axShopShopHour },
  pdRecruit: { phoneNumber: '', date: pdRecruitDate, hour: pdRecruitHour },
  adminReservations: {
    types: [],
    items: [],
    selectedType: '',
    date: '',
    time: '',
    projectKey: '',
  },
  adminSignatures: {
    items: [],
  },
  mapError: '',
  successMessage: '',
  verifyCode: '',
  verifiedToken: '',
  certificateDownloadCode: '',
  endingCredits: {
    code: '',
    entries: [],
    isFullscreen: false,
    leadMessage: '',
    tailMessage: '',
    rollDurationSeconds: 18,
    rollGapPx: 72,
    fontScalePercent: 100,
    stopAfterOneCycle: false,
    includeIdentifierEntries: true,
    highlightedCodes: {},
  },
  identifierReissue: {
    message: '',
  },
  signatureNameLanguage: 'EN',
  signatureKoreanName: '',
  signaturePreview: {
    token: '',
    recognizedText: '',
    englishName: '',
    koreanName: '',
    koreanMeaningText: '',
    detectedLanguage: '',
    ocrConfidence: null,
  },
  adminAccess: null,
})

const tabletToken = computed(() => new URLSearchParams(window.location.search).get('token') || '')
const canvasRef = ref(null)
const endingCreditsShellRef = ref(null)
const endingCreditsHighlightTimer = ref(null)
const drawing = ref(false)
const sponsorshipPaymentProviderOptions = computed(
  () => sponsorshipPaymentProviderOptionsByType[state.sponsorship.paymentMethodType] || []
)
const endingCreditsRouteLabel = computed(() => {
  return 'Indonesia Familiarization Tour Ending Credit'
})
const endingCreditsText = computed(() => {
  if (locale.value === 'ko') {
    return {
      title: 'Indonesia Familiarization Tour Ending Credit',
      description: '\uC2DD\uBCC4\uC790 \uCF54\uB4DC\uB97C \uC785\uB825\uD558\uBA74 \uCC38\uC5EC\uC790\uC758 \uC601\uC5B4 \uC774\uB984, \uD55C\uAE00 \uC774\uB984, \uC11C\uC608 \uC11C\uBA85 \uC774\uBBF8\uC9C0\uB97C \uC5D4\uB529 \uD06C\uB808\uB51F\uC5D0 \uD45C\uC2DC\uD569\uB2C8\uB2E4.',
      inputLabel: '\u0036\uC790\uB9AC \uC2DD\uBCC4\uC790 \uCF54\uB4DC',
      inputPlaceholder: '\uC22B\uC790 \u0036\uC790\uB9AC\uB97C \uC785\uB825\uD558\uC138\uC694.',
      button: endingCreditsLoadButtonLabelKo,
      englishName: '\uC601\uC5B4 \uC774\uB984',
      koreanName: '\uD55C\uAE00 \uC774\uB984',
      signature: '\uC11C\uC608 \uC11C\uBA85',
      resultTitle: 'Ending Credit Cast',
      stopAfterOneCycle: '\u0031\uD68C \uC7AC\uC0DD \uD6C4 \uBA48\uCD94\uAE30',
      includeIdentifierEntries: '\uC2DD\uBCC4\uC790 \uB9AC\uC2A4\uD2B8 \uBC18\uC601',
    }
  }
  return {
    title: 'Indonesia Familiarization Tour Ending Credit',
    description: 'Enter the identifier code to display the participant English name, Korean name, and calligraphy signature image in the ending credits.',
    inputLabel: '6-digit identifier code',
    inputPlaceholder: 'Enter 6 digits',
    button: 'Load Ending Credits',
    englishName: 'English Name',
    koreanName: 'Korean Name',
    signature: 'Calligraphy Signature',
    resultTitle: 'Ending Credit Cast',
    stopAfterOneCycle: 'Stop after one cycle',
    includeIdentifierEntries: 'Include identifier entries',
  }
})
const endingCreditsMessageGroups = computed(() => [
  ...(state.endingCredits.leadMessage.trim()
    ? [{
        id: 'lead',
        title: 'Opening Message',
        lines: state.endingCredits.leadMessage
          .split('\n')
          .map((line) => line.trim())
          .filter(Boolean),
      }]
    : []),
])
const endingCreditsClosingGroups = computed(() => [
  ...(state.endingCredits.tailMessage.trim()
    ? [{
        id: 'tail',
        title: 'Closing Message',
        lines: state.endingCredits.tailMessage
          .split('\n')
          .map((line) => line.trim())
          .filter(Boolean),
      }]
    : []),
])
const endingCreditsRollEntries = computed(() =>
  state.endingCredits.includeIdentifierEntries
    ? state.endingCredits.entries.filter((entry) => entry.hasKoreanName || entry.hasSignature || entry.hasEnglishName)
    : []
)
const isCustomSponsorshipPaymentProvider = computed(
  () => state.sponsorship.paymentProviderPreset === customPaymentProviderValue
)
const apiFetch = createApiFetch(apiBase, () => t('common.requestFailed'))
const emailLoginHref = computed(() => {
  const redirectPath = getGlobalRedirectPath()
  const loginPath = pageHref('/login/email')
  return redirectPath ? `${loginPath}?redirect=${encodeURIComponent(redirectPath)}` : loginPath
})
const emailSignupHref = computed(() => {
  const redirectPath = getGlobalRedirectPath()
  const signupPath = pageHref('/signup/email')
  return redirectPath ? `${signupPath}?redirect=${encodeURIComponent(redirectPath)}` : signupPath
})
const currentRedirectPath = computed(() => window.location.pathname)

function pageHref(pathname) {
  return isOriginalRoute ? `/original${pathname}` : pathname
}

function syncEndingCreditsFullscreenState() {
  state.endingCredits.isFullscreen = !!document.fullscreenElement
}

function clearEndingCreditsHighlightInterval() {
  if (endingCreditsHighlightTimer.value) {
    clearInterval(endingCreditsHighlightTimer.value)
    endingCreditsHighlightTimer.value = null
  }
}

function markEndingCreditsHighlight(code) {
  if (!code || state.endingCredits.highlightedCodes[code]) {
    return
  }
  state.endingCredits.highlightedCodes[code] = true
  window.setTimeout(() => {
    delete state.endingCredits.highlightedCodes[code]
  }, 1000)
}

function monitorEndingCreditsCenterLine() {
  const shell = endingCreditsShellRef.value
  if (!shell) {
    return
  }

  const messageCard = shell.querySelector('.ending-credits-message-card')
  if (!messageCard) {
    return
  }

  const cardRect = messageCard.getBoundingClientRect()
  const centerY = cardRect.top + cardRect.height / 2
  const threshold = Math.max(16, Math.min(44, cardRect.height * 0.04))
  const items = shell.querySelectorAll('.ending-credits-cast-group[data-code]')

  items.forEach((item) => {
    const rect = item.getBoundingClientRect()
    const itemCenterY = rect.top + rect.height / 2
    if (Math.abs(itemCenterY - centerY) <= threshold) {
      markEndingCreditsHighlight(item.dataset.code)
    }
  })
}

function ensureEndingCreditsHighlightInterval() {
  clearEndingCreditsHighlightInterval()
  if (!isEndingCreditsPage.value) {
    return
  }
  endingCreditsHighlightTimer.value = window.setInterval(monitorEndingCreditsCenterLine, 120)
}

function setLocale(next) {
  locale.value = next
  try {
    localStorage.setItem(localeStorageKey, next)
  } catch (_) {}
}

function userError(message) {
  const error = new Error(message)
  error.exposeToUser = true
  return error
}

function setSafeError(error) {
  state.error = error?.exposeToUser ? error.message : t('common.requestFailed')
}

function goToSuccessPage(message) {
  try {
    sessionStorage.setItem(successMessageStorageKey, message)
  } catch (_) {}
  window.location.href = pageHref('/success')
}

onMounted(async () => {
  try {
    const savedLocale = localStorage.getItem(localeStorageKey)
    if (savedLocale && ['ko', 'en', 'zh', 'ja'].includes(savedLocale)) {
      locale.value = savedLocale
    }
  } catch (_) {}

  if (isSuccessPage.value) {
    try {
      state.successMessage = sessionStorage.getItem(successMessageStorageKey) || ''
    } catch (_) {
      state.successMessage = ''
    }
  }

  if (window.location.hostname === 'localhost') {
    const canonicalUrl = new URL(window.location.href)
    canonicalUrl.hostname = '127.0.0.1'
    window.location.replace(canonicalUrl.toString())
    return
  }

  const canvas = canvasRef.value
  if (canvas) {
    const ctx = canvas.getContext('2d')
    ctx.fillStyle = '#fff'
    ctx.fillRect(0, 0, canvas.width, canvas.height)
    ctx.strokeStyle = '#111'
    ctx.lineWidth = 2
    ctx.lineCap = 'round'
  }

  if (isOauthCallbackPage.value && callbackCode && callbackState) {
    exchangeOAuthCode()
  }

  if (isTabletPage.value && tabletToken.value) {
    const payload = parseJwtPayload(tabletToken.value)
    if (payload?.type === 'VERIFIED') {
      state.verifiedToken = tabletToken.value
      state.message = t('tablet.verifiedDirect')
    }
  }

  if (isIdentifierCodeReissuePage.value && !isIdeaContestLoggedIn()) {
    redirectToLogin(currentRedirectPath.value)
    return
  }

  if (isIdeaContestPage.value && !isIdeaContestLoggedIn()) {
    redirectToLogin(currentRedirectPath.value)
    return
  }
  if (isArtistMeetingPage.value && !isIdeaContestLoggedIn()) {
    redirectToLogin(currentRedirectPath.value)
    return
  }
  if (isAxSpacePage.value && !isIdeaContestLoggedIn()) {
    redirectToLogin(currentRedirectPath.value)
    return
  }
  if (isKartAxPage.value && !isIdeaContestLoggedIn()) {
    redirectToLogin(currentRedirectPath.value)
    return
  }
  if (isAxShopShopPage.value && !isIdeaContestLoggedIn()) {
    redirectToLogin(currentRedirectPath.value)
    return
  }
  if (isPdRecruitPage.value && !isIdeaContestLoggedIn()) {
    redirectToLogin(currentRedirectPath.value)
    return
  }
  if (isAdminDashboardPage.value || isAdminReservationsPage.value || isAdminSignaturesPage.value) {
    if (!isIdeaContestLoggedIn()) {
      redirectToLogin(currentRedirectPath.value)
      return
    }
    try {
      await fetchAdminReservationTypes()
      state.adminAccess = true
      if (isAdminReservationsPage.value) {
        await fetchAdminReservations()
      }
      if (isAdminSignaturesPage.value) {
        await fetchAdminSignatures()
      }
    } catch (e) {
      state.adminAccess = false
    }
    return
  }

  if (loginRedirectPath) {
    setGlobalRedirectPath(loginRedirectPath)
  }

  document.addEventListener('fullscreenchange', syncEndingCreditsFullscreenState)
  ensureEndingCreditsHighlightInterval()

})

onUnmounted(() => {
  document.removeEventListener('fullscreenchange', syncEndingCreditsFullscreenState)
  clearEndingCreditsHighlightInterval()
})

async function copyLocationAddress() {
  try {
    await navigator.clipboard.writeText(locationAddress)
    state.message = t('location.copyDone')
  } catch (_) {
    state.error = t('location.copyFailed')
  }
}

function openAddressSearch(target) {
  if (!window.daum?.Postcode) {
    state.error = t('common.addressApiUnavailable')
    return
  }

  new window.daum.Postcode({
    oncomplete(data) {
      const selectedAddress = data.userSelectedType === 'R' ? data.roadAddress : data.jibunAddress
      target.address = selectedAddress || data.address || ''
      target.addressDetail = ''
      state.error = ''
    },
  }).open()
}

watch(
  () => [state.street.reservationDate, state.street.reservationHour, isStreetCollaborationPage.value],
  async () => {
    if (!isStreetCollaborationPage.value) return
    if (!state.street.reservationDate) {
      state.street.availability = null
      return
    }
    await fetchStreetAvailability()
  }
)

watch(
  () => state.artistMeeting.date,
  (nextDate) => {
    const hours = artistMeetingHourMap[nextDate] || []
    if (!hours.includes(Number(state.artistMeeting.hour))) {
      state.artistMeeting.hour = hours.length > 0 ? String(hours[0]) : ''
    }
  }
)

watch(
  () => state.sponsorship.paymentMethodType,
  () => {
    state.sponsorship.paymentProviderPreset = ''
    state.sponsorship.paymentProviderName = ''
  },
)

watch(
  () => state.sponsorship.paymentProviderPreset,
  (nextProvider) => {
    if (nextProvider !== customPaymentProviderValue) {
      state.sponsorship.paymentProviderName = nextProvider
    } else {
      state.sponsorship.paymentProviderName = ''
    }
  },
)

watch(
  () => [
    isEndingCreditsPage.value,
    state.endingCredits.entries.length,
    state.endingCredits.rollDurationSeconds,
    state.endingCredits.rollGapPx,
    state.endingCredits.fontScalePercent,
    state.endingCredits.leadMessage,
    state.endingCredits.tailMessage,
  ],
  () => {
    window.setTimeout(() => {
      ensureEndingCreditsHighlightInterval()
    }, 0)
  }
)

async function submitIdeaContest() {
  state.loading = true
  state.error = ''
  try {
    const authToken = getIdeaContestAuthToken()
    if (!authToken) {
      throw userError(t('common.loginTokenRequired'))
    }

    const formData = new FormData()
    for (const file of state.ideaContest.files) {
      formData.append('images', file)
    }

    const res = await fetch(`${apiRoot}/idea-contests`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${authToken}`,
      },
      body: formData,
    })

    if (!res.ok) {
      throw new Error(await parseErrorResponse(res, t('common.requestFailed')))
    }

    const json = await res.json()
    goToSuccessPage(t('idea.submitted', { count: json.images?.length ?? 0 }))
  } catch (e) {
    setSafeError(e)
  } finally {
    state.loading = false
  }
}

function onIdeaFilesChange(event) {
  const files = Array.from(event.target.files || [])
  state.ideaContest.files = files
}

function onIdeaPhoneInput(event) {
  state.ideaContest.phoneNumber = formatPhoneInput(event)
}

function onSponsorshipPhoneInput(event) {
  state.sponsorship.phoneNumber = formatPhoneInput(event)
}

async function submitSponsorship() {
  state.loading = true
  state.error = ''
  try {
    const payload = {
      name: state.sponsorship.name,
      phoneNumber: state.sponsorship.phoneNumber,
      bankAccount: state.sponsorship.bankAccount,
      paymentMethodType: state.sponsorship.paymentMethodType,
      paymentProviderName: state.sponsorship.paymentProviderName,
      amount: Number(state.sponsorship.amount),
      address: combineAddress(state.sponsorship.address, state.sponsorship.addressDetail),
    }

    const res = await fetch(`${apiRoot}/sponsorship-applications`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    })

    if (!res.ok) {
      throw new Error(await parseErrorResponse(res, t('common.requestFailed')))
    }

    await res.json()
    goToSuccessPage(t('sponsorship.submitted'))
  } catch (e) {
    setSafeError(e)
  } finally {
    state.loading = false
  }
}

async function submitStreetCollaboration() {
  state.loading = true
  state.error = ''
  try {
    if (!state.street.reservationDate) {
      throw userError(t('street.reservationDateRequired'))
    }
    if (state.street.availability && !state.street.availability.available) {
      throw userError(t('street.full'))
    }

    const payload = {
      reservationAt: `${state.street.reservationDate}T${state.street.reservationHour.padStart(2, '0')}:00:00`,
      name: state.street.name,
      phoneNumber: state.street.phoneNumber,
    }

    const res = await fetch(`${apiRoot}/street-collaboration-reservations`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    })

    if (!res.ok) {
      throw new Error(await parseErrorResponse(res, t('common.requestFailed')))
    }

    await res.json()
    goToSuccessPage(t('street.submitted'))
  } catch (e) {
    setSafeError(e)
  } finally {
    state.loading = false
  }
}

async function submitArtistMeetingReservation() {
  state.loading = true
  state.error = ''
  try {
    const authToken = getIdeaContestAuthToken()
    if (!authToken) {
      throw userError(t('common.loginTokenRequired'))
    }

    const payload = {
      date: state.artistMeeting.date,
      time: `${String(state.artistMeeting.hour).padStart(2, '0')}:00:00`,
    }

    const res = await fetch(`${apiRoot}/exhibition-artist-meeting-reservations`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${authToken}`,
      },
      body: JSON.stringify(payload),
    })

    if (!res.ok) {
      throw new Error(await parseErrorResponse(res, t('common.requestFailed')))
    }

    await res.json()
    goToSuccessPage(t('artistMeeting.submitted'))
  } catch (e) {
    setSafeError(e)
  } finally {
    state.loading = false
  }
}

async function submitExhibitionSurvey() {
  state.loading = true
  state.error = ''
  try {
    const requiredFields = [
      state.exhibitionSurvey.phoneNumber,
      state.exhibitionSurvey.name,
      combineAddress(state.exhibitionSurvey.address, state.exhibitionSurvey.addressDetail),
      state.exhibitionSurvey.impressivePoint,
      state.exhibitionSurvey.improvementNeeded,
      state.exhibitionSurvey.desiredGenre,
      state.exhibitionSurvey.invitedArtist,
      state.exhibitionSurvey.feedback,
    ]
    if (requiredFields.some((value) => !String(value || '').trim())) {
      throw userError(t('common.requiredAll'))
    }
    if (
      String(state.exhibitionSurvey.identifierCode || '').trim() &&
      !/^\d{6}$/.test(String(state.exhibitionSurvey.identifierCode).trim())
    ) {
      throw userError(t('survey.identifierOptionalRule'))
    }
    const payload = { ...state.exhibitionSurvey }
    const res = await fetch(`${apiRoot}/exhibition-surveys`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })
    if (!res.ok) {
      throw new Error(await parseErrorResponse(res, t('common.requestFailed')))
    }
    window.location.href = pageHref('/exhibition-survey-success')
  } catch (e) {
    setSafeError(e)
  } finally {
    state.loading = false
  }
}

async function submitExperienceSurvey() {
  state.loading = true
  state.error = ''
  try {
    const requiredFields = [
      state.experienceSurvey.phoneNumber,
      state.experienceSurvey.name,
      combineAddress(state.experienceSurvey.address, state.experienceSurvey.addressDetail),
      state.experienceSurvey.impressiveSpace,
      state.experienceSurvey.improvementIdeaSpace,
      state.experienceSurvey.streamingParticipation,
      state.experienceSurvey.desiredGoods,
      state.experienceSurvey.feedback,
    ]
    if (requiredFields.some((value) => !String(value || '').trim())) {
      throw userError(t('common.requiredAll'))
    }
    const payload = { ...state.experienceSurvey }
    const res = await fetch(`${apiRoot}/experience-zone-surveys`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })
    if (!res.ok) {
      throw new Error(await parseErrorResponse(res, t('common.requestFailed')))
    }
    window.location.href = pageHref('/experience-zone-survey-success')
  } catch (e) {
    setSafeError(e)
  } finally {
    state.loading = false
  }
}

async function submitProjectRecruitment(projectKey, phoneNumber, date, hour) {
  state.loading = true
  state.error = ''
  try {
    const authToken = getIdeaContestAuthToken()
    if (!authToken) throw userError(t('common.loginTokenRequired'))
    const payload = { phoneNumber, date, time: `${String(hour).padStart(2, '0')}:00:00` }
    const res = await fetch(`${apiRoot}/project-recruitments/${projectKey}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${authToken}` },
      body: JSON.stringify(payload),
    })
    if (!res.ok) {
      throw new Error(await parseErrorResponse(res, t('common.requestFailed')))
    }
    goToSuccessPage(t('common.applicationCompleted'))
  } catch (e) {
    setSafeError(e)
  } finally {
    state.loading = false
  }
}

async function fetchStreetAvailability() {
  state.street.checkingAvailability = true
  try {
    const reservationAt = `${state.street.reservationDate}T${state.street.reservationHour.padStart(2, '0')}:00:00`
    const query = encodeURIComponent(reservationAt)
    const res = await fetch(`${apiRoot}/street-collaboration-reservations/availability?reservationAt=${query}`)

    if (!res.ok) {
      throw new Error(await parseErrorResponse(res, t('common.requestFailed')))
    }

    state.street.availability = await res.json()
  } catch (e) {
    setSafeError(e)
    state.street.availability = null
  } finally {
    state.street.checkingAvailability = false
  }
}

function onStreetPhoneInput(event) {
  state.street.phoneNumber = formatPhoneInput(event)
}

function onExhibitionSurveyPhoneInput(event) {
  state.exhibitionSurvey.phoneNumber = formatPhoneInput(event)
}

function onExperienceSurveyPhoneInput(event) {
  state.experienceSurvey.phoneNumber = formatPhoneInput(event)
}

function onAxSpacePhoneInput(event) {
  state.axSpace.phoneNumber = formatPhoneInput(event)
}

function onKArtAxPhoneInput(event) {
  state.kArtAx.phoneNumber = formatPhoneInput(event)
}

function onAxShopShopPhoneInput(event) {
  state.axShopShop.phoneNumber = formatPhoneInput(event)
}

function onPdRecruitPhoneInput(event) {
  state.pdRecruit.phoneNumber = formatPhoneInput(event)
}

async function fetchAdminReservationTypes() {
  const authToken = getIdeaContestAuthToken()
  if (!authToken) throw userError(t('common.loginTokenRequired'))
  const res = await fetch(`${apiRoot}/admin/reservations/types`, {
    headers: { Authorization: `Bearer ${authToken}` },
  })
  if (!res.ok) throw new Error(await parseErrorResponse(res, t('common.requestFailed')))
  state.adminReservations.types = await res.json()
}

async function fetchAdminReservations() {
  state.loading = true
  state.error = ''
  try {
    const authToken = getIdeaContestAuthToken()
    if (!authToken) throw userError(t('common.loginTokenRequired'))

    const params = new URLSearchParams()
    if (state.adminReservations.selectedType) params.set('type', state.adminReservations.selectedType)
    if (state.adminReservations.date) params.set('date', state.adminReservations.date)
    if (state.adminReservations.time) params.set('time', `${state.adminReservations.time}:00`)
    if (state.adminReservations.projectKey) params.set('projectKey', state.adminReservations.projectKey)

    const query = params.toString()
    const res = await fetch(`${apiRoot}/admin/reservations${query ? `?${query}` : ''}`, {
      headers: { Authorization: `Bearer ${authToken}` },
    })
    if (!res.ok) throw new Error(await parseErrorResponse(res, t('common.requestFailed')))
    state.adminReservations.items = await res.json()
  } catch (e) {
    setSafeError(e)
  } finally {
    state.loading = false
  }
}

async function fetchAdminSignatures() {
  state.loading = true
  state.error = ''
  try {
    const authToken = getIdeaContestAuthToken()
    if (!authToken) throw userError(t('common.loginTokenRequired'))
    const res = await fetch(`${apiRoot}/admin/signatures`, {
      headers: { Authorization: `Bearer ${authToken}` },
    })
    if (!res.ok) throw new Error(await parseErrorResponse(res, t('common.requestFailed')))
    state.adminSignatures.items = await res.json()
  } catch (e) {
    setSafeError(e)
  } finally {
    state.loading = false
  }
}

async function startOAuth(provider) {
  state.loading = true
  state.error = ''
  try {
    if (loginRedirectPath) {
      setGlobalRedirectPath(loginRedirectPath)
    }
    const redirectPath = getGlobalRedirectPath()
    if (redirectPath) {
      setGlobalRedirectPath(redirectPath)
    }
    const authBase = shouldUseIdeaContestAuthApi() ? ideaAuthApiBase : apiBase
    const redirectQuery = redirectPath ? `?redirect=${encodeURIComponent(redirectPath)}` : ''
    const result = await apiFetchWithBase(authBase, `/oauth/authorize-url/${provider}${redirectQuery}`)
    window.location.href = result.authorizationUrl
  } catch (e) {
    setSafeError(e)
    state.loading = false
  }
}

async function exchangeOAuthCode() {
  state.loading = true
  state.error = ''
  try {
    const payload = {
      code: callbackCode,
      state: callbackState,
      language: locale.value,
    }
    const authBase = shouldUseIdeaContestAuthApi() ? ideaAuthApiBase : apiBase
    const result = await apiFetchWithBase(authBase, '/oauth/exchange', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
    state.loginResult = result
    saveIdeaContestLogin(result.userId)
    saveIdeaContestAuthToken(result.registerToken)
    redirectAfterLogin()
  } catch (e) {
    setSafeError(e)
  } finally {
    state.loading = false
  }
}

async function doEmailLogin() {
  state.loading = true
  state.error = ''
  try {
    const authBase = shouldUseIdeaContestAuthApi() ? ideaAuthApiBase : apiBase
    await apiFetchWithBase(authBase, '/login/email/send-code', {
      method: 'POST',
      body: JSON.stringify({ email: state.emailLogin.email, language: locale.value }),
    })
    state.message = t('auth.codeSentToEmail')
  } catch (e) {
    setSafeError(e)
  } finally {
    state.loading = false
  }
}

async function loginEmailWithIdentifier() {
  state.loading = true
  state.error = ''
  try {
    const authBase = shouldUseIdeaContestAuthApi() ? ideaAuthApiBase : apiBase
    const result = await apiFetchWithBase(authBase, '/login/email/identifier', {
      method: 'POST',
      body: JSON.stringify({ email: state.emailLogin.email, code: state.emailLogin.code }),
    })
    state.loginResult = result
    saveIdeaContestLogin(result.userId)
    saveIdeaContestAuthToken(result.registerToken)
    state.message = t('auth.emailLoginSuccess')
    redirectAfterLogin()
  } catch (e) {
    setSafeError(e)
  } finally {
    state.loading = false
  }
}

async function verifyEmailLoginCode() {
  state.loading = true
  state.error = ''
  try {
    const authBase = shouldUseIdeaContestAuthApi() ? ideaAuthApiBase : apiBase
    const result = await apiFetchWithBase(authBase, '/login/email/verify-code', {
      method: 'POST',
      body: JSON.stringify({ email: state.emailLogin.email, code: state.emailLogin.code }),
    })
    state.loginResult = result
    saveIdeaContestLogin(result.userId)
    saveIdeaContestAuthToken(result.registerToken)
    state.message = t('auth.emailSignupSuccess')
    redirectAfterLogin()
  } catch (e) {
    setSafeError(e)
  } finally {
    state.loading = false
  }
}

async function reissueIdentifierCode() {
  state.loading = true
  state.error = ''
  state.identifierReissue.message = ''
  try {
    const authToken = getIdeaContestAuthToken()
    if (!authToken) {
      throw userError(t('common.loginTokenRequired'))
    }

    const result = await apiFetch('/identifier-code/reissue', {
      method: 'POST',
      headers: { Authorization: `Bearer ${authToken}` },
      body: JSON.stringify({ language: locale.value }),
    })
    state.identifierReissue.message = result.message || t('identifierReissue.sent')
    state.message = state.identifierReissue.message
  } catch (e) {
    setSafeError(e)
  } finally {
    state.loading = false
  }
}

function redirectToLogin(redirectPath) {
  setGlobalRedirectPath(redirectPath)
  const loginPath = isOriginalRoute ? '/original/login-page' : '/login-page'
  window.location.href = `${loginPath}?redirect=${encodeURIComponent(redirectPath)}`
}

function shouldUseIdeaContestAuthApi() {
  if (isIdeaContestLoginPage.value || isBootstrapLoginPage.value || isEmailLoginPage.value || isEmailSignupPage.value) {
    return true
  }
  const redirectPath = getGlobalRedirectPath()
  return redirectPath === '/idea-contest'
}

function getRedirectPathFromOAuthState() {
  if (!callbackState) return ''
  const payload = parseJwtPayload(callbackState)
  const redirect = typeof payload?.redirect === 'string' ? payload.redirect : ''
  return redirect.startsWith('/') ? redirect : ''
}

function redirectAfterLogin() {
  const redirectPath = getGlobalRedirectPath() || getRedirectPathFromOAuthState() || '/'
  clearGlobalRedirectPath()
  window.location.href = redirectPath
}

function goEmailLoginPage() {
  window.location.href = emailLoginHref.value
}

async function verifyOnTablet() {
  state.loading = true
  state.error = ''
  try {
    const result = await apiFetch('/verify', {
      method: 'POST',
      body: JSON.stringify({ code: state.verifyCode }),
    })
    state.verifiedToken = result.verifiedToken
    resetSignaturePreview()
    state.message = t('tablet.verifiedDone')
  } catch (e) {
    setSafeError(e)
  } finally {
    state.loading = false
  }
}

function pointerPos(e, canvas) {
  const rect = canvas.getBoundingClientRect()
  return { x: e.clientX - rect.left, y: e.clientY - rect.top }
}

function startDraw(e) {
  const canvas = canvasRef.value
  if (!canvas) return
  drawing.value = true
  const ctx = canvas.getContext('2d')
  const p = pointerPos(e, canvas)
  ctx.beginPath()
  ctx.moveTo(p.x, p.y)
}

function draw(e) {
  if (!drawing.value) return
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  const p = pointerPos(e, canvas)
  ctx.lineTo(p.x, p.y)
  ctx.stroke()
}

function endDraw() {
  drawing.value = false
}

function clearSignature() {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  ctx.fillStyle = '#fff'
  ctx.fillRect(0, 0, canvas.width, canvas.height)
  resetSignaturePreview()
}

function resetSignaturePreview() {
  state.signaturePreview.token = ''
  state.signaturePreview.recognizedText = ''
  state.signaturePreview.englishName = ''
  state.signaturePreview.koreanName = ''
  state.signaturePreview.koreanMeaningText = ''
  state.signaturePreview.detectedLanguage = ''
  state.signaturePreview.ocrConfidence = null
}

function applyEndingCreditsLeadPreset(content) {
  state.endingCredits.leadMessage = content
}

function applyEndingCreditsTailPreset(content) {
  state.endingCredits.tailMessage = content
}

async function createTabletSignatureFormData() {
  const canvas = canvasRef.value
  if (!canvas) return null

  const signatureBlob = await new Promise((resolve, reject) => {
    canvas.toBlob((blob) => {
      if (blob) resolve(blob)
      else reject(userError(t('tablet.signatureImageFailed')))
    }, 'image/png')
  })

  const formData = new FormData()
  formData.append('signatureImage', signatureBlob, 'signature.png')
  formData.append('nameLanguage', state.signatureNameLanguage)
  if (state.signatureKoreanName.trim()) {
    formData.append('koreanName', state.signatureKoreanName.trim())
  }
  return formData
}

async function submitSignature() {
  if (!canvasRef.value) return

  state.loading = true
  state.error = ''
  try {
    const formData = await createTabletSignatureFormData()
    const res = await fetch(`${apiRoot}/auth/signature`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${state.verifiedToken}` },
      body: formData,
    })
    if (!res.ok) throw new Error(await parseErrorResponse(res, t('common.requestFailed')))
    state.message = t('tablet.signatureSaved')
  } catch (e) {
    setSafeError(e)
  } finally {
    state.loading = false
  }
}

async function previewSignatureOnTablet() {
  if (!canvasRef.value) return

  state.loading = true
  state.error = ''
  try {
    const formData = await createTabletSignatureFormData()
    const res = await fetch(`${apiRoot}/auth/signature/preview`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${state.verifiedToken}` },
      body: formData,
    })
    if (!res.ok) throw new Error(await parseErrorResponse(res, t('common.requestFailed')))
    const result = await res.json()
    state.signaturePreview.token = result.previewToken || ''
    state.signaturePreview.recognizedText = result.recognizedText || ''
    state.signaturePreview.englishName = result.englishName || ''
    state.signaturePreview.koreanName = result.koreanName || ''
    state.signaturePreview.koreanMeaningText = result.koreanMeaningText || ''
    state.signaturePreview.detectedLanguage = result.detectedLanguage || ''
    state.signaturePreview.ocrConfidence = result.ocrConfidence
    state.message = 'OCR 寃곌낵瑜??뺤씤??????ν빐 二쇱꽭??'
  } catch (e) {
    resetSignaturePreview()
    setSafeError(e)
  } finally {
    state.loading = false
  }
}

async function confirmPreviewSignatureOnTablet() {
  if (!state.signaturePreview.token) {
    setSafeError(userError('癒쇱? OCR 寃곌낵瑜??뺤씤??二쇱꽭??'))
    return
  }

  state.loading = true
  state.error = ''
  try {
    const res = await fetch(`${apiRoot}/auth/signature/confirm`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${state.verifiedToken}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        previewToken: state.signaturePreview.token,
      }),
    })
    if (!res.ok) throw new Error(await parseErrorResponse(res, t('common.requestFailed')))
    resetSignaturePreview()
    state.message = '?쒕챸????λ릺?덉뒿?덈떎.'
  } catch (e) {
    setSafeError(e)
  } finally {
    state.loading = false
  }
}

async function downloadSignatureArtifact(path, filename, payload = {}) {
  state.loading = true
  state.error = ''
  try {
    const res = await fetch(`${apiRoot}${path}`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${state.verifiedToken}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    })
    if (!res.ok) throw new Error(await parseErrorResponse(res, t('common.requestFailed')))

    const blob = await res.blob()
    const url = URL.createObjectURL(blob)
    const anchor = document.createElement('a')
    anchor.href = url
    anchor.download = filename
    document.body.appendChild(anchor)
    anchor.click()
    anchor.remove()
    URL.revokeObjectURL(url)
    state.message = t('tablet.artifactDownloaded')
  } catch (e) {
    setSafeError(e)
  } finally {
    state.loading = false
  }
}

async function downloadCertificateArtifactByCode(path, filename) {
  state.loading = true
  state.error = ''
  try {
    const code = state.certificateDownloadCode.trim()
    if (!/^\d{6}$/.test(code)) {
      throw userError(t('certificateDownload.invalidCode'))
    }

    const res = await fetch(`${apiRoot}${path}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ code }),
    })
    if (!res.ok) throw new Error(await parseErrorResponse(res, t('common.requestFailed')))

    const blob = await res.blob()
    const url = URL.createObjectURL(blob)
    const anchor = document.createElement('a')
    anchor.href = url
    anchor.download = filename
    document.body.appendChild(anchor)
    anchor.click()
    anchor.remove()
    URL.revokeObjectURL(url)
    state.message = t('certificateDownload.downloaded')
  } catch (e) {
    setSafeError(e)
  } finally {
    state.loading = false
  }
}

function revokeEndingCreditsSignatureImage() {
  for (const entry of state.endingCredits.entries) {
    if (entry.signatureImageUrl) {
      URL.revokeObjectURL(entry.signatureImageUrl)
      entry.signatureImageUrl = ''
    }
  }
}

function onEndingCreditsCodeInput(event) {
  state.endingCredits.code = String(event.target.value || '').replace(/\D/g, '').slice(0, 6)
}

function onEndingCreditsDurationInput(event) {
  const next = Number(event.target.value || 18)
  state.endingCredits.rollDurationSeconds = Math.min(60, Math.max(8, next))
}

function onEndingCreditsGapInput(event) {
  const next = Number(event.target.value || 72)
  state.endingCredits.rollGapPx = Math.min(180, Math.max(24, next))
}

function onEndingCreditsFontScaleInput(event) {
  const next = Number(event.target.value || 100)
  state.endingCredits.fontScalePercent = Math.min(180, Math.max(60, next))
}

async function addEndingCreditsEntry() {
  state.loading = true
  state.error = ''
  state.message = ''

  try {
    const code = state.endingCredits.code.trim()
    if (!/^\d{6}$/.test(code)) {
      throw userError(endingCreditsText.value.inputPlaceholder)
    }

    if (state.endingCredits.entries.some((entry) => entry.code === code)) {
      throw userError('\uC774\uBBF8 \uCD94\uAC00\uB41C \uC2DD\uBCC4\uC790 \uCF54\uB4DC\uC785\uB2C8\uB2E4.')
    }

    const entryRes = await fetch(`${apiRoot}/ending-credits/lookup`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ code }),
    })
    if (!entryRes.ok) throw new Error(await parseErrorResponse(entryRes, t('common.requestFailed')))

    const entry = await entryRes.json()

    const signatureRes = await fetch(`${apiRoot}/certificate-download/signature-image`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ code }),
    })
    if (!signatureRes.ok) throw new Error(await parseErrorResponse(signatureRes, t('common.requestFailed')))

    const signatureBlob = await signatureRes.blob()
    state.endingCredits.entries.push({
      code,
      englishName: entry.englishName || '',
      koreanName: entry.koreanName || '',
      hasEnglishName: !!entry.hasEnglishName,
      hasKoreanName: !!entry.hasKoreanName,
      hasSignature: !!entry.hasSignature,
      signatureImageUrl: URL.createObjectURL(signatureBlob),
    })
    state.endingCredits.code = ''
  } catch (e) {
    setSafeError(e)
  } finally {
    state.loading = false
  }
}

async function showEndingCreditsFullscreen() {
  state.error = ''
  if (endingCreditsRollEntries.value.length === 0) {
    setSafeError(userError('\uBA3C\uC800 \uC2DD\uBCC4\uC790 \uCF54\uB4DC\uB97C \uCD94\uAC00\uD574 \uC5D4\uB529 \uD06C\uB808\uB51F \uD56D\uBAA9\uC744 \uB9CC\uB4E4\uC5B4 \uC8FC\uC138\uC694.'))
    return
  }

  const target = endingCreditsShellRef.value
  if (!target || !target.requestFullscreen) {
    setSafeError(userError('\uD604\uC7AC \uD658\uACBD\uC5D0\uC11C\uB294 \uC804\uCCB4\uD654\uBA74 \uAE30\uB2A5\uC744 \uC9C0\uC6D0\uD558\uC9C0 \uC54A\uC2B5\uB2C8\uB2E4.'))
    return
  }

  try {
    await target.requestFullscreen()
    state.endingCredits.isFullscreen = true
  } catch (_) {
    setSafeError(userError('\uC804\uCCB4\uD654\uBA74 \uC804\uD658\uC5D0 \uC2E4\uD328\uD588\uC2B5\uB2C8\uB2E4.'))
  }
}

function removeEndingCreditsEntry(code) {
  const target = state.endingCredits.entries.find((entry) => entry.code === code)
  if (target?.signatureImageUrl) {
    URL.revokeObjectURL(target.signatureImageUrl)
  }
  state.endingCredits.entries = state.endingCredits.entries.filter((entry) => entry.code !== code)
}

function downloadSignatureImage() {
  return downloadSignatureArtifact('/auth/signature/render', 'signature-render.png', {
    fontFamily: 'Nanum Brush Script',
    fontSize: 88,
    width: 800,
    height: 240,
  })
}

function downloadCertificateSample() {
  return downloadSignatureArtifact('/auth/signature/certificate-sample', 'certificate-sample.png', {
    title: 'BackToScreen Participation Certificate',
    fontFamily: 'Nanum Brush Script',
    nameX: 700,
    nameY: 540,
    signatureX: 1050,
    signatureY: 830,
  })
}

function downloadStoredSignatureImage() {
  return downloadCertificateArtifactByCode('/certificate-download/signature-image', 'signature-image.png')
}

function downloadStoredCertificatePdf() {
  return downloadCertificateArtifactByCode('/certificate-download/certificate-pdf', 'certificate.pdf')
}
</script>

<template>
  <main :class="['container', { 'bootstrap-mode': isBootstrapRoute, 'ending-credits-layout': isEndingCreditsPage }]">
    <div class="actions" style="justify-content: flex-end; margin-bottom: 8px">
      <label class="locale-select-label">
        <select :value="locale" aria-label="Language Select" @change="setLocale($event.target.value)">
          <option value="ko">{{ isEndingCreditsPage ? localeDisplayNames.ko : t('lang.ko') }}</option>
          <option value="en">{{ isEndingCreditsPage ? localeDisplayNames.en : t('lang.en') }}</option>
          <option value="zh">{{ isEndingCreditsPage ? localeDisplayNames.zh : t('lang.zh') }}</option>
          <option value="ja">{{ isEndingCreditsPage ? localeDisplayNames.ja : t('lang.ja') }}</option>
        </select>
      </label>
    </div>

    <section v-if="isHome" class="card">
      <div class="home-layout">
        <div class="home-group">
          <h3 class="home-group-title">{{ t('home.login') }}</h3>
          <div class="actions home-actions">
            <button :disabled="state.loading" @click="startOAuth('kakao')">{{ t('nav.kakaoLogin') }}</button>
            <button :disabled="state.loading" @click="startOAuth('google')">{{ t('nav.googleLogin') }}</button>
            <a class="route-button" :href="emailLoginHref">{{ t('nav.emailLoginPage') }}</a>
          </div>
        </div>
        <div class="home-group">
          <h3 class="home-group-title">{{ t('home.pageLinks') }}</h3>
          <div class="actions home-actions">
            <a class="route-button" :href="pageHref('/idea-contest')">{{ t('nav.ideaContest') }}</a>
            <a class="route-button" :href="pageHref('/sponsorship-application')">{{ t('nav.sponsorshipApplication') }}</a>
            <a class="route-button" :href="pageHref('/street-collaboration')">{{ t('nav.streetCollaboration') }}</a>
            <a class="route-button" :href="pageHref('/artist-meet')">{{ t('nav.exhibitionArtistMeeting') }}</a>
            <a class="route-button" :href="pageHref('/exhibition-survey')">{{ t('nav.exhibitionSurvey') }}</a>
            <a class="route-button" :href="pageHref('/experience-zone-survey')">{{ t('nav.experienceZoneSurvey') }}</a>
            <a class="route-button" :href="pageHref('/project-participant')">{{ t('nav.projectParticipant') }}</a>
            <a class="route-button" :href="pageHref('/streaming-recruit')">{{ t('nav.streamingRecruit') }}</a>
            <a class="route-button" :href="pageHref('/location')">{{ t('nav.location') }}</a>
          </div>
        </div>
      </div>
    </section>

    <section v-if="isBootstrapHome" class="bootstrap-hub-page">
      <div class="container-fluid py-5">
        <div class="row justify-content-center">
          <div class="col-12 col-xl-10">
            <div class="text-center mb-4">
              <span class="badge rounded-pill text-bg-light border border-info-subtle text-info-emphasis px-3 py-2 mb-3">
                BackToScreen
              </span>
              <h1 class="h2 fw-bold mb-2">BackToScreen</h1>
              <p class="text-secondary mb-0">AX Convergence Calligraphy Exhibition</p>
            </div>
            <div class="row g-3">
              <div v-for="page in bootstrapPages" :key="page.href" class="col-12 col-md-6 col-lg-4">
                <a class="btn btn-light border border-info-subtle shadow-sm w-100 py-3 fw-semibold text-info-emphasis bootstrap-hub-link" :href="page.href">
                  {{ page.label || t(page.key) }}
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section v-if="isEndingCreditsPage" class="ending-credits-page">
      <div ref="endingCreditsShellRef" class="ending-credits-shell" :class="{ 'ending-credits-shell-fullscreen': state.endingCredits.isFullscreen }">
        <div class="ending-credits-hero">
          <article class="ending-credits-poster-card">
            <img :src="artistMeetingPosterUrl" alt="BackToScreen Poster" />
          </article>

          <article class="ending-credits-message-card">
            <div
              class="ending-credits-roll"
              :style="{
                '--ending-roll-duration': `${state.endingCredits.rollDurationSeconds}s`,
                '--ending-roll-gap': `${state.endingCredits.rollGapPx}px`,
                '--ending-font-scale': `${state.endingCredits.fontScalePercent / 100}`,
                '--ending-roll-iteration-count': state.endingCredits.stopAfterOneCycle ? '1' : 'infinite',
                '--ending-roll-fill-mode': state.endingCredits.stopAfterOneCycle ? 'forwards' : 'none',
              }"
            >
              <div
                v-for="group in endingCreditsMessageGroups"
                :key="group.id"
                class="ending-credits-group"
              >
                <p class="ending-credits-group-title">{{ group.title }}</p>
                <p
                  v-for="line in group.lines"
                  :key="`${group.id}-${line}`"
                  class="ending-credits-group-line"
                >
                  {{ line }}
                </p>
              </div>

              <div
                v-for="entry in endingCreditsRollEntries"
                :key="`roll-${entry.code}`"
                :class="['ending-credits-cast-group', { 'is-highlighted': state.endingCredits.highlightedCodes[entry.code] }]"
                :data-code="entry.code"
              >
                <p v-if="entry.englishName" class="ending-credits-cast-line">{{ entry.englishName }}</p>
                <p v-if="entry.koreanName" class="ending-credits-cast-line ending-credits-cast-line-korean">{{ entry.koreanName }}</p>
                <img
                  v-if="entry.signatureImageUrl"
                  class="ending-credits-cast-signature"
                  :src="entry.signatureImageUrl"
                  alt="Calligraphy Signature"
                />
              </div>

              <div
                v-for="group in endingCreditsClosingGroups"
                :key="group.id"
                class="ending-credits-group"
              >
                <p class="ending-credits-group-title">{{ group.title }}</p>
                <p
                  v-for="line in group.lines"
                  :key="`${group.id}-${line}`"
                  class="ending-credits-group-line"
                >
                  {{ line }}
                </p>
              </div>
            </div>
          </article>
        </div>

        <article class="ending-credits-control-card">
          <div class="ending-credits-control-header">
            <h2>{{ endingCreditsText.title }}</h2>
            <p>{{ endingCreditsText.description }}</p>
          </div>
          <div class="ending-credits-settings-grid">
            <label class="ending-credits-field ending-credits-field-wide">
              <span>{{ '\uCD5C\uC0C1\uB2E8 \uCCAB \uBB38\uAD6C' }}</span>
              <div class="ending-credits-preset-actions">
                <button
                  type="button"
                  class="ending-credits-preset-button"
                  @click="applyEndingCreditsLeadPreset(endingCreditsLeadPreset1)"
                >
                  {{ '\uC0C1\uB2E8 \uCD94\uAC00 \uBB38\uAD6C 1\uBC88' }}
                </button>
              </div>
              <textarea
                v-model="state.endingCredits.leadMessage"
                rows="3"
                :placeholder="'\uAC00\uC7A5 \uBA3C\uC800 \uC62C\uB77C\uAC08 \uBB38\uAD6C\uB97C \uC904\uBC14\uAFC8\uC73C\uB85C \uC785\uB825\uD558\uC138\uC694.'"
              />
            </label>
            <label class="ending-credits-field ending-credits-field-wide">
              <span>{{ '\uCD5C\uD558\uB2E8 \uB9C8\uC9C0\uB9C9 \uBB38\uAD6C' }}</span>
              <div class="ending-credits-preset-actions">
                <button
                  type="button"
                  class="ending-credits-preset-button"
                  @click="applyEndingCreditsTailPreset(endingCreditsTailPreset1)"
                >
                  {{ '\uD558\uB2E8 \uCD94\uAC00 \uBB38\uAD6C 1\uBC88' }}
                </button>
              </div>
              <textarea
                v-model="state.endingCredits.tailMessage"
                rows="3"
                :placeholder="'\uAC00\uC7A5 \uB9C8\uC9C0\uB9C9\uC5D0 \uC62C\uB77C\uAC08 \uBB38\uAD6C\uB97C \uC904\uBC14\uAFC8\uC73C\uB85C \uC785\uB825\uD558\uC138\uC694.'"
              />
            </label>
            <label class="ending-credits-field">
              <span>{{ '\uB864\uB9C1 \uC18D\uB3C4(\uCD08)' }}</span>
              <input
                :value="state.endingCredits.rollDurationSeconds"
                type="number"
                min="8"
                max="60"
                step="1"
                @input="onEndingCreditsDurationInput"
              />
            </label>
            <label class="ending-credits-field">
              <span>{{ '\uD56D\uBAA9 \uAC04\uACA9(px)' }}</span>
              <input
                :value="state.endingCredits.rollGapPx"
                type="number"
                min="24"
                max="180"
                step="4"
                @input="onEndingCreditsGapInput"
              />
            </label>
            <label class="ending-credits-field">
              <span>{{ '\uAE00\uC790 \uD06C\uAE30(%)' }}</span>
              <input
                :value="state.endingCredits.fontScalePercent"
                type="number"
                min="60"
                max="180"
                step="5"
                @input="onEndingCreditsFontScaleInput"
              />
            </label>
            <label class="ending-credits-field ending-credits-toggle-field">
              <span>{{ endingCreditsText.stopAfterOneCycle }}</span>
              <input v-model="state.endingCredits.stopAfterOneCycle" type="checkbox" />
            </label>
            <label class="ending-credits-field ending-credits-toggle-field">
              <span>{{ endingCreditsText.includeIdentifierEntries }}</span>
              <input v-model="state.endingCredits.includeIdentifierEntries" type="checkbox" />
            </label>
          </div>
          <div class="ending-credits-control-row">
            <label class="ending-credits-field">
              <span>{{ endingCreditsText.inputLabel }}</span>
              <input
                :value="state.endingCredits.code"
                type="text"
                maxlength="6"
                inputmode="numeric"
                :placeholder="endingCreditsText.inputPlaceholder"
                @input="onEndingCreditsCodeInput"
              />
            </label>
            <button :disabled="state.loading" @click="addEndingCreditsEntry">{{ endingCreditsText.button }}</button>
            <button :disabled="state.loading || endingCreditsRollEntries.length === 0" @click="showEndingCreditsFullscreen">{{ endingCreditsViewButtonLabelKo }}</button>
          </div>
        </article>
        <article class="ending-credits-result-card">
          <p class="ending-credits-result-title">{{ endingCreditsText.resultTitle }}</p>
          <div class="ending-credits-list-wrap">
            <table class="ending-credits-list-table">
              <thead>
                <tr>
                  <th>Code</th>
                  <th>{{ endingCreditsText.englishName }}</th>
                  <th>{{ endingCreditsText.koreanName }}</th>
                  <th>{{ endingCreditsText.signature }}</th>
                  <th>{{ '\uAD00\uB9AC' }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="entry in state.endingCredits.entries" :key="entry.code">
                  <td>{{ entry.code }}</td>
                  <td>
                    <span class="ending-credits-ready-badge" :class="{ 'is-ready': entry.hasEnglishName }">
                      {{ entry.hasEnglishName ? '\uC900\uBE44\uB428' : '\uBBF8\uC900\uBE44' }}
                    </span>
                    <strong v-if="entry.englishName">{{ entry.englishName }}</strong>
                  </td>
                  <td>
                    <span class="ending-credits-ready-badge" :class="{ 'is-ready': entry.hasKoreanName }">
                      {{ entry.hasKoreanName ? '\uC900\uBE44\uB428' : '\uBBF8\uC900\uBE44' }}
                    </span>
                    <strong v-if="entry.koreanName">{{ entry.koreanName }}</strong>
                  </td>
                  <td>
                    <span class="ending-credits-ready-badge" :class="{ 'is-ready': entry.hasSignature }">
                      {{ entry.hasSignature ? '\uC900\uBE44\uB428' : '\uBBF8\uC900\uBE44' }}
                    </span>
                  </td>
                  <td>
                    <button class="ending-credits-delete-button" type="button" @click="removeEndingCreditsEntry(entry.code)">{{ '\uC0AD\uC81C' }}</button>
                  </td>
                </tr>
                <tr v-if="state.endingCredits.entries.length === 0">
                  <td colspan="5">{{ '\uCD94\uAC00\uB41C \uC2DD\uBCC4\uC790 \uCF54\uB4DC\uAC00 \uC544\uC9C1 \uC5C6\uC2B5\uB2C8\uB2E4.' }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </article>
      </div>
    </section>

    <section v-if="isSuccessPage" class="split-page">
      <div class="split-page-top"></div>
      <div class="split-page-body">
        <div class="card idea-pane">
          <div class="idea-form">
            <h2>{{ t('common.successTitle') }}</h2>
            <p>{{ state.successMessage || t('common.applicationCompleted') }}</p>
          </div>
        </div>
        <div class="card idea-pane">
          <div class="idea-poster"><img :src="ideaPosterUrl" alt="Success" /></div>
        </div>
      </div>
    </section>

    <section v-if="isArtistMeetingPage" class="idea-shell">
      <div class="card idea-pane">
        <div class="idea-form">
          <h2>{{ t('artistMeeting.title') }}</h2>
          <div class="grid">
            <label>{{ t('artistMeeting.date') }}
              <select v-model="state.artistMeeting.date">
                <option v-for="date in artistMeetingDateOptions" :key="date" :value="date">{{ date }}</option>
              </select>
            </label>
            <label>{{ t('artistMeeting.time') }}
              <select v-model="state.artistMeeting.hour">
                <option v-for="hour in (artistMeetingHourMap[state.artistMeeting.date] || [])" :key="hour" :value="String(hour)">{{ hour }}:00</option>
              </select>
            </label>
          </div>
          <button :disabled="state.loading" @click="submitArtistMeetingReservation">{{ t('artistMeeting.submit') }}</button>
        </div>
      </div>
      <div class="card idea-pane">
        <div class="idea-poster">
          <img :src="artistMeetingPosterUrl" alt="Artist meeting poster" />
        </div>
      </div>
    </section>

    <section v-if="isIdeaContestLoginPage" class="idea-login-shell">
      <div class="card idea-login-card">
        <div class="idea-login-actions">
          <button class="idea-login-button" :disabled="state.loading" @click="startOAuth('kakao')">{{ t('nav.kakaoLogin') }}</button>
          <button class="idea-login-button" :disabled="state.loading" @click="startOAuth('google')">{{ t('nav.googleLogin') }}</button>
          <button class="idea-login-button" :disabled="state.loading" @click="goEmailLoginPage">{{ t('nav.emailLoginPage') }}</button>
        </div>
      </div>
    </section>

    <section v-if="isBootstrapLoginPage" class="bootstrap-login-page">
      <div class="container-fluid py-5">
        <div class="row justify-content-center">
          <div class="col-12 col-md-8 col-lg-5 col-xl-4">
            <div class="card border-0 shadow-lg bootstrap-login-card">
              <div class="card-body p-4 p-md-5">
                <div class="text-center mb-4">
                  <span class="badge rounded-pill text-bg-light border border-info-subtle text-info-emphasis px-3 py-2 mb-3">
                    ZDO Cinema
                  </span>
                  <h1 class="h3 fw-bold mb-2">{{ t('bootstrapLogin.title') }}</h1>
                  <p class="text-secondary mb-0">{{ t('bootstrapLogin.subtitle') }}</p>
                </div>

                <div class="d-grid gap-3">
                  <button class="btn btn-info text-white fw-semibold py-3 shadow-sm" :disabled="state.loading" @click="startOAuth('kakao')">
                    {{ t('nav.kakaoLogin') }}
                  </button>
                  <button class="btn btn-outline-info fw-semibold py-3" :disabled="state.loading" @click="startOAuth('google')">
                    {{ t('nav.googleLogin') }}
                  </button>
                  <button class="btn btn-light border border-info-subtle fw-semibold py-3 text-info-emphasis" :disabled="state.loading" @click="goEmailLoginPage">
                    {{ t('nav.emailLoginPage') }}
                  </button>
                </div>
                <p class="bootstrap-signup-link-wrap text-center mt-4 mb-0">
                  <a class="bootstrap-signup-link" :href="emailSignupHref">{{ t('nav.emailSignup') }}</a>
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section v-if="isStreetCollaborationPage" class="idea-shell">
      <div class="card idea-pane">
        <div class="idea-form">
          <h2>{{ t('street.title') }}</h2>
          <div class="grid">
            <label>{{ t('street.reservationDate') }} <input v-model="state.street.reservationDate" type="date" :placeholder="t('common.placeholders.date')" /></label>
            <label>{{ t('street.reservationHour') }}
              <select v-model="state.street.reservationHour">
                <option v-for="hour in streetHourOptions" :key="hour" :value="String(hour)">{{ hour }}:00</option>
              </select>
            </label>
            <label>{{ t('street.name') }} <input v-model="state.street.name" type="text" :placeholder="t('common.placeholders.name')" /></label>
            <label>{{ t('street.phone') }} <input v-model="state.street.phoneNumber" type="text" :placeholder="t('common.placeholders.phone')" @input="onStreetPhoneInput" /></label>
          </div>
          <button :disabled="state.loading || state.street.checkingAvailability || (state.street.availability && !state.street.availability.available)" @click="submitStreetCollaboration">{{ t('street.submit') }}</button>
          <p v-if="state.street.checkingAvailability">{{ t('street.checking') }}</p>
          <p v-else-if="state.street.availability" :class="state.street.availability.available ? 'ok' : 'error'">
            {{ t('street.availability', { current: state.street.availability.currentCount, capacity: state.street.availability.capacity, remaining: state.street.availability.remaining }) }}
          </p>
          <p v-if="state.street.availability && !state.street.availability.available" class="error">{{ t('street.full') }}</p>
        </div>
      </div>
      <div class="card idea-pane">
        <div class="idea-poster">
          <img :src="ideaPosterUrl" alt="Poster Placeholder" />
        </div>
      </div>
    </section>

    <section v-if="isSponsorshipPage" class="idea-shell">
      <div class="card idea-pane">
        <div class="idea-form">
          <h2>{{ t('sponsorship.title') }}</h2>
          <div class="grid">
            <label>{{ t('sponsorship.name') }} <input v-model="state.sponsorship.name" type="text" :placeholder="t('common.placeholders.name')" /></label>
            <label>{{ t('sponsorship.phone') }} <input v-model="state.sponsorship.phoneNumber" type="text" :placeholder="t('common.placeholders.phone')" @input="onSponsorshipPhoneInput" /></label>
            <label>{{ t('sponsorship.paymentMethodType') }}
              <select v-model="state.sponsorship.paymentMethodType">
                <option value="BANK">{{ t('sponsorship.paymentMethods.bank') }}</option>
                <option value="PAYMENT">{{ t('sponsorship.paymentMethods.payment') }}</option>
                <option value="CARD">{{ t('sponsorship.paymentMethods.card') }}</option>
              </select>
            </label>
            <label>{{ t('sponsorship.paymentProviderName') }}
              <select v-model="state.sponsorship.paymentProviderPreset">
                <option disabled value="">{{ t('common.placeholders.paymentProviderName') }}</option>
                <option v-for="provider in sponsorshipPaymentProviderOptions" :key="provider" :value="provider">{{ provider }}</option>
                <option :value="customPaymentProviderValue">{{ t('sponsorship.paymentProviderCustom') }}</option>
              </select>
            </label>
            <label v-if="isCustomSponsorshipPaymentProvider">{{ t('sponsorship.paymentProviderCustom') }} <input v-model="state.sponsorship.paymentProviderName" type="text" :placeholder="t('common.placeholders.paymentProviderName')" /></label>
            <label>{{ t('sponsorship.bankAccount') }} <input v-model="state.sponsorship.bankAccount" type="text" :placeholder="t('common.placeholders.bankAccount')" /></label>
            <label>{{ t('sponsorship.amount') }} <input v-model="state.sponsorship.amount" type="number" min="1" :placeholder="t('common.placeholders.amount')" /></label>
          </div>
          <label>{{ t('sponsorship.address') }}</label>
          <div class="address-field">
            <div class="address-search-row">
              <input v-model="state.sponsorship.address" type="text" readonly :placeholder="t('common.baseAddress')" />
              <button type="button" :disabled="state.loading" @click="openAddressSearch(state.sponsorship)">{{ t('common.searchAddress') }}</button>
            </div>
            <input v-model="state.sponsorship.addressDetail" type="text" :placeholder="t('common.detailAddress')" />
          </div>
          <button :disabled="state.loading" @click="submitSponsorship">{{ t('sponsorship.submit') }}</button>
        </div>
      </div>
      <div class="card idea-pane">
        <div class="idea-poster">
          <img :src="ideaPosterUrl" alt="Poster Placeholder" />
        </div>
      </div>
    </section>

    <section v-if="isIdeaContestPage" class="idea-shell">
      <div class="card idea-pane">
        <div class="idea-form">
          <h2>{{ t('idea.title') }}</h2>
          <label>{{ t('idea.memoLabel') }}</label>
          <input type="file" multiple accept="image/*" :aria-label="t('common.placeholders.imageFile')" @change="onIdeaFilesChange" />
          <p>{{ t('idea.selectedFiles', { count: state.ideaContest.files.length }) }}</p>
          <button :disabled="state.loading" @click="submitIdeaContest">{{ t('idea.submit') }}</button>
        </div>
      </div>
      <div class="card idea-pane">
        <div class="idea-poster">
          <img :src="ideaPosterUrl" alt="Docker Poster Placeholder" />
        </div>
      </div>
    </section>

    <section v-if="isSponsorshipThanksPage" class="split-page">
      <div class="split-page-top"></div>
      <div class="split-page-body">
        <div class="card idea-pane"><div class="idea-form"><h2>{{ t('sponsorship.thanksTitle') }}</h2><p>{{ t('sponsorship.thanksDesc') }}</p></div></div>
        <div class="card idea-pane"><div class="idea-poster"><img :src="ideaPosterUrl" alt="Thanks Poster" /></div></div>
      </div>
    </section>

    <section v-if="isExhibitionSurveyPage" class="card">
      <h2>{{ t('survey.exhibitionTitle') }}</h2>
      <div class="survey-stack">
        <label>{{ t('survey.nameRequired') }} <input v-model="state.exhibitionSurvey.name" type="text" required :placeholder="t('common.placeholders.name')" /></label>
        <label>{{ t('survey.phoneRequired') }} <input v-model="state.exhibitionSurvey.phoneNumber" type="text" required :placeholder="t('common.placeholders.phone')" @input="onExhibitionSurveyPhoneInput" /></label>
        <label>{{ t('survey.addressRequired') }}
          <div class="address-field">
            <div class="address-search-row">
              <input v-model="state.exhibitionSurvey.address" type="text" readonly required :placeholder="t('common.baseAddress')" />
              <button type="button" :disabled="state.loading" @click="openAddressSearch(state.exhibitionSurvey)">{{ t('common.searchAddress') }}</button>
            </div>
            <input v-model="state.exhibitionSurvey.addressDetail" type="text" :placeholder="t('common.detailAddress')" />
          </div>
        </label>
        <label>{{ t('survey.identifierOptional') }} <input v-model="state.exhibitionSurvey.identifierCode" type="text" maxlength="6" :placeholder="t('common.placeholders.identifierCode')" /></label>
      </div>
      <div class="survey-stack">
        <label>{{ t('survey.exhibition.q1') }} <textarea v-model="state.exhibitionSurvey.impressivePoint" required :placeholder="t('common.placeholders.answer')" /></label>
        <label>{{ t('survey.exhibition.q2') }} <textarea v-model="state.exhibitionSurvey.improvementNeeded" required :placeholder="t('common.placeholders.answer')" /></label>
        <label>{{ t('survey.exhibition.q3') }} <textarea v-model="state.exhibitionSurvey.desiredGenre" required :placeholder="t('common.placeholders.answer')" /></label>
        <label>{{ t('survey.exhibition.q4') }} <textarea v-model="state.exhibitionSurvey.invitedArtist" required :placeholder="t('common.placeholders.answer')" /></label>
        <label>{{ t('survey.exhibition.q5') }} <textarea v-model="state.exhibitionSurvey.feedback" required :placeholder="t('common.placeholders.answer')" /></label>
      </div>
      <button :disabled="state.loading" @click="submitExhibitionSurvey">{{ t('survey.submitExhibition') }}</button>
    </section>

    <section v-if="isExhibitionSurveySuccessPage" class="split-page">
      <div class="split-page-top"></div>
      <div class="split-page-body">
        <div class="card idea-pane"><div class="idea-form"><h2>{{ t('survey.exhibitionSuccessTitle') }}</h2><p>{{ t('survey.successDesc') }}</p></div></div>
        <div class="card idea-pane"><div class="idea-poster"><img :src="ideaPosterUrl" alt="Survey Success" /></div></div>
      </div>
    </section>

    <section v-if="isExperienceZoneSurveyPage" class="card">
      <h2>{{ t('survey.experienceTitle') }}</h2>
      <div class="survey-stack">
        <label>{{ t('survey.nameRequired') }} <input v-model="state.experienceSurvey.name" type="text" required :placeholder="t('common.placeholders.name')" /></label>
        <label>{{ t('survey.phoneRequired') }} <input v-model="state.experienceSurvey.phoneNumber" type="text" required :placeholder="t('common.placeholders.phone')" @input="onExperienceSurveyPhoneInput" /></label>
        <label>{{ t('survey.addressRequired') }}
          <div class="address-field">
            <div class="address-search-row">
              <input v-model="state.experienceSurvey.address" type="text" readonly required :placeholder="t('common.baseAddress')" />
              <button type="button" :disabled="state.loading" @click="openAddressSearch(state.experienceSurvey)">{{ t('common.searchAddress') }}</button>
            </div>
            <input v-model="state.experienceSurvey.addressDetail" type="text" :placeholder="t('common.detailAddress')" />
          </div>
        </label>
      </div>
      <div class="survey-stack">
        <label>{{ t('survey.experience.q1') }} <textarea v-model="state.experienceSurvey.impressiveSpace" required :placeholder="t('common.placeholders.answer')" /></label>
        <label>{{ t('survey.experience.q2') }} <textarea v-model="state.experienceSurvey.improvementIdeaSpace" required :placeholder="t('common.placeholders.answer')" /></label>
        <label>{{ t('survey.experience.q3') }} <textarea v-model="state.experienceSurvey.streamingParticipation" required :placeholder="t('common.placeholders.answer')" /></label>
        <label>{{ t('survey.experience.q4') }} <textarea v-model="state.experienceSurvey.desiredGoods" required :placeholder="t('common.placeholders.answer')" /></label>
        <label>{{ t('survey.experience.q5') }} <textarea v-model="state.experienceSurvey.feedback" required :placeholder="t('common.placeholders.answer')" /></label>
      </div>
      <button :disabled="state.loading" @click="submitExperienceSurvey">{{ t('survey.submitExperience') }}</button>
    </section>

    <section v-if="isExperienceZoneSurveySuccessPage" class="split-page">
      <div class="split-page-top"></div>
      <div class="split-page-body">
        <div class="card idea-pane"><div class="idea-form"><h2>{{ t('survey.experienceSuccessTitle') }}</h2><p>{{ t('survey.successDesc') }}</p></div></div>
        <div class="card idea-pane"><div class="idea-poster"><img :src="ideaPosterUrl" alt="Experience Survey Success" /></div></div>
      </div>
    </section>

    <section v-if="isProjectParticipantPage" class="split-page">
      <div class="split-page-top"><h2>{{ t('projectParticipant.title') }}</h2></div>
      <div class="split-page-body">
        <div class="card idea-pane"><div class="idea-form"><a class="route-button" :href="pageHref('/ax-space')">{{ t('projectParticipant.axSpace') }}</a></div></div>
        <div class="card idea-pane"><div class="idea-form"><a class="route-button" :href="pageHref('/k-art-ax')">{{ t('projectParticipant.kArtAx') }}</a></div></div>
      </div>
    </section>

    <section v-if="isStreamingRecruitPage" class="split-page">
      <div class="split-page-top"><h2>{{ t('streamingRecruit.title') }}</h2></div>
      <div class="split-page-body">
        <div class="card idea-pane"><div class="idea-form"><a class="route-button" :href="pageHref('/ax-shop-shop')">{{ t('streamingRecruit.axShopShop') }}</a></div></div>
        <div class="card idea-pane"><div class="idea-form"><a class="route-button" :href="pageHref('/pd-recruit')">{{ t('streamingRecruit.pdRecruit') }}</a></div></div>
      </div>
    </section>

    <section v-if="isLocationPage" class="location-page">
      <div class="location-header">
        <h2>{{ t('location.title') }}</h2>
        <p>{{ t('location.description') }}</p>
      </div>

      <div class="location-address-panel">
        <span class="location-section-title">{{ t('location.addressLabel') }}</span>
        <strong>{{ locationAddress }}</strong>
        <button type="button" @click="copyLocationAddress">{{ t('location.copyAddress') }}</button>
      </div>

      <div class="location-actions">
        <a class="route-button" :href="kakaoDirectionsUrl" target="_blank" rel="noreferrer">{{ t('location.kakaoDirections') }}</a>
        <a class="route-button" :href="naverDirectionsUrl" target="_blank" rel="noreferrer">{{ t('location.naverDirections') }}</a>
      </div>

      <div class="location-info-grid">
        <article class="location-info-card">
          <h3>{{ t('location.publicTransitTitle') }}</h3>
          <ul>
            <li>{{ t('location.publicTransitStop') }}</li>
            <li>{{ t('location.publicTransitStation') }}</li>
          </ul>
        </article>
        <article class="location-info-card">
          <h3>{{ t('location.carTitle') }}</h3>
          <ul>
            <li>{{ t('location.carNavigation') }}</li>
            <li>{{ t('location.carParking') }}</li>
          </ul>
        </article>
        <article class="location-info-card location-info-card-wide">
          <h3>{{ t('location.contactTitle') }}</h3>
          <p>{{ t('location.contactPhone') }}</p>
        </article>
      </div>
    </section>

    <section v-if="isAxSpacePage" class="card">
      <h2>{{ t('project.axSpaceTitle') }}</h2>
      <div class="grid">
        <label>{{ t('project.reservationDate') }} <input v-model="state.axSpace.date" type="date" readonly :placeholder="t('common.placeholders.date')" /></label>
        <label>{{ t('project.reservationTime') }} <input :value="`${state.axSpace.hour}:00`" type="text" readonly :placeholder="t('common.placeholders.time')" /></label>
        <label>{{ t('project.phone') }} <input v-model="state.axSpace.phoneNumber" type="text" :placeholder="t('common.placeholders.phone')" @input="onAxSpacePhoneInput" /></label>
      </div>
      <button :disabled="state.loading" @click="submitProjectRecruitment('ax-space', state.axSpace.phoneNumber, state.axSpace.date, state.axSpace.hour)">{{ t('project.submit') }}</button>
    </section>

    <section v-if="isKartAxPage" class="card">
      <h2>{{ t('project.kArtAxTitle') }}</h2>
      <div class="grid">
        <label>{{ t('project.reservationDate') }} <input v-model="state.kArtAx.date" type="date" readonly :placeholder="t('common.placeholders.date')" /></label>
        <label>{{ t('project.reservationTime') }} <input :value="`${state.kArtAx.hour}:00`" type="text" readonly :placeholder="t('common.placeholders.time')" /></label>
        <label>{{ t('project.phone') }} <input v-model="state.kArtAx.phoneNumber" type="text" :placeholder="t('common.placeholders.phone')" @input="onKArtAxPhoneInput" /></label>
      </div>
      <button :disabled="state.loading" @click="submitProjectRecruitment('k-art-ax', state.kArtAx.phoneNumber, state.kArtAx.date, state.kArtAx.hour)">{{ t('project.submit') }}</button>
    </section>

    <section v-if="isAxShopShopPage" class="card">
      <h2>{{ t('project.axShopShopTitle') }}</h2>
      <div class="grid">
        <label>{{ t('project.reservationDate') }} <input v-model="state.axShopShop.date" type="date" readonly :placeholder="t('common.placeholders.date')" /></label>
        <label>{{ t('project.reservationTime') }} <input :value="`${state.axShopShop.hour}:00`" type="text" readonly :placeholder="t('common.placeholders.time')" /></label>
        <label>{{ t('project.phone') }}{{ t('common.optionalSuffix') }} <input v-model="state.axShopShop.phoneNumber" type="text" :placeholder="t('common.placeholders.phone')" @input="onAxShopShopPhoneInput" /></label>
      </div>
      <button :disabled="state.loading" @click="submitProjectRecruitment('ax-shop-shop', state.axShopShop.phoneNumber, state.axShopShop.date, state.axShopShop.hour)">{{ t('project.submit') }}</button>
    </section>

    <section v-if="isPdRecruitPage" class="card">
      <h2>{{ t('project.pdRecruitTitle') }}</h2>
      <div class="grid">
        <label>{{ t('project.reservationDate') }} <input v-model="state.pdRecruit.date" type="date" readonly :placeholder="t('common.placeholders.date')" /></label>
        <label>{{ t('project.reservationTime') }} <input :value="`${state.pdRecruit.hour}:00`" type="text" readonly :placeholder="t('common.placeholders.time')" /></label>
        <label>{{ t('project.phone') }} <input v-model="state.pdRecruit.phoneNumber" type="text" :placeholder="t('common.placeholders.phone')" @input="onPdRecruitPhoneInput" /></label>
      </div>
      <button :disabled="state.loading" @click="submitProjectRecruitment('pd-writer-edit-sound', state.pdRecruit.phoneNumber, state.pdRecruit.date, state.pdRecruit.hour)">{{ t('project.submit') }}</button>
    </section>

    <section v-if="isIdentifierCodeReissuePage" class="card">
      <h2>{{ t('identifierReissue.title') }}</h2>
      <p>{{ t('identifierReissue.description') }}</p>
      <div class="actions">
        <button :disabled="state.loading" @click="reissueIdentifierCode">{{ t('identifierReissue.submit') }}</button>
      </div>
      <p v-if="state.identifierReissue.message" class="ok">{{ state.identifierReissue.message }}</p>
    </section>

    <section v-if="isEmailLoginPage" class="card">
      <h2>{{ t('auth.emailLogin') }}</h2>
      <div class="grid">
        <label>{{ t('auth.email') }} <input v-model="state.emailLogin.email" type="email" :placeholder="t('common.placeholders.email')" /></label>
        <label>{{ t('auth.identifierCode') }} <input v-model="state.emailLogin.code" type="text" maxlength="6" :placeholder="t('common.placeholders.identifierCode')" /></label>
      </div>
      <div class="actions">
        <button :disabled="state.loading" @click="loginEmailWithIdentifier">{{ t('auth.loginWithIdentifier') }}</button>
      </div>
    </section>

    <section v-if="isAdminDashboardPage && state.adminAccess === true" class="card admin-dashboard">
      <h2>{{ t('admin.dashboardTitle') }}</h2>
      <div class="actions">
        <a class="route-button" :href="pageHref('/admin/reservations')">{{ t('admin.goToReservations') }}</a>
        <a class="route-button" :href="pageHref('/admin/signatures')">{{ t('admin.goToSignatures') }}</a>
      </div>
    </section>

    <section v-if="(isAdminDashboardPage || isAdminReservationsPage || isAdminSignaturesPage) && state.adminAccess === false" class="card">
      <h2>{{ t('common.error') }}</h2>
      <p class="error">{{ t('admin.accessDenied') }}</p>
    </section>

    <section v-if="isAdminReservationsPage && state.adminAccess === true" class="card admin-reservations">
      <h2>{{ t('adminReservations.title') }}</h2>
      <div class="grid">
        <label>{{ t('adminReservations.type') }}
          <select v-model="state.adminReservations.selectedType">
            <option value="">{{ t('adminReservations.allTypes') }}</option>
            <option v-for="type in state.adminReservations.types" :key="type.type" :value="type.type">
              {{ type.label }} ({{ type.count }})
            </option>
          </select>
        </label>
        <label>{{ t('adminReservations.date') }}
          <input v-model="state.adminReservations.date" type="date" :placeholder="t('common.placeholders.date')" />
        </label>
        <label>{{ t('adminReservations.time') }}
          <input v-model="state.adminReservations.time" type="time" step="3600" :placeholder="t('common.placeholders.time')" />
        </label>
        <label>{{ t('adminReservations.projectKey') }}
          <input v-model="state.adminReservations.projectKey" type="text" :placeholder="t('adminReservations.projectKeyPlaceholder')" />
        </label>
      </div>
      <div class="actions">
        <button :disabled="state.loading" @click="fetchAdminReservations">{{ t('adminReservations.search') }}</button>
      </div>
      <div class="admin-table-wrap">
        <table class="admin-table">
          <thead>
            <tr>
              <th>{{ t('adminReservations.type') }}</th>
              <th>{{ t('adminReservations.id') }}</th>
              <th>{{ t('adminReservations.name') }}</th>
              <th>{{ t('adminReservations.phone') }}</th>
              <th>{{ t('adminReservations.email') }}</th>
              <th>{{ t('adminReservations.projectKey') }}</th>
              <th>{{ t('adminReservations.reservationDate') }}</th>
              <th>{{ t('adminReservations.reservationTime') }}</th>
              <th>{{ t('adminReservations.amount') }}</th>
              <th>{{ t('adminReservations.payment') }}</th>
              <th>{{ t('adminReservations.provider') }}</th>
              <th>{{ t('adminReservations.account') }}</th>
              <th>{{ t('adminReservations.createdAt') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in state.adminReservations.items" :key="`${item.type}-${item.id}`">
              <td>{{ item.type }}</td>
              <td>{{ item.id || '-' }}</td>
              <td>{{ item.name || '-' }}</td>
              <td>{{ item.phoneNumber || '-' }}</td>
              <td>{{ item.userEmail || '-' }}</td>
              <td>{{ item.projectKey || '-' }}</td>
              <td>{{ item.date || '-' }}</td>
              <td>{{ item.time || '-' }}</td>
              <td>{{ item.amount || '-' }}</td>
              <td>{{ item.paymentMethodType || '-' }}</td>
              <td>{{ item.paymentProviderName || '-' }}</td>
              <td>{{ item.bankAccountMasked || '-' }}</td>
              <td>{{ item.createdAt || '-' }}</td>
            </tr>
            <tr v-if="state.adminReservations.items.length === 0">
              <td colspan="13">{{ t('adminReservations.empty') }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <section v-if="isAdminSignaturesPage && state.adminAccess === true" class="card admin-reservations">
      <h2>{{ t('adminSignatures.title') }}</h2>
      <div class="actions">
        <button :disabled="state.loading" @click="fetchAdminSignatures">{{ t('adminSignatures.refresh') }}</button>
      </div>
      <div class="admin-table-wrap">
        <table class="admin-table">
          <thead>
            <tr>
              <th>{{ t('adminSignatures.id') }}</th>
              <th>{{ t('adminSignatures.userId') }}</th>
              <th>{{ t('adminSignatures.originalName') }}</th>
              <th>{{ t('adminSignatures.recognizedText') }}</th>
              <th>{{ t('adminSignatures.englishName') }}</th>
              <th>{{ t('adminSignatures.koreanName') }}</th>
              <th>{{ t('adminSignatures.koreanMeaningText') }}</th>
              <th>{{ t('adminSignatures.nameLanguage') }}</th>
              <th>{{ t('adminSignatures.detectedLanguage') }}</th>
              <th>{{ t('adminSignatures.conversionSource') }}</th>
              <th>{{ t('adminSignatures.ocrStatus') }}</th>
              <th>{{ t('adminSignatures.ocrConfidence') }}</th>
              <th>{{ t('adminSignatures.ocrProcessedAt') }}</th>
              <th>{{ t('adminSignatures.updatedAt') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in state.adminSignatures.items" :key="item.id">
              <td>{{ item.id || '-' }}</td>
              <td>{{ item.userId || '-' }}</td>
              <td>{{ item.originalName || '-' }}</td>
              <td>{{ item.recognizedText || '-' }}</td>
              <td>{{ item.englishName || '-' }}</td>
              <td>{{ item.koreanName || '-' }}</td>
              <td>{{ item.koreanMeaningText || '-' }}</td>
              <td>{{ item.nameLanguage || '-' }}</td>
              <td>{{ item.detectedLanguage || '-' }}</td>
              <td>{{ item.nameConversionSource || '-' }}</td>
              <td>{{ item.ocrStatus || '-' }}</td>
              <td>{{ item.ocrConfidence ?? '-' }}</td>
              <td>{{ item.ocrProcessedAt || '-' }}</td>
              <td>{{ item.updatedAt || '-' }}</td>
            </tr>
            <tr v-if="state.adminSignatures.items.length === 0">
              <td colspan="14">{{ t('adminSignatures.empty') }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <section v-if="isEmailSignupPage" class="card">
      <h2>{{ t('auth.emailSignup') }}</h2>
      <div class="grid">
        <label>{{ t('auth.email') }} <input v-model="state.emailLogin.email" type="email" :placeholder="t('common.placeholders.email')" /></label>
        <label>{{ t('auth.identifierCode') }} <input v-model="state.emailLogin.code" type="text" maxlength="6" :placeholder="t('common.placeholders.identifierCode')" /></label>
      </div>
      <div class="actions">
        <button :disabled="state.loading" @click="doEmailLogin">{{ t('auth.sendCode') }}</button>
        <button :disabled="state.loading" @click="verifyEmailLoginCode">{{ t('auth.verifyCodeAndSignup') }}</button>
      </div>
    </section>

    <section v-if="isOauthCallbackPage" class="card">
      <h2>{{ t('auth.callbackTitle') }}</h2>
      <p>{{ t('auth.callbackDesc') }}</p>
    </section>

    <section v-if="isCertificateDownloadPage" class="card certificate-download-page">
      <h2>{{ t('certificateDownload.title') }}</h2>
      <p>{{ t('certificateDownload.description') }}</p>
      <label class="field">
        <span>{{ t('certificateDownload.identifierCode') }}</span>
        <input
          v-model="state.certificateDownloadCode"
          type="text"
          maxlength="6"
          inputmode="numeric"
          :placeholder="t('certificateDownload.placeholder')"
        />
      </label>
      <div class="actions">
        <button :disabled="state.loading" @click="downloadStoredSignatureImage">{{ t('certificateDownload.signatureImage') }}</button>
        <button :disabled="state.loading" @click="downloadStoredCertificatePdf">{{ t('certificateDownload.certificatePdf') }}</button>
      </div>
    </section>

    <section v-if="isTabletPage" class="card">
      <h2>{{ t('tablet.verifyTitle') }}</h2>
      <input v-model="state.verifyCode" type="text" maxlength="6" :placeholder="t('tablet.verifyPlaceholder')" />
      <button :disabled="state.loading" @click="verifyOnTablet">{{ t('tablet.verify') }}</button>

      <h2>{{ t('tablet.signatureTitle') }}</h2>
      <label class="field">
        <span>{{ t('tablet.nameLanguage') }}</span>
        <select v-model="state.signatureNameLanguage">
          <option value="EN">{{ t('tablet.languageOptions.en') }}</option>
          <option value="FR">{{ t('tablet.languageOptions.fr') }}</option>
          <option value="DE">{{ t('tablet.languageOptions.de') }}</option>
          <option value="JA">{{ t('tablet.languageOptions.ja') }}</option>
          <option value="ZH">{{ t('tablet.languageOptions.zh') }}</option>
          <option value="VI">{{ t('tablet.languageOptions.vi') }}</option>
          <option value="ES">{{ t('tablet.languageOptions.es') }}</option>
          <option value="IT">{{ t('tablet.languageOptions.it') }}</option>
          <option value="OTHER">{{ t('tablet.languageOptions.other') }}</option>
        </select>
      </label>
      <input
        v-model="state.signatureKoreanName"
        type="text"
        :placeholder="t('tablet.koreanNamePlaceholder')"
      />
      <canvas
        ref="canvasRef"
        class="signature"
        width="600"
        height="220"
        @pointerdown="startDraw"
        @pointermove="draw"
        @pointerup="endDraw"
        @pointerleave="endDraw"
      />
      <div class="actions">
        <button :disabled="state.loading" @click="clearSignature">{{ t('tablet.clear') }}</button>
        <button :disabled="state.loading || !state.verifiedToken" @click="submitSignature">{{ t('tablet.save') }}</button>
      </div>
      <div class="actions">
        <button :disabled="state.loading || !state.verifiedToken" @click="previewSignatureOnTablet">OCR 확인</button>
        <button :disabled="state.loading || !state.verifiedToken || !state.signaturePreview.token" @click="confirmPreviewSignatureOnTablet">확인 후 저장</button>
      </div>
      <div v-if="state.signaturePreview.token" class="card">
        <h3>OCR 결과 확인</h3>
        <p><strong>OCR 결과:</strong> {{ state.signaturePreview.recognizedText || '-' }}</p>
        <p><strong>영어 이름:</strong> {{ state.signaturePreview.englishName || '-' }}</p>
        <p><strong>한글 이름:</strong> {{ state.signaturePreview.koreanName || '-' }}</p>
        <p><strong>의미 번역:</strong> {{ state.signaturePreview.koreanMeaningText || '-' }}</p>
        <p><strong>감지 언어:</strong> {{ state.signaturePreview.detectedLanguage || '-' }}</p>
        <p><strong>신뢰도:</strong> {{ state.signaturePreview.ocrConfidence ?? '-' }}</p>
      </div>
    </section>

    <p v-if="state.message" class="ok">{{ state.message }}</p>
    <p v-if="state.error" class="error">{{ state.error }}</p>
  </main>
</template>




