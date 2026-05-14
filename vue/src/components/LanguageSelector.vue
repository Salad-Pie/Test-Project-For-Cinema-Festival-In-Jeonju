<script setup>
import { inject } from 'vue'

/**
 * 전역 애플리케이션 컨텍스트를 주입받음
 */
const { t, locale, isEndingCreditsPage, pageHref, isHome } = inject('appContext')

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
  <!-- 네비게이션 및 언어 선택 영역 -->
  <div class="actions main-nav-container">
    <nav class="nav-links">
      <a :href="pageHref('/')" class="nav-btn" :class="{ active: isHome }">
        <i class="bi bi-house-door-fill"></i>
        <span>{{ t('nav.home') || '홈' }}</span>
      </a>
      <a :href="pageHref('/login-page')" class="nav-btn">
        <i class="bi bi-person-fill"></i>
        <span>{{ t('nav.login') || '로그인' }}</span>
      </a>
      <a :href="pageHref('/tablet')" class="nav-btn">
        <i class="bi bi-pen-fill"></i>
        <span>{{ t('nav.tablet') || '서명' }}</span>
      </a>
      <a :href="pageHref('/ending-credits-cn')" class="nav-btn">
        <i class="bi bi-camera-reels-fill"></i>
        <span>{{ t('nav.endingCredits') || '엔딩 크레딧' }}</span>
      </a>
    </nav>
    
    <div class="locale-wrapper">
      <label class="locale-select-label">
        <select :value="locale" aria-label="Language Select" @change="setLocale($event.target.value)">
          <option value="ko">{{ isEndingCreditsPage ? localeDisplayNames.ko : t('lang.ko') }}</option>
          <option value="en">{{ isEndingCreditsPage ? localeDisplayNames.en : t('lang.en') }}</option>
          <option value="zh">{{ isEndingCreditsPage ? localeDisplayNames.zh : t('lang.zh') }}</option>
          <option value="ja">{{ isEndingCreditsPage ? localeDisplayNames.ja : t('lang.ja') }}</option>
        </select>
      </label>
    </div>
  </div>
</template>

<style scoped>
.main-nav-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 10px 16px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(8px);
  border-radius: 16px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.3);
  flex-wrap: wrap;
  gap: 15px;
}

.nav-links {
  display: flex;
  gap: 10px;
  align-items: center;
}

.nav-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 12px;
  text-decoration: none;
  font-weight: 600;
  color: #555;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: transparent;
  font-size: 0.95rem;
}

.nav-btn i {
  font-size: 1.1rem;
  transition: transform 0.3s ease;
}

.nav-btn:hover {
  background: rgba(13, 110, 253, 0.08);
  color: #0d6efd;
  transform: translateY(-2px);
}

.nav-btn:hover i {
  transform: scale(1.1);
}

.nav-btn.active {
  background: #0d6efd;
  color: #ffffff;
  box-shadow: 0 4px 10px rgba(13, 110, 253, 0.2);
}

.locale-wrapper {
  display: flex;
  align-items: center;
}

@media (max-width: 600px) {
  .main-nav-container {
    justify-content: center;
    padding: 12px;
  }
  .nav-btn span {
    display: none;
  }
  .nav-btn {
    padding: 10px;
  }
}
</style>
