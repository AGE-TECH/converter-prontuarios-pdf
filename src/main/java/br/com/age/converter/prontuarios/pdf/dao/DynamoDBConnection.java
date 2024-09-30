package br.com.age.converter.prontuarios.pdf.dao;

import java.util.Iterator;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Index;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;

public class DynamoDBConnection {

	private AmazonDynamoDB client;
	private DynamoDB dynamoDB;
	private String nomeTabela;
	
	public DynamoDBConnection(String nomeTabela) {
		this.nomeTabela = nomeTabela;
		this.client = AmazonDynamoDBClientBuilder.standard().withRegion(System.getenv("REGION")).build();
		this.dynamoDB = new DynamoDB(client);
	}
	
	public String buscarConexaoPorIdConversao(String idConexaoSoc) {
		String retorno = "";
		
		try {
			Table table = dynamoDB.getTable(nomeTabela);
			Index index = table.getIndex("idConexaoSoc-index");

			QuerySpec querySpec = new QuerySpec()
					 .withKeyConditionExpression("idConexaoSoc = :idConexaoSoc")
            		 .withValueMap(new ValueMap().withString(":idConexaoSoc", idConexaoSoc));
	        
			ItemCollection<QueryOutcome> items = index.query(querySpec);
	        Iterator<Item> iterator = items.iterator();
	
	        if (iterator.hasNext()) {
	        	retorno = iterator.next().get("connectionId").toString();
	        }
			
		} catch (AmazonDynamoDBException e) {
			e.printStackTrace();
			return "";
		}
		
		return retorno;
	}
	
}
