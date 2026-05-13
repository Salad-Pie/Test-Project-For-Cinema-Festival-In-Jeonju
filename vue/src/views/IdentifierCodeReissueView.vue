<script setup>
import { inject, reactive } from 'vue'
import { getIdeaContestAuthToken } from '../utils/authStorage'

const { t, locale, state: globalState, setSafeError, userError, apiFetch } = inject('appContext')

const identifierReissueState = reactive({
  message: ''
})

async function reissueIdentifierCode() {
  globalState.loading = true
  globalState.error = ''
  identifierReissueState.message = ''
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
    identifierReissueState.message = result.message || t('identifierReissue.sent')
    globalState.message = identifierReissueState.message
  } catch (e) {
    setSafeError(e)
  } finally {
    globalState.loading = false
  }
}
</script>

<template>
  <section class="card">
    <h2>{{ t('identifierReissue.title') }}</h2>
    <p>{{ t('identifierReissue.description') }}</p>
    <div class="actions">
      <button :disabled="globalState.loading" @click="reissueIdentifierCode">{{ t('identifierReissue.submit') }}</button>
    </div>
    <p v-if="identifierReissueState.message" class="ok">{{ identifierReissueState.message }}</p>
  </section>
</template>
