package com.aditya.nexora.profileService.exception;




import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class GlobalHandleApiResponse implements ResponseBodyAdvice {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        String declaringClassName = returnType.getDeclaringClass().getName();
        if (declaringClassName.contains("springdoc") || declaringClassName.contains("swagger")) {
            return false;
        }
        
        // Skip wrapping if the message converter is for raw binary/resource data
        if (org.springframework.http.converter.ByteArrayHttpMessageConverter.class.isAssignableFrom(converterType) ||
            org.springframework.http.converter.ResourceHttpMessageConverter.class.isAssignableFrom(converterType) ||
            org.springframework.http.converter.ResourceRegionHttpMessageConverter.class.isAssignableFrom(converterType)) {
            return false;
        }
        
        // Check method return type direct class
        Class<?> parameterType = returnType.getParameterType();
        if (parameterType.equals(byte[].class) || org.springframework.core.io.Resource.class.isAssignableFrom(parameterType)) {
            return false;
        }
        
        return true;
    }




    @Override
    public @Nullable Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (response instanceof ServletServerHttpResponse servletResponse
                && servletResponse.getServletResponse().getStatus() == HttpStatus.NO_CONTENT.value()) {
            return body;
        }

        // If the body is already raw binary or resource, return it directly without wrapping
        if (body instanceof byte[] || body instanceof org.springframework.core.io.Resource) {
            return body;
        }

        if(body instanceof ApiError) {
            return new ApiResponse<>(
                    false,
                    null,
                    (ApiError) body
            );
        }
        if(body instanceof ApiResponse<?>)
        {
            return body;
        }

        return new ApiResponse<>(
                true,
                body,
                null
        );
    }
}
