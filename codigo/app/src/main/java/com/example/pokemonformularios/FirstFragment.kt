package com.example.pokemonformularios

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.pokemonformularios.analizador.LexerFormulario
import com.example.pokemonformularios.analizador.ParserFormulario
import com.example.pokemonformularios.ast.Entorno
import com.example.pokemonformularios.ast.Instruccion
import com.example.pokemonformularios.reportes.ErrorCompi
import java.io.StringReader

class FirstFragment : Fragment()
{
    private var listaErroresActuales: List<ErrorCompi> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_first, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        val btnCompilar = view.findViewById<Button>(R.id.btnCompilar)
        val btnErrores = view.findViewById<Button>(R.id.btnErrores)
        val entradaCodigo = view.findViewById<EditText>(R.id.entradaCodigo)
        val contenedorFormulario = view.findViewById<LinearLayout>(R.id.contenedorFormulario)
        btnErrores.isEnabled = false
        btnCompilar.setOnClickListener{
            procesarCompilacion(entradaCodigo, contenedorFormulario, btnErrores)
        }
        btnErrores.setOnClickListener{
            mostrarTablaErrores()
        }
    }

    private fun procesarCompilacion(entradaCodigo: EditText, contenedorFormulario: LinearLayout, btnErrores: Button)
    {
        contenedorFormulario.removeAllViews()
        listaErroresActuales = emptyList()
        btnErrores.isEnabled = false
        val codigoStr = entradaCodigo.text.toString()
        if (codigoStr.isBlank())
        {
            return
        }
        try
        {
            val lexer = LexerFormulario(StringReader(codigoStr))
            val parser = ParserFormulario(lexer)
            val sym = parser.parse()
            val todosLosErrores = mutableListOf<ErrorCompi>()
            todosLosErrores.addAll(lexer.erroresLexicos)
            todosLosErrores.addAll(parser.erroresSintacticos)
            if (todosLosErrores.isEmpty())
            {
                val raizAST = sym.value as? Instruccion
                val entornoGlobal = Entorno(null, requireContext(), contenedorFormulario)
                raizAST?.ejecutar(entornoGlobal)
                Toast.makeText(requireContext(), "¡Formulario Generado con éxito!", Toast.LENGTH_SHORT).show()
            }
            else
            {
                listaErroresActuales = todosLosErrores
                btnErrores.isEnabled = true
                Toast.makeText(requireContext(), "Hay ${todosLosErrores.size} error(es). Revisa la tabla.", Toast.LENGTH_LONG).show()
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Ocurrió un error grave al analizar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarTablaErrores()
    {
        val dialogView = layoutInflater.inflate(R.layout.dialog_errores, null)
        val tablaErrores = dialogView.findViewById<TableLayout>(R.id.tablaErrores)
        for (error in listaErroresActuales)
        {
            val fila = crearFilaError(error)
            tablaErrores.addView(fila)
        }
        AlertDialog.Builder(requireContext()).setView(dialogView).setPositiveButton("Cerrar") { dialog, _ -> dialog.dismiss() }.show()
    }

    private fun crearFilaError(error: ErrorCompi): TableRow
    {
        val fila = TableRow(requireContext())
        fila.setPadding(0, 16, 0, 16)
        val tvTipo = TextView(requireContext()).apply{
            text = error.tipo
            setPadding(0, 0, 16, 0)
        }
        val tvDesc = TextView(requireContext()).apply{
            text = error.descripcion
            setPadding(0, 0, 16, 0)
            maxWidth = 400
        }
        val tvLinea = TextView(requireContext()).apply{
            text = error.linea.toString()
            setPadding(0, 0, 16, 0)
        }
        val tvColumna = TextView(requireContext()).apply{
            text = error.columna.toString()
        }
        fila.addView(tvTipo)
        fila.addView(tvDesc)
        fila.addView(tvLinea)
        fila.addView(tvColumna)

        return fila
    }
}