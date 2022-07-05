package com.mienmq.client.enums;

public enum RequestType {
    SEND_MESSAGE(0),
    PULL_MESSAGE(1),
    HEART_BEAT(2),

    ERROR_TYPE(-1);

    private int requestType;

    private RequestType(int requestType) {
        this.requestType = requestType;
    }

    public int getRequestType() {
        return this.requestType;
    }

    /**
     * 通过枚举名称获取对应枚举
     * @param enumName
     * @return
     */
    public static RequestType getTypeByEnumName(String enumName) {
        for (RequestType value : RequestType.values()) {
            if (value.name().equals(enumName)) {
                return value;
            }
        }
        return ERROR_TYPE;
    }
}
