package br.com.age.converter.prontuarios.pdf.exceptions;

public class FalhaNotificacaoWebSocketException extends Exception {

	private static final long serialVersionUID = 1L;

	public FalhaNotificacaoWebSocketException(Throwable e) {
		super(e);
	}

	public FalhaNotificacaoWebSocketException(String str) {
		super(str);
	}

	public FalhaNotificacaoWebSocketException(String str, Throwable e) {
		super(str, e);
	}
	
}
