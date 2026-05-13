<script setup>
import { inject, reactive } from 'vue'
import { apiBase, ideaAuthApiBase } from '../config/api'
import { apiFetchWithBase } from '../api/client'
import { saveIdeaContestLogin, saveIdeaContestAuthToken, getGlobalRedirectPath, clearGlobalRedirectPath } from '../utils/authStorage'
import { parseJwtPayload } from '../utils/jwt'

const { t, locale, state: globalState, setSafeError, pageHref } = inject('appContext')

const emailLoginState = reactive({
  email: '',
  code: ''
})

function shouldUseIdeaContestAuthApi() {
  return true
}

function getRedirectPathFromOAuthState() {
  const q = new URLSearchParams(window.location.search)
  const callbackState = q.get('state') || ''
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

async function doEmailLogin() {
  globalState.loading = true
  globalState.error = ''
  try {
    const authBase = shouldUseIdeaContestAuthApi() ? ideaAuthApiBase : apiBase
    await apiFetchWithBase(authBase, '/login/email/send-code', {
      method: 'POST',
      body: JSON.stringify({ email: emailLoginState.email, language: locale.value }),
    })
    globalState.message = t('auth.codeSentToEmail')
  } catch (e) {
    setSafeError(e)
  } finally {
    globalState.loading = false
  }
}

async function verifyEmailLoginCode() {
  globalState.loading = true
  globalState.error = ''
  try {
    const authBase = shouldUseIdeaContestAuthApi() ? ideaAuthApiBase : apiBase
    const result = await apiFetchWithBase(authBase, '/login/email/verify-code', {
      method: 'POST',
      body: JSON.stringify({ email: emailLoginState.email, code: emailLoginState.code }),
    })
    globalState.loginResult = result
    saveIdeaContestLogin(result.userId)
    saveIdeaContestAuthToken(result.registerToken)
    globalState.message = t('auth.emailSignupSuccess')
    setTimeout(() => {
      window.location.href = pageHref('/login/email')
    }, 2000)
  } catch (e) {
    setSafeError(e)
  } finally {
    globalState.loading = false
  }
}
</script>

<template>
  <section class="card">
    <h2>{{ t('auth.emailSignup') }}</h2>
    <div class="grid">
      <label>{{ t('auth.email') }} <input v-model="emailLoginState.email" type="email" :placeholder="t('common.placeholders.email')" /></label>
      <label>{{ t('auth.identifierCode') }} <input v-model="emailLoginState.code" type="text" maxlength="6" :placeholder="t('common.placeholders.identifierCode')" /></label>
    </div>
    <div class="actions">
      <button :disabled="globalState.loading" @click="doEmailLogin">{{ t('auth.sendCode') }}</button>
      <button :disabled="globalState.loading" @click="verifyEmailLoginCode">{{ t('auth.verifyCodeAndSignup') }}</button>
    </div>
  </section>
</template>
