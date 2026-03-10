package gajeman.jagalchi.jagalchiserver.domain.event;

/**
 * Event 타입 (logic.md 기준)
 * 서버가 클라이언트에게 전송하는 응답 타입
 */
public enum EventType {
    SNAPSHOT,   // 초기 전체 상태
    ACK,        // 명령 접수 응답
    EVENT,      // 상태 확정 브로드캐스트
    PRESENCE    // 마우스 커서 등 인터랙션
}

