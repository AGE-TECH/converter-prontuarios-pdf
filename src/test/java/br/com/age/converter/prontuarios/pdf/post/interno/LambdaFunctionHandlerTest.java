package br.com.age.converter.prontuarios.pdf.post.interno;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;

import br.com.age.converter.prontuarios.pdf.handler.LambdaFunctionHandler;
import br.com.age.converter.prontuarios.pdf.handler.Response;

public class LambdaFunctionHandlerTest {
	@Test
	public void test(){
		LambdaFunctionHandler handler = new LambdaFunctionHandler();
		
		List<SQSMessage> records = new ArrayList<>();
		SQSEvent event = new SQSEvent();
		ContextTest context = new ContextTest();
		
		SQSMessage message = new SQSMessage();
		message.setBody(this.criarJsonTeste());
		records.add(message);
		
		event.setRecords(records);
		
		Response response = handler.handleRequest(event, context);
		
		assertEquals(200, response.getRetorno());		
	}
	
	private String criarJsonTeste() {
		return "{\"codigoEmpresaPrincipal\": \"101\", "
				+ "\"codigoEmpresa\": \"101\",  "
				+ "\"criarGed\": \"true\"}";
	}
}