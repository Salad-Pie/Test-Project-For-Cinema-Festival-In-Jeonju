<script setup>
import { inject, computed } from 'vue'
import PointRankingDashboard from '../components/PointRankingDashboard.vue'

const { t, startOAuth, pageHref, goEmailLoginPage, state } = inject('appContext')
</script>

<template>
  <PointRankingDashboard />
  <section class="card">
    <div class="home-layout">
      <div class="home-group">
        <h3 class="home-group-title">{{ t('home.login') }}</h3>
        <div class="actions home-actions">
          <button :disabled="state.loading" @click="startOAuth('kakao')">{{ t('nav.kakaoLogin') }}</button>
          <button :disabled="state.loading" @click="startOAuth('google')">{{ t('nav.googleLogin') }}</button>
          <button class="route-button" @click="goEmailLoginPage">{{ t('nav.emailLoginPage') }}</button>
        </div>
      </div>
      <div class="home-group">
        <h3 class="home-group-title">{{ t('home.pageLinks') }}</h3>
        <div class="actions home-actions">
          <a class="route-button" :href="pageHref('/idea-contest')">{{ t('nav.ideaContest') }}</a>
          <a class="route-button" :href="pageHref('/sponsorship-application')">{{ t('nav.sponsorshipApplication') }}</a>
          <a class="route-button" :href="pageHref('/street-collaboration')">{{ t('nav.streetCollaboration') }}</a>
          <a class="route-button" :href="pageHref('/artist-meet')">{{ t('nav.exhibitionArtistMeeting') }}</a>
          <a class="route-button" :href="pageHref('/exhibition-survey')">{{ t('nav.exhibitionSurvey') }}</a>
          <a class="route-button" :href="pageHref('/experience-zone-survey')">{{ t('nav.experienceZoneSurvey') }}</a>
          <a class="route-button" :href="pageHref('/project-participant')">{{ t('nav.projectParticipant') }}</a>
          <a class="route-button" :href="pageHref('/streaming-recruit')">{{ t('nav.streamingRecruit') }}</a>
          <a class="route-button" :href="pageHref('/location')">{{ t('nav.location') }}</a>
        </div>
      </div>
    </div>
  </section>
</template>
