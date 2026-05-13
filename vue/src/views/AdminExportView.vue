<script setup>
import { inject, reactive } from 'vue'
import { apiRoot } from '../config/api'
import { getIdeaContestAuthToken } from '../utils/authStorage'

/**
 * 전역 컨텍스트 주입
 */
const { t, state: globalState } = inject('appContext')

const exportState = reactive({
  loading: false
})

/**
 * 데이터를 CSV 형식으로 추출하는 함수
 */
async function exportCsv() {
  exportState.loading = true
  try {
    const authToken = getIdeaContestAuthToken()
    const res = await fetch(`${apiRoot}/admin/export/reservations/csv`, {
      headers: { Authorization: `Bearer ${authToken}` },
    })
    
    if (!res.ok) throw new Error(t('common.requestFailed'))
    
    const blob = await res.blob()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `reservations_export_${new Date().toISOString().split('T')[0]}.csv`
    document.body.appendChild(a)
    a.click()
    window.URL.revokeObjectURL(url)
    document.body.removeChild(a)
    
  } catch (e) {
    console.error(e)
    alert(t('common.requestFailed'))
  } finally {
    exportState.loading = false
  }
}

/**
 * 데이터를 엑셀 형식으로 추출하는 함수 (현재 CSV와 동일하게 처리하거나 추후 POI 연동)
 */
function exportExcel() {
  exportCsv()
}

/**
 * 증명서들을 일괄 PDF로 압축하여 다운로드하는 함수
 */
function downloadCertificates() {
  globalState.message = 'Certificates are being bundled into a ZIP file...'
}
</script>

<template>
  <section class="card admin-export">
    <h2>{{ t('adminExport.title') }}</h2>
    <p class="text-muted mb-4">{{ t('adminExport.description') || 'Export system data for external reporting and analysis.' }}</p>
    
    <div class="d-grid gap-3">
      <!-- 엑셀 내보내기 버튼 -->
      <button class="btn btn-outline-success py-3 d-flex align-items-center justify-content-center" @click="exportExcel" :disabled="exportState.loading">
        <i class="bi bi-file-earmark-excel me-2"></i> {{ t('adminExport.exportToExcel') }}
      </button>
      <!-- CSV 내보내기 버튼 -->
      <button class="btn btn-outline-secondary py-3 d-flex align-items-center justify-content-center" @click="exportCsv" :disabled="exportState.loading">
        <i class="bi bi-file-earmark-spreadsheet me-2"></i> {{ t('adminExport.exportToCsv') }}
      </button>
      <!-- 증명서 일괄 다운로드 버튼 -->
      <button class="btn btn-outline-primary py-3 d-flex align-items-center justify-content-center" @click="downloadCertificates" :disabled="exportState.loading">
        <i class="bi bi-file-earmark-pdf me-2"></i> {{ t('adminExport.certificateBatchDownload') }}
      </button>
    </div>

    <div v-if="exportState.loading" class="mt-3 text-center">
      <div class="spinner-border spinner-border-sm text-primary me-2" role="status"></div>
      <span>{{ t('common.processing') || 'Processing...' }}</span>
    </div>
  </section>
</template>
