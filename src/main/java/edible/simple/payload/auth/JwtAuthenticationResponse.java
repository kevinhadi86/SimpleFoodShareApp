/**
 * Alipay.com Inc.
 * Copyright (c) 2004‐2019 All Rights Reserved.
 */
package edible.simple.payload.auth;

/**
 * @author Kevin Hadinata
 * @version $Id: JwtAuthenticationResponse.java, v 0.1 2019‐09‐12 18:31 Kevin Hadinata Exp $$
 */
public class JwtAuthenticationResponse {

    private boolean success;
    private String accessToken;
    private String tokenType = "Bearer";

    public JwtAuthenticationResponse(boolean success, String accessToken) {
        this.success = success;
        this.accessToken = accessToken;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}
