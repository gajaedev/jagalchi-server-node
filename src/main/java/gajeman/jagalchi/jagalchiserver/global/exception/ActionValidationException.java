package gajeman.jagalchi.jagalchiserver.global.exception;

/**
 * Action 검증 실패 예외
 * Validator에서 발생하는 예외
 *
 * convention.md: Validation 분리 기준
 * → 검증 실패는 NACK으로 클라이언트에 알림
 */
public class ActionValidationException extends RuntimeException {
    private final String errorCode;
    private final String errorMessage;

    public ActionValidationException(
            String errorCode,
            String errorMessage
    ) {
        super(errorMessage);
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

