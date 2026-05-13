<script setup>
import { inject } from 'vue'

const { t, state: globalState } = inject('appContext')

const locationAddress = '전북특별자치도 전주시 완산구 전주객사3길 67'
const kakaoDirectionsUrl = `https://map.kakao.com/link/search/${encodeURIComponent(locationAddress)}`
const naverDirectionsUrl = `https://map.naver.com/p/search/${encodeURIComponent(locationAddress)}`

async function copyLocationAddress() {
  try {
    await navigator.clipboard.writeText(locationAddress)
    globalState.message = t('location.copyDone')
  } catch (_) {
    globalState.error = t('location.copyFailed')
  }
}
</script>

<template>
  <section class="location-page">
    <div class="location-header">
      <h2>{{ t('location.title') }}</h2>
      <p>{{ t('location.description') }}</p>
    </div>

    <div class="location-address-panel">
      <span class="location-section-title">{{ t('location.addressLabel') }}</span>
      <strong>{{ locationAddress }}</strong>
      <button type="button" @click="copyLocationAddress">{{ t('location.copyAddress') }}</button>
    </div>

    <div class="location-actions">
      <a class="route-button" :href="kakaoDirectionsUrl" target="_blank" rel="noreferrer">{{ t('location.kakaoDirections') }}</a>
      <a class="route-button" :href="naverDirectionsUrl" target="_blank" rel="noreferrer">{{ t('location.naverDirections') }}</a>
    </div>

    <div class="location-info-grid">
      <article class="location-info-card">
        <h3>{{ t('location.publicTransitTitle') }}</h3>
        <ul>
          <li>{{ t('location.publicTransitStop') }}</li>
          <li>{{ t('location.publicTransitStation') }}</li>
        </ul>
      </article>
      <article class="location-info-card">
        <h3>{{ t('location.carTitle') }}</h3>
        <ul>
          <li>{{ t('location.carNavigation') }}</li>
          <li>{{ t('location.carParking') }}</li>
        </ul>
      </article>
      <article class="location-info-card location-info-card-wide">
        <h3>{{ t('location.contactTitle') }}</h3>
        <p>{{ t('location.contactPhone') }}</p>
      </article>
    </div>
  </section>
</template>
