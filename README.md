# woody-service-back

Spring Boot 기반 백엔드 서비스입니다.  
예약 관리 API, 날씨/휴일 조회 기능, 파일 다운로드 엔드포인트, React 정적 파일 서빙(` /reservation `)을 제공합니다.

## 1) 기술 스택

- Java 21
- Spring Boot 3.4.2
- Spring Web, Spring Data JPA, Spring Security, Actuator
- MariaDB
- Gradle
- Jsoup

## 2) 주요 기능

- 예약 시스템 API (`/api/reservation/**`)
- 날씨 조회 API (`POST /weather`)
- 휴일 여부 조회 (`GET|POST /holiday`)
- 경제 뉴스 상위 목록 조회 (`POST /news10`)
- 다운로드 페이지/파일 제공 (`/download`, `/DT_Gen`, `/ChannelSchdMgr`, `/CCInfo`, `/XSDGenerator`)
- 프런트 정적 리소스 제공 (`/reservation`)

## 3) 프로젝트 구조

```text
src/main/java/com/restapi
├─ config         # 보안/CORS 설정
├─ controller     # API/Web 컨트롤러
├─ entity         # JPA 엔티티
├─ repository     # JPA 리포지토리
├─ scheduler      # 휴일 배치 스케줄러
└─ service        # 비즈니스 로직
```

## 4) 실행 전 준비사항

1. JDK 21 설치
2. MariaDB 실행
3. (기본 빌드 경로 사용 시) 프런트 프로젝트가 `../woody-service-front` 경로에 존재해야 함
4. Windows 기준 Gradle Wrapper 사용: `gradlew.bat`

## 5) 환경 변수 / 설정값

`src/main/resources/application.properties` 기준 기본값입니다.

| 키 | 기본값 | 설명 |
|---|---|---|
| `LOG_FILE` | `logs/woody-service-back.log` | 로그 파일 경로 |
| `SERVER_PORT` | `8080` | 서버 포트 |
| `SERVER_SSL_ENABLED` | `false` | SSL 활성화 여부 |
| `DB_URL` | `jdbc:mariadb://localhost:3306/private_was?useUnicode=true&characterEncoding=utf8` | DB 접속 URL |
| `DB_USER` | `root` | DB 계정 |
| `DB_PASSWORD` | (빈 값) | DB 비밀번호 |
| `JPA_DDL_AUTO` | `update` | JPA DDL 전략 |
| `JPA_SHOW_SQL` | `true` | SQL 로그 출력 |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | 허용 Origin(쉼표 구분) |
| `WOODY_DOWNLOAD_DIR` | `/opt/woody/downloads` | 다운로드 ZIP 파일 위치 |
| `HOLIDAY_SERVICE_KEY` | (빈 값) | 공휴일 API 서비스 키 |

## 6) 실행 방법

### 백엔드만 실행(프런트 빌드 스킵)

```bash
gradlew.bat bootRun -PskipFront=true
```

### 백엔드 실행(프런트 자동 빌드/복사 포함)

```bash
gradlew.bat bootRun
```

동작 방식:
- `build.gradle`에서 기본적으로 `../woody-service-front`를 `npm ci`/`npm run build` 후
- 결과물을 `src/main/resources/static/reservation`으로 복사합니다.

## 7) API 요약

### 7-1. 예약 API (`/api/reservation`)

1. `GET /api/reservation/students`  
학생 목록 조회
2. `POST /api/reservation/students`  
학생 등록  
요청 예시:
```json
{ "name": "홍길동" }
```
3. `DELETE /api/reservation/students/{id}`  
학생 삭제
4. `GET /api/reservation/schedules`  
예약 목록 조회
5. `POST /api/reservation/schedules`  
예약 등록  
요청 예시:
```json
{
  "studentName": "홍길동",
  "scheduleDate": "2026-02-10",
  "scheduleTime": "10:30",
  "status": "pending"
}
```
6. `DELETE /api/reservation/schedules/{id}`  
예약 삭제(과거 시간은 삭제 불가)
7. `POST /api/reservation/schedules/{id}/confirm`  
예약 확정
8. `POST /api/reservation/login`  
비밀번호 검증  
요청 예시:
```json
{ "password": "1234" }
```
성공 응답:
```json
{ "ok": true }
```
실패 시 `401` + `{ "ok": false }`
9. `POST /api/reservation/password`  
비밀번호 변경  
요청 예시:
```json
{ "newPassword": "새비밀번호" }
```

예약 비즈니스 룰:
- 예약 등록/확정/삭제는 모두 "현재 시각 이후" 일정만 허용
- 예약 등록 시 학생이 사전 등록되어 있어야 함
- 예약 확정 시
  - 같은 학생의 같은 주(`월~일`) 내 `pending` 예약은 삭제
  - 같은 날짜/시간의 다른 `pending` 예약도 삭제
- 최초 로그인 검증 시 `teacher` 계정이 없으면 자동 생성
  - 기본 계정: `teacher / 1234` (DB에는 BCrypt 해시로 저장)

### 7-2. 기타 API

1. `GET /reservation`, `GET /reservation/`  
`/reservation/index.html`로 포워딩
2. `GET /download`  
다운로드 링크 HTML 반환
3. `GET /DT_Gen`, `GET /ChannelSchdMgr`, `GET /CCInfo`, `GET /XSDGenerator`  
설정된 다운로드 폴더에서 ZIP 파일 응답
4. `GET /weather`  
안내 문자열 반환
5. `POST /weather`  
날씨 조회 문자열 반환 (`userKey`, `callIP`, `callTime`, `coX`, `coY`, `locate` 파라미터 사용)
6. `GET|POST /holiday`  
현재 휴일 상태(`Y` 또는 `N`) 반환
7. `POST /news10`  
경제 뉴스 목록 문자열 반환

## 8) 배치 스케줄러

- 클래스: `JangWoodyBatchScheduler`
- 실행 시점
  - 애플리케이션 기동 직후 1회
  - 매일 `00:10` (Asia/Seoul)
- 동작
  - 공휴일 API + 주말 여부를 기반으로 `Y`/`N` 계산
  - 전역 상태(`RestApiTest02Application`)에 저장

## 9) 데이터베이스 테이블

엔티티 기준으로 다음 테이블을 사용합니다.

- `student_info`
- `schedule`
- `teacher_info`

`spring.jpa.hibernate.ddl-auto=update` 기본값으로 실행 시 스키마가 자동 반영됩니다.

