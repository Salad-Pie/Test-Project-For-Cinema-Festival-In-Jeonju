<script setup>
import { inject, reactive, ref, onMounted } from 'vue'
import { apiRoot } from '../config/api'
import { parseErrorResponse } from '../api/client'
import { parseJwtPayload } from '../utils/jwt'

const { t, state: globalState, setSafeError, userError, apiFetch } = inject('appContext')

const tabletState = reactive({
  verifyCode: '',
  verifiedToken: '',
  signatureNameLanguage: 'EN',
  signatureKoreanName: '',
  signaturePreview: {
    token: '',
    recognizedText: '',
    englishName: '',
    koreanName: '',
    koreanMeaningText: '',
    detectedLanguage: '',
    ocrConfidence: null,
  },
})

const canvasRef = ref(null)
const drawing = ref(false)

onMounted(() => {
  const q = new URLSearchParams(window.location.search)
  const tabletToken = q.get('token') || ''
  
  const canvas = canvasRef.value
  if (canvas) {
    const ctx = canvas.getContext('2d')
    ctx.fillStyle = '#fff'
    ctx.fillRect(0, 0, canvas.width, canvas.height)
    ctx.strokeStyle = '#111'
    ctx.lineWidth = 2
    ctx.lineCap = 'round'
  }

  if (tabletToken) {
    const payload = parseJwtPayload(tabletToken)
    if (payload?.type === 'VERIFIED') {
      tabletState.verifiedToken = tabletToken
      globalState.message = t('tablet.verifiedDirect')
    }
  }
})

async function verifyOnTablet() {
  globalState.loading = true
  globalState.error = ''
  try {
    const result = await apiFetch('/verify', {
      method: 'POST',
      body: JSON.stringify({ code: tabletState.verifyCode }),
    })
    tabletState.verifiedToken = result.verifiedToken
    resetSignaturePreview()
    globalState.message = t('tablet.verifiedDone')
  } catch (e) {
    setSafeError(e)
  } finally {
    globalState.loading = false
  }
}

function pointerPos(e, canvas) {
  const rect = canvas.getBoundingClientRect()
  return { x: e.clientX - rect.left, y: e.clientY - rect.top }
}

function startDraw(e) {
  const canvas = canvasRef.value
  if (!canvas) return
  drawing.value = true
  const ctx = canvas.getContext('2d')
  const p = pointerPos(e, canvas)
  ctx.beginPath()
  ctx.moveTo(p.x, p.y)
}

function draw(e) {
  if (!drawing.value) return
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  const p = pointerPos(e, canvas)
  ctx.lineTo(p.x, p.y)
  ctx.stroke()
}

function endDraw() {
  drawing.value = false
}

function clearSignature() {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  ctx.fillStyle = '#fff'
  ctx.fillRect(0, 0, canvas.width, canvas.height)
  resetSignaturePreview()
}

function resetSignaturePreview() {
  tabletState.signaturePreview.token = ''
  tabletState.signaturePreview.recognizedText = ''
  tabletState.signaturePreview.englishName = ''
  tabletState.signaturePreview.koreanName = ''
  tabletState.signaturePreview.koreanMeaningText = ''
  tabletState.signaturePreview.detectedLanguage = ''
  tabletState.signaturePreview.ocrConfidence = null
}

async function createTabletSignatureFormData() {
  const canvas = canvasRef.value
  if (!canvas) return null

  const signatureBlob = await new Promise((resolve, reject) => {
    canvas.toBlob((blob) => {
      if (blob) resolve(blob)
      else reject(userError(t('tablet.signatureImageFailed')))
    }, 'image/png')
  })

  const formData = new FormData()
  formData.append('signatureImage', signatureBlob, 'signature.png')
  formData.append('nameLanguage', tabletState.signatureNameLanguage)
  if (tabletState.signatureKoreanName.trim()) {
    formData.append('koreanName', tabletState.signatureKoreanName.trim())
  }
  return formData
}

async function submitSignature() {
  if (!canvasRef.value) return

  globalState.loading = true
  globalState.error = ''
  try {
    const formData = await createTabletSignatureFormData()
    const res = await fetch(`${apiRoot}/auth/signature`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${tabletState.verifiedToken}` },
      body: formData,
    })
    if (!res.ok) throw new Error(await parseErrorResponse(res, t('common.requestFailed')))
    globalState.message = t('tablet.signatureSaved')
    setTimeout(() => {
      window.location.reload()
    }, 1500)
  } catch (e) {
    setSafeError(e)
  } finally {
    globalState.loading = false
  }
}

async function previewSignatureOnTablet() {
  if (!canvasRef.value) return

  globalState.loading = true
  globalState.error = ''
  try {
    const formData = await createTabletSignatureFormData()
    const res = await fetch(`${apiRoot}/auth/signature/preview`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${tabletState.verifiedToken}` },
      body: formData,
    })
    if (!res.ok) throw new Error(await parseErrorResponse(res, t('common.requestFailed')))
    const result = await res.json()
    tabletState.signaturePreview.token = result.previewToken || ''
    tabletState.signaturePreview.recognizedText = result.recognizedText || ''
    tabletState.signaturePreview.englishName = result.englishName || ''
    tabletState.signaturePreview.koreanName = result.koreanName || ''
    tabletState.signaturePreview.koreanMeaningText = result.koreanMeaningText || ''
    tabletState.signaturePreview.detectedLanguage = result.detectedLanguage || ''
    tabletState.signaturePreview.ocrConfidence = result.ocrConfidence
    globalState.message = 'OCR 결과를 확인한 후 저장해 주세요.'
  } catch (e) {
    resetSignaturePreview()
    setSafeError(e)
  } finally {
    globalState.loading = false
  }
}

async function confirmPreviewSignatureOnTablet() {
  if (!tabletState.signaturePreview.token) {
    setSafeError(userError('먼저 OCR 결과를 확인해 주세요.'))
    return
  }

  globalState.loading = true
  globalState.error = ''
  try {
    const res = await fetch(`${apiRoot}/auth/signature/confirm`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${tabletState.verifiedToken}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        previewToken: tabletState.signaturePreview.token,
      }),
    })
    if (!res.ok) throw new Error(await parseErrorResponse(res, t('common.requestFailed')))
    resetSignaturePreview()
    globalState.message = '서명이 저장되었습니다.'
    setTimeout(() => {
      window.location.reload()
    }, 1500)
  } catch (e) {
    setSafeError(e)
  } finally {
    globalState.loading = false
  }
}

</script>

<template>
  <section class="card">
    <h2>{{ t('tablet.verifyTitle') }}</h2>
    <input v-model="tabletState.verifyCode" type="text" maxlength="6" :placeholder="t('tablet.verifyPlaceholder')" />
    <button :disabled="globalState.loading" @click="verifyOnTablet">{{ t('tablet.verify') }}</button>

    <h2>{{ t('tablet.signatureTitle') }}</h2>
    <label class="field">
      <span>{{ t('tablet.nameLanguage') }}</span>
      <select v-model="tabletState.signatureNameLanguage">
        <option value="EN">{{ t('tablet.languageOptions.en') }}</option>
        <option value="FR">{{ t('tablet.languageOptions.fr') }}</option>
        <option value="DE">{{ t('tablet.languageOptions.de') }}</option>
        <option value="JA">{{ t('tablet.languageOptions.ja') }}</option>
        <option value="ZH">{{ t('tablet.languageOptions.zh') }}</option>
        <option value="VI">{{ t('tablet.languageOptions.vi') }}</option>
        <option value="ES">{{ t('tablet.languageOptions.es') }}</option>
        <option value="IT">{{ t('tablet.languageOptions.it') }}</option>
        <option value="OTHER">{{ t('tablet.languageOptions.other') }}</option>
      </select>
    </label>
    <input
      v-model="tabletState.signatureKoreanName"
      type="text"
      :placeholder="t('tablet.koreanNamePlaceholder')"
    />
    <canvas
      ref="canvasRef"
      class="signature"
      width="600"
      height="220"
      @pointerdown="startDraw"
      @pointermove="draw"
      @pointerup="endDraw"
      @pointerleave="endDraw"
    />
    <div class="actions">
      <button :disabled="globalState.loading" @click="clearSignature">{{ t('tablet.clear') }}</button>
      <button :disabled="globalState.loading || !tabletState.verifiedToken" @click="submitSignature">{{ t('tablet.save') }}</button>
    </div>
    <div class="actions">
      <button :disabled="globalState.loading || !tabletState.verifiedToken" @click="previewSignatureOnTablet">OCR 확인</button>
      <button :disabled="globalState.loading || !tabletState.verifiedToken || !tabletState.signaturePreview.token" @click="confirmPreviewSignatureOnTablet">확인 후 저장</button>
    </div>
    <div v-if="tabletState.signaturePreview.token" class="card">
      <h3>OCR 결과 확인</h3>
      <p><strong>OCR 결과:</strong> {{ tabletState.signaturePreview.recognizedText || '-' }}</p>
      <p><strong>영어 이름:</strong> {{ tabletState.signaturePreview.englishName || '-' }}</p>
      <p><strong>한글 이름:</strong> {{ tabletState.signaturePreview.koreanName || '-' }}</p>
    </div>
  </section>
</template>
