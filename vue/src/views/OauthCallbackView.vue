<script setup>
import { inject } from 'vue'

const { t } = inject('appContext')
</script>

<template>
  <section class="card">
    <h2>{{ t('auth.callbackTitle') }}</h2>
    <p>{{ t('auth.callbackDesc') }}</p>
  </section>
</template>
