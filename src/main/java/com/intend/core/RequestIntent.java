package com.intend.core;
import java.net.URI;
public final class RequestIntent{
    public enum Mode { EXPLAIN, APPLY }
    private final String method;
    private final URI uri;
    private final String authType;
    private final boolean trace;
    private final Mode mode;
    public RequestIntent(String method, URI uri, String authType, boolean trace, Mode mode) {
        this.method = method;
        this.uri = uri;
        this.authType = authType;
        this.trace = trace;
        this.mode = mode;
    }
    public String getMethod() {
         return method;
        }
    public URI getUri(){ 
        return uri; 
    }
    public String getAuthType(){ 
        return authType; 
    }
    public boolean isTrace() 
    { return trace; 
    }
    public Mode getMode(){ 
        return mode;}
    @Override
    public String toString(){
        return String.format("[Intent: %s %s | Mode: %s | Auth: %s]", method, uri, mode, authType);
    }
}
