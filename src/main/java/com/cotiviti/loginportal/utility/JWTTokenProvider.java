package com.cotiviti.loginportal.utility;

import com.auth0.jwt.JWT;
import static com.cotiviti.loginportal.constansts.SecurityContstant.*;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.cotiviti.loginportal.security.UserPriniciple;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

@Component
public class JWTTokenProvider {
    @Value("${jwt.secret}")
    private String secret;

    public String genertateJwtToken(UserPriniciple userPriniciple){
        String[] claims = getClaimsForUser(userPriniciple);
        return JWT.create()
                .withIssuer(GET_ARRAYS_LLC)
                .withAudience(GET_ARRAYS_ADMINISTRATION)
                .withIssuedAt(new Date())
                .withSubject(userPriniciple.getUsername())
                .withArrayClaim(AUTHORITIES, claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(HMAC512(secret.getBytes()));
    }

    public List<GrantedAuthority> getAuthorities(String token){
        /*JWTVerifier verifier;
        Algorithm algorithm = HMAC512(secret);
        verifier = JWT.require(algorithm).withIssuer(GET_ARRAYS_LLC).build();
        String cl[] = verifier.verify(token).getClaim(AUTHORITIES).asArray(String.class);
        return Arrays.stream(cl).map(SimpleGrantedAuthority::new).collect(Collectors.toList());*/
        String[] claims = getClaimsForToken(token);
        return Arrays.stream(claims).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    public Authentication getAuthentication(String username, List<GrantedAuthority> authorities, HttpServletRequest request){
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new
                UsernamePasswordAuthenticationToken(username,null, authorities);
        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return usernamePasswordAuthenticationToken;
    }

    public boolean isTOkenValid(String username, String token){
        JWTVerifier verifier = getJwtVerfier();
        return StringUtils.isNotEmpty(username) && !isTokenExpired(verifier, token);

    }

    public String getSubject(String token){
        JWTVerifier verifier = getJwtVerfier();
        return verifier.verify(token).getSubject();
    }

    private boolean isTokenExpired(JWTVerifier verifier, String token) {
        Date Expiration = verifier.verify(token).getExpiresAt();
        return Expiration.before(new Date());
    }

    private String[] getClaimsForToken(String token) {
        JWTVerifier jwtVerifier = getJwtVerfier();
        return jwtVerifier.verify(token).getClaim(AUTHORITIES).asArray(String.class);
    }

    private JWTVerifier getJwtVerfier() {
        JWTVerifier verifier;
        try {
        Algorithm algorithm = HMAC512(secret);
        verifier = JWT.require(algorithm).withIssuer(GET_ARRAYS_LLC).build();

        }catch (JWTVerificationException jwtVerfier){
            throw new JWTVerificationException(TOKEN_CANNOT_BE_VERIFIED);
        }
        return verifier;
    }

    private String[] getClaimsForUser(UserPriniciple user) {
        List<String> authorities = new ArrayList<>();
        for(GrantedAuthority grantedAuthority: user.getAuthorities()){
            authorities.add(grantedAuthority.getAuthority());
        }
        //user.getAuthorities().forEach(x -> authorities.add(x.getAuthority()));
        return authorities.toArray(new String[0]);
    }
}
