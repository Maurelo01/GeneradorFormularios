package com.example.pokemonformularios.ast;

public class OperacionRelacional implements Expresion
{
    private Expresion izq;
    private String operador;
    private Expresion der;

    public OperacionRelacional(Expresion izq, String operador, Expresion der)
    {
        this.izq = izq;
        this.operador = operador;
        this.der = der;
    }
    @Override
    public Object evaluar(Entorno ent)
    {
        Object valIzq = izq.evaluar(ent);
        Object valDer = der.evaluar(ent);
        if (valIzq == null || valDer == null) return null;
        if (valIzq instanceof Double && valDer instanceof Double)
        {
            double num1 = (Double) valIzq;
            double num2 = (Double) valDer;
            switch (operador)
            {
                case ">":  return (num1 > num2) ? 1.0 : 0.0;
                case "<":  return (num1 < num2) ? 1.0 : 0.0;
                case ">=": return (num1 >= num2) ? 1.0 : 0.0;
                case "<=": return (num1 <= num2) ? 1.0 : 0.0;
                case "==": return (num1 == num2) ? 1.0 : 0.0;
                case "!!": return (num1 != num2) ? 1.0 : 0.0;
            }
        }
        if (valIzq instanceof String && valDer instanceof String)
        {
            String str1 = (String) valIzq;
            String str2 = (String) valDer;
            switch (operador)
            {
                case "==": return str1.equals(str2) ? 1.0 : 0.0;
                case "!!": return !str1.equals(str2) ? 1.0 : 0.0;
            }
        }
        System.err.println("Error Semántico: No se pueden comparar los valores con el operador '" + operador + "'.");
        return 0.0;
    }
}