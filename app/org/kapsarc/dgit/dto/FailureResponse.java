package org.kapsarc.dgit.dto;

public class FailureResponse extends Response {
	public FailureResponse(Object error) {
		super.setError(error);
		setSuccess(false);
	}
}
