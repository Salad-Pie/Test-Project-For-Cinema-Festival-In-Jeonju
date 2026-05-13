<script setup>
import { reactive, inject, watch } from 'vue'
import { apiRoot } from '../config/api'
import { formatPhoneInput } from '../utils/format'
import { parseErrorResponse } from '../api/client'

const { t, state: globalState, setSafeError, goToSuccessPage, userError, ideaPosterUrl } = inject('appContext')

const streetHourOptions = [17, 18, 19, 20, 21, 22]

const streetState = reactive({
  reservationDate: '',
  reservationHour: '17',
  name: '',
  phoneNumber: '',
  availability: null,
  checkingAvailability: false,
})

watch(
  () => [streetState.reservationDate, streetState.reservationHour],
  async () => {
    if (!streetState.reservationDate) {
      streetState.availability = null
      return
    }
    await fetchStreetAvailability()
  }
)

async function fetchStreetAvailability() {
  streetState.checkingAvailability = true
  try {
    const reservationAt = `${streetState.reservationDate}T${streetState.reservationHour.padStart(2, '0')}:00:00`
    const query = encodeURIComponent(reservationAt)
    const res = await fetch(`${apiRoot}/street-collaboration-reservations/availability?reservationAt=${query}`)

    if (!res.ok) {
      throw new Error(await parseErrorResponse(res, t('common.requestFailed')))
    }

    streetState.availability = await res.json()
  } catch (e) {
    setSafeError(e)
    streetState.availability = null
  } finally {
    streetState.checkingAvailability = false
  }
}

function onStreetPhoneInput(event) {
  streetState.phoneNumber = formatPhoneInput(event)
}

async function submitStreetCollaboration() {
  globalState.loading = true
  globalState.error = ''
  try {
    if (!streetState.reservationDate) {
      throw userError(t('street.reservationDateRequired'))
    }
    if (streetState.availability && !streetState.availability.available) {
      throw userError(t('street.full'))
    }

    const payload = {
      reservationAt: `${streetState.reservationDate}T${streetState.reservationHour.padStart(2, '0')}:00:00`,
      name: streetState.name,
      phoneNumber: streetState.phoneNumber,
    }

    const res = await fetch(`${apiRoot}/street-collaboration-reservations`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    })

    if (!res.ok) {
      throw new Error(await parseErrorResponse(res, t('common.requestFailed')))
    }

    await res.json()
    goToSuccessPage(t('street.submitted'))
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
        <h2>{{ t('street.title') }}</h2>
        <div class="grid">
          <label>{{ t('street.reservationDate') }} <input v-model="streetState.reservationDate" type="date" :placeholder="t('common.placeholders.date')" /></label>
          <label>{{ t('street.reservationHour') }}
            <select v-model="streetState.reservationHour">
              <option v-for="hour in streetHourOptions" :key="hour" :value="String(hour)">{{ hour }}:00</option>
            </select>
          </label>
          <label>{{ t('street.name') }} <input v-model="streetState.name" type="text" :placeholder="t('common.placeholders.name')" /></label>
          <label>{{ t('street.phone') }} <input v-model="streetState.phoneNumber" type="text" :placeholder="t('common.placeholders.phone')" @input="onStreetPhoneInput" /></label>
        </div>
        <button :disabled="globalState.loading || streetState.checkingAvailability || (streetState.availability && !streetState.availability.available)" @click="submitStreetCollaboration">{{ t('street.submit') }}</button>
        <p v-if="streetState.checkingAvailability">{{ t('street.checking') }}</p>
        <p v-else-if="streetState.availability" :class="streetState.availability.available ? 'ok' : 'error'">
          {{ t('street.availability', { current: streetState.availability.currentCount, capacity: streetState.availability.capacity, remaining: streetState.availability.remaining }) }}
        </p>
        <p v-if="streetState.availability && !streetState.availability.available" class="error">{{ t('street.full') }}</p>
      </div>
    </div>
    <div class="card idea-pane">
      <div class="idea-poster">
        <img :src="ideaPosterUrl" alt="Poster Placeholder" />
      </div>
    </div>
  </section>
</template>
