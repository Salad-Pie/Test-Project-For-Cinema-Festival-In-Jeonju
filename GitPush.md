# Git Push History & Summary

이 파일은 프로젝트의 주요 변경 사항과 푸시 내역을 기록하여 추적 및 롤백을 용이하게 하기 위해 관리됩니다.

## 📅 2026-05-13: 중국인 엔딩 크레딧 기능 고도화 및 인증 복구

### 🚀 요약
중국 관광객 전용 엔딩 크레딧 시스템의 전체 흐름을 완성하고, 누락되었던 OAuth 콜백 로직을 복구하여 실제 서비스가 가능한 수준으로 안정화했습니다.

### 📂 변경된 파일 목록

#### Backend (Spring Boot)
- **[MODIFY]** `springboot/src/main/java/com/example/springboot/domain/SignatureLanguage.java`
  - `ZH` (중국어) 타입 추가
- **[MODIFY]** `springboot/src/main/java/com/example/springboot/service/AuthService.java`
  - `detectKoOrEn` -> `detectLanguage`로 리네이밍 및 한자(Hanzi) 감지 로직 추가
- **[MODIFY]** `springboot/src/main/java/com/example/springboot/service/EnglishKoreanNameService.java`
  - 중국어 성씨(Zhang, Li, Wang 등) 및 주요 음절 매핑 30여 개 추가
  - `ZH` 언어의 음차 변환 지원 강화
- **[MODIFY]** `springboot/src/main/resources/application.yml`
  - 로컬 개발용 기본 암호화 키(`APP_DATA_ENCRYPTION_KEY`) 설정 추가

#### Frontend (Vue.js)
- **[MODIFY]** `vue/src/App.vue`
  - OAuth 인증 콜백(`/oauth/callback`) 처리 로직 복구 (인증 코드 교환 및 토큰 저장)
  - **[FIX]** 잘못된 API 경로 수정 (`/auth/oauth/exchange` -> `/oauth/exchange`)
- **[NEW]** `vue/src/views/EndingCreditsChineseView.vue`
  - 중국 관광객 전용 25분 시퀀스 엔딩 크레딧 뷰 구현

### 🛠️ 주요 기능 검증 결과
1. **OAuth 로그인**: 카카오/구글 로그인 후 정상적으로 토큰이 발급되어 로컬 스토리지에 저장됨을 확인.
2. **이름 변환**: 중국어 병음(Pinyin) 입력 시 한국어 발음으로 자연스럽게 변환됨을 확인 (예: Zhang -> 장).
3. **언어 감지**: OCR 결과에 한자가 포함될 경우 자동으로 중국어 사용자로 판별함.
4. **엔딩 크레딧**: 실시간 가입자 데이터 연동 및 페이즈(NAMES-BLACK-TAIL) 전환 정상 확인.

---

## 💡 롤백 및 참고 가이드
- 이전 상태로 되돌리려면 위 파일들의 수정 사항을 취소(revert)하십시오.
- 특정 커밋 시점으로 돌아가려면 `git log`를 통해 `중국인 엔딩 크레딧` 관련 작업을 확인하십시오.
