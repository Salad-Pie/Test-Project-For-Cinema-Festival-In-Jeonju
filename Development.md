# Development.md

## 개발 완료 현황

작성 기준: 현재 저장소 코드 기준

## Frontend(Vue)

| 구분 | 완료 내용 | 주요 경로 |
|---|---|---|
| 메인/허브 | 로그인 버튼 영역과 페이지 이동 버튼 영역 분리 | `/`, `/original` |
| 로그인 공용 페이지 | 카카오, 구글, 이메일 로그인 진입 버튼 제공 | `/login-page` |
| 이메일 회원가입 | 이메일 기반 코드 전송/검증 플로우 화면 제공 | `/signup/email` |
| 이메일 로그인 | 이메일과 6자리 식별자 코드 기반 로그인 화면 제공 | `/login/email` |
| OAuth 콜백 | 카카오/구글 OAuth code 수신 후 백엔드 교환 API 호출 | `/oauth/callback` |
| 태블릿 인증/서명 | 6자리 식별자 입력, 서명 캔버스, 서명 저장 요청, 서명 이미지/증명서 샘플 다운로드 | `/tablet` |
| 아이디어 공모 | 로그인 여부 분기, 이미지 업로드 기반 공모 제출 | `/idea-contest` |
| 후원 상품 신청 | 입금 수단, 은행/페이먼트/카드사 선택/직접입력, 계좌번호/식별번호, 금액, 주소 입력 | `/sponsorship-application` |
| 영화의 거리 소상공인 협력 | 시간대별 신청 가능 여부 조회, 중복/정원 검증 연동 | `/street-collaboration` |
| 전시 작가와의 만남 | 예약 일자/시간 선택, 전용 포스터 이미지 적용 | `/artist-meet` |
| 전시 설문 | 세로형 질문 입력 폼, 성공 페이지 이동 | `/exhibition-survey`, `/exhibition-survey-success` |
| 체험존 설문 | 세로형 질문 입력 폼, 성공 페이지 이동 | `/experience-zone-survey`, `/experience-zone-survey-success` |
| 프로젝트 참여자 모집 | AX 공간, K-Art AX 신청 페이지 진입 | `/project-participant`, `/ax-space`, `/k-art-ax` |
| 스트리밍 방송국 모집 | AX Shop&Shop, PD/작가/편집/음향 신청 페이지 진입 | `/streaming-recruit`, `/ax-shop-shop`, `/pd-recruit` |
| 오시는길 | 주소, 주소 복사, 카카오/네이버 지도 길찾기 링크, 교통 안내 | `/location` |
| i18n | 한국어, 영어, 중국어, 일본어 번역 구조 적용 | 전역 언어 선택 |
| 주소 입력 | 주소 검색 API 스크립트 연동 후 상세주소 별도 입력 | 후원/설문 페이지 |
| 관리자 대시보드 | ADMIN 권한 접근 가능한 대시보드 및 권한 없음 에러 화면 분기 | `/admin` |
| 로컬 개발 프록시 | Vite `/api` 요청을 Spring Boot `8080`으로 프록시 | `vue/vite.config.js` |

## Backend(Spring Boot)

| 구분 | 완료 내용 | 주요 API |
|---|---|---|
| 인증 | JWT 기반 인증, OAuth code exchange, 이메일 코드 로그인/회원가입 | `/api/auth/**`, `/api/idea-contest-auth/**` |
| OAuth | 카카오/구글 OAuth authorize URL 생성 및 code 교환 | `/api/auth/oauth/**` |
| 이메일 코드 | Gmail SMTP 기반 6자리 코드 발송 구조 | `/api/auth/login/email/**`, `/api/auth/register/email/**` |
| 문자 발송 | Twilio 기반 SMS 발송 구조 | 설정 기반 |
| 사용자 Entity | `user`, `auth_credential`, `session` DDL 기준 Entity 구조 반영 | User/AuthCredential/Session |
| 식별자 코드 | User와 분리된 식별자 코드 Entity 저장 | IdentifierCode |
| 서명/OCR | 서명 이미지 저장, Google Vision API OCR, 영어 OCR 결과의 발음/의미 기준 한글 변환 | `/api/auth/signature` |
| 서명 이미지화 | 저장된 서명 텍스트에 Font를 적용한 PNG 이미지 응답 | `/api/auth/signature/render` |
| 증명서 샘플 합성 | 샘플 증명서 템플릿 좌표에 이름/서명 텍스트를 삽입한 PNG 응답 | `/api/auth/signature/certificate-sample` |
| 한글 폰트 렌더링 | Java AWT 렌더링 시 사용 가능한 한글 폰트 우선 선택, Docker 런타임에 Noto CJK 설치 | `SignatureImageService`, `springboot/Dockerfile` |
| 아이디어 공모 | 로그인 유저 기준 이미지 업로드, 메모 이미지 저장 | `/api/idea-contests` |
| S3 업로드 | S3 업로드, presigned download URL 구조 | S3UploadService |
| 이미지 최적화 | jpg/jpeg/png만 허용, WebP 변환/압축 후 업로드 | S3UploadService |
| 후원 신청 | 입금 수단, 기관명, 계좌번호/식별번호 기반 신청 저장 | `/api/sponsorship-applications` |
| 후원 중복 검증 | 입금 수단 + 기관명 + 계좌번호/식별번호 해시 기준 10분 중복 방지 | SponsorshipApplicationService |
| 계좌정보 보호 | 원본 계좌번호/식별번호 암호화, 마스킹, 해시 저장 | DataEncryptionService/DataHashService |
| 소상공인 협력 | 17~22시 시간대 신청, 시간별 최대 50명, 동일 이름/연락처 중복 방지 | `/api/street-collaboration-reservations` |
| 전시 작가와의 만남 | 로그인 유저 기준 예약 저장 | `/api/exhibition-artist-meeting-reservations` |
| 설문 | 전시 설문, 체험존 설문 저장 | `/api/exhibition-surveys`, `/api/experience-zone-surveys` |
| 프로젝트 모집 | AX 공간, K-Art AX, AX Shop&Shop, PD/작가/편집/음향 신청 저장 | `/api/project-recruitments/{projectKey}` |
| AX Shop&Shop | 전화번호 선택 입력 허용 | `projectKey=ax-shop-shop` |
| 예외 표준화 | 예외 원문 미노출, 짧은 고정 메시지 응답 | ApiExceptionHandler |
| 요청 로깅 | HTTP 요청 처리 로그 기록 | RequestLoggingFilter |

## 주요 API 목록

| API | Method | 용도 |
|---|---|---|
| `/api/auth/oauth/authorize-url/{provider}` | GET | OAuth authorize URL 생성 |
| `/api/auth/oauth/exchange` | POST | OAuth code 교환 및 JWT 발급 |
| `/api/idea-contest-auth/oauth/authorize-url/{provider}` | GET | 아이디어 공모 로그인용 OAuth URL 생성 |
| `/api/idea-contest-auth/oauth/exchange` | POST | 아이디어 공모 로그인용 OAuth code 교환 |
| `/api/auth/login/email/send-code` | POST | 이메일 로그인 코드 발송 |
| `/api/auth/login/email/verify` | POST | 이메일 로그인 코드 검증 |
| `/api/auth/register/email/send-code` | POST | 이메일 회원가입 코드 발송 |
| `/api/auth/register/email/verify` | POST | 이메일 회원가입 코드 검증 |
| `/api/auth/verify` | POST | 태블릿 6자리 식별자 검증 |
| `/api/auth/signature` | POST | 서명 저장 및 OCR 처리 |
| `/api/auth/signature/render` | POST | 저장된 서명 텍스트 기반 Font 적용 PNG 생성 |
| `/api/auth/signature/certificate-sample` | POST | 샘플 증명서 템플릿에 이름/서명 좌표 삽입 |
| `/api/idea-contests` | POST | 아이디어 공모 제출 |
| `/api/sponsorship-applications` | POST | 후원 상품 신청 |
| `/api/street-collaboration-reservations` | POST | 소상공인 협력 신청 |
| `/api/street-collaboration-reservations/availability` | GET | 시간대별 신청 가능 여부 조회 |
| `/api/exhibition-artist-meeting-reservations` | POST | 전시 작가와의 만남 예약 |
| `/api/exhibition-surveys` | POST | 전시 설문 제출 |
| `/api/experience-zone-surveys` | POST | 체험존 설문 제출 |
| `/api/project-recruitments/{projectKey}` | POST | 프로젝트 모집 신청 |

## 데이터/보안 처리

| 항목 | 처리 방식 |
|---|---|
| 인증 방식 | 세션 대신 JWT 사용 |
| OAuth 토큰 전달 | 프론트가 OAuth code 수신 후 백엔드 exchange API 호출 |
| 로그인 후 이동 | `zdoPostLoginRedirectPath` 기반 redirect 처리 |
| 계좌번호/식별번호 | 암호화 저장, 마스킹 저장, 해시 저장 |
| 중복 신청 검증 | 민감정보 원문 대신 해시 기준 검증 |
| API 예외 응답 | `ex.getMessage()` 원문 미노출, 짧은 고정 메시지 사용 |
| OCR | Google Vision API 기반 구조, 실패 원인별 오류 코드 분리 |
| 영어 서명 한글화 | 발음 기준 한글 변환은 내부 규칙, 의미 기준 한글 번역은 Google Translation API 사용 |
| 파일 다운로드 | presigned download URL 구조 |
| 이미지 업로드 | jpg/jpeg/png 허용 후 WebP 변환 |

## 배포/운영 구성

| 구분 | 내용 |
|---|---|
| Docker | Vue, Spring Boot 각각 이미지 기반 실행 |
| Docker Compose | `docker-compose.test.yml`, `docker-compose.prod.yml` 구성 |
| PROD env | `/etc/cinema/springboot.env.prod` 기반 env_file 사용 |
| Vue 포트 | 컨테이너/호스트 `5173:5173` |
| Spring Boot 포트 | `8080:8080` |
| Nginx 구조 | `cinema.zdo.co.kr`에서 `/api`는 백엔드, 그 외는 프론트로 reverse proxy 예정 |
| GitHub Actions | Docker image build/push 및 원격 compose 배포 흐름 구성 |
| Docker Hub | private repository 사용 전제 |

## 환경변수 주요 항목

| 영역 | 환경변수 |
|---|---|
| DB | `PROD_DB_URL`, `PROD_DB_USERNAME`, `PROD_DB_PASSWORD` |
| JWT | `PROD_APP_JWT_SECRET`, `PROD_APP_JWT_EXPIRATION_MINUTES` |
| 암호화 | `PROD_APP_DATA_ENCRYPTION_KEY` |
| OAuth | `KAKAO_REST_API_KEY`, `KAKAO_CLIENT_SECRET`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` |
| SMTP | `GMAIL_SMTP_USERNAME`, `GMAIL_SMTP_APP_PASSWORD` |
| SMS | `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN`, `TWILIO_FROM_NUMBER` |
| OCR | `GOOGLE_VISION_API_KEY` |
| Translation | `GOOGLE_TRANSLATE_API_KEY` |
| S3 | `AWS_S3_BUCKET`, `AWS_REGION`, `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_S3_PRESIGNED_DOWNLOAD_EXPIRATION_MINUTES` |

## 최근 검증 상태

| 항목 | 결과 |
|---|---|
| Vue production build | 성공 |
| Spring Boot compileJava | 성공 |
| Spring Boot test | `APP_DATA_ENCRYPTION_KEY` 미주입 시 실패 확인, 키 주입 실행은 로컬 Gradle 캐시 권한 문제로 재시도 필요 |
| Vue local dev server | `http://127.0.0.1:5173` 실행 확인 |
| Spring Boot local server | `http://127.0.0.1:8080` 실행 확인 |
| Vite `/api` proxy | `5173/api` 요청이 `8080`으로 프록시됨 |

## 남은 확인 사항

| 항목 | 비고 |
|---|---|
| 실제 Gmail SMTP 앱 비밀번호 | 운영 env에 실제 값 필요 |
| 실제 Twilio 정보 | SMS 실발송 시 실제 계정 필요 |
| Google Vision API Key | OCR 운영 사용 시 실제 키 필요 |
| Google Translation API Key | 영어 OCR 결과의 의미 기준 한글 번역 사용 시 필요 |
| S3 권한 | 버킷, IAM 권한, CORS 정책 확인 필요 |
| 운영 DB 마이그레이션 | `ddl-auto=update` 사용 중이나 운영에서는 별도 migration 도입 권장 |
| 관리자 화면 | 신청/설문/예약 데이터 확인용 관리자 기능은 별도 필요 |
