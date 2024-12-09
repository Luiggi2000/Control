package com.example.control

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.LocalTime

class MainActivity : AppCompatActivity() {
    // Instancia de Firestore
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Inicializar Firestore
        firestore = FirebaseFirestore.getInstance()

        val etDocenteId = findViewById<EditText>(R.id.inputDocente)
        val etLaboratorioId = findViewById<EditText>(R.id.inputLaboratorio)
        val etCurso = findViewById<EditText>(R.id.inputCurso)

        // Referencia al botón
        val btnSubirRegistro = findViewById<Button>(R.id.btnRegistrar)
        val btnMarcarSalida = findViewById<Button>(R.id.btn_marcar_salida)
        // Evento del botón
        btnSubirRegistro.setOnClickListener {
            val docenteId = etDocenteId.text.toString().trim()
            val laboratorioId = etLaboratorioId.text.toString().trim()
            val curso = etCurso.text.toString().trim()
            // Validar que los campos no estén vacíos
            if (docenteId.isNotEmpty() && laboratorioId.isNotEmpty() && curso.isNotEmpty()) {
                // Llamar a la función para registrar el acceso
                registrarAcceso(docenteId, laboratorioId, curso)
            } else {
                // Mostrar un mensaje de error si falta algún campo
                Toast.makeText(this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show()
            }
        }

        // Evento del botón para marcar la salida
        btnMarcarSalida.setOnClickListener {
            marcarSalida()
        }
    }

    private fun registrarAcceso(docenteId: String, laboratorioId: String, curso: String) {
        // Obtener instancia de Firestore
        val db = FirebaseFirestore.getInstance()
        val fechaActual = LocalDate.now().toString() // Formato: "YYYY-MM-DD"
        val horaActual = LocalTime.now().toString() // Formato: "HH:mm:ss"
        // Crear el registro
        val registro = hashMapOf(
            "id_docente" to docenteId,           // ID del docente
            "laboratorio_id" to laboratorioId,     // ID del laboratorio
            "curso" to curso, // Curso asignado
            "fecha" to fechaActual,          // Fecha del registro
            "hora_entrada" to horaActual,        // Hora de entrada
            "hora_salida" to null,            // Hora de salida (inicialmente null)
            "estado" to "En curso"            // Estado del acceso
        )


        // Guardar en Firestore
        db.collection("registros_acceso")
            .add(registro)
            .addOnSuccessListener {
                Toast.makeText(this, "Acceso registrado con éxito", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al registrar acceso: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun marcarSalida() {
        if (::registroId.isInitialized) { // Verifica si ya tenemos el ID del registro
            // Obtener la hora actual de salida
            val horaSalida = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))

            // Actualizar el registro en Firestore
            val registroActualizado = hashMapOf(
                "hora_salida" to horaSalida,   // Marcar la hora de salida
                "estado" to "Completado"       // Cambiar el estado a "Completado"
            )

            // Actualizar el documento en Firestore usando el ID del registro
            firestore.collection("registros_acceso")
                .document(registroId)
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