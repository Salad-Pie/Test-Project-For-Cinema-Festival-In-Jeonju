<script setup>
import { inject, reactive } from 'vue'
import { apiRoot } from '../config/api'
import { parseErrorResponse } from '../api/client'
import { getIdeaContestAuthToken } from '../utils/authStorage'
import { formatPhoneInput } from '../utils/format'

const { t, state: globalState, setSafeError, userError, goToSuccessPage } = inject('appContext')

const pdRecruitState = reactive({
  phoneNumber: '',
  date: '2026-05-09',
  hour: '19'
})

function onPdRecruitPhoneInput(event) {
  pdRecruitState.phoneNumber = formatPhoneInput(event)
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
    <h2>{{ t('project.pdRecruitTitle') }}</h2>
    <div class="grid">
      <label>{{ t('project.reservationDate') }} <input v-model="pdRecruitState.date" type="date" readonly :placeholder="t('common.placeholders.date')" /></label>
      <label>{{ t('project.reservationTime') }} <input :value="`${pdRecruitState.hour}:00`" type="text" readonly :placeholder="t('common.placeholders.time')" /></label>
      <label>{{ t('project.phone') }} <input v-model="pdRecruitState.phoneNumber" type="text" :placeholder="t('common.placeholders.phone')" @input="onPdRecruitPhoneInput" /></label>
    </div>
    <button :disabled="globalState.loading" @click="submitProjectRecruitment('pd-writer-edit-sound', pdRecruitState.phoneNumber, pdRecruitState.date, pdRecruitState.hour)">{{ t('project.submit') }}</button>
  </section>
</template>
