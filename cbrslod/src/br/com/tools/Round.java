package br.com.tools;

import java.math.BigDecimal;
import java.util.List;

public class Round {
	
	public static double round(double value, int numberOfDigitsAfterDecimalPoint) {
        BigDecimal bigDecimal = new BigDecimal(value);
        bigDecimal = bigDecimal.setScale(numberOfDigitsAfterDecimalPoint,
                BigDecimal.ROUND_HALF_UP);
        return bigDecimal.doubleValue();
    }
	
	public static strictfp Double getMedia(double[] valor) {
	    try {
	        return getSoma(valor) / valor.length;
	    } catch (NullPointerException e) {
	        throw new IllegalArgumentException("The list has null values");
	    }
	}

	public static strictfp Double getSoma(double[] valor) {
	    Double soma = 0D;
	    for (int i = 0; i < valor.length; i++) {
	        soma += valor[i];
	    }
	    return soma;
	}

	public static strictfp Double getDesvioPadrao(double[] valor) {
		Double media = getMedia(valor);
	    int size = valor.length;
	    Double desvPadrao = 0D;
	   
	    for (int i = 0; i < valor.length; i++) {
	        Double aux = valor[i] - media;
	        desvPadrao += aux * aux;	       
	    }
	    return Math.sqrt(desvPadrao / (size - 1));
	}
	
}
