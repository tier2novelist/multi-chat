package edu.gwu.cs6431.multichat.core.server.exception;

import org.apache.commons.lang3.StringUtils;

public class MalformedMessageException extends Exception {

    private String headerFieldName;

    public MalformedMessageException(String headerFieldName) {
        this.headerFieldName = headerFieldName;
    }

    @Override
    public String getMessage() {
        return StringUtils.join( this.headerFieldName, " is not presented");
    }
}
