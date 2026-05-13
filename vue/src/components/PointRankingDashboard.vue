<script setup>
import { ref, onMounted, inject, computed } from 'vue'
import { getIdeaContestUserId } from '../utils/authStorage'

const { apiFetch, t } = inject('appContext')
const rankings = ref([])
const loading = ref(true)

// 페이지네이션 관련 상태
const currentPage = ref(1)
const pageSize = 3

const fetchRankings = async () => {
  try {
    loading.value = true
    const data = await apiFetch('/point-reward/ranking?hours=0')
    rankings.value = data || []
  } catch (e) {
    console.error('Failed to fetch rankings:', e)
  } finally {
    loading.value = false
  }
}

// 내 순위 정보 계산
const myRanking = computed(() => {
  const userId = getIdeaContestUserId()?.trim().toLowerCase()
  if (!userId) return null
  // userId(email)과 rankings의 email을 매칭 (대소문자/공백 무시)
  return rankings.value.find(r => r.email?.trim().toLowerCase() === userId)
})

// 현재 페이지에 해당하는 데이터 계산
const totalPages = computed(() => Math.ceil(rankings.value.length / pageSize))
const paginatedRankings = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return rankings.value.slice(start, end)
})

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
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
          <span class="badge bg-primary me-2">ALL-TIME</span>
          {{ t('ranking.allTimeTitle', '전체 기간 포인트 랭킹') }}
        </h3>
        <small class="text-custom-muted fw-bold">{{ t('ranking.totalStatus', '누적 데이터 기준') }}</small>
      </div>

      <div class="card-body px-4 pb-3">
        <div v-if="loading && rankings.length === 0" class="text-center py-5">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Loading...</span>
          </div>
        </div>

        <div v-else-if="rankings.length === 0" class="text-center py-5 text-muted italic">
          {{ t('ranking.noActivity', '최근 활동이 없습니다. 첫 주인공이 되어보세요!') }}
        </div>

        <div v-else class="ranking-content">
          <div class="ranking-list mt-2">
            <div 
              v-for="user in paginatedRankings" 
              :key="user.rank" 
              class="ranking-row d-flex align-items-center mb-3 p-3 rounded-4 transition-all"
              :class="getRankClass(user.rank)"
            >
              <div class="rank-position me-3 d-flex align-items-center justify-content-center">
                <span class="rank-number">{{ getRankIcon(user.rank) }}</span>
              </div>
              
              <div class="user-details flex-grow-1">
                <div class="user-identity fw-bold">
                  {{ maskEmail(user.email) }}
                </div>
              </div>

              <div class="points-display text-end">
                <span class="points-amount fw-black h4 mb-0">{{ user.totalPoints.toLocaleString() }}</span>
                <span class="points-label ms-1 small">SP</span>
              </div>
            </div>
          </div>

          <!-- 페이지네이션 컨트롤 -->
          <nav v-if="totalPages > 1" class="mt-4 d-flex justify-content-center">
            <ul class="pagination pagination-sm mb-0">
              <li class="page-item" :class="{ disabled: currentPage === 1 }">
                <a class="page-link border-0 rounded-circle me-2" href="#" @click.prevent="changePage(currentPage - 1)">
                  &laquo;
                </a>
              </li>
              <li 
                v-for="page in totalPages" 
                :key="page" 
                class="page-item" 
                :class="{ active: currentPage === page }"
              >
                <a class="page-link border-0 rounded-circle mx-1" href="#" @click.prevent="changePage(page)">
                  {{ page }}
                </a>
              </li>
              <li class="page-item" :class="{ disabled: currentPage === totalPages }">
                <a class="page-link border-0 rounded-circle ms-2" href="#" @click.prevent="changePage(currentPage + 1)">
                  &raquo;
                </a>
              </li>
            </ul>
          </nav>

          <!-- 내 순위 별도 표기 -->
          <div v-if="myRanking" class="my-ranking-section mt-4 pt-3 border-top">
            <div class="d-flex align-items-center justify-content-between p-3 rounded-4 my-ranking-box">
              <div class="d-flex align-items-center">
                <div class="my-rank-badge me-3">{{ getRankIcon(myRanking.rank) }}</div>
                <div class="my-rank-text">
                  <div class="small text-muted">{{ t('ranking.myRank', '내 현재 순위') }}</div>
                  <div class="fw-bold">{{ maskEmail(myRanking.email) }} (나)</div>
                </div>
              </div>
              <div class="points-display text-end">
                <span class="points-amount fw-black h4 mb-0">{{ myRanking.totalPoints.toLocaleString() }}</span>
                <span class="points-label ms-1 small">SP</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ranking-card {
  background: #ffffff;
  border: 1px solid #cfe8ff !important;
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.05) !important;
  overflow: hidden;
}

.ranking-main-title {
  color: #1c3b5b;
  font-weight: 800;
  letter-spacing: -0.5px;
}

.text-custom-muted {
  color: #6c757d;
}

.pulse-animation {
  animation: pulse-red 2s infinite;
  box-shadow: 0 0 0 0 rgba(239, 68, 68, 0.4);
  font-size: 0.65rem;
  padding: 0.35em 0.65em;
  vertical-align: middle;
}

@keyframes pulse-red {
  0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(239, 68, 68, 0.4); }
  70% { transform: scale(1); box-shadow: 0 0 0 10px rgba(239, 68, 68, 0); }
  100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(239, 68, 68, 0); }
}

.ranking-row {
  border: 1px solid #f0f7ff;
  background: #f8fbff;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.ranking-row:hover {
  transform: translateY(-3px);
  background: #ffffff;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
  border-color: #7ebcff;
}

.rank-position {
  width: 44px;
  height: 44px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.05);
}

.rank-number {
  font-size: 1.25rem;
  font-weight: 800;
}

.user-identity {
  color: #2f4f74;
  font-size: 1.05rem;
  letter-spacing: 0.2px;
}

/* 랭킹별 특별 스타일 (파스텔톤) */
.rank-gold {
  background: #fffdf5;
  border: 1px solid #fde68a !important;
}
.rank-gold .points-amount { color: #d97706 !important; }

.rank-silver {
  background: #f8fafc;
  border: 1px solid #e2e8f0 !important;
}
.rank-silver .points-amount { color: #475569 !important; }

.rank-bronze {
  background: #fff7ed;
  border: 1px solid #fdba74 !important;
}
.rank-bronze .points-amount { color: #9a3412 !important; }

.rank-normal {
  border: 1px solid #f0f7ff !important;
}

.fw-black { font-weight: 900; }
.transition-all { transition: all 0.3s ease; }

.points-amount {
  font-family: 'Inter', sans-serif;
  letter-spacing: -0.5px;
}

.points-label {
  color: #94a3b8;
  font-weight: 600;
}

/* 페이지네이션 스타일 커스텀 */
.page-link {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fbff;
  color: #6db4ff;
  font-weight: 700;
  transition: all 0.2s ease;
}

.page-link:hover {
  background: #e8f3ff;
  color: #0d6efd;
}

.page-item.active .page-link {
  background: #6db4ff;
  color: #fff;
  box-shadow: 0 4px 10px rgba(109, 180, 255, 0.3);
}

.page-item.disabled .page-link {
  background: transparent;
  color: #dee2e6;
}

/* 내 순위 섹션 스타일 */
.my-ranking-box {
  background: linear-gradient(135deg, #f0f7ff 0%, #e0f0ff 100%);
  border: 1px solid #7ebcff;
  box-shadow: 0 4px 15px rgba(109, 180, 255, 0.1);
  transition: all 0.3s ease;
}

.my-ranking-box:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(109, 180, 255, 0.15);
}

.my-rank-badge {
  width: 44px;
  height: 44px;
  background: #ffffff;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  font-weight: 800;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}

.my-rank-text .small {
  font-size: 0.75rem;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}
</style>
