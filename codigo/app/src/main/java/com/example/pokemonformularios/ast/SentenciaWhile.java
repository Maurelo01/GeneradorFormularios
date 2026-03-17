package com.example.pokemonformularios.ast;
import java.util.List;

public class SentenciaWhile implements Instruccion
{
    private Expresion condicion;
    private List<Instruccion> instrucciones;
    private boolean esDoWhile;

    public SentenciaWhile(Expresion condicion, List<Instruccion> instrucciones, boolean esDoWhile)
    {
        this.condicion = condicion;
        this.instrucciones = instrucciones;
        this.esDoWhile = esDoWhile;
    }

    @Override
    public Object ejecutar(Entorno ent)
    {
        System.out.println(" Iniciando Ciclo " + (esDoWhile ? "DO-WHILE" : "WHILE") + "...");
        boolean primeraVez = true;
        while (true)
        {
            if (!esDoWhile || !primeraVez)
            {
                Object valorCondicion = condicion.evaluar(ent);
                if (!(valorCondicion instanceof Double))
                {
                    System.err.println("Error Semántico: La condición del ciclo debe ser numérica.");
                    break;
                }
                if ((Double) valorCondicion < 1.0)
                {
                    break;
                }
            }
            Entorno entornoLocal = new Entorno(ent);
            for (Instruccion instr : instrucciones)
            {
                if (instr != null) instr.ejecutar(entornoLocal);
            }
            primeraVez = false;
        }
        return null;
    }
}