<script setup>
import { ref, onMounted, inject } from 'vue'

const { apiFetch, t } = inject('appContext')
const rankings = ref([])
const loading = ref(true)

const fetchRankings = async () => {
  try {
    loading.value = true
    const data = await apiFetch('/point-reward/ranking?hours=2')
    rankings.value = data || []
  } catch (e) {
    console.error('Failed to fetch rankings:', e)
  } finally {
    loading.value = false
  }
}

// 이메일 마스킹 로직: 앞 3글자 유지, 나머지는 별표 처리 (예: sal***@naver.com)
const maskEmail = (email) => {
  if (!email) return '익명 사용자'
  const [localPart, domain] = email.split('@')
  if (localPart.length <= 3) {
    return localPart + '***' + (domain ? '@' + domain : '')
  }
  return localPart.substring(0, 3) + '***@' + domain
}

onMounted(() => {
  fetchRankings()
  const timer = setInterval(fetchRankings, 30000)
  return () => clearInterval(timer)
})

const getRankClass = (rank) => {
  if (rank === 1) return 'rank-gold'
  if (rank === 2) return 'rank-silver'
  if (rank === 3) return 'rank-bronze'
  return 'rank-normal'
}

const getRankIcon = (rank) => {
  if (rank === 1) return '🥇'
  if (rank === 2) return '🥈'
  if (rank === 3) return '🥉'
  return rank
}
</script>

<template>
  <div class="ranking-container mb-4">
    <div class="card ranking-card border-0 shadow-lg">
      <div class="card-header bg-transparent border-0 pt-4 px-4 d-flex justify-content-between align-items-center">
        <h3 class="ranking-main-title mb-0">
          <span class="badge bg-danger pulse-animation me-2">LIVE</span>
          {{ t('ranking.title', '실시간 활동 랭킹') }}
        </h3>
        <small class="text-white-50">{{ t('ranking.lastTwoHours', '최근 2시간 기준') }}</small>
      </div>

      <div class="card-body px-4 pb-4">
        <div v-if="loading && rankings.length === 0" class="text-center py-5">
          <div class="spinner-border text-light" role="status">
            <span class="visually-hidden">Loading...</span>
          </div>
        </div>

        <div v-else-if="rankings.length === 0" class="text-center py-5 text-white-50 italic">
          {{ t('ranking.noActivity', '최근 활동이 없습니다. 첫 주인공이 되어보세요!') }}
        </div>

        <div v-else class="ranking-list mt-3">
          <div 
            v-for="user in rankings.slice(0, 5)" 
            :key="user.rank" 
            class="ranking-row d-flex align-items-center mb-3 p-3 rounded-4 transition-all"
            :class="getRankClass(user.rank)"
          >
            <div class="rank-position me-3 d-flex align-items-center justify-content-center">
              <span class="rank-number">{{ getRankIcon(user.rank) }}</span>
            </div>
            
            <div class="user-details flex-grow-1">
              <div class="user-identity text-white fw-bold">
                {{ maskEmail(user.email) }}
              </div>
            </div>

            <div class="points-display text-end">
              <span class="points-amount fw-black text-warning h4 mb-0">{{ user.totalPoints.toLocaleString() }}</span>
              <span class="points-label ms-1 text-white-50 small">SP</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ranking-card {
  background: rgba(20, 20, 30, 0.7);
  backdrop-filter: blur(15px);
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  border-radius: 24px;
  overflow: hidden;
}

.ranking-main-title {
  color: #fff;
  font-weight: 800;
  letter-spacing: -0.5px;
}

.pulse-animation {
  animation: pulse-red 2s infinite;
  box-shadow: 0 0 0 0 rgba(239, 68, 68, 0.7);
  font-size: 0.65rem;
  padding: 0.35em 0.65em;
  vertical-align: middle;
}

@keyframes pulse-red {
  0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(239, 68, 68, 0.7); }
  70% { transform: scale(1); box-shadow: 0 0 0 10px rgba(239, 68, 68, 0); }
  100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(239, 68, 68, 0); }
}

.ranking-row {
  border: 1px solid rgba(255, 255, 255, 0.05);
  background: rgba(255, 255, 255, 0.02);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.ranking-row:hover {
  transform: scale(1.02) translateX(5px);
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.2);
}

.rank-position {
  width: 44px;
  height: 44px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 12px;
}

.rank-number {
  font-size: 1.25rem;
  font-weight: 800;
}

.user-identity {
  font-size: 1.05rem;
  letter-spacing: 0.2px;
}

/* 랭킹별 특별 스타일 */
.rank-gold {
  background: linear-gradient(90deg, rgba(251, 191, 36, 0.15), rgba(251, 191, 36, 0.02));
  border-left: 5px solid #fbbf24 !important;
}
.rank-gold .points-amount { color: #fbbf24 !important; text-shadow: 0 0 15px rgba(251, 191, 36, 0.4); }

.rank-silver {
  background: linear-gradient(90deg, rgba(226, 232, 240, 0.1), rgba(226, 232, 240, 0.02));
  border-left: 5px solid #e2e8f0 !important;
}
.rank-silver .points-amount { color: #e2e8f0 !important; }

.rank-bronze {
  background: linear-gradient(90deg, rgba(217, 119, 6, 0.1), rgba(217, 119, 6, 0.02));
  border-left: 5px solid #d97706 !important;
}
.rank-bronze .points-amount { color: #d97706 !important; }

.rank-normal {
  border-left: 5px solid rgba(255, 255, 255, 0.1) !important;
}

.fw-black { font-weight: 900; }
.transition-all { transition: all 0.3s ease; }
</style>
