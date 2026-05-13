<script setup>
import { inject } from 'vue'

const { t, state: globalState, startOAuth, goEmailLoginPage } = inject('appContext')
</script>

<template>
  <section class="idea-login-shell">
    <div class="card idea-login-card">
      <div class="idea-login-actions">
        <button class="idea-login-button" :disabled="globalState.loading" @click="startOAuth('kakao')">{{ t('nav.kakaoLogin') }}</button>
        <button class="idea-login-button" :disabled="globalState.loading" @click="startOAuth('google')">{{ t('nav.googleLogin') }}</button>
        <button class="idea-login-button" :disabled="globalState.loading" @click="goEmailLoginPage">{{ t('nav.emailLoginPage') }}</button>
      </div>
    </div>
  </section>
</template>
