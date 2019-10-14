package com.ibyte.common.core.validation;

import com.ibyte.common.i18n.ResourceUtil;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;

import java.util.Locale;

/**
 * 校验提示语
 *
 * @author li.Shangzhi
 * @Date: 2019-10-14
 */
public class ValidationMessageInterpolator
		extends ResourceBundleMessageInterpolator {
	private static final String[] HBM_MESSAGE_PREFIX = { "{javax.validation.",
			"{org.hibernate.validator." };

	private static final String TEMPLATE_LEFT = "{";

	private static final String TEMPLATE_RIGHT = "}";

	@Override
	public String interpolate(String messageTemplate, Context context) {
		return interpolate(messageTemplate, context,
				ResourceUtil.currentLocale());
	}

	@Override
	public String interpolate(String message, Context context, Locale locale) {
		if (message.startsWith(TEMPLATE_LEFT)
				&& message.endsWith(TEMPLATE_RIGHT)) {
			for (String prefix : HBM_MESSAGE_PREFIX) {
				if (message.startsWith(prefix)) {
					return super.interpolate(message, context, locale);
				}
			}
			return ResourceUtil
					.getString(message.substring(1, message.length() - 1));
		} else {
			return message;
		}
	}
}
