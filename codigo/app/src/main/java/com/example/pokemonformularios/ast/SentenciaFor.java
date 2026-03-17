package com.example.pokemonformularios.ast;
import java.util.List;

public class SentenciaFor implements Instruccion
{
    private String idVariable;
    private Expresion inicio;
    private Expresion fin;
    private List<Instruccion> instrucciones;

    public SentenciaFor(String idVariable, Expresion inicio, Expresion fin, List<Instruccion> instrucciones)
    {
        this.idVariable = idVariable;
        this.inicio = inicio;
        this.fin = fin;
        this.instrucciones = instrucciones;
    }

    @Override
    public Object ejecutar(Entorno ent)
    {
        Object valInicio = inicio.evaluar(ent);
        Object valFin = fin.evaluar(ent);
        if (valInicio instanceof Double && valFin instanceof Double)
        {
            int start = ((Double) valInicio).intValue();
            int end = ((Double) valFin).intValue();

            System.out.println(" Iniciando FOR desde " + start + " hasta " + end);

            for (int i = start; i <= end; i++)
            {
                Entorno entornoLocal = new Entorno(ent);
                entornoLocal.agregarVariable(idVariable, "number", (double) i);
                for (Instruccion instr : instrucciones)
                {
                    if (instr != null) instr.ejecutar(entornoLocal);
                }
            }
        }
        else
        {
            System.err.println("Error Semántico: Los límites del FOR deben ser números.");
        }
        return null;
    }
}