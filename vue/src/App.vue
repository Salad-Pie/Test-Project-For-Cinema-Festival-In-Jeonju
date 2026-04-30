<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
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

const isHome = computed(() => isOriginalRoute && (routePath === '/' || routePath === '/index.html'))
const isBootstrapHome = computed(() => !isOriginalRoute && (routePath === '/' || routePath === '/index.html'))
const isEmailLoginPage = computed(() => routePath.startsWith('/login/email'))
const isEmailSignupPage = computed(() => routePath.startsWith('/signup/email'))
const isOauthCallbackPage = computed(() => routePath.startsWith('/oauth/callback'))
const isTabletPage = computed(() => routePath.startsWith('/tablet'))
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
const bootstrapPages = [
  { key: 'nav.loginPage', href: '/login-page' },
  { key: 'nav.ideaContest', href: '/idea-contest' },
  { key: 'nav.sponsorshipApplication', href: '/sponsorship-application' },
  { key: 'nav.streetCollaboration', href: '/street-collaboration' },
  { key: 'nav.exhibitionArtistMeeting', href: '/artist-meet' },
  { key: 'nav.exhibitionSurvey', href: '/exhibition-survey' },
  { key: 'nav.experienceZoneSurvey', href: '/experience-zone-survey' },
  { key: 'nav.projectParticipant', href: '/project-participant' },
  { key: 'nav.streamingRecruit', href: '/streaming-recruit' },
  { key: 'nav.location', href: '/location' },
]
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
const locationAddress = '전북특별자치도 전주시 완산구 전주객사3길 67'
const kakaoDirectionsUrl = `https://map.kakao.com/link/search/${encodeURIComponent(locationAddress)}`
const naverDirectionsUrl = `https://map.naver.com/p/search/${encodeURIComponent(locationAddress)}`
const ideaPosterUrl = 'data:image/png;base64,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'

const q = new URLSearchParams(window.location.search)
const callbackCode = q.get('code') || ''
const callbackState = q.get('state') || ''
const loginRedirectPath = q.get('redirect') || ''
const localeStorageKey = 'zdo.locale'

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
  mapError: '',
  verifyCode: '',
  verifiedToken: '',
})

const tabletToken = computed(() => new URLSearchParams(window.location.search).get('token') || '')
const canvasRef = ref(null)
const drawing = ref(false)
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

onMounted(() => {
  try {
    const savedLocale = localStorage.getItem(localeStorageKey)
    if (savedLocale && ['ko', 'en', 'zh', 'ja'].includes(savedLocale)) {
      locale.value = savedLocale
    }
  } catch (_) {}

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

  if (loginRedirectPath) {
    setGlobalRedirectPath(loginRedirectPath)
  }

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
    state.message = t('idea.submitted', { id: json.id, count: json.images?.length ?? 0 })
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

    const json = await res.json()
    state.message = t('sponsorship.submitted', { id: json.id })
    window.location.href = pageHref('/sponsorship-thanks')
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

    const json = await res.json()
    state.message = t('street.submitted', { id: json.id })
    await fetchStreetAvailability()
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

    const json = await res.json()
    state.message = t('artistMeeting.submitted', { id: json.id })
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
    state.message = t('common.applicationCompleted')
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
    }
    const authBase = shouldUseIdeaContestAuthApi() ? ideaAuthApiBase : apiBase
    const result = await apiFetchWithBase(authBase, '/oauth/exchange', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
    state.loginResult = result
    saveIdeaContestLogin(result.userId)
    saveIdeaContestAuthToken(result.registerToken)
    const redirectPath = getGlobalRedirectPath() || getRedirectPathFromOAuthState()
    if (redirectPath) {
      setGlobalRedirectPath(redirectPath)
      clearGlobalRedirectPath()
      window.location.href = redirectPath
    }
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
      body: JSON.stringify({ email: state.emailLogin.email }),
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
    const redirectPath = getGlobalRedirectPath()
    if (redirectPath) {
      clearGlobalRedirectPath()
      window.location.href = redirectPath
    }
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
    const redirectPath = getGlobalRedirectPath()
    if (redirectPath) {
      clearGlobalRedirectPath()
      window.location.href = redirectPath
    }
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

function goEmailLoginPage() {
  window.location.href = emailLoginHref.value
}

async function verifyOnTablet() {
  state.loading = true
  state.error = ''
  try {
    const result = await apiFetch('/verify', {
      method: 'POST',
      body: JSON.stringify({ token: tabletToken.value, code: state.verifyCode }),
    })
    state.verifiedToken = result.verifiedToken
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
}

async function submitSignature() {
  const canvas = canvasRef.value
  if (!canvas) return

  state.loading = true
  state.error = ''
  try {
    const signatureDataUrl = canvas.toDataURL('image/png')

    await apiFetch('/signature', {
      method: 'POST',
      headers: { Authorization: `Bearer ${state.verifiedToken}` },
      body: JSON.stringify({ signatureDataUrl }),
    })
    state.message = t('tablet.signatureSaved')
  } catch (e) {
    setSafeError(e)
  } finally {
    state.loading = false
  }
}
</script>

<template>
  <main :class="['container', { 'bootstrap-mode': isBootstrapRoute }]">
    <div class="actions" style="justify-content: flex-end; margin-bottom: 8px">
      <label class="locale-select-label">
        <select :value="locale" aria-label="Language Select" @change="setLocale($event.target.value)">
          <option value="ko">{{ t('lang.ko') }}</option>
          <option value="en">{{ t('lang.en') }}</option>
          <option value="zh">{{ t('lang.zh') }}</option>
          <option value="ja">{{ t('lang.ja') }}</option>
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
              <h1 class="h2 fw-bold mb-2">폐영화관 재생프로젝트</h1>
              <p class="text-secondary mb-0">AX 융복합 서예 전시</p>
            </div>
            <div class="row g-3">
              <div v-for="page in bootstrapPages" :key="page.href" class="col-12 col-md-6 col-lg-4">
                <a class="btn btn-light border border-info-subtle shadow-sm w-100 py-3 fw-semibold text-info-emphasis bootstrap-hub-link" :href="page.href">
                  {{ t(page.key) }}
                </a>
              </div>
            </div>
          </div>
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
          <img :src="ideaPosterUrl" alt="Poster Placeholder" />
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
        <label>{{ t('project.phone') }} <input v-model="state.axShopShop.phoneNumber" type="text" :placeholder="t('common.placeholders.phone')" @input="onAxShopShopPhoneInput" /></label>
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

    <section v-if="isTabletPage" class="card">
      <h2>{{ t('tablet.verifyTitle') }}</h2>
      <input v-model="state.verifyCode" type="text" maxlength="6" :placeholder="t('tablet.verifyPlaceholder')" />
      <button :disabled="state.loading || !tabletToken" @click="verifyOnTablet">{{ t('tablet.verify') }}</button>

      <h2>{{ t('tablet.signatureTitle') }}</h2>
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
    </section>

    <p v-if="state.message" class="ok">{{ state.message }}</p>
    <p v-if="state.error" class="error">{{ state.error }}</p>
  </main>
</template>




