package com.example.todo.logging;

import com.example.todo.service.exception.ServiceException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

@Aspect
@Component
public class ServiceLoggingAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceLoggingAspect.class);

    @Around("execution(public * com.example.todo.service..*(..))")
    public Object logServiceCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        String argsSummary = summarizeArguments(joinPoint);
        long startNanos = System.nanoTime();
        LOGGER.info("service_method_enter method={} args={}", methodName, argsSummary);
        try {
            Object result = joinPoint.proceed();
            long durationMs = nanosToMillis(System.nanoTime() - startNanos);
            LOGGER.info("service_method_exit method={} durationMs={} result={}",
                    methodName, durationMs, summarizeValue(result));
            return result;
        } catch (Throwable throwable) {
            long durationMs = nanosToMillis(System.nanoTime() - startNanos);
            String type = throwable.getClass().getSimpleName();
            String message = sanitizeExceptionMessage(throwable.getMessage());
            if (throwable instanceof ServiceException) {
                LOGGER.warn("service_method_exception method={} durationMs={} type={} message={}",
                        methodName, durationMs, type, message);
            } else {
                LOGGER.error("service_method_exception method={} durationMs={} type={} message={}",
                        methodName, durationMs, type, message, throwable);
            }
            throw throwable;
        }
    }

    private String summarizeArguments(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String[] parameterNames = resolveParameterNames(joinPoint, args.length);
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        for (int i = 0; i < args.length; i++) {
            String parameterName = parameterNames[i];
            String value = isSensitiveParameter(parameterName) ? "<redacted>" : summarizeValue(args[i]);
            joiner.add(parameterName + "=" + value);
        }
        return joiner.toString();
    }

    private String[] resolveParameterNames(ProceedingJoinPoint joinPoint, int count) {
        String[] fallback = new String[count];
        for (int i = 0; i < count; i++) {
            fallback[i] = "arg" + i;
        }
        if (!(joinPoint.getSignature() instanceof CodeSignature codeSignature)) {
            return fallback;
        }
        String[] names = codeSignature.getParameterNames();
        if (names == null || names.length != count) {
            return fallback;
        }
        return names;
    }

    private boolean isSensitiveParameter(String parameterName) {
        if (parameterName == null) {
            return false;
        }
        String normalized = parameterName.toLowerCase(Locale.ROOT);
        return normalized.contains("password")
                || normalized.contains("token")
                || normalized.contains("secret")
                || normalized.contains("authorization");
    }

    private String summarizeValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof CharSequence sequence) {
            return "String(len=" + sequence.length() + ")";
        }
        if (value instanceof Number || value instanceof Boolean || value instanceof Enum<?>) {
            return String.valueOf(value);
        }
        if (value instanceof Pageable pageable) {
            return "Pageable(page=" + pageable.getPageNumber()
                    + ",size=" + pageable.getPageSize()
                    + ",sort=" + pageable.getSort() + ")";
        }
        if (value instanceof Optional<?> optional) {
            return optional.isPresent() ? "Optional(" + summarizeValue(optional.get()) + ")" : "Optional.empty";
        }
        if (value instanceof Collection<?> collection) {
            return value.getClass().getSimpleName() + "(size=" + collection.size() + ")";
        }
        if (value instanceof Map<?, ?> map) {
            return value.getClass().getSimpleName() + "(size=" + map.size() + ")";
        }
        if (value.getClass().isArray()) {
            return value.getClass().getComponentType().getSimpleName() + "[](len=" + Array.getLength(value) + ")";
        }
        return value.getClass().getSimpleName();
    }

    private String sanitizeExceptionMessage(String message) {
        if (message == null || message.isBlank()) {
            return "<empty>";
        }
        if (message.length() <= 256) {
            return message;
        }
        return message.substring(0, 256) + "...";
    }

    private long nanosToMillis(long nanos) {
        if (nanos <= 0L) {
            return 0L;
        }
        return nanos / 1_000_000L;
    }
}
