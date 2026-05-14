<script setup>
import { inject } from 'vue'

/**
 * 전역 컨텍스트 주입
 */
const { t, pageHref } = inject('appContext')

/**
 * 관리 메뉴 구성 데이터
 */
const adminMenus = [
  {
    title: t('admin.goToReservations'),
    desc: '예약 및 신청 현황 통합 관리',
    href: '/admin/reservations',
    icon: '📅',
    color: '#4e73df'
  },
  {
    title: t('admin.goToStatistics'),
    desc: '실시간 데이터 및 트렌드 통계',
    href: '/admin/statistics',
    icon: '📊',
    color: '#1cc88a'
  },
  {
    title: t('admin.goToUsers'),
    desc: '사용자 목록 및 권한 관리',
    href: '/admin/users',
    icon: '👥',
    color: '#36b9cc'
  },
  {
    title: t('admin.goToConfig'),
    desc: '서비스 정원 및 운영 설정',
    href: '/admin/config',
    icon: '⚙️',
    color: '#f6c23e'
  },
  {
    title: t('admin.goToExport'),
    desc: '데이터 엑셀 및 CSV 추출',
    href: '/admin/export',
    icon: '📥',
    color: '#e74a3b'
  },
  {
    title: t('admin.goToDataBrowser'),
    desc: '전체 엔티티 로우 데이터 조회',
    href: '/admin/data',
    icon: '🔍',
    color: '#6c757d'
  }
]
</script>

<template>
  <section class="admin-dashboard-container py-4">
    <div class="header-section mb-5 text-center">
      <h1 class="display-5 fw-bold text-primary">{{ t('admin.dashboardTitle') }}</h1>
      <p class="text-muted">시스템 운영 및 데이터 관리를 위한 통합 컨트롤 센터입니다.</p>
    </div>

    <div class="row g-4">
      <div v-for="menu in adminMenus" :key="menu.href" class="col-md-6 col-lg-4">
        <a :href="pageHref(menu.href)" class="menu-card-link">
          <div class="card h-100 menu-card shadow-sm border-0">
            <div class="card-body p-4 d-flex align-items-center">
              <div class="icon-box me-4" :style="{ backgroundColor: menu.color }">
                <span class="icon">{{ menu.icon }}</span>
              </div>
              <div class="content">
                <h4 class="card-title h5 mb-1 fw-bold text-dark">{{ menu.title }}</h4>
                <p class="card-text text-muted small mb-0">{{ menu.desc }}</p>
              </div>
            </div>
          </div>
        </a>
      </div>
    </div>
  </section>
</template>

<style scoped>
.admin-dashboard-container {
  max-width: 1200px;
  margin: 0 auto;
}

.menu-card-link {
  text-decoration: none;
  transition: transform 0.2s ease-in-out;
  display: block;
}

.menu-card-link:hover {
  transform: translateY(-5px);
}

.menu-card {
  transition: all 0.3s ease;
  border-radius: 15px;
}

.menu-card-link:hover .menu-card {
  box-shadow: 0 10px 20px rgba(0,0,0,0.1) !important;
}

.icon-box {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
}

.icon {
  font-size: 1.8rem;
  color: white;
}

.content .card-title {
  transition: color 0.2s;
}

.menu-card-link:hover .card-title {
  color: #0d6efd !important;
}
</style>
