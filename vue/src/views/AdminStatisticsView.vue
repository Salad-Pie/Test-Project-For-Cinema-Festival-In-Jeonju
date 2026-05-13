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
 * 통계 데이터를 담는 반응형 상태
 */
const statsState = reactive({
  totalUsers: 0,
  totalSignatures: 0,
  totalReservations: 0,
  reservationTrend: [],
  projectShare: [],
  localeDistribution: [],
  ocrAccuracy: 0,
  loading: false
})

/**
 * 백엔드로부터 실시간 통계 데이터 로드
 */
async function fetchStatistics() {
  statsState.loading = true
  try {
    const authToken = getIdeaContestAuthToken()
    const res = await fetch(`${apiRoot}/admin/statistics`, {
      headers: { Authorization: `Bearer ${authToken}` },
    })
    
    if (!res.ok) throw new Error(await parseErrorResponse(res, t('common.requestFailed')))
    
    const data = await res.json()
    
    statsState.totalUsers = data.totalUsers
    statsState.totalSignatures = data.totalSignatures
    statsState.totalReservations = data.totalReservations
    statsState.reservationTrend = data.dailyTrend
    statsState.ocrAccuracy = data.averageOcrConfidence
    
    // 프로젝트 점유율 변환
    statsState.projectShare = Object.entries(data.reservationsByType).map(([name, count]) => ({
      name,
      count
    }))
    
    // 언어 분포 변환
    statsState.localeDistribution = Object.entries(data.userLocales).map(([lang, count]) => ({
      lang,
      count
    }))
    
  } catch (e) {
    setSafeError(e)
  } finally {
    statsState.loading = false
  }
}

onMounted(() => {
  fetchStatistics()
})
</script>

<template>
  <section class="card admin-statistics">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2>{{ t('adminStatistics.title') }}</h2>
      <button class="btn btn-sm btn-outline-primary" @click="fetchStatistics" :disabled="statsState.loading">
        {{ t('common.refresh') || 'Refresh' }}
      </button>
    </div>

    <!-- 요약 카드 영역 (대표님 보고용 핵심 지표) -->
    <div class="row mb-4">
      <div class="col-md-4">
        <div class="card text-center bg-light p-3">
          <small class="text-muted">{{ t('adminStatistics.totalUsers') || 'Total Users' }}</small>
          <h3 class="mb-0">{{ statsState.totalUsers }}</h3>
        </div>
      </div>
      <div class="col-md-4">
        <div class="card text-center bg-light p-3">
          <small class="text-muted">{{ t('adminStatistics.totalSignatures') || 'Total Signatures' }}</small>
          <h3 class="mb-0">{{ statsState.totalSignatures }}</h3>
        </div>
      </div>
      <div class="col-md-4">
        <div class="card text-center bg-light p-3">
          <small class="text-muted">{{ t('adminStatistics.totalReservations') || 'Total Reservations' }}</small>
          <h3 class="mb-0">{{ statsState.totalReservations }}</h3>
        </div>
      </div>
    </div>
    
    <div class="grid">
      <!-- 예약 추이 카드 -->
      <article class="card">
        <h3>{{ t('adminStatistics.reservationTrend') }}</h3>
        <div v-if="statsState.reservationTrend.length">
          <ul>
            <li v-for="item in statsState.reservationTrend" :key="item.date">
              {{ item.date }}: <strong>{{ item.count }}</strong>
            </li>
          </ul>
        </div>
        <p v-else class="text-muted text-center">{{ t('common.noData') }}</p>
      </article>

      <!-- 프로젝트별 현황 카드 -->
      <article class="card">
        <h3>{{ t('adminStatistics.projectShare') }}</h3>
        <ul v-if="statsState.projectShare.length">
          <li v-for="item in statsState.projectShare" :key="item.name">
            {{ item.name }}: <strong>{{ item.count }}</strong>
          </li>
        </ul>
        <p v-else class="text-muted text-center">{{ t('common.noData') }}</p>
      </article>

      <!-- 사용자 언어 분포 카드 -->
      <article class="card">
        <h3>{{ t('adminStatistics.userLocaleDistribution') }}</h3>
        <ul v-if="statsState.localeDistribution.length">
          <li v-for="item in statsState.localeDistribution" :key="item.lang">
            {{ item.lang }}: <strong>{{ item.count }}</strong>
          </li>
        </ul>
        <p v-else class="text-muted text-center">{{ t('common.noData') }}</p>
      </article>

      <!-- OCR 인식 정확도 카드 -->
      <article class="card">
        <h3>{{ t('adminStatistics.ocrAccuracy') }}</h3>
        <div class="h-100 d-flex flex-column justify-content-center align-items-center">
          <p class="display-4 text-primary mb-0">{{ statsState.ocrAccuracy }}%</p>
          <small class="text-muted">{{ t('adminStatistics.averageConfidence') || 'Average Confidence' }}</small>
        </div>
      </article>
    </div>
  </section>
</template>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}
.admin-statistics article {
  min-height: 250px;
}
</style>
