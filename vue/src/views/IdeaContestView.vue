<script setup>
import { inject, reactive } from 'vue'
import { apiRoot } from '../config/api'
import { parseErrorResponse } from '../api/client'
import { getIdeaContestAuthToken } from '../utils/authStorage'

const { t, state: globalState, setSafeError, userError, goToSuccessPage, ideaPosterUrl } = inject('appContext')

const ideaContestState = reactive({
  files: [],
})

function onIdeaFilesChange(event) {
  ideaContestState.files = Array.from(event.target.files || [])
}

async function submitIdeaContest() {
  globalState.loading = true
  globalState.error = ''
  try {
    const authToken = getIdeaContestAuthToken()
    if (!authToken) {
      throw userError(t('common.loginTokenRequired'))
    }

    const formData = new FormData()
    for (const file of ideaContestState.files) {
      formData.append('images', file)
    }

    const res = await fetch(`${apiRoot}/idea-contests`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${authToken}`,
      },
      body: formData,
    })

    if (!res.ok) {
      throw new Error(await parseErrorResponse(res, t('common.requestFailed')))
    }

    const json = await res.json()
    goToSuccessPage(t('idea.submitted', { count: json.images?.length ?? 0 }))
  } catch (e) {
    setSafeError(e)
  } finally {
    globalState.loading = false
  }
}
</script>

<template>
  <section class="idea-shell">
    <div class="card idea-pane">
      <div class="idea-form">
        <h2>{{ t('idea.title') }}</h2>
        <label>{{ t('idea.memoLabel') }}</label>
        <input type="file" multiple accept="image/*" :aria-label="t('common.placeholders.imageFile')" @change="onIdeaFilesChange" />
        <p>{{ t('idea.selectedFiles', { count: ideaContestState.files.length }) }}</p>
        <button :disabled="globalState.loading" @click="submitIdeaContest">{{ t('idea.submit') }}</button>
      </div>
    </div>
    <div class="card idea-pane">
      <div class="idea-poster">
        <img :src="ideaPosterUrl" alt="Docker Poster Placeholder" />
      </div>
    </div>
  </section>
</template>
