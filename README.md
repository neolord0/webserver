### 이 프로젝트는 Http/1.1 - RFC-2616에서 정의한 서버를 구현한 프로젝트이다.

* 주요 기능
    * HTTP 서버
        * 가상 호스트 : 여러 개의 호스트를 가상으로 생성할 수 있음
        * 지속 연결(Persistent Connection) : Connection, Keep-Alive 헤더 관련
        * SSL 지원 : jks 인증서 설정 등
        * 서버 사이드 내용 협상(Server Side Content Negotiation) : Accept-* 헤더 관련
        * 특정 디렉토리에서 서비스 되지 말아야 하는 파일 선별
        * 가상 디렉토리
        * 리소스 별 기능 
            * Basic 타입에 인증(Authorization) : Authorization, WWW-Authenticate 헤더 관련
            * 허락된 요청 메소드 체크
            * Charset 인코딩
            * Content 인코딩 : gzip, deflate 압축 
            * Expect 체크 :  Expect : 100-Continue 헤더 관련
            * URL-Redirection
    * 연동 
        * AJP 프로토콜 : Tomcat과 연동
        * HTTP 프록시 : 다른 웹 서버와 연동

* Http/1.1 - RFC-2616에서 명기된 기능 중에 구현되지 않은 부분은 다음과 같다.
    * 전송 코딩(Transfer Coding) : Chunked 만을 구현함.
    * 내용 코딩(Content-Coding) : Gzip, Deflate 만을 구현함
    * 요청(Request)
        1. Multi-Part 요청의 본문은 파싱(Parsing) 하지 않음.
        2. GET, HEAD Method에 대한 기능만을 구현함.
    * 인증(Authorization)
        1. 원본 서버에 대한 인증만 구현하고, 프록시 서버에 대한 부분은 구현하지 않음.
        2. Basic 타입에 인증 만을 구현함
    * 캐시(Cache) : 캐시 검증 부분에서 조건부(Conditional) GET Method를 처리하는 기능 이외에 나머지 부분은 구현하지 않음

* [프로그램에 대한 설명](https://github.com/neolord0/webserver/wiki/%ED%94%84%EB%A1%9C%EA%B7%B8%EB%9E%A8-%EC%84%A4%EB%AA%85)

* [설정 파일에 대한 설명](https://github.com/neolord0/webserver/wiki/%EC%84%A4%EC%A0%95-%ED%8C%8C%EC%9D%BC-%5B%EB%A9%94%EC%9D%B8-%EB%B6%80%EB%B6%84%5D)
