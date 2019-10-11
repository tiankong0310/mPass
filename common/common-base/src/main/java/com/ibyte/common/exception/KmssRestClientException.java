package com.ibyte.common.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.client.RestClientException;

/**
 * RestClient 异常处理
 *
 * @author li.Shangzhi
 * @Date: 2019-10-12
 */
public class KmssRestClientException extends RestClientException {

	private static final long serialVersionUID = 5375798079254739419L;

	private RestClientException restClientException;

	@Getter
	@Setter
	private String body;

	public RestClientException getRestClientException() {
		return restClientException;
	}

	public void setRestClientException(RestClientException restClientException) {
		this.restClientException = restClientException;
	}

	public KmssRestClientException(String message, RestClientException restClientException, String body) {
		super(message);
		this.restClientException = restClientException;
		this.body = body;
	}

}
