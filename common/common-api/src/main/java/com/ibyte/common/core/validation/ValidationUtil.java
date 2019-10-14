package com.ibyte.common.core.validation;

import com.ibyte.common.exception.ParamsNotValidException;
import com.ibyte.common.util.StringHelper;
import org.hibernate.Hibernate;
import org.hibernate.validator.HibernateValidator;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

/**
 * 采用HibernateValidator对DTO进行校验
 *
 * @author li.Shangzhi
 * @Date: 2019-10-14
 */
public class ValidationUtil {
	private static Validator validator = Validation
			.byProvider(HibernateValidator.class).configure().failFast(true)
			.messageInterpolator(new ValidationMessageInterpolator())
			.buildValidatorFactory().getValidator();

	/**
	 * 执行校验
	 * 
	 * @param dto
	 */
	public static <T> void validate(T dto, Class<?>... groups)
			throws ParamsNotValidException {
		//处理hibernate延迟及代理对象
		if (!Hibernate.isInitialized(dto)) {
			return;
		}
		Set<ConstraintViolation<T>> result = validator
				.validate((T) Hibernate.unproxy(dto), groups);
		handleConstraintViolation(result);
		if (dto instanceof Validatable) {
			((Validatable) dto).validate(groups);
		}
	}

	/**
	 * 获取校验器
	 * 
	 * @return
	 */
	public static Validator getValidator() {
		return validator;
	}

	/**
	 * 校验失败则抛出异常
	 * 
	 * @param result
	 * @throws ParamsNotValidException
	 */
	public static <T> void
			handleConstraintViolation(Set<ConstraintViolation<T>> result)
					throws ParamsNotValidException {
		if (result.size() == 0) {
			return;
		}
		ConstraintViolation<T> violation = result.iterator().next();
		String message = StringHelper.join(violation.getPropertyPath(), ": ",
				violation.getMessage());
		throw new ParamsNotValidException(message);
	}
}
