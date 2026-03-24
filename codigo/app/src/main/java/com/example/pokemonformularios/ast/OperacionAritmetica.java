package com.example.pokemonformularios.ast;
public class OperacionAritmetica implements Expresion
{
    private Expresion izq;
    private Expresion der;
    private String operador;

    public OperacionAritmetica(Expresion izq, String operador, Expresion der)
    {
        this.izq = izq;
        this.operador = operador;
        this.der = der;
    }
    @Override
    public Object evaluar(Entorno ent)
    {
        Object valDer = (der != null) ? der.evaluar(ent) : null;
        if (operador.equals("UNARIO_MENOS"))
        {
            if (valDer instanceof Double) return -((Double) valDer);
            return null;
        }
        Object valIzq = (izq != null) ? izq.evaluar(ent) : null;
        if (valIzq == null || valDer == null) return null;
        if (operador.equals("+"))
        {
            if (valIzq instanceof Double && valDer instanceof Double)
            {
                return (Double) valIzq + (Double) valDer;
            }
            else if (valIzq instanceof String || valDer instanceof String)
            {
                return valIzq.toString() + valDer.toString();
            }
        }
        if (valIzq instanceof Double && valDer instanceof Double)
        {
            double num1 = (Double) valIzq;
            double num2 = (Double) valDer;
            switch (operador)
            {
                case "-": return num1 - num2;
                case "*": return num1 * num2;
                case "/":
                    if (num2 == 0)
                    {
                        System.err.println("Error Semántico: División por cero.");
                        return 0.0;
                    }
                    return num1 / num2;
                case "^": return Math.pow(num1, num2);
                case "%": return num1 % num2;
            }
        }
        System.err.println("Error Semántico: Tipos inválidos para la operación '" + operador + "'.");
        return null;
    }
}