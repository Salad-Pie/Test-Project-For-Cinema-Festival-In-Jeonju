<script setup>
import { inject, reactive, onMounted } from 'vue'
import { apiRoot } from '../config/api'
import { getIdeaContestAuthToken } from '../utils/authStorage'
import { parseErrorResponse } from '../api/client'

const { t, state: globalState, setSafeError, userError } = inject('appContext')

const adminReservationsState = reactive({
  types: [],
  items: [],
  selectedType: '',
  date: '',
  time: '',
  projectKey: '',
})

async function fetchAdminReservationTypes() {
  const authToken = getIdeaContestAuthToken()
  if (!authToken) throw userError(t('common.loginTokenRequired'))
  const res = await fetch(`${apiRoot}/admin/reservations/types`, {
    headers: { Authorization: `Bearer ${authToken}` },
  })
  if (!res.ok) throw new Error(await parseErrorResponse(res, t('common.requestFailed')))
  adminReservationsState.types = await res.json()
}

async function fetchAdminReservations() {
  globalState.loading = true
  globalState.error = ''
  try {
    const authToken = getIdeaContestAuthToken()
    if (!authToken) throw userError(t('common.loginTokenRequired'))

    const params = new URLSearchParams()
    if (adminReservationsState.selectedType) params.set('type', adminReservationsState.selectedType)
    if (adminReservationsState.date) params.set('date', adminReservationsState.date)
    if (adminReservationsState.time) params.set('time', `${adminReservationsState.time}:00`)
    if (adminReservationsState.projectKey) params.set('projectKey', adminReservationsState.projectKey)

    const query = params.toString()
    const res = await fetch(`${apiRoot}/admin/reservations${query ? `?${query}` : ''}`, {
      headers: { Authorization: `Bearer ${authToken}` },
    })
    if (!res.ok) throw new Error(await parseErrorResponse(res, t('common.requestFailed')))
    adminReservationsState.items = await res.json()
  } catch (e) {
    setSafeError(e)
  } finally {
    globalState.loading = false
  }
}

onMounted(() => {
  fetchAdminReservationTypes().catch(() => {})
  fetchAdminReservations()
})
</script>

<template>
  <section class="card admin-reservations">
    <h2>{{ t('adminReservations.title') }}</h2>
    <div class="grid">
      <label>{{ t('adminReservations.type') }}
        <select v-model="adminReservationsState.selectedType">
          <option value="">{{ t('adminReservations.allTypes') }}</option>
          <option v-for="type in adminReservationsState.types" :key="type.type" :value="type.type">
            {{ type.label }} ({{ type.count }})
          </option>
        </select>
      </label>
      <label>{{ t('adminReservations.date') }}
        <input v-model="adminReservationsState.date" type="date" :placeholder="t('common.placeholders.date')" />
      </label>
      <label>{{ t('adminReservations.time') }}
        <input v-model="adminReservationsState.time" type="time" step="3600" :placeholder="t('common.placeholders.time')" />
      </label>
      <label>{{ t('adminReservations.projectKey') }}
        <input v-model="adminReservationsState.projectKey" type="text" :placeholder="t('adminReservations.projectKeyPlaceholder')" />
      </label>
    </div>
    <div class="actions">
      <button :disabled="globalState.loading" @click="fetchAdminReservations">{{ t('adminReservations.search') }}</button>
    </div>
    <div class="admin-table-wrap">
      <table class="admin-table">
        <thead>
          <tr>
            <th>{{ t('adminReservations.type') }}</th>
            <th>{{ t('adminReservations.id') }}</th>
            <th>{{ t('adminReservations.name') }}</th>
            <th>{{ t('adminReservations.phone') }}</th>
            <th>{{ t('adminReservations.email') }}</th>
            <th>{{ t('adminReservations.projectKey') }}</th>
            <th>{{ t('adminReservations.reservationDate') }}</th>
            <th>{{ t('adminReservations.reservationTime') }}</th>
            <th>{{ t('adminReservations.amount') }}</th>
            <th>{{ t('adminReservations.payment') }}</th>
            <th>{{ t('adminReservations.provider') }}</th>
            <th>{{ t('adminReservations.account') }}</th>
            <th>{{ t('adminReservations.createdAt') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in adminReservationsState.items" :key="`${item.type}-${item.id}`">
            <td>{{ item.type }}</td>
            <td>{{ item.id || '-' }}</td>
            <td>{{ item.name || '-' }}</td>
            <td>{{ item.phoneNumber || '-' }}</td>
            <td>{{ item.userEmail || '-' }}</td>
            <td>{{ item.projectKey || '-' }}</td>
            <td>{{ item.date || '-' }}</td>
            <td>{{ item.time || '-' }}</td>
            <td>{{ item.amount || '-' }}</td>
            <td>{{ item.paymentMethodType || '-' }}</td>
            <td>{{ item.paymentProviderName || '-' }}</td>
            <td>{{ item.bankAccountMasked || '-' }}</td>
            <td>{{ item.createdAt || '-' }}</td>
          </tr>
          <tr v-if="adminReservationsState.items.length === 0">
            <td colspan="13">{{ t('adminReservations.empty') }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
