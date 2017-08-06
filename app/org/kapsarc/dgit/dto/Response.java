package org.kapsarc.dgit.dto;

public class Response {
	private Object data;
	private boolean success;
	private Object error;

	public void setData(Object data) {
		this.data = data;
	}

	public void setSuccess(boolean b) {
		this.success = b;
	}

	public void setError(Object error) {
		this.error = error;
	}

	public Object getData() {
		return data;
	}

	public boolean isSuccess() {
		return success;
	}

	public Object getError() {
		return error;
	}

}
