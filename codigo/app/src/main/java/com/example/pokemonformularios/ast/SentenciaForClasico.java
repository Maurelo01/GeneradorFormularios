package com.example.pokemonformularios.ast;
import java.util.List;
import com.example.pokemonformularios.reportes.ErrorCompi;

public class SentenciaForClasico implements Instruccion
{
    private Instruccion asignacionInicial;
    private Expresion condicion;
    private Instruccion asignacionActualizacion;
    private List<Instruccion> instrucciones;

    public SentenciaForClasico(Instruccion asignacionInicial, Expresion condicion, Instruccion asignacionActualizacion, List<Instruccion> instrucciones)
    {
        this.asignacionInicial = asignacionInicial;
        this.condicion = condicion;
        this.asignacionActualizacion = asignacionActualizacion;
        this.instrucciones = instrucciones;
    }

    @Override
    public Object ejecutar(Entorno ent)
    {
        System.out.println(" Iniciando FOR CLÁSICO...");
        if (this.asignacionInicial instanceof Asignacion)
        {
            Asignacion asig = (Asignacion) this.asignacionInicial;
            String idVariable = asig.getId();

            if (!ent.existeVariable(idVariable))
            {
                ent.agregarVariable(idVariable, "number", 0.0);
            }
            else
            {
                String tipo = ent.obtenerTipoVariable(idVariable);
                if (!"number".equals(tipo))
                {
                    ent.getErroresSemanticos().add(new ErrorCompi("Semántico", "La variable del FOR '" + idVariable + "' ya existe y NO es de tipo number", 0, 0));
                    return null;
                }
            }
        }
        if (asignacionInicial != null)
        {
            asignacionInicial.ejecutar(ent);
        }
        int contadorSeguridad = 0;
        while (true)
        {
            Object valorCondicion = condicion.evaluar(ent);
            if (!(valorCondicion instanceof Double))
            {
                ent.getErroresSemanticos().add(new ErrorCompi("Semántico", "La condición del FOR clásico debe ser numérica.", 0, 0));
                break;
            }
            if ((Double) valorCondicion < 1.0)
            {
                break;
            }
            for (Instruccion instr : instrucciones)
            {
                if (instr != null) instr.ejecutar(ent);
            }
            if (asignacionActualizacion != null)
            {
                asignacionActualizacion.ejecutar(ent);
            }
            contadorSeguridad++;
            if (contadorSeguridad > 100)
            {
                System.err.println("Advertencia: Ciclo FOR infinito detenido por seguridad.");
                break;
            }
        }
        System.out.println(" Fin de FOR CLÁSICO");
        return null;
    }
}