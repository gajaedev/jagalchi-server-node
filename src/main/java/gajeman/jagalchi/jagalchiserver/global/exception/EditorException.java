package gajeman.jagalchi.jagalchiserver.global.exception;

import lombok.Getter;

@Getter
public class EditorException extends RuntimeException {

    private final ErrorCode errorCode;

    public EditorException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

