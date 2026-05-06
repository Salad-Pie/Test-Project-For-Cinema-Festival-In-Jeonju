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
| 한글 폰트 렌더링 | Java AWT 렌더링 시 사용 가능한 한글/장식성 폰트 우선 선택, 궁서체 입력값 지원, Docker 런타임에 Noto CJK/Extra 설치 | `SignatureImageService`, `springboot/Dockerfile` |
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

## 증명서 PDF 생성 기능 추가

| 항목 | 내용 |
|---|---|
| 원본 템플릿 | `Korean Calligraphy Experience Certificate.pdf`를 Spring Boot resource 템플릿으로 사용 |
| 생성 방식 | Apache PDFBox 기반으로 원본 PDF에 이름/서명 이미지를 오버레이 |
| 영어 이름 | 고정 박스 안에서 기준 폰트 크기 유지, 긴 경우 최소 크기까지만 제한 축소, 그래도 길면 2줄 처리 |
| 한글 이름 | 고정 박스 안에서 기준 폰트 크기 유지, 긴 경우 제한 축소 |
| 원본 서명 | S3에 저장된 원본 PNG를 읽어 지정 박스 안에 원본 비율 유지 방식으로 삽입 |
| 샘플 API | `POST /api/auth/signature/certificate-pdf/sample` |
| 실제 API | `POST /api/auth/signature/certificate-pdf` |
| 검증 | `CertificatePdfServiceTest`에서 실제 PDF 생성 및 파일 생성 확인 |
| 샘플 파일 | `C:\Users\dldbs\Downloads\Korean Calligraphy Experience Certificate_backend_sample.pdf` |

## 증명서 진본 PDF 템플릿 적용

| 항목 | 내용 |
|---|---|
| 진본 파일명 | `(인증서) _AX 한글 체험 인증서 진본.pdf` |
| 적용 좌표 | 기존 확정 좌표 유지: 영어 이름 `(185,563,235,46)`, 한글 이름 `(205,513,195,46)`, 원본 서명 `(220,405,165,76)` |
| 변경 범위 | PDF 템플릿 경로만 진본 파일로 교체 |
| 샘플 파일 | `C:\Users\dldbs\Downloads\(인증서) _AX 한글 체험 인증서 진본_backend_sample.pdf` |

## 증명서 최종 원본 템플릿 적용

| 항목 | 내용 |
|---|---|
| 최종 원본 파일명 | `Certification_Sample.pdf` |
| 적용 좌표 | 기존 확정 좌표 유지: 영어 이름 `(185,563,235,46)`, 한글 이름 `(205,513,195,46)`, 원본 서명 `(220,405,165,76)` |
| 변경 범위 | PDF 템플릿 경로만 최종 원본 파일로 교체 |
| 샘플 파일 | `C:\Users\dldbs\Downloads\Certification_Sample_backend_sample.pdf` |

## 서명 이름 표시 필드 분리

| 항목 | 내용 |
|---|---|
| Entity | `Signature`에 `englishName`, `koreanName` 저장 필드 추가 |
| 저장 정책 | OCR 결과가 영어면 `englishName=recognizedText`, `koreanName=발음 기준 한글명` |
| 저장 정책 | OCR 결과가 한글이면 `koreanName=recognizedText`, `englishName=null` |
| 응답 | `SignatureResponse`에 `englishName`, `koreanName` 추가 |
| 사용처 | 증명서 PDF 기본 이름 값은 분리 저장된 이름 필드를 우선 사용 |
| 검증 | `compileJava`, 전체 `test` 성공 |

## 태블릿 서명 이름 언어 선택 구조 추가

| 항목 | 내용 |
|---|---|
| 사용자 입력 | 태블릿 서명 저장 시 이름 언어를 선택하고 한글 이름을 선택적으로 직접 입력 |
| 지원 언어 | `EN`, `FR`, `DE`, `JA`, `ZH`, `VI`, `ES`, `IT`, `OTHER` |
| Entity | `Signature.originalName`, `Signature.nameLanguage`, `Signature.englishName`, `Signature.koreanName`, `Signature.nameConversionSource` 저장 |
| 영어 이름 | 국립국어원 용례 사전 우선, 없으면 외래어 표기법 기반 fallback |
| 비영어 이름 | 자동 확정하지 않고 `MANUAL_REVIEW` 출처로 저장, 사용자가 입력한 한글명은 `MANUAL` 처리 |
| 프론트 | `/tablet` 서명 영역에 이름 언어 select box와 한글 이름 직접 수정 입력 추가 |
| 검증 | Spring Boot `compileJava`, 전체 `test`, Vue `npm run build` 성공 |

## 텍스트 기반 강한 서예 서명 PNG 생성

| 항목 | 내용 |
|---|---|
| 입력 텍스트 | `이창섭` 등 한글 이름 텍스트 |
| 생성 방식 | 한글 서예 계열 폰트 렌더링 후 먹선 농도 강화, 가장자리 거칠기, 번짐 효과, 자동 crop 적용 |
| API | `POST /api/auth/signature/calligraphy-text/sample` |
| 출력 | 증명서 삽입용 투명 PNG, 확인용 흰 배경 preview PNG |
| 샘플 파일 | `C:\Users\dldbs\Downloads\strong-calligraphy-이창섭.png`, `C:\Users\dldbs\Downloads\strong-calligraphy-이창섭-preview-white.png` |
| 검증 | `StrongCalligraphyTextTest` 성공 |

## 증명서 PDF 서예 서명 삽입 적용

| 항목 | 내용 |
|---|---|
| 적용 기준 | 증명서 서명 영역에 강한 서예 텍스트 PNG 삽입 |
| 샘플 이름 | `이창섭` |
| 처리 방식 | `CertificatePdfService`에서 원본 서명 이미지가 없을 경우 `koreanName`을 강한 서예 PNG로 렌더링 후 서명 박스에 삽입 |
| 결과 파일 | `C:\Users\dldbs\Downloads\이창섭-서명.pdf` |
| 검증 | `CertificatePdfServiceTest` 성공 |

## 6자리 식별자 기반 서명/증명서 다운로드 페이지

| 항목 | 내용 |
|---|---|
| 기준 테이블 | `email_identifier_codes`의 `code` 컬럼을 단독 식별자로 사용 |
| 조회 정책 | 6자리 코드 기준 최신 1건을 조회하고, 해당 이메일의 User와 Signature를 연결 |
| 서명 이미지 다운로드 | `POST /api/certificate-download/signature-image`에서 저장된 `koreanName` 기준 서예 PNG를 요청 시점에 생성 |
| 증명서 다운로드 | `POST /api/certificate-download/certificate-pdf`에서 저장된 `englishName`, `koreanName`과 요청 시점 생성한 서예 서명을 PDF에 삽입 |
| 저장 정책 | 생성된 서예 이미지와 PDF는 S3에 저장하지 않고 매 요청마다 생성 |
| 프론트 페이지 | `/certificate-download` 별도 페이지 추가 |
| 프론트 버튼 | `서명 이미지 다운로드`, `증명서 다운로드`를 별도 버튼과 별도 API로 분리 |
| 오류 문구 | 코드 오류, 서명 미존재를 일반 사용자 관점의 간단한 메시지로 응답 |
| 검증 | Spring Boot `compileJava`, 전체 `test`, Vue `npm run build` 성공 |

## 6자리 이메일 식별자 중복 방지 보완

| 항목 | 내용 |
|---|---|
| 전역 중복 방지 | `email_identifier_codes.code`에 unique 제약과 인덱스 기준 추가 |
| 생성 정책 | 새 코드 발급 시 다른 이메일에 이미 배정된 코드와 충돌하면 최대 20회 재생성 |
| 저장 기준 통일 | 이메일이 있는 회원가입/OAuth 코드 발급 시 `email_identifier_codes`에도 항상 저장 |
| 기존 호환 | 태블릿 인증용 `identifier_codes` 저장은 유지하여 기존 인증 흐름과 충돌 방지 |
| 로그 | 코드 발급/충돌 로그는 이메일 마스킹, 코드 뒤 2자리만 출력 |
| 검증 | Spring Boot `compileJava`, 전체 `test`, Vue `npm run build` 성공 |

## 6자리 식별자 코드 전용 테이블 통일

| 항목 | 내용 |
|---|---|
| 기준 테이블 | `identifier_codes` |
| 적용 범위 | 이메일 로그인/회원가입, OAuth 신규 가입, 전화번호 코드, 태블릿 인증, 서명 이미지/증명서 다운로드 |
| 저장 정책 | User 기준으로 6자리 코드를 발급하고 `identifier_codes.user_id`, `identifier_codes.code`에 저장 |
| 검증 정책 | 이메일 로그인은 이메일로 User를 찾고 최신 코드 검증, 태블릿/다운로드는 코드 단독으로 최신 코드 조회 |
| 중복 방지 | 새 코드 발급 시 `identifier_codes` 전체에서 코드 중복을 검사하고 충돌 시 재생성 |
| 제거한 코드 | `EmailIdentifierCode`, `EmailIdentifierCodeRepository` 제거 |
| DB 주의사항 | 기존 DB에 이미 생성된 `email_identifier_codes` 테이블은 `ddl-auto=update`로 자동 삭제되지 않으므로, 운영 DB 정리는 별도 migration으로 처리 필요 |
