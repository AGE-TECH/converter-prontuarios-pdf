package br.com.age.converter.prontuarios.pdf.handler;

import java.util.Optional;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

import br.com.age.converter.prontuarios.pdf.business.ConverterProntuariosPdf;
import br.com.age.converter.prontuarios.pdf.business.ConverterProntuariosPdfNotificacao;
import br.com.age.converter.prontuarios.pdf.db.Database;
import br.com.age.converter.prontuarios.pdf.db.DatabaseReplica;
import br.com.age.converter.prontuarios.pdf.exceptions.FalhaNotificacaoWebSocketException;
import br.com.age.converter.prontuarios.pdf.exceptions.SemResultadoException;
import br.com.age.converter.prontuarios.pdf.vo.ParametrosVo;

public class LambdaFunctionHandler implements RequestHandler<SQSEvent, Response>{
	
	private static final DatabaseReplica databaseReplica = new DatabaseReplica();
	private static final Database database = new Database();
	private static final ObjectMapper mapper = new ObjectMapper();
	
	@Override
	public Response handleRequest(SQSEvent event, Context context) {
		Response response = new Response();		
		ConverterProntuariosPdf business;
		ConverterProntuariosPdfNotificacao notificacaoBusiness;
		String jsonResponse;
		ParametrosVo parametrosVo = null;
		
		try {
			parametrosVo = mapper.readValue(getMessagem(event), ParametrosVo.class);

			business = new ConverterProntuariosPdf(context, parametrosVo, database, databaseReplica);			

			jsonResponse = business.converterProntuario();
			
			notificacaoBusiness = new ConverterProntuariosPdfNotificacao(parametrosVo);
			notificacaoBusiness.notificarConexaoUsuario(jsonResponse);
			
			response.setRetorno("Sucesso");
			
		} catch (FalhaNotificacaoWebSocketException e) {
			response.setRetorno(e.getMessage());
			context.getLogger().log(e.getMessage());

		} catch (SemResultadoException e) {
			response.setRetorno(e.getMessage());
			context.getLogger().log(e.getMessage());
			notificarWebSocketCasoErro(context, parametrosVo);
		
		} catch (Exception e) {
			e.printStackTrace();
			context.getLogger().log("Erro: " + e.getMessage());
			response.setRetorno("Erro");
			
			throw new RuntimeException(e.getMessage());
		}
		
		return response;
	}

	private void notificarWebSocketCasoErro(Context context, ParametrosVo parametrosVo) {
		ConverterProntuariosPdfNotificacao notificacaoBusiness = new ConverterProntuariosPdfNotificacao(parametrosVo);
		JsonObject jsonErroResponse = new JsonObject();
		jsonErroResponse.addProperty("erro", true);
		String jsonResponse = jsonErroResponse.toString();
		
		try {
			notificacaoBusiness.notificarConexaoUsuario(jsonResponse);
		} catch (FalhaNotificacaoWebSocketException e1) {
			e1.printStackTrace();
		}
	}

	private String getMessagem(SQSEvent event) {
		String jsonBody = "";
		
		Optional<SQSMessage> optional = event.getRecords().stream().findFirst();
		
		if(optional.isPresent()) {
			jsonBody = optional.get().getBody();
		}
		
		return jsonBody;
	}
}
