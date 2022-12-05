package com.voiceroom.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;

@Slf4j
public class ValidationUtil {

    public static void validate(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String msg = bindingResult.getFieldError().getDefaultMessage();
            log.warn("illegal request parameter: {}", msg);
            throw new IllegalArgumentException(msg);
        }
    }
}
