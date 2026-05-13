<script setup>
import { computed, inject } from 'vue'

const { t, state: globalState, startOAuth, goEmailLoginPage, pageHref } = inject('appContext')

const emailSignupHref = computed(() => {
  const redirectPath = typeof window !== 'undefined' ? new URLSearchParams(window.location.search).get('redirect') : ''
  const signupPath = pageHref('/signup/email')
  return redirectPath ? `${signupPath}?redirect=${encodeURIComponent(redirectPath)}` : signupPath
})
</script>

<template>
  <section class="bootstrap-login-page">
    <div class="container-fluid py-5">
      <div class="row justify-content-center">
        <div class="col-12 col-md-8 col-lg-5 col-xl-4">
          <div class="card border-0 shadow-lg bootstrap-login-card">
            <div class="card-body p-4 p-md-5">
              <div class="text-center mb-4">
                <span class="badge rounded-pill text-bg-light border border-info-subtle text-info-emphasis px-3 py-2 mb-3">
                  ZDO Cinema
                </span>
                <h1 class="h3 fw-bold mb-2">{{ t('bootstrapLogin.title') }}</h1>
                <p class="text-secondary mb-0">{{ t('bootstrapLogin.subtitle') }}</p>
              </div>

              <div class="d-grid gap-3">
                <button class="btn btn-info text-white fw-semibold py-3 shadow-sm" :disabled="globalState.loading" @click="startOAuth('kakao')">
                  {{ t('nav.kakaoLogin') }}
                </button>
                <button class="btn btn-outline-info fw-semibold py-3" :disabled="globalState.loading" @click="startOAuth('google')">
                  {{ t('nav.googleLogin') }}
                </button>
                <button class="btn btn-light border border-info-subtle fw-semibold py-3 text-info-emphasis" :disabled="globalState.loading" @click="goEmailLoginPage">
                  {{ t('nav.emailLoginPage') }}
                </button>
              </div>
              <p class="bootstrap-signup-link-wrap text-center mt-4 mb-0">
                <a class="bootstrap-signup-link" :href="emailSignupHref">{{ t('nav.emailSignup') }}</a>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
