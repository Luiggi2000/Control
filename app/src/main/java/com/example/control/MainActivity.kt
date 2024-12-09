package com.example.control

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {
    // Instancia de Firestore
    private lateinit var firestore: FirebaseFirestore

    // Variable para almacenar el ID del registro
    private var registroId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar Firestore
        firestore = FirebaseFirestore.getInstance()

        val etDocenteId = findViewById<EditText>(R.id.inputDocente)
        val etLaboratorioId = findViewById<EditText>(R.id.inputLaboratorio)
        val etCurso = findViewById<EditText>(R.id.inputCurso)

        // Referencia a los botones
        val btnSubirRegistro = findViewById<Button>(R.id.btnRegistrar)
        val btnMarcarSalida = findViewById<Button>(R.id.btn_marcar_salida)

        // Evento del botón para registrar acceso
        btnSubirRegistro.setOnClickListener {
            val docenteId = etDocenteId.text.toString().trim()
            val laboratorioId = etLaboratorioId.text.toString().trim()
            val curso = etCurso.text.toString().trim()

            if (docenteId.isNotEmpty() && laboratorioId.isNotEmpty() && curso.isNotEmpty()) {
                registrarAcceso(docenteId, laboratorioId, curso)
            } else {
                Toast.makeText(this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show()
            }
        }

        // Evento del botón para marcar salida
        btnMarcarSalida.setOnClickListener {
            marcarSalida()
        }
    }

    private fun registrarAcceso(docenteId: String, laboratorioId: String, curso: String) {
        val fechaActual = LocalDate.now().toString() // Formato: "YYYY-MM-DD"
        val horaActual = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) // Formato: "HH:mm"

        // Crear el registro
        val registro = hashMapOf(
            "id_docente" to docenteId,
            "laboratorio_id" to laboratorioId,
            "curso" to curso,
            "fecha" to fechaActual,
            "hora_entrada" to horaActual,
            "hora_salida" to null,
            "estado" to "En curso"
        )

        // Guardar en Firestore
        firestore.collection("registros_acceso")
            .add(registro)
            .addOnSuccessListener { documentReference ->
                registroId = documentReference.id // Guardar el ID del registro creado
                Toast.makeText(this, "Acceso registrado con éxito", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al registrar acceso: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun marcarSalida() {
        if (registroId != null) { // Verificar si el registro ya existe
            val horaSalida = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))

            // Actualizar el registro en Firestore
            val registroActualizado = mapOf(
                "hora_salida" to horaSalida,
                "estado" to "Completado"
            )

            firestore.collection("registros_acceso")
                .document(registroId!!)
                .update(registroActualizado)
                .addOnSuccessListener {
                    Log.d("Firestore", "Registro actualizado exitosamente")
                    Toast.makeText(this, "Salida registrada", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error al actualizar el registro", e)
                    Toast.makeText(this, "Error al registrar salida", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Debe registrar un acceso primero", Toast.LENGTH_SHORT).show()
        }
    }
}
