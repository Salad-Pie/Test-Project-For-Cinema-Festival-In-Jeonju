<script setup>
import { inject } from 'vue'

const { t, state, ideaPosterUrl } = inject('appContext')
</script>

<template>
  <section class="split-page">
    <div class="split-page-top"></div>
    <div class="split-page-body">
      <div class="card idea-pane">
        <div class="idea-form">
          <h2>{{ t('common.successTitle') }}</h2>
          <p>{{ state.successMessage || t('common.applicationCompleted') }}</p>
        </div>
      </div>
      <div class="card idea-pane">
        <div class="idea-poster"><img :src="ideaPosterUrl" alt="Success" /></div>
      </div>
    </div>
  </section>
</template>
