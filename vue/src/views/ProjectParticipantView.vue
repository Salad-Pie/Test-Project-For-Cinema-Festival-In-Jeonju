<script setup>
import { inject } from 'vue'

const { t, pageHref } = inject('appContext')
</script>

<template>
  <section class="split-page">
    <div class="split-page-top">
      <h2>{{ t('projectParticipant.title') }}</h2>
    </div>
    <div class="split-page-body">
      <div class="card idea-pane">
        <div class="idea-form">
          <a class="route-button" :href="pageHref('/ax-space')">{{ t('projectParticipant.axSpace') }}</a>
        </div>
      </div>
      <div class="card idea-pane">
        <div class="idea-form">
          <a class="route-button" :href="pageHref('/k-art-ax')">{{ t('projectParticipant.kArtAx') }}</a>
        </div>
      </div>
    </div>
  </section>
</template>
