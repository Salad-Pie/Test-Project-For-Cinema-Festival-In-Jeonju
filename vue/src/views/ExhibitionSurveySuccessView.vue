<script setup>
import { inject } from 'vue'

const { t, ideaPosterUrl } = inject('appContext')
</script>

<template>
  <section class="split-page">
    <div class="split-page-top"></div>
    <div class="split-page-body">
      <div class="card idea-pane">
        <div class="idea-form">
          <h2>{{ t('survey.exhibitionSuccessTitle') }}</h2>
          <p>{{ t('survey.successDesc') }}</p>
        </div>
      </div>
      <div class="card idea-pane">
        <div class="idea-poster">
          <img :src="ideaPosterUrl" alt="Survey Success" />
        </div>
      </div>
    </div>
  </section>
</template>
