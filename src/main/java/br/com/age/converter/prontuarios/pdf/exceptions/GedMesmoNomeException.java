package br.com.age.converter.prontuarios.pdf.exceptions;

public class GedMesmoNomeException extends Exception {

	private static final long serialVersionUID = 1L;

	public GedMesmoNomeException(Throwable e) {
		super(e);
	}

	public GedMesmoNomeException(String str) {
		super(str);
	}

	public GedMesmoNomeException(String str, Throwable e) {
		super(str, e);
	}
	
}
