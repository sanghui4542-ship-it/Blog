-- =====================================================================
-- [7장] import.sql : 프로그램을 시작할 때 자동으로 실행되는 "처음 넣을 글 데이터" 파일
-- 파일 위치 : src/main/resources/import.sql
-- 6장과 달라진 점 : created_at, updated_at 열 두 개를 추가하고, NOW() 로 현재 시각을 넣습니다.
--   이유 : 7장 Article 엔티티에 createdAt, updatedAt 변수가 생겼기 때문에, DB 표에도 같은 이름의 열이 있어야 합니다.
-- NOW() : DB 가 지금 이 순간의 날짜와 시각을 돌려주는 함수입니다.
-- 실행 확인 : 앱 실행 로그에 INSERT INTO article (title, content, created_at, updated_at) ... 가 출력됩니다.
-- 주의 : 콘솔에서 한글이 ?? 로 보이면 터미널의 글자 인코딩 문제입니다. DB 에는 한글이 정상으로 들어갑니다.
-- =====================================================================

-- 1번째 글 : 제목 '제목 1', 내용 '내용 1', 작성 시각과 수정 시각은 지금 시각(NOW())입니다.
INSERT INTO article (title, content, created_at, updated_at) VALUES ('제목 1', '내용 1', NOW(), NOW())

-- 2번째 글 : 같은 방식입니다.
INSERT INTO article (title, content, created_at, updated_at) VALUES ('제목 2', '내용 2', NOW(), NOW())

-- 3번째 글 : 같은 방식입니다.
INSERT INTO article (title, content, created_at, updated_at) VALUES ('제목 3', '내용 3', NOW(), NOW())
