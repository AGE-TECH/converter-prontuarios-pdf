package br.com.age.converter.prontuarios.pdf.exceptions;

public class BusinessException extends Exception {

	private static final long serialVersionUID = 1L;

	public BusinessException(Throwable e) {
		super(e);
	}

	public BusinessException(String str) {
		super(str);
	}

	public BusinessException(String str, Throwable e) {
		super(str, e);
	}
	
}
