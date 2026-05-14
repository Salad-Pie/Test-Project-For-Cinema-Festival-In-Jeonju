<script setup>
import { inject, reactive, onMounted } from 'vue'
import { apiRoot } from '../config/api'
import { getIdeaContestAuthToken } from '../utils/authStorage'
import { parseErrorResponse } from '../api/client'

/**
 * 전역 컨텍스트 주입
 */
const { t, state: globalState, setSafeError } = inject('appContext')

/**
 * 사용자 목록 상태
 */
const usersState = reactive({
  users: [],
  loading: false
})

/**
 * 백엔드로부터 실제 사용자 목록 로드
 */
async function fetchUsers() {
  usersState.loading = true
  try {
    const authToken = getIdeaContestAuthToken()
    const res = await fetch(`${apiRoot}/admin/users`, {
      headers: { Authorization: `Bearer ${authToken}` },
    })
    
    if (!res.ok) throw new Error(await parseErrorResponse(res, t('common.requestFailed')))
    
    usersState.users = await res.json()
  } catch (e) {
    setSafeError(e)
  } finally {
    usersState.loading = false
  }
}

/**
 * 사용자 권한 변경 (USER <-> ADMIN)
 */
async function toggleRole(user) {
  const newRole = user.role === 'ADMIN' ? 'USER' : 'ADMIN'
  if (!confirm(`${user.email}의 권한을 ${newRole}(으)로 변경하시겠습니까?`)) return

  try {
    const authToken = getIdeaContestAuthToken()
    const res = await fetch(`${apiRoot}/admin/users/${user.id}/role`, {
      method: 'PATCH',
      headers: { 
        'Authorization': `Bearer ${authToken}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ role: newRole })
    })

    if (!res.ok) throw new Error(await parseErrorResponse(res, t('common.requestFailed')))
    
    // 성공 시 로컬 데이터 업데이트
    user.role = newRole
  } catch (e) {
    setSafeError(e)
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<template>
  <section class="card admin-users">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2 class="mb-0">{{ t('adminUsers.title') }}</h2>
      <button class="btn btn-outline-primary btn-sm" @click="fetchUsers" :disabled="usersState.loading">
        {{ t('common.refresh') || '새로고침' }}
      </button>
    </div>
    
    <div class="admin-table-wrap shadow-sm rounded">
      <table class="admin-table table table-hover mb-0">
        <thead class="table-light">
          <tr>
            <th>ID</th>
            <th>{{ t('adminReservations.email') }}</th>
            <th>{{ t('adminUsers.role') }}</th>
            <th>{{ t('adminUsers.lastAccess') }}</th>
            <th>{{ t('adminUsers.manageRole') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in usersState.users" :key="user.id">
            <td>{{ user.id }}</td>
            <td class="fw-bold">{{ user.email }}</td>
            <td>
              <span class="badge" :class="user.role === 'ADMIN' ? 'bg-danger' : 'bg-success'">
                {{ user.role }}
              </span>
            </td>
            <td>{{ user.lastLoginAt ? new Date(user.lastLoginAt).toLocaleString() : '-' }}</td>
            <td>
              <button 
                class="btn btn-sm" 
                :class="user.role === 'ADMIN' ? 'btn-outline-secondary' : 'btn-outline-danger'"
                @click="toggleRole(user)"
              >
                {{ user.role === 'ADMIN' ? 'USER로 변경' : 'ADMIN으로 승격' }}
              </button>
            </td>
          </tr>
          <tr v-if="usersState.users.length === 0 && !usersState.loading">
            <td colspan="5" class="text-center py-5 text-muted">사용자가 없습니다.</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<style scoped>
.admin-users {
  padding: 2rem;
}
.admin-table-wrap {
  overflow-x: auto;
  background: white;
}
.admin-table th {
  font-weight: 600;
  color: #495057;
}
.badge {
  font-size: 0.85rem;
  padding: 0.4em 0.8em;
}
</style>
