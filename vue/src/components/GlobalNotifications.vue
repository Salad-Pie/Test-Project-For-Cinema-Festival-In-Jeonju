<script setup>
import { inject } from 'vue'

/**
 * 전역 상태 정보를 주입받음
 */
const { state } = inject('appContext')
</script>

<template>
  <!-- 전역 메시지 또는 에러가 있을 때만 렌더링 -->
  <div v-if="state.message || state.error" class="notifications">
    <!-- 성공 또는 일반 알림 메시지 -->
    <p v-if="state.message" class="ok">{{ state.message }}</p>
    <!-- 에러 메시지 -->
    <p v-if="state.error" class="error">{{ state.error }}</p>
  </div>
</template>

<style scoped>
.notifications {
  margin-top: 1rem;
}
</style>
