package com.example.pokemonformularios.ast;
import java.util.ArrayList;
import java.util.List;

public class ComponenteSeccion implements Instruccion
{
    private List<Atributo> atributos;

    public ComponenteSeccion(List<Atributo> atributos)
    {
        this.atributos = atributos;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object ejecutar(Entorno ent)
    {
        System.out.println("Dibujando SECCION...");
        ent.registrarSeccionPKM();
        ent.getPkmBuilder().append("<section=-1, -1, -1, -1, VERTICAL>\n<content>\n");
        List<Instruccion> elementosHijos = new ArrayList<>();
        for (Atributo attr : atributos)
        {
            if (attr.getNombre().equals("elements"))
            {
                elementosHijos = (List<Instruccion>) attr.getValor();
            }
        }
        if (!elementosHijos.isEmpty())
        {
            System.out.println(" Contiene " + elementosHijos.size() + " subelementos. Procesando...");
            for (Instruccion hijo : elementosHijos)
            {
                if (hijo != null)
                {
                    hijo.ejecutar(ent);
                }
            }
        }
        ent.getPkmBuilder().append("</content>\n</section>\n");
        return null;
    }
}