<script setup>
import { reactive, inject, watch } from 'vue'
import { apiRoot } from '../config/api'
import { getIdeaContestAuthToken } from '../utils/authStorage'
import { parseErrorResponse } from '../api/client'

const { t, state: globalState, setSafeError, goToSuccessPage, userError, artistMeetingPosterUrl } = inject('appContext')

const artistMeetingDateOptions = ['2026-05-02', '2026-05-04']
const artistMeetingHourMap = {
  '2026-05-02': [17, 19],
  '2026-05-04': [19, 21],
}

const artistMeetingState = reactive({
  date: '2026-05-02',
  hour: '17',
})

watch(
  () => artistMeetingState.date,
  (nextDate) => {
    const hours = artistMeetingHourMap[nextDate] || []
    if (!hours.includes(Number(artistMeetingState.hour))) {
      artistMeetingState.hour = hours.length > 0 ? String(hours[0]) : ''
    }
  }
)

async function submitArtistMeetingReservation() {
  globalState.loading = true
  globalState.error = ''
  try {
    const authToken = getIdeaContestAuthToken()
    if (!authToken) {
      throw userError(t('common.loginTokenRequired'))
    }

    const payload = {
      date: artistMeetingState.date,
      time: `${String(artistMeetingState.hour).padStart(2, '0')}:00:00`,
    }

    const res = await fetch(`${apiRoot}/exhibition-artist-meeting-reservations`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${authToken}`,
      },
      body: JSON.stringify(payload),
    })

    if (!res.ok) {
      const errorMsg = await parseErrorResponse(res, t('common.requestFailed'))
      throw new Error(errorMsg)
    }

    await res.json()
    goToSuccessPage(t('artistMeeting.submitted'))
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
        <h2>{{ t('artistMeeting.title') }}</h2>
        <div class="grid">
          <label>{{ t('artistMeeting.date') }}
            <select v-model="artistMeetingState.date">
              <option v-for="date in artistMeetingDateOptions" :key="date" :value="date">{{ date }}</option>
            </select>
          </label>
          <label>{{ t('artistMeeting.time') }}
            <select v-model="artistMeetingState.hour">
              <option v-for="hour in (artistMeetingHourMap[artistMeetingState.date] || [])" :key="hour" :value="String(hour)">{{ hour }}:00</option>
            </select>
          </label>
        </div>
        <button :disabled="globalState.loading" @click="submitArtistMeetingReservation">{{ t('artistMeeting.submit') }}</button>
      </div>
    </div>
    <div class="card idea-pane">
      <div class="idea-poster">
        <img :src="artistMeetingPosterUrl" alt="Artist meeting poster" />
      </div>
    </div>
  </section>
</template>
