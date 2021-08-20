package com.cotiviti.loginportal.exception.domain;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.cotiviti.loginportal.domain.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.persistence.NoResultException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Objects;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class ExceptionHandling implements ErrorController {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    public static final String ACCOUNT_LOCKED = "Your account has been locked. Please contact Administration";
    public static final String METHOD_IS_NOT_ALLOWED = "This request method is not allowed on this endpoint. Please send a '%s' request";
    public static final String INTERNAL_SERVER_ERROR_MSG = "An error occured while processing the request";
    public static final String INCORRECT_CREDENTIALS = "Username / Password incorrect. Please try again";
    public static final String ACCOUNT_DISABLED = "Your account has been disabled. If this erro, please contact administration";
    public static final String ERROR_PROCESSING_FILE = "Error Occured while processing files";
    public static final String NOT_ENOUGH_PERMISSION = "You do not have enough permission";
    public static final String ERROR_PATH = "/error";

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<HttpResponse> accountDisableException(){
        return createHttpResponse(BAD_REQUEST,ACCOUNT_DISABLED);
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<HttpResponse> badCredentialsException(){
        return createHttpResponse(BAD_REQUEST,INCORRECT_CREDENTIALS);
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<HttpResponse> accessDeniedExcepiton(){
        return createHttpResponse(FORBIDDEN,NOT_ENOUGH_PERMISSION);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<HttpResponse> lockedExcepiton(){
        return createHttpResponse(UNAUTHORIZED,ACCOUNT_LOCKED);
    }
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<HttpResponse> tokenExpiredException(TokenExpiredException exception){
        return createHttpResponse(UNAUTHORIZED, exception.getMessage().toUpperCase()  );
    }
    @ExceptionHandler(EmailExitsException.class)
    public ResponseEntity<HttpResponse> emailExistException(EmailExitsException exception){
        return createHttpResponse(BAD_REQUEST, exception.getMessage().toUpperCase()  );
    }
    @ExceptionHandler(UserExitsException.class)
    public ResponseEntity<HttpResponse> userExistException(UserExitsException exception){
        return createHttpResponse(BAD_REQUEST, exception.getMessage().toUpperCase()  );
    }
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<HttpResponse> userNameNotFoundException(UserNotFoundException exception){
        return createHttpResponse(BAD_REQUEST, exception.getMessage().toUpperCase()  );
    }
    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<HttpResponse> emailNotFoundException(EmailNotFoundException exception){
        return createHttpResponse(BAD_REQUEST, exception.getMessage().toUpperCase()  );
    }

    /*@ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<HttpResponse> noHandlerFoundException(NoHandlerFoundException exception){
        return createHttpResponse(BAD_REQUEST, "This page was not found");
    }*/


    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<HttpResponse> methodNotSupportedException(HttpRequestMethodNotSupportedException exception){
        HttpMethod supportedMethod = Objects.requireNonNull(exception.getSupportedHttpMethods().iterator().next());
        return createHttpResponse(METHOD_NOT_ALLOWED, String.format(METHOD_IS_NOT_ALLOWED,supportedMethod));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<HttpResponse> internalServerErrorException(Exception exception){
        LOGGER.error(exception.getMessage());
        return createHttpResponse(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MSG);
    }

    @ExceptionHandler(NoResultException.class)
    public ResponseEntity<HttpResponse> notFoundException(NoResultException exception){
        LOGGER.error(exception.getMessage());
        return createHttpResponse(NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<HttpResponse> iOEException(IOException exception){
        LOGGER.error(exception.getMessage());
        return createHttpResponse(INTERNAL_SERVER_ERROR, ERROR_PROCESSING_FILE);
    }

    private ResponseEntity<HttpResponse> createHttpResponse(HttpStatus httpStatus, String message){
        HttpResponse httpResponse = new HttpResponse(httpStatus.value(),httpStatus,httpStatus.getReasonPhrase().toUpperCase(),message.toUpperCase());
        return new ResponseEntity<>(httpResponse,httpStatus);
    }
    @RequestMapping(ERROR_PATH)
    public ResponseEntity<HttpResponse> notFound404(){
        return createHttpResponse(NOT_FOUND, "There is no mapping for this URL");
    }
}
