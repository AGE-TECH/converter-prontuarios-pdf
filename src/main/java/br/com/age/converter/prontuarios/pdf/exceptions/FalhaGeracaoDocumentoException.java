package br.com.age.converter.prontuarios.pdf.exceptions;

public class FalhaGeracaoDocumentoException extends Exception {

	private static final long serialVersionUID = 1L;

	public FalhaGeracaoDocumentoException(Throwable e) {
		super(e);
	}

	public FalhaGeracaoDocumentoException(String str) {
		super(str);
	}

	public FalhaGeracaoDocumentoException(String str, Throwable e) {
		super(str, e);
	}
	
}
