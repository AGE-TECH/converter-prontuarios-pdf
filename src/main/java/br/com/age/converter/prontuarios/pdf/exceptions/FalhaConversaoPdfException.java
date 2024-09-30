package br.com.age.converter.prontuarios.pdf.exceptions;

public class FalhaConversaoPdfException extends Exception {

	private static final long serialVersionUID = 1L;

	public FalhaConversaoPdfException(Throwable e) {
		super(e);
	}

	public FalhaConversaoPdfException(String str) {
		super(str);
	}

	public FalhaConversaoPdfException(String str, Throwable e) {
		super(str, e);
	}
	
}
