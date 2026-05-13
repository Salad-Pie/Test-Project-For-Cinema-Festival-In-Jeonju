<script setup>
import { reactive, inject } from 'vue'
import { apiRoot } from '../config/api'
import { combineAddress, formatPhoneInput } from '../utils/format'
import { parseErrorResponse } from '../api/client'

const { t, state: globalState, setSafeError, pageHref, openAddressSearch, userError } = inject('appContext')

const exhibitionSurveyState = reactive({
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
})

function onExhibitionSurveyPhoneInput(event) {
  exhibitionSurveyState.phoneNumber = formatPhoneInput(event)
}

async function submitExhibitionSurvey() {
  globalState.loading = true
  globalState.error = ''
  try {
    const requiredFields = [
      exhibitionSurveyState.phoneNumber,
      exhibitionSurveyState.name,
      combineAddress(exhibitionSurveyState.address, exhibitionSurveyState.addressDetail),
      exhibitionSurveyState.impressivePoint,
      exhibitionSurveyState.improvementNeeded,
      exhibitionSurveyState.desiredGenre,
      exhibitionSurveyState.invitedArtist,
      exhibitionSurveyState.feedback,
    ]
    if (requiredFields.some((value) => !String(value || '').trim())) {
      throw userError(t('common.requiredAll'))
    }
    if (
      String(exhibitionSurveyState.identifierCode || '').trim() &&
      !/^\d{6}$/.test(String(exhibitionSurveyState.identifierCode).trim())
    ) {
      throw userError(t('survey.identifierOptionalRule'))
    }
    const payload = { ...exhibitionSurveyState }
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
    globalState.loading = false
  }
}
</script>

<template>
  <section class="card">
    <h2>{{ t('survey.exhibitionTitle') }}</h2>
    <div class="survey-stack">
      <label>{{ t('survey.nameRequired') }} <input v-model="exhibitionSurveyState.name" type="text" required :placeholder="t('common.placeholders.name')" /></label>
      <label>{{ t('survey.phoneRequired') }} <input v-model="exhibitionSurveyState.phoneNumber" type="text" required :placeholder="t('common.placeholders.phone')" @input="onExhibitionSurveyPhoneInput" /></label>
      <label>{{ t('survey.addressRequired') }}
        <div class="address-field">
          <div class="address-search-row">
            <input v-model="exhibitionSurveyState.address" type="text" readonly required :placeholder="t('common.baseAddress')" />
            <button type="button" :disabled="globalState.loading" @click="openAddressSearch(exhibitionSurveyState)">{{ t('common.searchAddress') }}</button>
          </div>
          <input v-model="exhibitionSurveyState.addressDetail" type="text" :placeholder="t('common.detailAddress')" />
        </div>
      </label>
      <label>{{ t('survey.identifierOptional') }} <input v-model="exhibitionSurveyState.identifierCode" type="text" maxlength="6" :placeholder="t('common.placeholders.identifierCode')" /></label>
    </div>
    <div class="survey-stack">
      <label>{{ t('survey.exhibition.q1') }} <textarea v-model="exhibitionSurveyState.impressivePoint" required :placeholder="t('common.placeholders.answer')" /></label>
      <label>{{ t('survey.exhibition.q2') }} <textarea v-model="exhibitionSurveyState.improvementNeeded" required :placeholder="t('common.placeholders.answer')" /></label>
      <label>{{ t('survey.exhibition.q3') }} <textarea v-model="exhibitionSurveyState.desiredGenre" required :placeholder="t('common.placeholders.answer')" /></label>
      <label>{{ t('survey.exhibition.q4') }} <textarea v-model="exhibitionSurveyState.invitedArtist" required :placeholder="t('common.placeholders.answer')" /></label>
      <label>{{ t('survey.exhibition.q5') }} <textarea v-model="exhibitionSurveyState.feedback" required :placeholder="t('common.placeholders.answer')" /></label>
    </div>
    <button :disabled="globalState.loading" @click="submitExhibitionSurvey">{{ t('survey.submitExhibition') }}</button>
  </section>
</template>
