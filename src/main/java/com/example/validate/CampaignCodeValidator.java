package com.example.validate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import com.example.annotation.ValidCampaignCode;

public class CampaignCodeValidator implements ConstraintValidator<ValidCampaignCode, String> {
	@Override
	public void initialize(ValidCampaignCode constraintAnnotation) {}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null || value.length() != 4) {
			return false;
		}

		try {
			int intValue = Integer.parseInt(value);
			return intValue >= 0 && intValue <= 9999;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
