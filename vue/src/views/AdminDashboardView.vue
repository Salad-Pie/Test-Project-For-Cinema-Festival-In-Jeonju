<script setup>
import { inject } from 'vue'

/**
 * 전역 컨텍스트 주입
 */
const { t, pageHref } = inject('appContext')
</script>

<template>
  <!-- 관리자 통합 대시보드 섹션 -->
  <section class="card admin-dashboard">
    <h2>{{ t('admin.dashboardTitle') }}</h2>
    <div class="actions">
      <!-- 각 관리 도구로 바로가기 버튼 목록 -->
      <a class="route-button" :href="pageHref('/admin/reservations')">{{ t('admin.goToReservations') }}</a>
      <a class="route-button" :href="pageHref('/admin/signatures')">{{ t('admin.goToSignatures') }}</a>
      <a class="route-button" :href="pageHref('/admin/statistics')">{{ t('admin.goToStatistics') }}</a>
      <a class="route-button" :href="pageHref('/admin/ocr-review')">{{ t('admin.goToOCRReview') }}</a>
      <a class="route-button" :href="pageHref('/admin/users')">{{ t('admin.goToUsers') }}</a>
      <a class="route-button" :href="pageHref('/admin/config')">{{ t('admin.goToConfig') }}</a>
      <a class="route-button" :href="pageHref('/admin/export')">{{ t('admin.goToExport') }}</a>
      <a class="route-button" :href="pageHref('/admin/i18n-editor')">{{ t('admin.goToI18nEditor') }}</a>
    </div>
  </section>
</template>
