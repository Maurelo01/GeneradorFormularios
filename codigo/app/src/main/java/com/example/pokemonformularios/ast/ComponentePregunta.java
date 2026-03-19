package com.example.pokemonformularios.ast;
import java.util.List;
import android.graphics.Color;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import java.util.ArrayList;

public class ComponentePregunta implements Instruccion
{
    private String tipoPregunta;
    private List<Atributo> atributos;

    public ComponentePregunta(String tipoPregunta, List<Atributo> atributos)
    {
        this.tipoPregunta = tipoPregunta;
        this.atributos = atributos;
    }

    @Override
    public Object ejecutar(Entorno ent)
    {
        String label = "Pregunta sin título";
        List<Object> opciones = new ArrayList<>();
        if (atributos != null)
        {
            for (Atributo attr : atributos)
            {
                if (attr.getNombre().equals("label"))
                {
                    Object val = ((Expresion) attr.getValor()).evaluar(ent);
                    if (val != null) label = val.toString();
                }
                else if (attr.getNombre().equals("options"))
                {
                    opciones = (List<Object>) attr.getValor();
                }
            }
        }
        if (ent.getContexto() != null && ent.getLayoutActual() != null)
        {
            LinearLayout layoutPregunta = new LinearLayout(ent.getContexto());
            layoutPregunta.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 16, 0, 48);
            layoutPregunta.setLayoutParams(params);
            TextView tvLabel = new TextView(ent.getContexto());
            tvLabel.setText(label);
            tvLabel.setTextSize(16f);
            tvLabel.setTextColor(Color.parseColor("#333333"));
            tvLabel.setPadding(0, 0, 0, 16);
            layoutPregunta.addView(tvLabel);
            switch (tipoPregunta)
            {
                case "OPEN":
                    EditText editText = new EditText(ent.getContexto());
                    editText.setHint("Tu respuesta...");
                    layoutPregunta.addView(editText);
                    break;
                case "SELECT":
                    RadioGroup radioGroup = new RadioGroup(ent.getContexto());
                    for (Object obj : opciones)
                    {
                        if (obj instanceof Expresion)
                        {
                            Object val = ((Expresion) obj).evaluar(ent);
                            String textoOpcion = val != null ? val.toString() : "";
                            RadioButton rb = new RadioButton(ent.getContexto());
                            rb.setText(textoOpcion);
                            radioGroup.addView(rb);
                        }
                    }
                    layoutPregunta.addView(radioGroup);
                    break;
                case "MULTIPLE":
                    for (Object obj : opciones)
                    {
                        if (obj instanceof Expresion)
                        {
                            Object val = ((Expresion) obj).evaluar(ent);
                            String textoOpcion = val != null ? val.toString() : "";
                            CheckBox cb = new CheckBox(ent.getContexto());
                            cb.setText(textoOpcion);
                            layoutPregunta.addView(cb);
                        }
                    }
                    break;
                case "DROP":
                    TextView tvDrop = new TextView(ent.getContexto());
                    tvDrop.setText("[ Aquí para la PokéAPI");
                    tvDrop.setTextColor(Color.parseColor("#D32F2F"));
                    layoutPregunta.addView(tvDrop);
                    break;
            }
            ent.getLayoutActual().addView(layoutPregunta);
        }
        return null;
    }
}