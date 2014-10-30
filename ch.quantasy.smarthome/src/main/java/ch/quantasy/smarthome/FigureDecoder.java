package ch.quantasy.smarthome;

import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Reto E. Koenig <reto.koenig@bfh.ch>
 */
public class FigureDecoder implements Decoder.Text<Figure> {

    @Override
    public Figure decode(String string) throws DecodeException {
	JsonObject jsonObject = Json.createReader(new StringReader(string)).readObject();
	Figure f = new Figure(jsonObject);
	return f;
    }

    @Override
    public boolean willDecode(String string) {
	try {
	    Json.createReader(new StringReader(string)).readObject();
	    return true;
	} catch (JsonException ex) {
	    ex.printStackTrace();
	    return false;
	}

    }

    @Override
    public void init(EndpointConfig ec) {
	System.out.println("init");
    }

    @Override
    public void destroy() {
	System.out.println("destroy");
    }
}