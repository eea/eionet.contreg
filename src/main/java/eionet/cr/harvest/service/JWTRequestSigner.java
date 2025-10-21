package eionet.cr.harvest.service;

import com.auth0.jwt.Algorithm;
import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import com.auth0.jwt.internal.com.fasterxml.jackson.databind.ObjectMapper;
import eionet.cr.harvest.HarvestException;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;

/**
 * DD push methods request signer.
 *
 * @author Enver
 */
public class JWTRequestSigner {
    /**
     * Verify a JWT using secret key.
     *
     * @param secretKey    a secret key to verify.
     * @param audience     audience.
     * @param jsonWebToken jwt value.
     * @return resolve json object.
     * @throws eionet.cr.harvest.HarvestException if any error occurs.
     */
    public JSONObject verify(String secretKey, String audience, String jsonWebToken) throws HarvestException {
        try {
            JWTVerifier jwtVerifier = new JWTVerifier(secretKey, audience);
            Map<String, Object> decodedPayload = jwtVerifier.verify(jsonWebToken);
            return JSONObject.fromObject(decodedPayload);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new HarvestException(e.getMessage(), e);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            throw new HarvestException(e.getMessage(), e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new HarvestException(e.getMessage(), e);
        } catch (SignatureException e) {
            e.printStackTrace();
            throw new HarvestException(e.getMessage(), e);
        } catch (JWTVerifyException e) {
            e.printStackTrace();
            throw new HarvestException(e.getMessage(), e);
        }
    }

    /**
     * Sign (encode) a json to create json web token.
     *
     * @param secretKey       a secret key to sign.
     * @param audience        audience.
     * @param jsonString      json as string.
     * @param expiryInMinutes expiration time in minutes (added to creation time).
     * @param algorithm       Algorithm as string value, supported algorithms are HS256, HS384 and HS512. Default is HS512.
     * @return encoded json web token.
     * @throws HarvestException if any error occurs.
     */
    public String sign(String secretKey, String audience, String jsonString, int expiryInMinutes, String algorithm) throws HarvestException {
        try {
            Map result = new ObjectMapper().readValue(jsonString, HashMap.class);
            return sign(secretKey, audience, result, expiryInMinutes, algorithm);
        } catch (IOException e) {
            e.printStackTrace();
            throw new HarvestException(e.getMessage(), e);
        }
    }

    /**
     * Sign (encode) a json to create json web token.
     *
     * @param secretKey       a secret key to sign.
     * @param audience        audience.
     * @param json            json as object.
     * @param expiryInMinutes expiration time in minutes (added to creation time).
     * @param algorithm       Algorithm as string value, supported algorithms are HS256, HS384 and HS512. Default is HS512.
     * @return encoded json web token.
     * @throws HarvestException if any error occurs.
     */
    public String sign(String secretKey, String audience, JSONObject json, int expiryInMinutes, String algorithm) throws HarvestException {
        if (json != null) {
            return sign(secretKey, audience, json.toString(), expiryInMinutes, algorithm);
        } else {
            return sign(secretKey, audience, (String) null, expiryInMinutes, algorithm);
        }
    }

    /**
     * Sign (encode) a json to create json web token.
     *
     * @param secretKey       a secret key to sign.
     * @param audience        audience.
     * @param jsonMap         json as map.
     * @param expiryInMinutes expiration time in minutes (added to creation time).
     * @param algorithm       Algorithm as string value, supported algorithms are HS256, HS384 and HS512. Default is HS512.
     * @return encoded json web token.
     * @throws HarvestException if any error occurs.
     */
    public String sign(String secretKey, String audience, Map jsonMap, int expiryInMinutes, String algorithm) throws HarvestException {
        JWTSigner jwtSigner = new JWTSigner(secretKey);
        JWTSigner.Options options = new JWTSigner.Options();
        options.setAlgorithm(getAlgorithm(algorithm));
        options.setExpirySeconds(expiryInMinutes * 60);
        options.setIssuedAt(true);
        options.setJwtId(true);
        return jwtSigner.sign(jsonMap, options);
    }

    /**
     * Gets or returns default algorithm for given string.
     * Note: Not using map for memory and since it is handled only in this method.
     *
     * @param algorithmStr algorithm identifier as string.
     * @return Algorithm object.
     */
    private Algorithm getAlgorithm(String algorithmStr) {
        if (StringUtils.equals("HS384", algorithmStr)) {
            return Algorithm.HS384;
        } else if (StringUtils.equals("HS256", algorithmStr)) {
            return Algorithm.HS256;
        }

        return Algorithm.HS512;
    }
}
