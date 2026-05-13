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
  return true // Since this is the email login page, we should use ideaAuthApiBase as per original logic.
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

async function loginEmailWithIdentifier() {
  globalState.loading = true
  globalState.error = ''
  try {
    const authBase = shouldUseIdeaContestAuthApi() ? ideaAuthApiBase : apiBase
    const result = await apiFetchWithBase(authBase, '/login/email/identifier', {
      method: 'POST',
      body: JSON.stringify({ email: emailLoginState.email, code: emailLoginState.code }),
    })
    globalState.loginResult = result
    saveIdeaContestLogin(result.userId)
    saveIdeaContestAuthToken(result.registerToken)
    globalState.message = t('auth.emailLoginSuccess')
    redirectAfterLogin()
  } catch (e) {
    setSafeError(e)
  } finally {
    globalState.loading = false
  }
}
</script>

<template>
  <section class="card">
    <h2>{{ t('auth.emailLogin') }}</h2>
    <div class="grid">
      <label>{{ t('auth.email') }} <input v-model="emailLoginState.email" type="email" :placeholder="t('common.placeholders.email')" /></label>
      <label>{{ t('auth.identifierCode') }} <input v-model="emailLoginState.code" type="text" maxlength="6" :placeholder="t('common.placeholders.identifierCode')" /></label>
    </div>
    <div class="actions">
      <button :disabled="globalState.loading" @click="loginEmailWithIdentifier">{{ t('auth.loginWithIdentifier') }}</button>
    </div>
  </section>
</template>
