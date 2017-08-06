package org.kapsarc.dgit.dto;


public class SuccessResponse extends Response {
	public SuccessResponse(Object data) {
		super.setData(data);
		super.setSuccess(true);
	}
}
