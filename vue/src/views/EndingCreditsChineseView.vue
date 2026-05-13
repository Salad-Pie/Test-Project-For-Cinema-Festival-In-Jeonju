<script setup>
import { computed, inject, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { apiRoot } from '../config/api'
import { parseErrorResponse } from '../api/client'

const { t, locale, state: globalState, setSafeError, userError, artistMeetingPosterUrl } = inject('appContext')

/**
 * 중국 관광객 전용 엔딩 크레딧 설정 (기존 단계 유지)
 */
const phases = {
  IDLE: -1,
  NAMES: 0,
  BLACK: 1,
  TAIL: 2,
  FINAL: 3
}

const leadPreset1 = `제목
AX 융복합 한 코드 프로젝트
제작 
인사이트투어 
후원
전주문화공판장재단
기획 운영
샐러드파이 주식회사 
스탭
박도겸
통역
강봉주
출연`

// 중국인 관광객용 최종 문구 (기존 유지용)
const tailPreset1 = `“이제 우리는 손님과 주최자가 아니라… 친구가 되었습니다!”
“现在，我们不再只是客人与主人……
我们已经成为朋友了！”

오늘 전주에서 함께한 시간이
여러분에게 따뜻한 기억으로
남기를 바랍니다.
希望今天我们在全州一起度过的时光，
能成为大家温暖的回忆。

여러분과 함께 한국의 문화와
마음을 나눌 수 있어 정말 행복했습니다.
能够和大家一起分享韩国의 文化与心意，
我们感到非常幸福。

다음에는 여행자가 아니라
친구로 만나기를 기대하겠습니다.
期待下一次，
我们不再以旅行者的身份相见，
而是作为朋友再次相聚。

감사합니다.
非常感谢大家。`

const state = reactive({
  entries: [],
  currentPhase: phases.IDLE,
  isFullscreen: false,
  highlightedCodes: {},
  loading: false,
  
  // 인도네시아 페이지와 동일한 기능 추가
  leadMessage: '',
  tailMessage: '',
  rollDurationSeconds: 30, // 30초를 기본값으로 설정
  rollGapPx: 100,
  fontScalePercent: 100,
  stopAfterOneCycle: false,
  code: '',
})

const shellRef = ref(null)
const highlightTimer = ref(null)
const phaseTimer = ref(null)

/**
 * 백엔드로부터 최근 30분 내 가입 유저 조회 (기존 기능 유지)
 */
async function fetchRecentEntries() {
  state.loading = true
  try {
    const res = await fetch(`${apiRoot}/ending-credits/recent?minutes=30`)
    if (!res.ok) throw new Error(await parseErrorResponse(res, t('common.requestFailed')))
    const data = await res.json()
    data.forEach(entry => {
      if (!state.entries.some(e => e.code === entry.code)) {
        state.entries.push({
          code: entry.code,
          originalName: entry.originalName,
          koreanName: entry.koreanName,
          hasEnglishName: true,
          hasKoreanName: true
        })
      }
    })
  } catch (e) {
    setSafeError(e)
  } finally {
    state.loading = false
  }
}

/**
 * 수동 가입자 추가 (인도네시아 페이지와 동일 기능)
 */
async function addEntry() {
  if (!state.code || state.code.length !== 6) {
    setSafeError(userError('6자리 코드를 입력해주세요.'))
    return
  }
  if (state.entries.some(e => e.code === state.code)) {
    setSafeError(userError('이미 추가된 코드입니다.'))
    return
  }
  
  state.loading = true
  try {
    const res = await fetch(`${apiRoot}/ending-credits/lookup`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ code: state.code })
    })
    if (!res.ok) throw new Error(await parseErrorResponse(res, t('common.requestFailed')))
    const entry = await res.json()
    state.entries.push({
      code: state.code,
      originalName: entry.englishName || '',
      koreanName: entry.koreanName || '',
      hasEnglishName: !!entry.hasEnglishName,
      hasKoreanName: !!entry.hasKoreanName
    })
    state.code = ''
  } catch (e) {
    setSafeError(e)
  } finally {
    state.loading = false
  }
}

function removeEntry(code) {
  state.entries = state.entries.filter(e => e.code !== code)
}

/**
 * 단계별 타이밍 관리 (기존 10분-10분-5분 유지)
 */
function startPhaseTimer() {
  clearTimeout(phaseTimer.value)
  state.currentPhase = phases.NAMES
  
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
 * 하이라이트 감지 로직
 */
function monitorCenterLine() {
  if (state.currentPhase !== phases.NAMES) return
  
  const shell = shellRef.value
  if (!shell) return

  const messageCard = shell.querySelector('.ending-credits-message-card')
  if (!messageCard) return

  const cardRect = messageCard.getBoundingClientRect()
  const centerY = cardRect.top + cardRect.height / 2
  const threshold = 40
  
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
    state.isFullscreen = true
    // 전체화면 진입 시 시퀀스 처음부터 시작
    startPhaseTimer()
  } else {
    await document.exitFullscreen()
    state.isFullscreen = false
    // 전체화면 해제 시 타이머 중단 및 IDLE 상태로 복구
    clearTimeout(phaseTimer.value)
    state.currentPhase = phases.IDLE
  }
}

onMounted(async () => {
  await fetchRecentEntries()
  // 하단 문구 자동 적용
  state.tailMessage = tailPreset1
  // 자동 시작 제거
  highlightTimer.value = setInterval(monitorCenterLine, 150)
  document.addEventListener('fullscreenchange', syncFullscreen)
})

onUnmounted(() => {
  clearTimeout(phaseTimer.value)
  clearInterval(highlightTimer.value)
  document.removeEventListener('fullscreenchange', syncFullscreen)
})

const rollEntries = computed(() => state.entries)
const leadLines = computed(() => state.leadMessage.split('\n').map(l => l.trim()).filter(Boolean))
const tailLines = computed(() => state.tailMessage.split('\n').map(l => l.trim()).filter(Boolean))

</script>

<template>
  <section class="ending-credits-cn-page">
    <div ref="shellRef" class="ending-credits-shell" :class="{ 'is-fullscreen': state.isFullscreen, 'is-black': state.currentPhase === phases.BLACK || state.currentPhase === phases.FINAL }">
      
      <!-- 상단 메인 영역 (100vh) -->
      <div class="ending-credits-hero">
        <!-- 배경 포스터 -->
        <div v-if="state.currentPhase !== phases.BLACK && state.currentPhase !== phases.FINAL" class="poster-area">
          <img :src="artistMeetingPosterUrl" alt="Poster" />
        </div>

        <!-- 메인 롤링 영역 -->
        <div class="ending-credits-message-card">
          
          <!-- 단계 0: 이름 롤링 -->
          <div v-if="state.currentPhase === phases.NAMES" 
               class="roll-container phase-names"
               :style="{
                 '--ending-roll-duration': `${state.rollDurationSeconds}s`,
                 '--ending-roll-gap': `${state.rollGapPx}px`,
                 '--ending-font-scale': `${state.fontScalePercent / 100}`,
                 '--ending-roll-iteration-count': state.stopAfterOneCycle ? '1' : 'infinite'
               }">
            
            <div v-if="leadLines.length" class="ending-credits-group">
              <p v-for="(line, idx) in leadLines" :key="idx" class="ending-credits-group-line">{{ line }}</p>
            </div>

            <div v-for="entry in rollEntries" :key="entry.code" 
                 class="ending-credits-cast-group" :class="{ 'is-highlighted': state.highlightedCodes[entry.code] }"
                 :data-code="entry.code">
              <p class="name-original">{{ entry.originalName }}</p>
              <p class="name-korean">{{ entry.koreanName }}</p>
              <div class="welcome-msg">
                <p class="msg-ko">전주에 온 것을 환영합니다</p>
                <p class="msg-en">Welcome to Jeonju</p>
              </div>
            </div>

            <div v-if="tailLines.length" class="ending-credits-group">
              <p v-for="(line, idx) in tailLines" :key="idx" class="ending-credits-group-line">{{ line }}</p>
            </div>
          </div>

          <!-- 단계 1: 블랙 화면 -->

          <!-- 단계 2: 최종 감사 문구 -->
          <div v-if="state.currentPhase === phases.TAIL" class="roll-container phase-tail"
               :style="{ '--ending-roll-duration': '300s' }">
            <div class="tail-content">
              <p v-for="(line, idx) in tailPreset1.split('\n')" :key="idx" 
                 :class="{ 'highlight-line': line.trim().startsWith('“') }">
                {{ line }}
              </p>
            </div>
          </div>

          <!-- 단계 3: 최종 종료 안내 -->
          <div v-if="state.currentPhase === phases.FINAL" class="final-message">
            <p>다음 장소로 이동해주시면 감사하겠습니다.</p>
          </div>
        </div>
      </div>

      <!-- 하단 컨트롤 패널 (레이아웃 하단에 추가됨) -->
      <article v-if="!state.isFullscreen" class="control-panel-layout">
        <div class="control-panel-card card shadow-lg">
          <div class="control-header">
            <h5><i class="bi bi-gear-fill me-2"></i>엔딩 크레딧 설정 (중국어)</h5>
            <div class="d-flex gap-2">
              <button class="btn btn-sm btn-outline-light" @click="fetchRecentEntries">최근 데이터 갱신</button>
              <button class="btn btn-sm btn-warning" @click="toggleFullscreen">시작</button>
            </div>
          </div>

          <div class="settings-grid">
            <div class="setting-item full-width">
              <div class="d-flex justify-content-between mb-1">
                <label>최상단 추가 문구</label>
                <button class="btn btn-xs btn-link p-0 text-info" @click="state.leadMessage = leadPreset1">자동 문구</button>
              </div>
              <textarea v-model="state.leadMessage" rows="2" placeholder="롤링 시작 부분에 표시될 문구"></textarea>
            </div>

            <div class="setting-item full-width">
              <div class="d-flex justify-content-between mb-1">
                <label>이름 뒤 추가 문구</label>
                <button class="btn btn-xs btn-link p-0 text-info" @click="state.tailMessage = tailPreset1">자동 문구</button>
              </div>
              <textarea v-model="state.tailMessage" rows="2" placeholder="이름 목록 바로 뒤에 표시될 문구"></textarea>
            </div>

            <div class="setting-item">
              <label>롤링 속도 (초)</label>
              <input v-model.number="state.rollDurationSeconds" type="number" min="10" max="600" />
            </div>
            <div class="setting-item">
              <label>항목 간격 (px)</label>
              <input v-model.number="state.rollGapPx" type="number" min="20" max="300" />
            </div>
            <div class="setting-item">
              <label>글자 크기 (%)</label>
              <input v-model.number="state.fontScalePercent" type="number" min="50" max="200" />
            </div>
            <div class="setting-item d-flex align-items-center">
              <label class="mb-0 me-2">1회 재생 후 멈춤</label>
              <input v-model="state.stopAfterOneCycle" type="checkbox" />
            </div>
          </div>

          <div class="entry-management">
            <div class="entry-input">
              <div class="input-group input-group-sm">
                <span class="input-group-text">코드</span>
                <input v-model="state.code" type="text" class="form-control" placeholder="6자리" maxlength="6" />
                <button class="btn btn-primary" @click="addEntry" :disabled="state.loading">추가</button>
              </div>
            </div>

            <div class="entry-list">
              <table class="table table-sm table-dark table-hover mb-0">
                <thead>
                  <tr>
                    <th>코드</th>
                    <th>이름</th>
                    <th>관리</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="entry in state.entries" :key="entry.code">
                    <td><code>{{ entry.code }}</code></td>
                    <td>{{ entry.koreanName || entry.originalName }}</td>
                    <td><button class="btn btn-xs btn-outline-danger" @click="removeEntry(entry.code)">삭제</button></td>
                  </tr>
                  <tr v-if="state.entries.length === 0">
                    <td colspan="3" class="text-center text-muted">데이터가 없습니다.</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </article>

    </div>
  </section>
</template>

<style scoped>
.ending-credits-cn-page {
  width: 100%;
  height: 100vh;
  background: #000;
  overflow-x: hidden;
  overflow-y: auto; /* 하단 레이아웃을 보기 위해 스크롤 허용 */
}

.ending-credits-shell {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #000;
  color: #fff;
  transition: background 1s ease;
}

.is-black {
  background: #000 !important;
}

.ending-credits-hero {
  display: flex;
  width: 100%;
  height: 100vh; /* 메인 영역은 화면을 꽉 채움 */
  flex-shrink: 0;
}

.poster-area {
  width: 35%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.poster-area img {
  max-width: 100%;
  max-height: 85%;
  box-shadow: 0 0 50px rgba(255,255,255,0.05);
  border-radius: 4px;
}

.ending-credits-message-card {
  flex: 1;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

@keyframes rollUp {
  0% { transform: translateY(100vh); }
  100% { transform: translateY(-100%); }
}

.roll-container {
  width: 100%;
  position: absolute;
  top: 0;
  animation: rollUp var(--ending-roll-duration, 60s) linear var(--ending-roll-iteration-count, infinite);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--ending-roll-gap, 100px);
  transform-origin: top center;
  transform: scale(var(--ending-font-scale, 1));
}

.ending-credits-group {
  text-align: center;
  margin-bottom: 20px;
  width: 100%;
}

.ending-credits-group-line {
  font-size: 1.8rem;
  margin-bottom: 15px;
  color: #ddd;
}

.ending-credits-cast-group {
  text-align: center;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 30px;
  border-radius: 20px;
  width: 80%;
}

.is-highlighted {
  transform: scale(1.15);
  color: #ffeb3b;
  text-shadow: 0 0 30px rgba(255, 235, 59, 0.4);
}

.name-original { font-size: 2rem; margin-bottom: 8px; opacity: 0.8; }
.name-korean { font-size: 3rem; font-weight: 800; margin-bottom: 20px; }
.welcome-msg .msg-ko { font-size: 1.3rem; color: #999; margin-bottom: 5px; }
.welcome-msg .msg-en { font-size: 1.1rem; color: #666; }

.phase-tail {
  animation-duration: 300s !important;
}

.tail-content {
  text-align: center;
  font-size: 2rem;
  line-height: 2.2;
  white-space: pre-wrap;
  padding: 100px 0;
}

.highlight-line {
  color: #ffeb3b;
  font-weight: bold;
}

.final-message {
  font-size: 3.5rem;
  font-weight: 900;
  text-align: center;
  animation: fadeIn 3s ease-in;
  color: #fff;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* 하단 컨트롤 패널 레이아웃 */
.control-panel-layout {
  width: 100%;
  padding: 40px 24px;
  background: #000;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.is-fullscreen .control-panel-layout {
  display: none;
}

.control-panel-card {
  max-width: 1100px;
  margin: 0 auto;
  background: rgba(20, 20, 20, 0.95) !important;
  border: 1px solid rgba(255, 255, 255, 0.15) !important;
  color: #fff;
  padding: 30px;
  border-radius: 20px;
}

.control-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}

.control-header h5 { margin: 0; font-weight: 700; color: #fff; font-size: 1.25rem; }

.settings-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.setting-item.full-width { grid-column: span 4; }
.setting-item label { display: block; font-size: 0.9rem; color: #aaa; margin-bottom: 8px; font-weight: 600; }
.setting-item input, .setting-item textarea {
  width: 100%;
  background: #111;
  border: 1px solid #333;
  color: #fff;
  padding: 12px;
  border-radius: 10px;
  font-size: 0.95rem;
}
.setting-item input:focus, .setting-item textarea:focus {
  outline: none;
  border-color: #ffc107;
}

.entry-management {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 30px;
  padding-top: 20px;
  border-top: 1px solid rgba(255,255,255,0.1);
}

.entry-list {
  background: #000;
  border-radius: 10px;
  max-height: 250px;
  overflow-y: auto;
  border: 1px solid #222;
}

.table { --bs-table-bg: transparent; color: #fff; font-size: 0.9rem; }
.btn-xs { padding: 4px 8px; font-size: 0.8rem; border-radius: 6px; }
code { color: #ffc107; background: rgba(255,193,7,0.1); padding: 3px 6px; border-radius: 4px; }

@media (max-width: 992px) {
  .settings-grid { grid-template-columns: 1fr 1fr; }
  .setting-item.full-width { grid-column: span 2; }
  .entry-management { grid-template-columns: 1fr; }
}
</style>
