package com.example.pokemonformularios.ast;

public class OperacionLogica implements Expresion
{
    private Expresion izq;
    private String operador;
    private Expresion der;

    public OperacionLogica(Expresion izq, String operador, Expresion der)
    {
        this.izq = izq;
        this.operador = operador;
        this.der = der;
    }

    public OperacionLogica(String operador, Expresion der)
    {
        this.izq = null;
        this.operador = operador;
        this.der = der;
    }

    @Override
    public Object evaluar(Entorno ent)
    {
        if (operador.equals("!"))
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
        System.err.println("Error Semántico: Los operadores lógicos requieren números u operaciones relacionales.");
        return 0.0;
    }
}