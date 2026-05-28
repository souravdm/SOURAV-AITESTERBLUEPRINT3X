package com.restfulbooker.api.constants;

/**
 * HTTP status code constants used in assertions.
 */
public final class StatusCodes {

    private StatusCodes() {}

    public static final int OK         = 200;
    public static final int CREATED    = 201;
    public static final int BAD_REQUEST  = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN    = 403;
    public static final int NOT_FOUND    = 404;
    public static final int METHOD_NOT_ALLOWED = 405;
    public static final int SERVER_ERROR = 500;
}
