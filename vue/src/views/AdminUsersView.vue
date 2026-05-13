<script setup>
import { inject, reactive, onMounted } from 'vue'

/**
 * 전역 컨텍스트 주입
 */
const { t, state: globalState } = inject('appContext')

/**
 * 사용자 목록 상태
 */
const usersState = reactive({
  users: [],
})

/**
 * 마운트 시 사용자 목록 로드 (Mock 데이터)
 */
onMounted(() => {
  usersState.users = [
    { id: 'user1', email: 'admin@zdo.co.kr', role: 'ADMIN', lastAccess: '2026-05-08 10:00' },
    { id: 'user2', email: 'guest@naver.com', role: 'USER', lastAccess: '2026-05-07 15:30' },
  ]
})
</script>

<template>
  <section class="card admin-users">
    <h2>{{ t('adminUsers.title') }}</h2>
    
    <div class="admin-table-wrap">
      <!-- 사용자 관리 테이블 -->
      <table class="admin-table">
        <thead>
          <tr>
            <th>{{ t('adminSignatures.userId') }}</th>
            <th>{{ t('adminReservations.email') }}</th>
            <th>{{ t('adminUsers.role') }}</th>
            <th>{{ t('adminUsers.lastAccess') }}</th>
            <th>{{ t('adminUsers.manageRole') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in usersState.users" :key="user.id">
            <td>{{ user.id }}</td>
            <td>{{ user.email }}</td>
            <!-- 권한에 따른 뱃지 색상 구분 -->
            <td><span class="badge" :class="user.role === 'ADMIN' ? 'bg-danger' : 'bg-secondary'">{{ user.role }}</span></td>
            <td>{{ user.lastAccess }}</td>
            <td>
              <!-- 본인이 어드민이거나 어드민 권한 수정은 비활성화 예시 -->
              <button class="btn btn-sm btn-outline-primary" :disabled="user.role === 'ADMIN'">{{ t('adminUsers.manageRole') }}</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
