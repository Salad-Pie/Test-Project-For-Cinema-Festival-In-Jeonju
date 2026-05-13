<script setup>
import { inject } from 'vue'

const { t } = inject('appContext')
</script>

<template>
  <section class="card">
    <h2>{{ t('common.error') }}</h2>
    <p class="error">{{ t('admin.accessDenied') }}</p>
  </section>
</template>
