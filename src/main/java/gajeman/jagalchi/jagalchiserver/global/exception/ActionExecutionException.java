package gajeman.jagalchi.jagalchiserver.global.exception;

/**
 * Action 실행 실패 예외
 * Handler에서 비즈니스 로직 실패 시 발생
 *
 * convention.md: 에러 처리 방식
 * → 실행 실패는 NACK으로 클라이언트에 알림
 */
public class ActionExecutionException extends RuntimeException {
    private final String errorCode;
    private final String errorMessage;

    public ActionExecutionException(
            String errorCode,
            String errorMessage
    ) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public ActionExecutionException(
            String errorCode,
            String errorMessage,
            Throwable cause
    ) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

