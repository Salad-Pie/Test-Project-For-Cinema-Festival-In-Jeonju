<script setup>
import { inject, reactive } from 'vue'

/**
 * 전역 컨텍스트 주입
 */
const { t, state: globalState } = inject('appContext')

/**
 * 서비스 설정 상태
 * capacities: 프로젝트별 정원 설정
 * maintenanceMode: 점검 모드 활성화 여부
 */
const configState = reactive({
  capacities: [
    { project: 'AX 공간 (AX Space)', capacity: 20 },
    { project: 'K-Art AX 신청', capacity: 15 },
    { project: '소상공인 협력 (Street)', capacity: 50 },
  ],
  maintenanceMode: false,
})

/**
 * 설정을 저장하는 함수 (현재는 UI 피드백만 제공)
 */
function saveConfig() {
  globalState.message = '설정이 저장되었습니다.'
}
</script>

<template>
  <section class="card admin-config">
    <h2>{{ t('adminConfig.title') }}</h2>

    <!-- 프로젝트 정원 관리 섹션 -->
    <div class="mb-4">
      <h3>{{ t('adminConfig.capacityManagement') }}</h3>
      <div v-for="item in configState.capacities" :key="item.project" class="mb-2">
        <label class="d-flex align-items-center justify-content-between">
          <span>{{ item.project }}</span>
          <input type="number" v-model="item.capacity" class="form-control w-25" />
        </label>
      </div>
    </div>

    <!-- 점검 모드 설정 섹션 -->
    <div class="mb-4">
      <h3>{{ t('adminConfig.maintenanceMode') }}</h3>
      <div class="form-check form-switch">
        <input class="form-check-input" type="checkbox" v-model="configState.maintenanceMode" id="maintenanceSwitch">
        <label class="form-check-label" for="maintenanceSwitch">{{ t('adminConfig.maintenanceMode') }}</label>
      </div>
    </div>

    <!-- 설정 저장 버튼 -->
    <button class="btn btn-primary w-100" @click="saveConfig">모든 설정 저장</button>
  </section>
</template>
