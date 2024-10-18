# Cucucook
![KakaoTalk_20241016_214145868](https://github.com/user-attachments/assets/6f5447e4-7bd7-4f47-aa16-34d5f4e03fab)


## 🔗사이트 링크
&emsp; https://cucucook.site/


&emsp;  테스트 아이디 (관리자)

&emsp;  아이디 : user01 ,비밀번호 :1111

&emsp;  테스트 아이디 (유저)

&emsp;  아이디 : user20 ,비밀번호 :1111

## 📝프로젝트 개요
&emsp; - 프로젝트: 요리 레시피 플렛폼 (반응형 웹)

&emsp; - 기획 및 제작: 이은혜([@eunh](https://github.com/2eunh)), 장해림([@haerimzzang](https://github.com/haerimzzang)), 이지은([@dev-jieun-lee](https://github.com/dev-jieun-lee))

&emsp; - 제작 기간: 2024.07.27 ~ 2024.10.16.

&emsp; - 배포일: 2024.10.16.

## ✨프로젝트 소개
&emsp; cucucook은 다양한 요리 레시피를 공유하고 소셜 기능을 통해 사용자들이 소통할 수 있는 플랫폼입니다. 

&emsp; 사용자는 자신만의 레시피를 등록하고 다른 사용자들의 레시피를 탐색할 수 있습니다. 

&emsp; 또한, 댓글과 좋아요 기능을 통해 상호 작용할 수 있습니다.

## 👩‍💻개발 환경
&emsp; Front-end : ![Typescript Badge](https://img.shields.io/badge/-TypeScript-3178C6?style=flat-square&logo=TypeScript&logoColor=white
)
![React Badge](https://img.shields.io/badge/-React-61DAFB?style=flat-square&logo=react&logoColor=white
)
![StyledComponents Badge](https://img.shields.io/badge/-StyledComponents-DB7093?style=flat-square&logo=styledcomponents&logoColor=white
)
![Pnpm Badge](https://img.shields.io/badge/-Pnpm-F69220?style=flat-square&logo=pnpm&logoColor=white
)
![Node.js Badge](https://img.shields.io/badge/-Node.js-5FA04E?style=flat-square&logo=nodedotjs&logoColor=white
)


&emsp; Back-end : ![Spring Boot Badge](https://img.shields.io/badge/-Spring%20boot-6DB33F?style=flat-square&logo=springboot&logoColor=white
)
![Spring security Badge](https://img.shields.io/badge/-Spring%20security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white
)
![Apache Maven Badge](https://img.shields.io/badge/-Apache%20Maven-C71A36?style=flat-square&logo=apachemaven&logoColor=white
)
![Java Badge](https://img.shields.io/badge/-Java-5382a1?style=flat-square&logoColor=white)

&emsp; 서버 & 상태관리 : ![Amazon Web Services Badge](https://img.shields.io/badge/-Amazon%20Web%20Services-232F3E?style=flat-square&logo=amazonwebservices&logoColor=white
)
![Ubuntu Badge](https://img.shields.io/badge/-Ubuntu-E95420?style=flat-square&logo=ubuntu&logoColor=white
)
![Nginx Badge](https://img.shields.io/badge/-Nginx-009639?style=flat-square&logo=nginx&logoColor=white
)
![Docker Badge](https://img.shields.io/badge/-Docker-2496ED?style=flat-square&logo=docker&logoColor=white
)
![GitHub Badge](https://img.shields.io/badge/-GitHub-181717?style=flat-square&logo=github&logoColor=white
)

&emsp; 데이터베이스 : ![PostgreSQL Badge](https://img.shields.io/badge/-PostgreSQL-4169E1?style=flat-square&logo=postgresql&logoColor=white
)

## 🗂️데이터베이스 (ERD)
![](https://velog.velcdn.com/images/eunhye2/post/4c01fc0e-0ff5-4e4b-a579-3aafb25ec87c/image.png)

## 💿기능 및 구현

1. **디자인**
    - 로고 제작
    - styled-component를 사용하여 재 사용성 최적화 
    - mui를 사용하여 ui 최적화
2. **로그인, 로그아웃**
    - 로그인 시 토큰과 스토리지를 사용하여 로그인 상태 관리
    - 아이디 찾기, 비밀번호 찾기 기능
    - 비밀번호 유효성 검사, 비밀번호 암호화
    - 아이디 저장, 자동 로그인
    - 카카오, 네이버 로그인 연동 
3. **외부 API 레시피 조회**
    - 공공 API를 사용하여 레시피 조회, 검색
    - 사용 API → [조리식품의 레시피 DB](https://www.foodsafetykorea.go.kr/api/openApiInfo.do?menu_grp=MENU_GRP31&menu_no=661&show_cnt=10&start_idx=1&svc_no=COOKRCP01)
4. **내부 API 레시피 CRUD**
    - 직접 개발한 API를 사용하여 레시피 CRUD 
    - 댓글/대댓글 및 이미지 업로드 기능
    - 검색, 찜(좋아요) 기능 
    - 페이징 기능 / 무한 스크롤
5. **게시판**
    - 공지사항, FAQ, 질의 응답
    - 공지사항과 FAQ는 관리자만 작성, 수정, 삭제 가능
    - 퀼 에디터 사용
    - 검색 및 페이징 (페이지네이션) 기능
    - 질의는 회원만, 응답은 관리자만 가능
    - 이미지 업로드 및 파일 첨부 기능 
6. **마이페이지**
    - 로그인 했을 경우에만 메뉴 활성화
    - 내 정보 조회 및 수정 기능
    - 비밀번호 변경 기능
    - 내 활동 → 내가 쓴 레시피, 찜한 레시피, 게시글, 댓글 한눈에 조회 및 상세 조회
7. **관리자**
    - 관리자 전용 게시판 → 권한이 관리자일 경우에만 메뉴 활성화
    - 회원 관리, 게시판과 레시피의 카테고리 관리 기능
8. **기타**
    - 권한이 없을 경우에 페이지 접근 불가능한 라우트 가드 
    - 권한이 없을 경우 API호출 불가
    - 다크 / 라이트 모드 기능 상태 관리
    - 한글 / 영어 번역 기능 상태 관리
    - 반응형 홈페이지로 제작



