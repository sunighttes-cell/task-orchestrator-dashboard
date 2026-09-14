package com.taskOrchestrator.app.common.logging;

public final class LoggingConstants {
    private LoggingConstants() {}

    public static final String REQUEST_ID = "requestId";
    public static final String USER_ID = "userId";
    public static final String JOB_ID = "jobId";
    public static final String EXECUTION_ID = "executionId";

    public static final String HTTP_METHOD = "method";
    public static final String HTTP_PATH = "path";

    public static final String DURATION_MS = "durationMs";
    public static final String STATUS = "status";

    public static final String REQUEST_ID_HEADER = "X-Request-ID";
}