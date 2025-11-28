package ec.edu.uisek.githubclient

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ec.edu.uisek.githubclient.databinding.ActivityRepoFormBinding
import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.models.RepoRequest
import ec.edu.uisek.githubclient.models.UpdateRepoRequest // Asegúrate de que este import exista
import ec.edu.uisek.githubclient.services.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RepoForm : AppCompatActivity() {

    private lateinit var binding: ActivityRepoFormBinding
    private var isEditMode = false // Indica si el formulario es para editar o crear
    private var repoNameToEdit: String? = null
    private var repoOwner: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRepoFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Revisa si la pantalla se abrió para editar o para crear
        isEditMode = intent.getBooleanExtra("IS_EDIT_MODE", false)

        if (isEditMode) {
            // Edición: Rellena los datos
            title = "Editar Repositorio"
            repoNameToEdit = intent.getStringExtra("REPO_NAME")
            repoOwner = intent.getStringExtra("REPO_OWNER")
            val description = intent.getStringExtra("REPO_DESCRIPTION")

            binding.repoNameInput.setText(repoNameToEdit)
            binding.repoNameInput.isEnabled = false //No se permite editar el nombre
            binding.repoDescriptionInput.setText(description)
        } else {
            title = "Crear Nuevo Repositorio"
        }

        binding.cancelButton.setOnClickListener { finish() }

        // Ahora, debe llamar a handleSave() para que decida qué hacer.
        binding.saveButton.setOnClickListener { handleSave() }
    }

    //Decide si llamar a la función de crear o de actualizar.
    private fun handleSave() {
        // La validación del nombre solo es necesaria en modo creación.
        if (!isEditMode && !validateForm()) {
            return
        }

        if (isEditMode) {
            updateRepo()
        } else {
            createRepo()
        }
    }

    //Valida que el nombre del repositorio no esté vacío y no contenga espacios.
    private fun validateForm(): Boolean {
        val repoName = binding.repoNameInput.text.toString()
        if (repoName.isBlank()) {
            binding.repoNameInput.error = "El nombre es requerido"
            return false
        }
        if (repoName.contains(" ")) {
            binding.repoNameInput.error = "El nombre no puede tener espacios"
            return false
        }
        binding.repoNameInput.error = null
        return true
    }

    // Llama a la API para crear un nuevo repositorio.
    private fun createRepo() {
        val repoName = binding.repoNameInput.text.toString().trim()
        val repoDescription = binding.repoDescriptionInput.text.toString().trim()
        val repoRequest = RepoRequest(repoName, repoDescription)

        RetrofitClient.getApiService().addRepo(repoRequest).enqueue(object : Callback<Repo> {
            override fun onResponse(call: Call<Repo>, response: Response<Repo>) {
                if (response.isSuccessful) {
                    showMessage("Repositorio creado exitosamente")
                    finish()
                } else {
                    showMessage("Error al crear: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Repo>, t: Throwable) {
                showMessage("Fallo al crear el repositorio: ${t.message}")
            }
        })
    }

    // Llama a la API para actualizar un repositorio existente.
    private fun updateRepo() {
        if (repoOwner == null || repoNameToEdit == null) {
            showMessage("Error: No se pudo obtener la información para editar.")
            return
        }

        val repoDescription = binding.repoDescriptionInput.text.toString().trim()

        // Crea una instancia del modelo especial para actualizar, que solo tiene la descripción.
        val updateRequest = UpdateRepoRequest(description = repoDescription)

        // Llama al méttodo `updateRepo` de la API con los datos correctos.
        RetrofitClient.getApiService().updateRepo(repoOwner!!, repoNameToEdit!!, updateRequest)
            .enqueue(object : Callback<Repo> {
                override fun onResponse(call: Call<Repo>, response: Response<Repo>) {
                    if (response.isSuccessful) {
                        showMessage("Repositorio actualizado exitosamente")
                        finish()
                    } else {
                        showMessage("Error al actualizar: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Repo>, t: Throwable) {
                    showMessage("Fallo al actualizar el repositorio: ${t.message}")
                }
            })
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
