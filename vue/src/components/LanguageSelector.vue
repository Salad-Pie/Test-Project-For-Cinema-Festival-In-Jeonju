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
  <div class="actions" style="justify-content: space-between; align-items: center; margin-bottom: 8px">
    <a :href="pageHref('/')" class="route-button" style="background: rgba(255, 255, 255, 0.9); color: #0d6efd; border: 1px solid #0d6efd; padding: 6px 14px; font-size: 14px; display: flex; align-items: center; gap: 6px; box-shadow: 0 2px 8px rgba(0,0,0,0.05);">
      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
        <path d="M8.707 1.5a1 1 0 0 0-1.414 0L.646 8.146a.5.5 0 0 0 .708.708L2 8.207V13.5A1.5 1.5 0 0 0 3.5 15h9a1.5 1.5 0 0 0 1.5-1.5V8.207l.646.647a.5.5 0 0 0 .708-.708L13 5.793V2.5a.5.5 0 0 0-.5-.5h-1a.5.5 0 0 0-.5.5v1.293L8.707 1.5Z"/>
        <path d="m8 3.293 6 6V13.5a.5.5 0 0 1-.5.5h-9a.5.5 0 0 1-.5-.5V9.293l6-6Z"/>
      </svg>
      {{ t('nav.home') }}
    </a>
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
