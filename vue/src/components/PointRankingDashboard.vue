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

onMounted(() => {
  fetchRankings()
  // 30초마다 갱신
  const timer = setInterval(fetchRankings, 30000)
  return () => clearInterval(timer)
})

const getRankIcon = (rank) => {
  if (rank === 1) return '🥇'
  if (rank === 2) return '🥈'
  if (rank === 3) return '🥉'
  return rank
}
</script>

<template>
  <div class="ranking-dashboard">
    <div class="ranking-header">
      <h2 class="ranking-title">
        <span class="live-badge">LIVE</span>
        최근 2시간 가입자 포인트 랭킹
      </h2>
      <p class="ranking-subtitle">현재 가장 활발하게 활동 중인 시네마 페스티벌 멤버들입니다.</p>
    </div>

    <div v-if="loading && rankings.length === 0" class="ranking-loading">
      데이터를 불러오는 중...
    </div>

    <div v-else-if="rankings.length === 0" class="ranking-empty">
      최근 2시간 내 새로운 활동이 없습니다. 첫 주인공이 되어보세요!
    </div>

    <div v-else class="ranking-list">
      <div v-for="user in rankings.slice(0, 5)" :key="user.rank" class="ranking-item" :class="'rank-' + user.rank">
        <div class="rank-badge">{{ getRankIcon(user.rank) }}</div>
        <div class="user-info">
          <span class="user-name">{{ user.nickname || user.name || '익명 관광객' }}</span>
        </div>
        <div class="user-points">
          <span class="points-value">{{ user.totalPoints }}</span>
          <span class="points-unit">SP</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ranking-dashboard {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 24px;
  margin-bottom: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  color: #fff;
  animation: fadeIn 0.8s ease-out;
}

.ranking-header {
  margin-bottom: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  padding-bottom: 12px;
}

.ranking-title {
  font-size: 1.5rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0;
  background: linear-gradient(90deg, #fff, #a5b4fc);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.live-badge {
  background: #ef4444;
  color: white;
  font-size: 0.7rem;
  padding: 2px 8px;
  border-radius: 4px;
  -webkit-text-fill-color: white;
  animation: pulse 2s infinite;
}

.ranking-subtitle {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.6);
  margin: 4px 0 0 0;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 12px;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.ranking-item:hover {
  transform: translateX(5px);
  background: rgba(255, 255, 255, 0.07);
  border-color: rgba(255, 255, 255, 0.2);
}

.rank-badge {
  width: 40px;
  font-size: 1.2rem;
  font-weight: 800;
  text-align: center;
}

.user-info {
  flex: 1;
}

.user-name {
  font-weight: 600;
  font-size: 1.1rem;
}

.user-points {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.points-value {
  font-size: 1.3rem;
  font-weight: 800;
  color: #fbbf24;
}

.points-unit {
  font-size: 0.8rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.4);
}

/* 상위권 특별 스타일 */
.rank-1 {
  background: linear-gradient(90deg, rgba(251, 191, 36, 0.1), transparent);
  border-left: 4px solid #fbbf24;
}

.rank-2 {
  background: linear-gradient(90deg, rgba(148, 163, 184, 0.1), transparent);
  border-left: 4px solid #94a3b8;
}

.rank-3 {
  background: linear-gradient(90deg, rgba(217, 119, 6, 0.1), transparent);
  border-left: 4px solid #d97706;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}

.ranking-loading, .ranking-empty {
  text-align: center;
  padding: 30px;
  color: rgba(255, 255, 255, 0.5);
  font-style: italic;
}
</style>
