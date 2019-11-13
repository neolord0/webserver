##  이 프로젝트는 Http/1.1 - RFC-2616에서 정의한 서버를 구현했다.

### 1. 주요 기능
    1. HTTP 서버
        1. 가상 호스트 : 여러 개의 호스트를 가상으로 생성할 수 있음
        2. 지속 연결(Persistent Connection) : Connection, Keep-Alive 헤더 관련
        3. SSL 지원 : jks 인증서 설정 등
        4. 서버 사이드 내용 협상(Server Side Content Negotiation) : Accept-* 헤더 관련
        5. 특정 디렉토리에서 서비스 되지 말아야 하는 파일 선별
        6. 가상 디렉토리
        7. 리소스 별 기능 
            1. Basic 타입에 인증(Authorization) : Authorization, WWW-Authenticate 헤더 관련
            2. 허락된 요청 메소드 체크
            3. Charset 인코딩
            4. Content 인코딩 : gzip, deflate 압축 
            5. Expect 체크 :  Expect : 100-Continue 헤더 관련
            6. URL-Redirection
    2. 연동 
         1. AJP 프로토콜 : Tomcat과 연동
         2. HTTP 프록시 : 다른 웹 서버와 연동

### 2. Http/1.1 - RFC-2616에서 명기된 기능 중에 구현되지 않은 부분은 다음과 같다.
    1. 전송 코딩(Transfer Coding) : Chunked 만을 구현함.
    2. 내용 코딩(Content-Coding) : Gzip, Deflate 만을 구현함
    3. 요청(Request)
        1. Multi-Part 요청의 본문은 파싱(Parsing) 하지 않음.
        2. GET, HEAD Method에 대한 기능만을 구현함.
    4. 인증(Authorization)
        1. 원본 서버에 대한 인증만 구현하고, 프록시 서버에 대한 부분은 구현하지 않음.
        2. Basic 타입에 인증 만을 구현함
    5. 내용 협상(Content-Negotiation) : Server Side Negotiation 기능 구현함.
    6. 캐시(Cache) : 캐시 검증 부분에서 조건부(Conditional) GET Method를 처리하는 부분이 이외에 부분은 구현하지 않음

### 3. 모듈 설명
    1. 구성도
![](https://user-images.githubusercontent.com/24711108/68758991-39800e80-0652-11ea-8356-6a64448f305f.png)


    2. 설명
        1. Server : 웹 서버 자체를 가리키는 모듈로, HTTP 요청을 처리하는 Processer 부분과 각 가상 호스트를 담당하는 Host 클래스를 가진다.  
            1. Processor : HTTP 요청을 처리하는 모듈을 기능별로 나눠서 모듈화 한 부분이다.
                1. Client-Listener : 클라이언트로부터 연결이 있을 때 이를 처리한다.
                2. SSL-Handshaker : SSL 연결에서 Handshaking 작업을 처리한다.
                3. Request-Receiver : 클라이언트로부터 보내진 데이터를 헤더까지 파싱하여 요청 객체를 만든다.
                4. Body-Receiver : 요청을 처리하다가 요청 분문이 필요할 때 본문을 받는다.
                5. Request-Performer : 요청을 처리하여 응답 객체를 만든다.
                6. Reply-Sender : 응답을 클라이언트로 보낸다.
                7. Proxy-Connector :  AJP, HTTP Server에 연결한다.
                8. Ajp-Proxier : Ajp Server(TOMCAT)와 연결의 요청/응답을 중계한다.
                9. HTTP-Proxier : HTTP Server와 연결의 요청/응답을 중계한다. 
            2. Host : 각각의 가상 호스트에 대한 정보와 리소스들을 저장한다.
               1. Resource : 루트 디렉토리 안에 디렉토리와 파일에 대한 정보를 관리한다. 각 리소스는 설정된 필터 객체를 관리한다. 
        2. Context :  위에 설명한 Processor 모듈에서 필요한 버퍼 객체나 중간 상태 등을 저장한다.
            1. Client-Connection : 클라이언트와 연결을 처리할 때 필요한 것들을 저장한다.
            2. Ajp-Proxy-Connection : Ajp Server(Tomcat)와 연결을 처리할 때 필요한 것들을 저장한다.
            3. Http-Proxy-Connection : HTTP Server와 연결을 처리할 때 필요한 것들을 저장한다.
            4. Request : 처리과정에서 만들어진 요청 객체다. 
            5. Reply : 처리과정에서 만들어진 요청 객체다.

    3. 흐름도
![](https://user-images.githubusercontent.com/24711108/68759193-94196a80-0652-11ea-96cc-9e6f775bb372.png)
