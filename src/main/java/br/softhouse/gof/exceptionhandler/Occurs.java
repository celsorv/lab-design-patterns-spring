package br.softhouse.gof.exceptionhandler;

import lombok.Getter;

@Getter
public enum Occurs {

    BUSINESS_RULES_VIOLATION("/business-rules-violation", "Business rules violation"),
    ENTITY_IN_USE("/entity-in-use", "Entity in use"),
    INVALID_DATA("/invalid-data", "Invalid data"),
    INVALID_PARAM("/invalid-param", "Invalid parameter"),
    RESOURCE_NOT_FOUND("/resource-not-found", "Resource not found"),
    INCOMPREHENSIBLE_MESSAGE("/incomprehensible-msg", "Incomprehensible message"),
    SYSTEM_ERROR("/system-error", "System error");

    private String title;
    private String uri;

    Occurs(String path, String title) {
        this.uri = "https://localhost:8080" + path;
        this.title = title;
    }

}
