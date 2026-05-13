<script setup>
import { inject, reactive } from 'vue'
import { apiRoot } from '../config/api'
import { parseErrorResponse } from '../api/client'

const { t, state: globalState, setSafeError, userError } = inject('appContext')

const certificateDownloadState = reactive({
  code: '',
})

async function downloadCertificateArtifactByCode(path, filename) {
  globalState.loading = true
  globalState.error = ''
  try {
    const code = certificateDownloadState.code.trim()
    if (!/^\d{6}$/.test(code)) {
      throw userError(t('certificateDownload.invalidCode'))
    }

    const res = await fetch(`${apiRoot}${path}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ code }),
    })
    if (!res.ok) throw new Error(await parseErrorResponse(res, t('common.requestFailed')))

    const blob = await res.blob()
    const url = URL.createObjectURL(blob)
    const anchor = document.createElement('a')
    anchor.href = url
    anchor.download = filename
    document.body.appendChild(anchor)
    anchor.click()
    anchor.remove()
    URL.revokeObjectURL(url)
    globalState.message = t('certificateDownload.downloaded')
  } catch (e) {
    setSafeError(e)
  } finally {
    globalState.loading = false
  }
}

function downloadStoredSignatureImage() {
  return downloadCertificateArtifactByCode('/certificate-download/signature-image', 'signature-image.png')
}

function downloadStoredCertificatePdf() {
  return downloadCertificateArtifactByCode('/certificate-download/certificate-pdf', 'certificate.pdf')
}
</script>

<template>
  <section class="card certificate-download-page">
    <h2>{{ t('certificateDownload.title') }}</h2>
    <p>{{ t('certificateDownload.description') }}</p>
    <label class="field">
      <span>{{ t('certificateDownload.identifierCode') }}</span>
      <input
        v-model="certificateDownloadState.code"
        type="text"
        maxlength="6"
        inputmode="numeric"
        :placeholder="t('certificateDownload.placeholder')"
      />
    </label>
    <div class="actions">
      <button :disabled="globalState.loading" @click="downloadStoredSignatureImage">{{ t('certificateDownload.signatureImage') }}</button>
      <button :disabled="globalState.loading" @click="downloadStoredCertificatePdf">{{ t('certificateDownload.certificatePdf') }}</button>
    </div>
  </section>
</template>
