package br.softhouse.gof.exceptionhandler;

import br.softhouse.gof.exception.BusinessException;
import br.softhouse.gof.exception.EntityInUseException;
import br.softhouse.gof.exception.EntityNotFoundException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.PropertyBindingException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String UMSG_GENERIC_ERROR = "Internal error occurred. Check the problem and try again.";

    @Autowired
    private MessageSource messageSource;

    // -----------------------
    // Exceptions
    // -----------------------

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleEntityNotFound(EntityNotFoundException ex, WebRequest request) {

        HttpStatus status = HttpStatus.NOT_FOUND;
        Occurs occurs = Occurs.RESOURCE_NOT_FOUND;

        return generatedResponseAny(ex, status, request, occurs);
    }

    @ExceptionHandler(EntityInUseException.class)
    public ResponseEntity<?> handleEntityInUse(EntityInUseException ex, WebRequest request) {

        HttpStatus status = HttpStatus.CONFLICT;
        Occurs occurs = Occurs.ENTITY_IN_USE;

        return generatedResponseAny(ex, status, request, occurs);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusinessRules(BusinessException ex, WebRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;
        Occurs occurs = Occurs.BUSINESS_RULES_VIOLATION;

        return generatedResponseAny(ex, status, request, occurs);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUncaught(Exception ex, WebRequest request) {

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        Occurs occurs = Occurs.SYSTEM_ERROR;

        ex.printStackTrace();

        return generatedResponseObject(ex, new HttpHeaders(), status, request, occurs, UMSG_GENERIC_ERROR);
    }

    // ------------------------
    // Redefines handle methods
    // ------------------------

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
                    HttpMessageNotReadableException ex, HttpHeaders headers,
                        HttpStatus status, WebRequest request) {

        Throwable rootCause = ExceptionUtils.getRootCause(ex);

        if (rootCause instanceof InvalidFormatException) {
            return handleInvalidFormat((InvalidFormatException) rootCause, headers, status, request);

        } else if (rootCause instanceof PropertyBindingException) {
            return handlePropertyBinding((PropertyBindingException) rootCause, headers, status, request);

        }

        String detail = "The request body is invalid. Please check syntax error and try again.";
        Occurs occurs = Occurs.INCOMPREHENSIBLE_MESSAGE;

        return generatedResponseObject(ex, headers, status, request, occurs, detail);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        String detail = "There are one or more invalid properties. Please correct and try again.";
        Occurs occurs = Occurs.INVALID_DATA;

        BindingResult bindingResult = ex.getBindingResult();

        List<Occurrence.Description> occurrenceDescriptions =
                bindingResult.getAllErrors().stream().map(obj -> {

                    String message = messageSource.getMessage(obj, LocaleContextHolder.getLocale());
                    String name = obj.getObjectName();

                    if (obj instanceof FieldError) {
                        name = ((FieldError) obj).getField();
                    }

                    return Occurrence.Description.builder()
                            .name(name)
                            .userMessage(message)
                            .build();

                }).collect(Collectors.toList());

        Occurrence occurrence = createOccurrenceBuilder(occurs, detail, status)
                .userMessage(detail)
                .descriptions(occurrenceDescriptions)
                .build();

        return handleExceptionInternal(ex, occurrence, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        Occurs occurs = Occurs.RESOURCE_NOT_FOUND;
        String detail = String.format(
                "The resource %s does not exist. Please check and try again.", ex.getRequestURL()
        );

        return generatedResponseObject(ex, headers, status, request, occurs, detail);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        if (ex instanceof MethodArgumentTypeMismatchException) {
            return handleMethodArgumentTypeMismatch(
                    (MethodArgumentTypeMismatchException) ex, headers, status, request);
        }

        return super.handleTypeMismatch(ex, headers, status, request);
    }

    // -----------------------
    // Private Methods
    // -----------------------

    private ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        String propType = "";
        if (ex.getRequiredType() != null) {
            propType = ex.getRequiredType().getSimpleName();
        }

        Occurs occurs = Occurs.INVALID_PARAM;
        String detail = String.format(
                "The value '%s' received as URL parameter '%s' is of an invalid type. " +
                        "Correct to a value of type '%s'.",
                ex.getValue(), ex.getName(), propType
        );

        return generatedResponseObject(ex, headers, status, request, occurs, detail);
    }

    private ResponseEntity<Object> handleInvalidFormat(
            InvalidFormatException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        String path = joinPath(ex.getPath());
        Occurs occurs = Occurs.INCOMPREHENSIBLE_MESSAGE;
        String detail = String.format(
                "The value '%s' is an incompatible type with the '%s' property. " +
                "Correct to a value compatible with the '%s' type",
                path, ex.getValue(), ex.getTargetType().getSimpleName()
        );

        return generatedResponseObject(ex, headers, status, request, occurs, detail);
    }

    private ResponseEntity<Object> handlePropertyBinding(
            PropertyBindingException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        String path = joinPath(ex.getPath());
        Occurs occurs = Occurs.INCOMPREHENSIBLE_MESSAGE;
        String detail = String.format("The property %s does not exist. Please check and try again.", path);

        Occurrence occurrence = createOccurrenceBuilder(occurs, detail, status)
            .userMessage(UMSG_GENERIC_ERROR)
            .build();

        return handleExceptionInternal(ex, occurrence, headers, status, request);
    }

    private ResponseEntity<Object> generatedResponseObject(
            Exception ex, HttpHeaders headers, HttpStatus status,
            WebRequest request, Occurs occurs, String detail) {

        Occurrence occurrence = createOccurrenceBuilder(occurs, detail, status)
                .userMessage(UMSG_GENERIC_ERROR)
                .build();

        return handleExceptionInternal(ex, occurrence, headers, status, request);
    }

    private ResponseEntity<?> generatedResponseAny(
            Exception ex, HttpStatus status, WebRequest request, Occurs occurs) {

        String detail = ex.getMessage();

        Occurrence occurrence = createOccurrenceBuilder(occurs, detail, status)
                .userMessage(detail)
                .build();

        return handleExceptionInternal(ex, occurrence, new HttpHeaders(), status, request);
    }

    private Occurrence.OccurrenceBuilder createOccurrenceBuilder(
                    Occurs occurs, String detail, HttpStatus status) {

        return Occurrence.builder()
                .title(occurs.getTitle())
                .type(occurs.getUri())
                .status(status.value())
                .detail(detail);
    }

    private String joinPath(List<JsonMappingException.Reference> references) {
        return references.stream()
                .map(JsonMappingException.Reference::getFieldName)
                .collect(Collectors.joining("."));
    }

}
