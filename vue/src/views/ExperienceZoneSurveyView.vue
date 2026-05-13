<script setup>
import { reactive, inject } from 'vue'
import { apiRoot } from '../config/api'
import { combineAddress, formatPhoneInput } from '../utils/format'
import { parseErrorResponse } from '../api/client'

const { t, state: globalState, setSafeError, pageHref, openAddressSearch, userError } = inject('appContext')

const experienceSurveyState = reactive({
  name: '',
  phoneNumber: '',
  address: '',
  addressDetail: '',
  impressiveSpace: '',
  improvementIdeaSpace: '',
  streamingParticipation: '',
  desiredGoods: '',
  feedback: '',
})

function onExperienceSurveyPhoneInput(event) {
  experienceSurveyState.phoneNumber = formatPhoneInput(event)
}

async function submitExperienceSurvey() {
  globalState.loading = true
  globalState.error = ''
  try {
    const requiredFields = [
      experienceSurveyState.phoneNumber,
      experienceSurveyState.name,
      combineAddress(experienceSurveyState.address, experienceSurveyState.addressDetail),
      experienceSurveyState.impressiveSpace,
      experienceSurveyState.improvementIdeaSpace,
      experienceSurveyState.streamingParticipation,
      experienceSurveyState.desiredGoods,
      experienceSurveyState.feedback,
    ]
    if (requiredFields.some((value) => !String(value || '').trim())) {
      throw userError(t('common.requiredAll'))
    }
    const payload = { ...experienceSurveyState }
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
    globalState.loading = false
  }
}
</script>

<template>
  <section class="card">
    <h2>{{ t('survey.experienceTitle') }}</h2>
    <div class="survey-stack">
      <label>{{ t('survey.nameRequired') }} <input v-model="experienceSurveyState.name" type="text" required :placeholder="t('common.placeholders.name')" /></label>
      <label>{{ t('survey.phoneRequired') }} <input v-model="experienceSurveyState.phoneNumber" type="text" required :placeholder="t('common.placeholders.phone')" @input="onExperienceSurveyPhoneInput" /></label>
      <label>{{ t('survey.addressRequired') }}
        <div class="address-field">
          <div class="address-search-row">
            <input v-model="experienceSurveyState.address" type="text" readonly required :placeholder="t('common.baseAddress')" />
            <button type="button" :disabled="globalState.loading" @click="openAddressSearch(experienceSurveyState)">{{ t('common.searchAddress') }}</button>
          </div>
          <input v-model="experienceSurveyState.addressDetail" type="text" :placeholder="t('common.detailAddress')" />
        </div>
      </label>
    </div>
    <div class="survey-stack">
      <label>{{ t('survey.experience.q1') }} <textarea v-model="experienceSurveyState.impressiveSpace" required :placeholder="t('common.placeholders.answer')" /></label>
      <label>{{ t('survey.experience.q2') }} <textarea v-model="experienceSurveyState.improvementIdeaSpace" required :placeholder="t('common.placeholders.answer')" /></label>
      <label>{{ t('survey.experience.q3') }} <textarea v-model="experienceSurveyState.streamingParticipation" required :placeholder="t('common.placeholders.answer')" /></label>
      <label>{{ t('survey.experience.q4') }} <textarea v-model="experienceSurveyState.desiredGoods" required :placeholder="t('common.placeholders.answer')" /></label>
      <label>{{ t('survey.experience.q5') }} <textarea v-model="experienceSurveyState.feedback" required :placeholder="t('common.placeholders.answer')" /></label>
    </div>
    <button :disabled="globalState.loading" @click="submitExperienceSurvey">{{ t('survey.submitExperience') }}</button>
  </section>
</template>
