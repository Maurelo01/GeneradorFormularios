package com.example.pokemonformularios.ast;
import com.example.pokemonformularios.reportes.ErrorCompi;

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
        if (!ent.existeVariable(idVariable))
        {
            ent.agregarVariable(idVariable, "number", 0.0);
        }
        else
        {
            String tipo = ent.obtenerTipoVariable(idVariable);
            if (!"number".equals(tipo))
            {
                ent.getErroresSemanticos().add(new ErrorCompi("Semántico", "La variable del FOR '" + idVariable + "' ya existe y no es de tipo number", 0, 0));
                return null;
            }
        }
        Object valInicio = inicio.evaluar(ent);
        Object valFin = fin.evaluar(ent);
        if (valInicio instanceof Double && valFin instanceof Double)
        {
            double start = (Double) valInicio;
            double end = (Double) valFin;
            System.out.println(" Iniciando FOR desde " + start + " hasta " + end);
            ent.actualizarVariable(idVariable, start);
            int contadorSeguridad = 0;
            while (true)
            {
                Object actualObj = ent.obtenerValorVariable(idVariable);
                double valorActual = 0.0;
                if (actualObj instanceof Double) valorActual = (Double) actualObj;
                if (valorActual > end) break;
                for (Instruccion instr : instrucciones)
                {
                    if (instr != null) instr.ejecutar(ent);
                }
                ent.actualizarVariable(idVariable, valorActual + 1.0);
                contadorSeguridad++;
                if (contadorSeguridad > 100)
                {
                    System.err.println("Advertencia: Ciclo FOR detenido por seguridad.");
                    break;
                }
            }
        }
        else
        {
            ent.getErroresSemanticos().add(new ErrorCompi("Semántico", "Los límites del FOR deben ser numéros.", 0, 0));
        }
        return null;
    }
}