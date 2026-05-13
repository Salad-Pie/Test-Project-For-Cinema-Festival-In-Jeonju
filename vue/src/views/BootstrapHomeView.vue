<script setup>
import { inject } from 'vue'
import PointRankingDashboard from '../components/PointRankingDashboard.vue'

const { t, bootstrapPages } = inject('appContext')
</script>

<template>
  <PointRankingDashboard />
  <section class="bootstrap-hub-page">
    <div class="container-fluid py-5">
      <div class="row justify-content-center">
        <div class="col-12 col-xl-10">
          <div class="text-center mb-4">
            <span class="badge rounded-pill text-bg-light border border-info-subtle text-info-emphasis px-3 py-2 mb-3">
              BackToScreen
            </span>
            <h1 class="h2 fw-bold mb-2">BackToScreen</h1>
            <p class="text-secondary mb-0">AX Convergence Calligraphy Exhibition</p>
          </div>
          <div class="row g-3">
            <div v-for="page in bootstrapPages" :key="page.href" class="col-12 col-md-6 col-lg-4">
              <a class="btn btn-light border border-info-subtle shadow-sm w-100 py-3 fw-semibold text-info-emphasis bootstrap-hub-link" :href="page.href">
                {{ page.label || t(page.key) }}
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
