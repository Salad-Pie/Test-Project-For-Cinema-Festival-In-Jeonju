<script setup>
import { inject, reactive, onMounted } from 'vue'
import { apiRoot } from '../config/api'
import { getIdeaContestAuthToken } from '../utils/authStorage'
import { parseErrorResponse } from '../api/client'

const { t, state: globalState, setSafeError, userError } = inject('appContext')

const adminSignaturesState = reactive({
  items: [],
})

async function fetchAdminSignatures() {
  globalState.loading = true
  globalState.error = ''
  try {
    const authToken = getIdeaContestAuthToken()
    if (!authToken) throw userError(t('common.loginTokenRequired'))
    const res = await fetch(`${apiRoot}/admin/signatures`, {
      headers: { Authorization: `Bearer ${authToken}` },
    })
    if (!res.ok) throw new Error(await parseErrorResponse(res, t('common.requestFailed')))
    adminSignaturesState.items = await res.json()
  } catch (e) {
    setSafeError(e)
  } finally {
    globalState.loading = false
  }
}

onMounted(() => {
  fetchAdminSignatures()
})
</script>

<template>
  <section class="card admin-reservations">
    <h2>{{ t('adminSignatures.title') }}</h2>
    <div class="actions">
      <button :disabled="globalState.loading" @click="fetchAdminSignatures">{{ t('adminSignatures.refresh') }}</button>
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
          <tr v-for="item in adminSignaturesState.items" :key="item.id">
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
          <tr v-if="adminSignaturesState.items.length === 0">
            <td colspan="14">{{ t('adminSignatures.empty') }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
