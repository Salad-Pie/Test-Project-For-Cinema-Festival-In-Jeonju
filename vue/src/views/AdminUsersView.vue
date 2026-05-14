<script setup>
import { inject, reactive, onMounted, watch } from 'vue'
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
  loading: false,
  selectedRole: '' // 필터링을 위한 선택된 권한
})

/**
 * 백엔드로부터 실제 사용자 목록 로드
 */
async function fetchUsers() {
  usersState.loading = true
  try {
    const authToken = getIdeaContestAuthToken()
    const url = new URL(`${apiRoot}/admin/users`)
    if (usersState.selectedRole) {
      url.searchParams.append('role', usersState.selectedRole)
    }

    const res = await fetch(url, {
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
    // 필터링 중일 경우 목록에서 사라져야 할 수 있으므로 다시 불러오기
    if (usersState.selectedRole) fetchUsers()
  } catch (e) {
    setSafeError(e)
  }
}

// 필터 변경 시 자동 재조회
watch(() => usersState.selectedRole, () => {
  fetchUsers()
})

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

    <!-- 권한 필터 영역 -->
    <div class="mb-4 row">
      <div class="col-md-4">
        <label class="form-label small text-muted">{{ t('adminUsers.role') }} 필터</label>
        <select class="form-select" v-model="usersState.selectedRole" :disabled="usersState.loading">
          <option value="">전체 권한</option>
          <option value="USER">USER (일반 사용자)</option>
          <option value="ADMIN">ADMIN (관리자)</option>
        </select>
      </div>
    </div>
    
    <div class="admin-table-wrap shadow-sm rounded">
      <table class="admin-table table table-hover mb-0">
        <thead class="table-light">
          <tr>
            <th>{{ t('adminReservations.id') }}</th>
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
            <td colspan="5" class="text-center py-5 text-muted">해당 권한의 사용자가 없습니다.</td>
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
