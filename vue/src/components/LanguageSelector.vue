<script setup>
import { inject } from 'vue'

/**
 * 전역 애플리케이션 컨텍스트를 주입받음
 */
const { t, locale, isEndingCreditsPage, pageHref } = inject('appContext')

// 언어별 표시 이름 정의
const localeDisplayNames = { ko: '\uD55C\uAD6D\uC5B4', en: 'English', zh: '\u4E2D\u6587', ja: '\u65E5\u672C\u8A9E' }
const localeStorageKey = 'zdo.locale'

/**
 * 사용자가 선택한 언어로 변경하고 설정을 유지하는 함수
 * @param {string} next - 선택된 언어 코드
 */
function setLocale(next) {
  locale.value = next
  try {
    localStorage.setItem(localeStorageKey, next)
  } catch (_) {}
}
</script>

<template>
  <!-- 언어 선택 드롭다운 영역 -->
  <div class="actions" style="justify-content: space-between; align-items: center; margin-bottom: 12px; flex-wrap: wrap; gap: 10px;">
    <div class="nav-links" style="display: flex; gap: 8px; align-items: center;">
      <a :href="pageHref('/')" class="route-button nav-btn">
        <i class="bi bi-house-door-fill"></i> 홈
      </a>
      <a :href="pageHref('/login-page')" class="route-button nav-btn">
        <i class="bi bi-person-fill"></i> 로그인
      </a>
      <a :href="pageHref('/tablet')" class="route-button nav-btn">
        <i class="bi bi-pen-fill"></i> 서명
      </a>
      <a :href="pageHref('/ending-credits-cn')" class="route-button nav-btn">
        <i class="bi bi-camera-reels-fill"></i> 엔딩 크레딧
      </a>
    </div>
    <label class="locale-select-label">
      <select :value="locale" aria-label="Language Select" @change="setLocale($event.target.value)">
        <!-- 엔딩 크레딧 페이지와 일반 페이지의 언어 표기 방식 대응 -->
        <option value="ko">{{ isEndingCreditsPage ? localeDisplayNames.ko : t('lang.ko') }}</option>
        <option value="en">{{ isEndingCreditsPage ? localeDisplayNames.en : t('lang.en') }}</option>
        <option value="zh">{{ isEndingCreditsPage ? localeDisplayNames.zh : t('lang.zh') }}</option>
        <option value="ja">{{ isEndingCreditsPage ? localeDisplayNames.ja : t('lang.ja') }}</option>
      </select>
    </label>
  </div>
</template>
