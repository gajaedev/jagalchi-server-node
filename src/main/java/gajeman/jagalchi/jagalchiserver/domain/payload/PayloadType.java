package gajeman.jagalchi.jagalchiserver.domain.payload;

/**
 * Payload 타입 (logic.md 기준)
 * EDIT 액션의 세부 동작을 구분
 */
public enum PayloadType {
    INFO,   // 정보 수정 (라벨, 메타데이터 등)
    MOVE,   // 이동 (x, y 좌표 변경)
    SCALE,  // 크기 조절
    LOCK,   // 잠금/해제
    COPY    // 복제
}

