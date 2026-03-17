package com.example.pokemonformularios.ast;
import java.util.List;

public class SentenciaIf implements Instruccion
{
    private Expresion condicion;
    private List<Instruccion> instruccionesTrue;
    private List<Instruccion> instruccionesFalse;

    public SentenciaIf(Expresion condicion, List<Instruccion> instTrue, List<Instruccion> instFalse)
    {
        this.condicion = condicion;
        this.instruccionesTrue = instTrue;
        this.instruccionesFalse = instFalse;
    }

    @Override
    public Object ejecutar(Entorno ent)
    {
        Object valorCondicion = condicion.evaluar(ent);
        if (valorCondicion instanceof Double)
        {
            boolean esVerdadero = ((Double) valorCondicion) >= 1.0;
            Entorno entornoLocal = new Entorno(ent);
            if (esVerdadero)
            {
                System.out.println(" Condición IF cumplida. Ejecutando interior...");
                for (Instruccion instr : instruccionesTrue)
                {
                    if (instr != null) instr.ejecutar(entornoLocal);
                }
            } else if (instruccionesFalse != null)
            {
                System.out.println("Condición IF falló. Ejecutando ELSE...");
                for (Instruccion instr : instruccionesFalse)
                {
                    if (instr != null) instr.ejecutar(entornoLocal);
                }
            }
            else
            {
                System.out.println("Condición IF falló y no hay ELSE.");
            }
        }
        else
        {
            System.err.println("Error Semántico: La condición del IF debe devolver un número.");
        }
        return null;
    }
}