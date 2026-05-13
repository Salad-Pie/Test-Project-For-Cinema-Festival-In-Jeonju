<script setup>
import { inject, reactive } from 'vue'
import { apiRoot } from '../config/api'
import { parseErrorResponse } from '../api/client'
import { getIdeaContestAuthToken } from '../utils/authStorage'
import { formatPhoneInput } from '../utils/format'

const { t, state: globalState, setSafeError, userError, goToSuccessPage } = inject('appContext')

const axSpaceState = reactive({
  phoneNumber: '',
  date: '2026-05-08',
  hour: '20'
})

function onAxSpacePhoneInput(event) {
  axSpaceState.phoneNumber = formatPhoneInput(event)
}

async function submitProjectRecruitment(projectKey, phoneNumber, date, hour) {
  globalState.loading = true
  globalState.error = ''
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
    globalState.loading = false
  }
}
</script>

<template>
  <section class="card">
    <h2>{{ t('project.axSpaceTitle') }}</h2>
    <div class="grid">
      <label>{{ t('project.reservationDate') }} <input v-model="axSpaceState.date" type="date" readonly :placeholder="t('common.placeholders.date')" /></label>
      <label>{{ t('project.reservationTime') }} <input :value="`${axSpaceState.hour}:00`" type="text" readonly :placeholder="t('common.placeholders.time')" /></label>
      <label>{{ t('project.phone') }} <input v-model="axSpaceState.phoneNumber" type="text" :placeholder="t('common.placeholders.phone')" @input="onAxSpacePhoneInput" /></label>
    </div>
    <button :disabled="globalState.loading" @click="submitProjectRecruitment('ax-space', axSpaceState.phoneNumber, axSpaceState.date, axSpaceState.hour)">{{ t('project.submit') }}</button>
  </section>
</template>
