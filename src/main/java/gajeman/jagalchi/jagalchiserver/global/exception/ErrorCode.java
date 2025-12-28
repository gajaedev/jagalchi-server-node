package gajeman.jagalchi.jagalchiserver.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Auth 관련
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_001", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_002", "권한이 없습니다."),
    INVALID_USER_ID(HttpStatus.BAD_REQUEST, "AUTH_003", "유효하지 않은 사용자 ID입니다."),

    // Action 관련
    DUPLICATE_ACTION(HttpStatus.CONFLICT, "ACTION_001", "이미 처리된 요청입니다."),
    INVALID_ACTION_TYPE(HttpStatus.BAD_REQUEST, "ACTION_002", "지원하지 않는 액션 타입입니다."),

    // Node 관련
    NODE_POSITION_REQUIRED(HttpStatus.BAD_REQUEST, "NODE_001", "위치 정보는 필수입니다."),
    NODE_NOT_FOUND(HttpStatus.NOT_FOUND, "NODE_002", "노드를 찾을 수 없습니다."),

    // Edge 관련
    EDGE_NOT_FOUND(HttpStatus.NOT_FOUND, "EDGE_001", "엣지를 찾을 수 없습니다."),

    // Roadmap 관련
    ROADMAP_NOT_FOUND(HttpStatus.NOT_FOUND, "ROADMAP_001", "로드맵을 찾을 수 없습니다."),

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_001", "잘못된 입력입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_002", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
