package br.com.age.converter.prontuarios.pdf.exceptions;

public class SemResultadoException extends Exception {

	private static final long serialVersionUID = 1L;

	public SemResultadoException(Throwable e) {
		super(e);
	}

	public SemResultadoException(String str) {
		super(str);
	}

	public SemResultadoException(String str, Throwable e) {
		super(str, e);
	}
	
}
