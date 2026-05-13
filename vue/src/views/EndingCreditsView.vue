<script setup>
import { computed, inject, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { apiRoot } from '../config/api'
import { parseErrorResponse } from '../api/client'

const { t, locale, state: globalState, setSafeError, userError, artistMeetingPosterUrl } = inject('appContext')

const endingCreditsLoadButtonLabelKo = '\uC5D4\uB529 \uD06C\uB808\uB527 \uBD88\uB7EC\uC624\uAE30'
const endingCreditsViewButtonLabelKo = '\uC5D4\uB529 \uD06C\uB808\uB527 \uBCF4\uAE30'

const endingCreditsLeadPreset1 = `\uC81C\uBAA9
AX \uC735\uBCF5\uD569 \uD55C \uCF54\uB4DC \uD504\uB85C\uC81D\uD2B8
\uC81C\uC791 
\uC778\uC0AC\uC774\uD2B8\uD22C\uC5B4 
\uD6C4\uC6D0
\uC804\uC8FC\uBB38\uD654\uACF5\uD310\uC7A5\uC7AC\uB2E8
\uAE30\uD68D  \uC6B4\uC601
\uC0D0\uB7EC\uB4DC\uD30C\uC774 \uC8FC\uC2DD\uD68C\uC0AC 
\uC2A4\uD0ED
\uBC15\uB3C4\uACB8
\uD1B5\uC5ED
\uAC15\uBD09\uC8FC
\uCD9C\uC5F0`
const endingCreditsTailPreset1 = `\u201C\uC774\uC81C \uC6B0\uB9AC\uB294 \uC190\uB2D8\uACFC \uC8FC\uCD5C\uC790\uAC00 \uC544\uB2C8\uB77C\u2026 \uCE5C\uAD6C\uAC00 \uB418\uC5C8\uC2B5\uB2C8\uB2E4!\u201D
\u201CSekarang kita bukan hanya tamu dan tuan rumah\u2026
kita sudah menjadi teman!\u201D

\uC624\uB298 \uC804\uC8FC\uC5D0\uC11C \uD568\uAED8\uD55C \uC2DC\uAC04\uC774
\uC5EC\uB7EC\uBD84\uC5D0\uAC8C \uB530\uB73B\uD55C \uAE30\uC5B5\uC73C\uB85C
\uB0A8\uAE30\uB97C \uBC14\uB78D\uB2C8\uB2E4.
Kami berharap waktu yang kita
habiskan bersama di Jeonju hari
ini
menjadi kenangan hangat bagi
Anda semua.

\uC5EC\uB7EC\uBD84\uACFC \uD568\uAED8 \uD55C\uAD6D\uC758 \uBB38\uD654\uC640
\uB9C8\uC74C\uC744 \uB098\uB20C \uC218 \uC788\uC5B4 \uC815\uB9D0 \uD589
\uBCF5\uD588\uC2B5\uB2C8\uB2E4.
Kami sangat bahagia dapat
berbagi budaya dan hati Korea
bersama Anda.

\uB2E4\uC74C\uC5D0\uB294 \uC5EC\uD589\uC790\uAC00 \uC544\uB2C8\uB77C
\uCE5C\uAD6C\uB85C\uB9CC\uB098\uAE30\uB97C\uAE30\uB300\uD558\uACA0\uC2B5\uB2C8\uB2E4
Semoga di lain waktu kita bisa
bertemu kembali,
bukan hanya sebagai tamu, tetapi
sebagai teman.

\uAC10\uC0AC\uD569\uB2C8\uB2E4.
Terima kasih banyak.`

const endingCreditsState = reactive({
  code: '',
  entries: [],
  isFullscreen: false,
  leadMessage: '',
  tailMessage: '',
  rollDurationSeconds: 18,
  rollGapPx: 72,
  fontScalePercent: 100,
  stopAfterOneCycle: false,
  includeIdentifierEntries: true,
  highlightedCodes: {},
})

const endingCreditsShellRef = ref(null)
const endingCreditsHighlightTimer = ref(null)

const endingCreditsText = computed(() => {
  if (locale.value === 'ko') {
    return {
      title: 'Indonesia Familiarization Tour Ending Credit',
      description: '\uC2DD\uBCC4\uC790 \uCF54\uB4DC\uB97C \uC785\uB825\uD558\uBA74 \uCC38\uC5EC\uC790\uC758 \uC601\uC5B4 \uC774\uB984, \uD55C\uAE00 \uC774\uB984, \uC11C\uC608 \uC11C\uBA85 \uC774\uBBF8\uC9C0\uB97C \uC5D4\uB529 \uD06C\uB808\uB527\uC5D0 \uD45C\uC2DC\uD569\uB2C8\uB2E4.',
      inputLabel: '\u0036\uC790\uB9AC \uC2DD\uBCC4\uC790 \uCF54\uB4DC',
      inputPlaceholder: '\uC22B\uC790 \u0036\uC790\uB9AC\uB97C \uC785\uB825\uD558\uC138\uC694.',
      button: endingCreditsLoadButtonLabelKo,
      englishName: '\uC601\uC5B4 \uC774\uB984',
      koreanName: '\uD55C\uAE00 \uC774\uB984',
      signature: '\uC11C\uC608 \uC11C\uBA85',
      resultTitle: 'Ending Credit Cast',
      stopAfterOneCycle: '\u0031\uD68C \uC7AC\uC0DD \uD6C4 \uBA48\uCD94\uAE30',
      includeIdentifierEntries: '\uC2DD\uBCC4\uC790 \uB9AC\uC2A4\uD2B8 \uBC18\uC601',
    }
  }
  return {
    title: 'Indonesia Familiarization Tour Ending Credit',
    description: 'Enter the identifier code to display the participant English name, Korean name, and calligraphy signature image in the ending credits.',
    inputLabel: '6-digit identifier code',
    inputPlaceholder: 'Enter 6 digits',
    button: 'Load Ending Credits',
    englishName: 'English Name',
    koreanName: 'Korean Name',
    signature: 'Calligraphy Signature',
    resultTitle: 'Ending Credit Cast',
    stopAfterOneCycle: 'Stop after one cycle',
    includeIdentifierEntries: 'Include identifier entries',
  }
})

const endingCreditsMessageGroups = computed(() => [
  ...(endingCreditsState.leadMessage.trim()
    ? [{
        id: 'lead',
        title: 'Opening Message',
        lines: endingCreditsState.leadMessage
          .split('\n')
          .map((line) => line.trim())
          .filter(Boolean),
      }]
    : []),
])
const endingCreditsClosingGroups = computed(() => [
  ...(endingCreditsState.tailMessage.trim()
    ? [{
        id: 'tail',
        title: 'Closing Message',
        lines: endingCreditsState.tailMessage
          .split('\n')
          .map((line) => line.trim())
          .filter(Boolean),
      }]
    : []),
])
const endingCreditsRollEntries = computed(() =>
  endingCreditsState.includeIdentifierEntries
    ? endingCreditsState.entries.filter((entry) => entry.hasKoreanName || entry.hasSignature || entry.hasEnglishName)
    : []
)

function syncEndingCreditsFullscreenState() {
  endingCreditsState.isFullscreen = !!document.fullscreenElement
}

function clearEndingCreditsHighlightInterval() {
  if (endingCreditsHighlightTimer.value) {
    clearInterval(endingCreditsHighlightTimer.value)
    endingCreditsHighlightTimer.value = null
  }
}

function markEndingCreditsHighlight(code) {
  if (!code || endingCreditsState.highlightedCodes[code]) {
    return
  }
  endingCreditsState.highlightedCodes[code] = true
  window.setTimeout(() => {
    delete endingCreditsState.highlightedCodes[code]
  }, 1000)
}

function monitorEndingCreditsCenterLine() {
  const shell = endingCreditsShellRef.value
  if (!shell) {
    return
  }

  const messageCard = shell.querySelector('.ending-credits-message-card')
  if (!messageCard) {
    return
  }

  const cardRect = messageCard.getBoundingClientRect()
  const centerY = cardRect.top + cardRect.height / 2
  const threshold = Math.max(16, Math.min(44, cardRect.height * 0.04))
  const items = shell.querySelectorAll('.ending-credits-cast-group[data-code]')

  items.forEach((item) => {
    const rect = item.getBoundingClientRect()
    const itemCenterY = rect.top + rect.height / 2
    if (Math.abs(itemCenterY - centerY) <= threshold) {
      markEndingCreditsHighlight(item.dataset.code)
    }
  })
}

function ensureEndingCreditsHighlightInterval() {
  clearEndingCreditsHighlightInterval()
  endingCreditsHighlightTimer.value = window.setInterval(monitorEndingCreditsCenterLine, 120)
}

onMounted(() => {
  document.addEventListener('fullscreenchange', syncEndingCreditsFullscreenState)
  ensureEndingCreditsHighlightInterval()
})

onUnmounted(() => {
  document.removeEventListener('fullscreenchange', syncEndingCreditsFullscreenState)
  clearEndingCreditsHighlightInterval()
  revokeEndingCreditsSignatureImage()
})

watch(
  () => [
    endingCreditsState.entries.length,
    endingCreditsState.rollDurationSeconds,
    endingCreditsState.rollGapPx,
    endingCreditsState.fontScalePercent,
    endingCreditsState.leadMessage,
    endingCreditsState.tailMessage,
  ],
  () => {
    window.setTimeout(() => {
      ensureEndingCreditsHighlightInterval()
    }, 0)
  }
)

function applyEndingCreditsLeadPreset(content) {
  endingCreditsState.leadMessage = content
}

function applyEndingCreditsTailPreset(content) {
  endingCreditsState.tailMessage = content
}

function revokeEndingCreditsSignatureImage() {
  for (const entry of endingCreditsState.entries) {
    if (entry.signatureImageUrl) {
      URL.revokeObjectURL(entry.signatureImageUrl)
      entry.signatureImageUrl = ''
    }
  }
}

function onEndingCreditsCodeInput(event) {
  endingCreditsState.code = String(event.target.value || '').replace(/\D/g, '').slice(0, 6)
}

function onEndingCreditsDurationInput(event) {
  const next = Number(event.target.value || 18)
  endingCreditsState.rollDurationSeconds = Math.min(60, Math.max(8, next))
}

function onEndingCreditsGapInput(event) {
  const next = Number(event.target.value || 72)
  endingCreditsState.rollGapPx = Math.min(180, Math.max(24, next))
}

function onEndingCreditsFontScaleInput(event) {
  const next = Number(event.target.value || 100)
  endingCreditsState.fontScalePercent = Math.min(180, Math.max(60, next))
}

async function addEndingCreditsEntry() {
  globalState.loading = true
  globalState.error = ''
  globalState.message = ''

  try {
    const code = endingCreditsState.code.trim()
    if (!/^\d{6}$/.test(code)) {
      throw userError(endingCreditsText.value.inputPlaceholder)
    }

    if (endingCreditsState.entries.some((entry) => entry.code === code)) {
      throw userError('\uC774\uBBF8 \uCD94\uAC00\uB41C \uC2DD\uBCC4\uC790 \uCF54\uB4DC\uC785\uB2C8\uB2E4.')
    }

    const entryRes = await fetch(`${apiRoot}/ending-credits/lookup`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ code }),
    })
    if (!entryRes.ok) throw new Error(await parseErrorResponse(entryRes, t('common.requestFailed')))

    const entry = await entryRes.json()

    const signatureRes = await fetch(`${apiRoot}/certificate-download/signature-image`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ code }),
    })
    if (!signatureRes.ok) throw new Error(await parseErrorResponse(signatureRes, t('common.requestFailed')))

    const signatureBlob = await signatureRes.blob()
    endingCreditsState.entries.push({
      code,
      englishName: entry.englishName || '',
      koreanName: entry.koreanName || '',
      hasEnglishName: !!entry.hasEnglishName,
      hasKoreanName: !!entry.hasKoreanName,
      hasSignature: !!entry.hasSignature,
      signatureImageUrl: URL.createObjectURL(signatureBlob),
    })
    endingCreditsState.code = ''
  } catch (e) {
    setSafeError(e)
  } finally {
    globalState.loading = false
  }
}

async function showEndingCreditsFullscreen() {
  globalState.error = ''

  const target = endingCreditsShellRef.value
  if (!target || !target.requestFullscreen) {
    setSafeError(userError('\uD604\uC7AC \uD658\uACBD\uC5D0\uC11C\uB294 \uC804\uCCB4\uD654\uBA74 \uAE30\uB2A5\uC744 \uC9C0\uC6D0\uD558\uC9C0 \uC54A\uC2B5\uB2C8\uB2E4.'))
    return
  }

  try {
    await target.requestFullscreen()
    endingCreditsState.isFullscreen = true
  } catch (_) {
    setSafeError(userError('\uC804\uCCB4\uD654\uBA74 \uC804\uD658\uC5D0 \uC2E4\uD328\uD588\uC2B5\uB2C8\uB2E4.'))
  }
}

function removeEndingCreditsEntry(code) {
  const target = endingCreditsState.entries.find((entry) => entry.code === code)
  if (target?.signatureImageUrl) {
    URL.revokeObjectURL(target.signatureImageUrl)
  }
  endingCreditsState.entries = endingCreditsState.entries.filter((entry) => entry.code !== code)
}
</script>

<template>
  <section class="ending-credits-page">
    <div ref="endingCreditsShellRef" class="ending-credits-shell" :class="{ 'ending-credits-shell-fullscreen': endingCreditsState.isFullscreen }">
      <div class="ending-credits-hero">
        <article class="ending-credits-poster-card">
          <img :src="artistMeetingPosterUrl" alt="BackToScreen Poster" />
        </article>

        <article class="ending-credits-message-card">
          <div
            :key="String(endingCreditsState.isFullscreen)"
            class="ending-credits-roll"
            :style="{
              '--ending-roll-duration': `${endingCreditsState.rollDurationSeconds}s`,
              '--ending-roll-gap': `${endingCreditsState.rollGapPx}px`,
              '--ending-font-scale': `${endingCreditsState.fontScalePercent / 100}`,
              '--ending-roll-iteration-count': endingCreditsState.stopAfterOneCycle ? '1' : 'infinite',
              '--ending-roll-fill-mode': endingCreditsState.stopAfterOneCycle ? 'forwards' : 'none',
              '--ending-roll-end-transform': endingCreditsState.stopAfterOneCycle ? 'translateY(-100%)' : 'translateY(calc(-100% - 100vh))'
            }"
          >
            <div
              v-for="group in endingCreditsMessageGroups"
              :key="group.id"
              class="ending-credits-group"
            >
              <p class="ending-credits-group-title">{{ group.title }}</p>
              <p
                v-for="line in group.lines"
                :key="`${group.id}-${line}`"
                class="ending-credits-group-line"
              >
                {{ line }}
              </p>
            </div>

            <div
              v-for="entry in endingCreditsRollEntries"
              :key="`roll-${entry.code}`"
              :class="['ending-credits-cast-group', { 'is-highlighted': endingCreditsState.highlightedCodes[entry.code] }]"
              :data-code="entry.code"
            >
              <p v-if="entry.englishName" class="ending-credits-cast-line">{{ entry.englishName }}</p>
              <p v-if="entry.koreanName" class="ending-credits-cast-line ending-credits-cast-line-korean">{{ entry.koreanName }}</p>
              <img
                v-if="entry.signatureImageUrl"
                class="ending-credits-cast-signature"
                :src="entry.signatureImageUrl"
                alt="Calligraphy Signature"
              />
            </div>

            <div
              v-for="group in endingCreditsClosingGroups"
              :key="group.id"
              class="ending-credits-group"
            >
              <p class="ending-credits-group-title">{{ group.title }}</p>
              <p
                v-for="line in group.lines"
                :key="`${group.id}-${line}`"
                class="ending-credits-group-line"
              >
                {{ line }}
              </p>
            </div>
          </div>
        </article>
      </div>

      <article class="ending-credits-control-card">
        <div class="ending-credits-control-header">
          <h2>{{ endingCreditsText.title }}</h2>
          <p>{{ endingCreditsText.description }}</p>
        </div>
        <div class="ending-credits-settings-grid">
          <label class="ending-credits-field ending-credits-field-wide">
            <span>{{ '\uCD5C\uC0C1\uB2E8 \uCCAB \uBB38\uAD6C' }}</span>
            <div class="ending-credits-preset-actions">
              <button
                type="button"
                class="ending-credits-preset-button"
                @click="applyEndingCreditsLeadPreset(endingCreditsLeadPreset1)"
              >
                {{ '\uC0C1\uB2E8 \uCD94\uAC00 \uBB38\uAD6C 1\uBC88' }}
              </button>
            </div>
            <textarea
              v-model="endingCreditsState.leadMessage"
              rows="3"
              :placeholder="'\uAC00\uC7A5 \uBA3C\uC800 \uC62C\uB77C\uAC08 \uBB38\uAD6C\uB97C \uC904\uBC14\uAFC8\uC73C\uB85C \uC785\uB825\uD558\uC138\uC694.'"
            />
          </label>
          <label class="ending-credits-field ending-credits-field-wide">
            <span>{{ '\uCD5C\uD558\uB2E8 \uB9C8\uC9C0\uB9C9 \uBB38\uAD6C' }}</span>
            <div class="ending-credits-preset-actions">
              <button
                type="button"
                class="ending-credits-preset-button"
                @click="applyEndingCreditsTailPreset(endingCreditsTailPreset1)"
              >
                {{ '\uD558\uB2E8 \uCD94\uAC00 \uBB38\uAD6C 1\uBC88' }}
              </button>
            </div>
            <textarea
              v-model="endingCreditsState.tailMessage"
              rows="3"
              :placeholder="'\uAC00\uC7A5 \uB9C8\uC9C0\uB9C9\uC5D0 \uC62C\uB77C\uAC08 \uBB38\uAD6C\uB97C \uC904\uBC14\uAFC8\uC73C\uB85C \uC785\uB825\uD558\uC138\uC694.'"
            />
          </label>
          <label class="ending-credits-field">
            <span>{{ '\uB864\uB9C1 \uC18D\uB3C4(\uCD08)' }}</span>
            <input
              :value="endingCreditsState.rollDurationSeconds"
              type="number"
              min="8"
              max="60"
              step="1"
              @input="onEndingCreditsDurationInput"
            />
          </label>
          <label class="ending-credits-field">
            <span>{{ '\uD56D\uBAA9 \uAC04\uACA9(px)' }}</span>
            <input
              :value="endingCreditsState.rollGapPx"
              type="number"
              min="24"
              max="180"
              step="4"
              @input="onEndingCreditsGapInput"
            />
          </label>
          <label class="ending-credits-field">
            <span>{{ '\uAE00\uC790 \uD06C\uAE30(%)' }}</span>
            <input
              :value="endingCreditsState.fontScalePercent"
              type="number"
              min="60"
              max="180"
              step="5"
              @input="onEndingCreditsFontScaleInput"
            />
          </label>
          <label class="ending-credits-field ending-credits-toggle-field">
            <span>{{ endingCreditsText.stopAfterOneCycle }}</span>
            <input v-model="endingCreditsState.stopAfterOneCycle" type="checkbox" />
          </label>
          <label class="ending-credits-field ending-credits-toggle-field">
            <span>{{ endingCreditsText.includeIdentifierEntries }}</span>
            <input v-model="endingCreditsState.includeIdentifierEntries" type="checkbox" />
          </label>
        </div>
        <div class="ending-credits-control-row">
          <label class="ending-credits-field">
            <span>{{ endingCreditsText.inputLabel }}</span>
            <input
              :value="endingCreditsState.code"
              type="text"
              maxlength="6"
              inputmode="numeric"
              :placeholder="endingCreditsText.inputPlaceholder"
              @input="onEndingCreditsCodeInput"
            />
          </label>
          <button :disabled="globalState.loading" @click="addEndingCreditsEntry">{{ endingCreditsText.button }}</button>
          <button :disabled="globalState.loading" @click="showEndingCreditsFullscreen">{{ endingCreditsViewButtonLabelKo }}</button>
        </div>
      </article>
      <article class="ending-credits-result-card">
        <p class="ending-credits-result-title">{{ endingCreditsText.resultTitle }}</p>
        <div class="ending-credits-list-wrap">
          <table class="ending-credits-list-table">
            <thead>
              <tr>
                <th>Code</th>
                <th>{{ endingCreditsText.englishName }}</th>
                <th>{{ endingCreditsText.koreanName }}</th>
                <th>{{ endingCreditsText.signature }}</th>
                <th>{{ '\uAD00\uB9AC' }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="entry in endingCreditsState.entries" :key="entry.code">
                <td>{{ entry.code }}</td>
                <td>
                  <span class="ending-credits-ready-badge" :class="{ 'is-ready': entry.hasEnglishName }">
                    {{ entry.hasEnglishName ? '\uC900\uBE44\uB428' : '\uBBF8\uC900\uBE44' }}
                  </span>
                  <strong v-if="entry.englishName">{{ entry.englishName }}</strong>
                </td>
                <td>
                  <span class="ending-credits-ready-badge" :class="{ 'is-ready': entry.hasKoreanName }">
                    {{ entry.hasKoreanName ? '\uC900\uBE44\uB428' : '\uBBF8\uC900\uBE44' }}
                  </span>
                  <strong v-if="entry.koreanName">{{ entry.koreanName }}</strong>
                </td>
                <td>
                  <span class="ending-credits-ready-badge" :class="{ 'is-ready': entry.hasSignature }">
                    {{ entry.hasSignature ? '\uC900\uBE44\uB428' : '\uBBF8\uC900\uBE44' }}
                  </span>
                </td>
                <td>
                  <button class="ending-credits-delete-button" type="button" @click="removeEndingCreditsEntry(entry.code)">{{ '\uC0AD\uC81C' }}</button>
                </td>
              </tr>
              <tr v-if="endingCreditsState.entries.length === 0">
                <td colspan="5">{{ '\uCD94\uAC00\uB41C \uC2DD\uBCC4\uC790 \uCF54\uB4DC\uAC00 \uC544\uC9C1 \uC5C6\uC2B5\uB2C8\uB2E4.' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </article>
    </div>
  </section>
</template>
