package com.example.pokemonformularios.ast;
import java.util.ArrayList;
import java.util.List;

public class ComponenteTabla implements Instruccion
{
    private List<Atributo> atributos;

    public ComponenteTabla(List<Atributo> atributos)
    {
        this.atributos = atributos;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object ejecutar(Entorno ent)
    {
        System.out.println(" Dibujando TABLA...");
        List<Object> filas = new ArrayList<>();
        for (Atributo attr : atributos)
        {
            if (attr.getNombre().equals("elements_table"))
            {
                filas = (List<Object>) attr.getValor();
            }
        }
        if (!filas.isEmpty())
        {
            System.out.println(" La tabla tiene " + filas.size() + " filas.");
            for (int i = 0; i < filas.size(); i++)
            {
                System.out.println(" --- Fila " + (i + 1) + " ---");
                Object fila = filas.get(i);
                if (fila instanceof List)
                {
                    List<Instruccion> columnas = (List<Instruccion>) fila;
                    for (Instruccion componente : columnas)
                    {
                        if (componente != null)
                        {
                            componente.ejecutar(ent);
                        }
                    }
                }
            }
        }
        System.out.println(" Fin de TABLA");
        return null;
    }
}