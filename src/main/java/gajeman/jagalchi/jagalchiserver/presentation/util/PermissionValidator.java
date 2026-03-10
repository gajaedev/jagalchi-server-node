package gajeman.jagalchi.jagalchiserver.presentation.util;

import gajeman.jagalchi.jagalchiserver.global.exception.EditorException;
import gajeman.jagalchi.jagalchiserver.global.exception.ErrorCode;

/**
 * 권한 검증 유틸리티
 * convention.md: Validation 분리
 *
 * 요청 시 권한 확인 메서드 제공
 */
public class PermissionValidator {

    public static void requireView(String permissions) {
        if (!hasPermission(permissions, "VIEW")) {
            throw new EditorException(ErrorCode.INVALID_INPUT);
        }
    }

    public static void requireCreateNode(String permissions) {
        if (!hasPermission(permissions, "CREATE_NODE")) {
            throw new EditorException(ErrorCode.INVALID_INPUT);
        }
    }

    public static void requireEditNode(String permissions) {
        if (!hasPermission(permissions, "EDIT_NODE")) {
            throw new EditorException(ErrorCode.INVALID_INPUT);
        }
    }

    public static void requireDeleteNode(String permissions) {
        if (!hasPermission(permissions, "DELETE_NODE")) {
            throw new EditorException(ErrorCode.INVALID_INPUT);
        }
    }

    public static void requireCreateEdge(String permissions) {
        if (!hasPermission(permissions, "CREATE_EDGE")) {
            throw new EditorException(ErrorCode.INVALID_INPUT);
        }
    }

    public static void requireEditEdge(String permissions) {
        if (!hasPermission(permissions, "EDIT_EDGE")) {
            throw new EditorException(ErrorCode.INVALID_INPUT);
        }
    }

    public static void requireDeleteEdge(String permissions) {
        if (!hasPermission(permissions, "DELETE_EDGE")) {
            throw new EditorException(ErrorCode.INVALID_INPUT);
        }
    }

    public static void requireCreateResource(String permissions) {
        if (!hasPermission(permissions, "CREATE_RESOURCE")) {
            throw new EditorException(ErrorCode.INVALID_INPUT);
        }
    }

    public static void requireEditResource(String permissions) {
        if (!hasPermission(permissions, "EDIT_RESOURCE")) {
            throw new EditorException(ErrorCode.INVALID_INPUT);
        }
    }

    public static void requireDeleteResource(String permissions) {
        if (!hasPermission(permissions, "DELETE_RESOURCE")) {
            throw new EditorException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * 특정 권한 확인
     * permissions: "EDIT,VIEW,CREATE_NODE,..." (쉼표 구분)
     */
    private static boolean hasPermission(
            String permissions,
            String permission
    ) {
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }

        String[] permissionArray = permissions.split(",");
        for (String p : permissionArray) {
            if (p.trim().equals(permission)) {
                return true;
            }
        }

        return false;
    }
}

