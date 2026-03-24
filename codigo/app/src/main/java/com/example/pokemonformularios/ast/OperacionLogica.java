package com.example.pokemonformularios.ast;

import com.example.pokemonformularios.reportes.ErrorCompi;

public class OperacionLogica implements Expresion
{
    private Expresion izq;
    private String operador;
    private Expresion der;
    private int linea;
    private int columna;

    public OperacionLogica(Expresion izq, String operador, Expresion der, int linea, int columna)
    {
        this.izq = izq;
        this.operador = operador;
        this.der = der;
        this.linea = linea;
        this.columna = columna;
    }

    public OperacionLogica(String operador, Expresion der, int linea, int columna)
    {
        this.izq = null;
        this.operador = operador;
        this.der = der;
        this.linea = linea;
        this.columna = columna;
    }

    @Override
    public Object evaluar(Entorno ent)
    {
        if (izq instanceof OperacionLogica)
        {
            String opIzq = ((OperacionLogica) izq).getOperador();
            if (!opIzq.equals("!") && !opIzq.equals("~") && !this.operador.equals(opIzq))
            {
                String mensaje = "No se pueden combinar diferentes operadores lógicos (&& y ||) en la misma expresión.";
                System.err.println("Error Semántico: " + mensaje);
                ent.getErroresSemanticos().add(new ErrorCompi("Semántico", mensaje, linea, columna));
                return 0.0;
            }
        }
        if (der instanceof OperacionLogica)
        {
            String opDer = ((OperacionLogica) der).getOperador();
            if (!opDer.equals("!") && !opDer.equals("~") && !this.operador.equals(opDer))
            {
                String mensaje = "No se pueden combinar diferentes operadores lógicos (&& y ||) en la misma expresión.";
                System.err.println("Error Semántico: " + mensaje);
                ent.getErroresSemanticos().add(new ErrorCompi("Semántico", mensaje, linea, columna));
                return 0.0;
            }
        }
        if (operador.equals("!") || operador.equals("~"))
        {
            Object valDer = der.evaluar(ent);
            if (valDer instanceof Double)
            {
                return ((Double) valDer >= 1.0) ? 0.0 : 1.0;
            }
            return 0.0;
        }
        Object valIzq = izq.evaluar(ent);
        Object valDer = der.evaluar(ent);
        if (valIzq instanceof Double && valDer instanceof Double)
        {
            boolean boolIzq = ((Double) valIzq >= 1.0);
            boolean boolDer = ((Double) valDer >= 1.0);
            switch (operador)
            {
                case "&&": return (boolIzq && boolDer) ? 1.0 : 0.0;
                case "||": return (boolIzq || boolDer) ? 1.0 : 0.0;
            }
        }
        String mensaje = "Los operadores lógicos requieren números u operaciones relacionales.";
        System.err.println("Error Semántico: " + mensaje);
        ent.getErroresSemanticos().add(new ErrorCompi("Semántico", mensaje, linea, columna));
        return 0.0;
    }

    public String getOperador()
    {
        return this.operador;
    }
}