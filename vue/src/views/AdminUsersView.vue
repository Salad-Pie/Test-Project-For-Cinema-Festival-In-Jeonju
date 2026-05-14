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
    const url = new URL(`${apiRoot}/admin/users`, window.location.origin)
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

    <!-- 필터 및 검색 영역 -->
    <div class="filter-section p-4 mb-4 rounded-4 shadow-sm border border-info-subtle bg-white">
      <div class="row align-items-end g-3">
        <div class="col-md-4">
          <label class="form-label fw-bold text-secondary mb-2">
            <i class="bi bi-filter-circle me-1"></i> {{ t('adminUsers.role') }} 필터
          </label>
          <select class="form-select form-select-lg" v-model="usersState.selectedRole" :disabled="usersState.loading">
            <option value="">전체 권한 보기</option>
            <option value="USER">일반 사용자 (USER)</option>
            <option value="ADMIN">관리자 (ADMIN)</option>
          </select>
        </div>
        <div class="col-md-8 text-end">
          <span class="text-muted small me-3" v-if="usersState.users.length > 0">
            총 <strong>{{ usersState.users.length }}</strong>명의 사용자가 검색되었습니다.
          </span>
        </div>
      </div>
    </div>
    
    <div class="admin-table-wrap shadow-sm rounded-4 overflow-hidden border">
      <table class="admin-table table table-hover mb-0 align-middle">
        <thead class="table-light">
          <tr>
            <th class="py-3 px-4">{{ t('adminReservations.id') }}</th>
            <th class="py-3 px-4">사용자 정보</th>
            <th class="py-3 px-4">{{ t('adminUsers.role') }}</th>
            <th class="py-3 px-4">{{ t('adminUsers.lastAccess') }}</th>
            <th class="py-3 px-4 text-center">{{ t('adminUsers.manageRole') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in usersState.users" :key="user.id">
            <td class="px-4 text-muted">#{{ user.id }}</td>
            <td class="px-4">
              <div class="d-flex flex-column">
                <span class="fw-bold text-dark">{{ user.email || user.phone || '정보 없음' }}</span>
                <span class="small text-muted" v-if="user.nickname && user.nickname !== 'nickname'">{{ user.nickname }}</span>
              </div>
            </td>
            <td class="px-4">
              <span class="badge rounded-pill" :class="user.role === 'ADMIN' ? 'bg-danger-subtle text-danger border border-danger-subtle' : 'bg-primary-subtle text-primary border border-primary-subtle'">
                {{ user.role }}
              </span>
            </td>
            <td class="px-4 text-secondary small">
              {{ user.lastLoginAt ? new Date(user.lastLoginAt).toLocaleString() : '접속 기록 없음' }}
            </td>
            <td class="px-4 text-center">
              <button 
                class="btn btn-sm px-3 rounded-pill" 
                :class="user.role === 'ADMIN' ? 'btn-outline-secondary' : 'btn-outline-danger'"
                @click="toggleRole(user)"
              >
                <i class="bi" :class="user.role === 'ADMIN' ? 'bi-person-dash' : 'bi-person-up'"></i>
                {{ user.role === 'ADMIN' ? '일반으로 강등' : '관리자 승격' }}
              </button>
            </td>
          </tr>
          <tr v-if="usersState.users.length === 0 && !usersState.loading">
            <td colspan="5" class="text-center py-5">
              <div class="py-4">
                <i class="bi bi-people text-muted display-1 d-block mb-3"></i>
                <p class="text-muted mb-0">조회된 사용자가 없습니다.</p>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<style scoped>
.admin-users {
  padding: 2.5rem;
  background-color: #f8faff;
}

.filter-section {
  transition: transform 0.2s ease;
}

.filter-section:hover {
  transform: translateY(-2px);
}

.admin-table-wrap {
  background: white;
  border: none !important;
}

.admin-table th {
  background-color: #f8f9fa;
  font-size: 0.85rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: #6c757d;
  border-bottom: 2px solid #dee2e6;
}

.admin-table tbody tr {
  transition: background-color 0.2s ease;
}

.admin-table tbody tr:hover {
  background-color: #f1f7ff !important;
}

.badge {
  font-size: 0.75rem;
  padding: 0.5em 1em;
  font-weight: 700;
}

.btn-sm {
  font-size: 0.8rem;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 5px;
}
</style>
