<script setup>
import { inject, reactive, onMounted } from 'vue'
import { apiRoot } from '../config/api'
import { getIdeaContestAuthToken } from '../utils/authStorage'
import { parseErrorResponse } from '../api/client'

/**
 * 전역 컨텍스트 주입
 */
const { t, state: globalState, setSafeError } = inject('appContext')

/**
 * 검수 대기 중인 OCR 데이터를 담는 상태
 */
const reviewState = reactive({
  pendingItems: [],
  loading: false,
  saving: false
})

/**
 * 백엔드로부터 저신뢰도 OCR 목록 로드
 */
async function fetchLowConfidenceItems() {
  reviewState.loading = true
  try {
    const authToken = getIdeaContestAuthToken()
    const res = await fetch(`${apiRoot}/admin/signatures/low-confidence?threshold=0.8`, {
      headers: { Authorization: `Bearer ${authToken}` },
    })
    
    if (!res.ok) throw new Error(await parseErrorResponse(res, t('common.requestFailed')))
    
    reviewState.pendingItems = await res.json()
  } catch (e) {
    setSafeError(e)
  } finally {
    reviewState.loading = false
  }
}

/**
 * OCR 인식 결과를 승인하거나 교정하여 저장
 */
async function updateSignature(id, correctedText, status) {
  reviewState.saving = true
  try {
    const authToken = getIdeaContestAuthToken()
    const res = await fetch(`${apiRoot}/admin/signatures/${id}`, {
      method: 'PATCH',
      headers: { 
        'Content-Type': 'application/json',
        Authorization: `Bearer ${authToken}` 
      },
      body: JSON.stringify({ correctedText, status })
    })
    
    if (!res.ok) throw new Error(await parseErrorResponse(res, t('common.requestFailed')))
    
    globalState.message = t('common.success') || 'Updated successfully.'
    // 목록에서 제거
    reviewState.pendingItems = reviewState.pendingItems.filter(item => item.id !== id)
  } catch (e) {
    setSafeError(e)
  } finally {
    reviewState.saving = false
  }
}

function approve(item) {
  updateSignature(item.id, item.koreanName, 'MANUAL_APPROVED')
}

function reject(id) {
  updateSignature(id, null, 'REJECTED')
}

onMounted(() => {
  fetchLowConfidenceItems()
})
</script>

<template>
  <section class="card admin-ocr-review">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2>{{ t('adminOCRReview.title') }}</h2>
      <button class="btn btn-sm btn-outline-primary" @click="fetchLowConfidenceItems" :disabled="reviewState.loading">
        {{ t('common.refresh') || 'Refresh' }}
      </button>
    </div>

    <!-- 신뢰도가 낮은 경우 경고 표시 -->
    <p v-if="reviewState.pendingItems.length > 0" class="alert alert-warning">
      <i class="bi bi-exclamation-triangle me-2"></i>
      {{ t('adminOCRReview.lowConfidenceWarning') }}
    </p>

    <div class="review-list">
      <div v-if="reviewState.loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status"></div>
      </div>
      
      <!-- 개별 검수 카드 -->
      <div v-else v-for="item in reviewState.pendingItems" :key="item.id" class="card review-card mb-4 shadow-sm">
        <div class="row align-items-center">
          <!-- 원본 서명 이미지 -->
          <div class="col-md-5">
            <div class="p-2 border bg-light text-center">
              <img v-if="item.imageUrl" :src="item.imageUrl" alt="Signature" class="img-fluid" style="max-height: 150px;" />
              <div v-else class="py-5 text-muted">No Image Available</div>
            </div>
          </div>
          <!-- 인식 결과 및 교정 필드 -->
          <div class="col-md-4">
            <div class="mb-2">
              <small class="text-muted d-block">Recognized Text (Original)</small>
              <span class="badge bg-light text-dark border">{{ item.recognizedText || '-' }}</span>
            </div>
            <div class="mb-2">
              <small class="text-muted d-block">Confidence</small>
              <div class="progress" style="height: 10px;">
                <div class="progress-bar" :class="item.ocrConfidence < 0.5 ? 'bg-danger' : 'bg-warning'" 
                     role="progressbar" :style="{ width: (item.ocrConfidence * 100) + '%' }"></div>
              </div>
              <small>{{ (item.ocrConfidence * 100).toFixed(1) }}%</small>
            </div>
            <label class="w-100 mt-2">
              <strong>{{ t('adminOCRReview.correctedText') }}</strong>
              <input type="text" v-model="item.koreanName" class="form-control" />
            </label>
          </div>
          <!-- 조작 버튼 (승인/반려) -->
          <div class="col-md-3 text-end d-flex flex-column gap-2">
            <button class="btn btn-success" @click="approve(item)" :disabled="reviewState.saving">
              <i class="bi bi-check-circle me-1"></i> {{ t('adminOCRReview.approve') }}
            </button>
            <button class="btn btn-outline-danger" @click="reject(item.id)" :disabled="reviewState.saving">
              <i class="bi bi-x-circle me-1"></i> {{ t('adminOCRReview.reject') }}
            </button>
          </div>
        </div>
      </div>
      
      <!-- 대기 항목이 없을 때 -->
      <div v-if="!reviewState.loading && reviewState.pendingItems.length === 0" class="text-center py-5 border rounded bg-light">
        <p class="mb-0 text-muted">{{ t('adminSignatures.empty') }}</p>
      </div>
    </div>
  </section>
</template>

<style scoped>
.review-card { padding: 20px; transition: transform 0.2s; }
.review-card:hover { transform: translateY(-3px); }
</style>

<style scoped>
.review-card { padding: 15px; }
</style>
