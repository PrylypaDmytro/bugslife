package com.example.annotation;

// import javax.validation.Constraint;
// import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

import jakarta.validation.Payload;
import jakarta.validation.Constraint;

import com.example.validate.CampaignCodeValidator;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CampaignCodeValidator.class)
@Documented
public @interface ValidCampaignCode {
    String message() default "Invalid campaign code";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}