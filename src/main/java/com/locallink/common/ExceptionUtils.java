package com.locallink.common;

/**
 * 从异常链中提取可读错误信息
 */
public final class ExceptionUtils {

    private ExceptionUtils() {
    }

    public static String resolveMessage(Throwable throwable) {
        if (throwable == null) {
            return "系统内部错误";
        }

        Throwable current = throwable;
        BusinessException businessException = null;
        String lastMessage = null;

        while (current != null) {
            if (current instanceof BusinessException) {
                businessException = (BusinessException) current;
            }
            if (current.getMessage() != null && !current.getMessage().isBlank()) {
                lastMessage = current.getMessage();
            }
            current = current.getCause();
        }

        if (businessException != null) {
            String businessMessage = businessException.getMessage();
            if (businessMessage != null && !businessMessage.isBlank()) {
                return businessMessage;
            }
        }

        if (lastMessage != null && !lastMessage.isBlank()) {
            return lastMessage;
        }

        Throwable root = throwable;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return root.getClass().getSimpleName();
    }
}
