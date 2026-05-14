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
        {{ t('common.refresh') || '새로고침' }}
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
      <!-- 예약 추이 그래프 (등락 추이 시각화) -->
      <article class="card trend-chart-container">
        <h3>{{ t('adminStatistics.reservationTrend') }}</h3>
        <div v-if="statsState.reservationTrend.length" class="trend-chart">
          <div v-for="item in statsState.reservationTrend" :key="item.date" class="chart-bar-wrap">
            <div 
              class="chart-bar" 
              :style="{ height: `${(item.count / Math.max(...statsState.reservationTrend.map(i => i.count), 1)) * 100}%` }"
              :title="`${item.date}: ${item.count}건`"
            >
              <span class="bar-value">{{ item.count }}</span>
            </div>
            <span class="bar-label">{{ item.date.slice(5) }}</span>
          </div>
        </div>
        <p v-else class="text-muted text-center py-5">{{ t('common.noData') }}</p>
      </article>

      <!-- 프로젝트별 현황 (비중 시각화) -->
      <article class="card share-chart-container">
        <h3>{{ t('adminStatistics.projectShare') }}</h3>
        <div v-if="statsState.projectShare.length" class="share-chart">
          <div v-for="item in statsState.projectShare" :key="item.name" class="share-item">
            <div class="share-info">
              <span>{{ item.name }}</span>
              <strong>{{ item.count }}건</strong>
            </div>
            <div class="progress-bg">
              <div 
                class="progress-fill" 
                :style="{ width: `${(item.count / Math.max(...statsState.projectShare.map(i => i.count), 1)) * 100}%` }"
              ></div>
            </div>
          </div>
        </div>
        <p v-else class="text-muted text-center py-5">{{ t('common.noData') }}</p>
      </article>

      <!-- 사용자 언어 분포 (시각화) -->
      <article class="card">
        <h3>{{ t('adminStatistics.userLocaleDistribution') }}</h3>
        <div v-if="statsState.localeDistribution.length" class="share-chart mt-3">
          <div v-for="item in statsState.localeDistribution" :key="item.lang" class="share-item">
            <div class="share-info">
              <span class="fw-bold">{{ item.lang }}</span>
              <strong>{{ item.count }}건</strong>
            </div>
            <div class="progress-bg">
              <div 
                class="progress-fill bg-info" 
                :style="{ width: `${(item.count / Math.max(...statsState.localeDistribution.map(i => i.count), 1)) * 100}%` }"
              ></div>
            </div>
          </div>
        </div>
        <p v-else class="text-muted text-center py-5">{{ t('common.noData') }}</p>
      </article>
    </div>
  </section>
</template>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 1.5rem;
}
.admin-statistics article {
  min-height: 320px;
  display: flex;
  flex-direction: column;
}

/* 예약 추이 세로 막대 그래프 */
.trend-chart {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 200px;
  padding-top: 30px;
  border-bottom: 2px solid #eee;
  margin-top: auto;
}
.chart-bar-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
  padding: 0 5px;
}
.chart-bar {
  width: 100%;
  max-width: 30px;
  background: linear-gradient(180deg, #007bff 0%, #0056b3 100%);
  border-radius: 4px 4px 0 0;
  position: relative;
  transition: height 0.8s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
.chart-bar:hover {
  background: #00c3ff;
}
.bar-value {
  position: absolute;
  top: -25px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 0.8rem;
  font-weight: bold;
  color: #007bff;
}
.bar-label {
  font-size: 0.75rem;
  color: #666;
  margin-top: 8px;
  white-space: nowrap;
}

/* 프로젝트 점유율 가로 막대 그래프 */
.share-chart {
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
  margin-top: 1rem;
}
.share-item {
  width: 100%;
}
.share-info {
  display: flex;
  justify-content: space-between;
  font-size: 0.9rem;
  margin-bottom: 5px;
}
.progress-bg {
  height: 10px;
  background: #f0f0f0;
  border-radius: 5px;
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  background: #28a745;
  border-radius: 5px;
  transition: width 1s ease-out;
}

.summary-card {
  transition: transform 0.3s ease;
}
.summary-card:hover {
  transform: scale(1.05);
}
</style>
