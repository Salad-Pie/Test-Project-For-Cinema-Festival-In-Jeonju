<script setup>
import { computed, inject, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { apiRoot } from '../config/api'
import { parseErrorResponse } from '../api/client'

const { t, locale, state: globalState, setSafeError, userError, artistMeetingPosterUrl } = inject('appContext')

/**
 * 중국 관광객 전용 엔딩 크레딧 설정
 */
const phases = {
  NAMES: 0,
  BLACK: 1,
  TAIL: 2,
  FINAL: 3
}

const state = reactive({
  entries: [],
  currentPhase: phases.NAMES,
  isFullscreen: false,
  highlightedCodes: {},
  loading: true
})

const shellRef = ref(null)
const highlightTimer = ref(null)
const phaseTimer = ref(null)

// 최하단 마지막 문구 프리셋 1
const tailPreset1 = `“이제 우리는 손님과 주최자가 아니라… 친구가 되었습니다!”
“Now we are not just guests and hosts…
we have become friends!”

오늘 전주에서 함께한 시간이
여러분에게 따뜻한 기억으로
남기를 바랍니다.
We hope the time we
spent together in Jeonju today
becomes a warm memory for
all of you.

여러분과 함께 한국의 문화와
마음을 나눌 수 있어 정말 행
복했습니다.
We were very happy to
share Korean culture and heart
with you.

다음에는 여행자가 아니라
친구로 만나기를 기대하겠습니다
Next time, we look forward to
meeting you as friends,
not as travelers.

감사합니다.
Thank you very much.`

/**
 * 백엔드로부터 최근 30분 내 가입 유저 조회
 */
async function fetchRecentEntries() {
  state.loading = true
  try {
    const res = await fetch(`${apiRoot}/ending-credits/recent?minutes=30`)
    if (!res.ok) throw new Error(await parseErrorResponse(res, t('common.requestFailed')))
    state.entries = await res.json()
  } catch (e) {
    setSafeError(e)
  } finally {
    state.loading = false
  }
}

/**
 * 단계별 타이밍 관리
 */
function startPhaseTimer() {
  // 1. 이름 롤링 (10분)
  phaseTimer.value = setTimeout(() => {
    state.currentPhase = phases.BLACK
    
    // 2. 블랙 화면 (10분)
    phaseTimer.value = setTimeout(() => {
      state.currentPhase = phases.TAIL
      
      // 3. 마지막 문구 (5분)
      phaseTimer.value = setTimeout(() => {
        state.currentPhase = phases.FINAL
      }, 5 * 60 * 1000)
      
    }, 10 * 60 * 1000)
    
  }, 10 * 60 * 1000)
}

/**
 * 하이라이트 감지 로직 (화면 중앙에 올 때 강조)
 */
function monitorCenterLine() {
  if (state.currentPhase !== phases.NAMES && state.currentPhase !== phases.TAIL) return
  
  const shell = shellRef.value
  if (!shell) return

  const messageCard = shell.querySelector('.ending-credits-message-card')
  if (!messageCard) return

  const cardRect = messageCard.getBoundingClientRect()
  const centerY = cardRect.top + cardRect.height / 2
  const threshold = 30 // 하이라이트 감지 범위
  
  const items = shell.querySelectorAll('.ending-credits-cast-group[data-code]')
  items.forEach((item) => {
    const rect = item.getBoundingClientRect()
    const itemCenterY = rect.top + rect.height / 2
    if (Math.abs(itemCenterY - centerY) <= threshold) {
      const code = item.dataset.code
      state.highlightedCodes[code] = true
      setTimeout(() => { delete state.highlightedCodes[code] }, 1000)
    }
  })
}

function syncFullscreen() {
  state.isFullscreen = !!document.fullscreenElement
}

async function toggleFullscreen() {
  const target = shellRef.value
  if (!target) return
  if (!document.fullscreenElement) {
    await target.requestFullscreen()
  } else {
    await document.exitFullscreen()
  }
}

onMounted(async () => {
  await fetchRecentEntries()
  startPhaseTimer()
  highlightTimer.value = setInterval(monitorCenterLine, 150)
  document.addEventListener('fullscreenchange', syncFullscreen)
})

onUnmounted(() => {
  clearTimeout(phaseTimer.value)
  clearInterval(highlightTimer.value)
  document.removeEventListener('fullscreenchange', syncFullscreen)
})
</script>

<template>
  <section class="ending-credits-cn-page">
    <div ref="shellRef" class="ending-credits-shell" :class="{ 'is-fullscreen': state.isFullscreen, 'is-black': state.currentPhase === phases.BLACK }">
      
      <!-- 배경 포스터 (좌측 고정) -->
      <div v-if="state.currentPhase !== phases.BLACK" class="poster-area">
        <img :src="artistMeetingPosterUrl" alt="Poster" />
      </div>

      <!-- 메인 롤링 영역 -->
      <div class="ending-credits-message-card">
        
        <!-- 단계 0: 이름 롤링 -->
        <div v-if="state.currentPhase === phases.NAMES" class="roll-container phase-names">
          <div v-for="entry in state.entries" :key="entry.code" 
               class="ending-credits-cast-group" :class="{ 'is-highlighted': state.highlightedCodes[entry.code] }"
               :data-code="entry.code">
            <p class="name-original">{{ entry.originalName }}</p>
            <p class="name-korean">{{ entry.koreanName }}</p>
            <div class="welcome-msg">
              <p class="msg-ko">전주에 온 것을 환영합니다</p>
              <p class="msg-en">Welcome to Jeonju</p>
            </div>
          </div>
        </div>

        <!-- 단계 1: 블랙 화면 (CSS class 'is-black'으로 처리) -->

        <!-- 단계 2: 마지막 문구 롤링 -->
        <div v-if="state.currentPhase === phases.TAIL" class="roll-container phase-tail">
          <div class="tail-content">
            <p v-for="(line, idx) in tailPreset1.split('\n')" :key="idx" 
               :class="{ 'highlight-line': line.trim().startsWith('“') }">
              {{ line }}
            </p>
          </div>
        </div>

        <!-- 단계 3: 최종 안내 문구 -->
        <div v-if="state.currentPhase === phases.FINAL" class="final-message">
          <p>다음 장소로 이동해주시면 감사하겠습니다.</p>
        </div>

      </div>

      <!-- 컨트롤바 (비전체화면일 때만 노출) -->
      <div v-if="!state.isFullscreen" class="control-bar card shadow-sm">
        <div class="d-flex justify-content-between align-items-center">
          <div>
            <h5 class="mb-0">China Tourist Ending Credits</h5>
            <small class="text-muted">Status: Phase {{ state.currentPhase }} ({{ state.entries.length }} entries)</small>
          </div>
          <div class="d-flex gap-2">
            <button class="btn btn-outline-primary" @click="fetchRecentEntries">Reload Names</button>
            <button class="btn btn-primary" @click="toggleFullscreen">Enter Fullscreen</button>
          </div>
        </div>
      </div>

    </div>
  </section>
</template>

<style scoped>
.ending-credits-cn-page {
  width: 100%;
  height: 100vh;
  background: #000;
  overflow: hidden;
  font-family: 'Inter', sans-serif;
}

.ending-credits-shell {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  background: #000;
  color: #fff;
}

.is-black {
  background: #000 !important;
}

.poster-area {
  width: 40%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 50px;
}

.poster-area img {
  max-width: 100%;
  max-height: 80%;
  box-shadow: 0 0 30px rgba(255,255,255,0.1);
  border-radius: 8px;
}

.ending-credits-message-card {
  flex: 1;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 롤링 애니메이션 */
@keyframes rollUp {
  0% { transform: translateY(100vh); }
  100% { transform: translateY(-100%); }
}

.roll-container {
  width: 100%;
  position: absolute;
  top: 0;
  animation: rollUp 60s linear infinite; /* 속도 조절 필요 시 수정 */
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 100px;
}

.ending-credits-cast-group {
  text-align: center;
  transition: all 0.3s;
  padding: 40px;
  border-radius: 20px;
}

.is-highlighted {
  transform: scale(1.2);
  color: #ffeb3b;
  text-shadow: 0 0 20px rgba(255, 235, 59, 0.5);
}

.name-original { font-size: 2rem; margin-bottom: 5px; }
.name-korean { font-size: 2.5rem; font-weight: bold; margin-bottom: 20px; }
.welcome-msg .msg-ko { font-size: 1.2rem; color: #aaa; }
.welcome-msg .msg-en { font-size: 1rem; color: #888; }

.phase-tail .tail-content {
  text-align: center;
  font-size: 1.5rem;
  line-height: 2;
  white-space: pre-wrap;
}

.highlight-line {
  color: #ffeb3b;
  font-weight: bold;
}

.final-message {
  font-size: 3rem;
  font-weight: bold;
  text-align: center;
  animation: fadeIn 2s ease-in;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.control-bar {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  width: 90%;
  max-width: 800px;
  padding: 20px;
  z-index: 1000;
  background: rgba(30, 30, 30, 0.9) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  color: #fff;
}

/* 전체화면 스타일 커스텀 */
.is-fullscreen .control-bar {
  display: none;
}
</style>
