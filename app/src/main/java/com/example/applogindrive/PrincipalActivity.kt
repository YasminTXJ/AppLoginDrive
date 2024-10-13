package com.example.applogindrive

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.InputStreamContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream


class PrincipalActivity : AppCompatActivity() {
    private lateinit var googleDriveService: Drive
    private lateinit var textFileName: TextView
    private var fileUri: Uri? = null
    private lateinit var googleAccount: GoogleSignInAccount
    private val context: Context get() = this
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.principal)

        val buttonSignOut: Button = findViewById(R.id.buttonsair)
        buttonSignOut.setOnClickListener {
            signOut()
        }
        createGoogleSignInClient()
        // Recupera a conta passada pela MainActivity
        googleAccount = intent.getParcelableExtra("account")
            ?: throw IllegalArgumentException("Conta Google não recebida")

        // Inicializa o serviço do Google Drive em uma coroutine
        initializeDriveService()

        // Exemplo de uso do Drive: Mostrar informações da conta logada
        displayAccountInfo()

        val buttonSelectFile: Button = findViewById(R.id.button_select_file)
        val buttonUploadFile: Button = findViewById(R.id.button_upload_file)
        textFileName = findViewById(R.id.text_file_name)

        buttonSelectFile.setOnClickListener {
            openFileChooser()
        }

        buttonUploadFile.setOnClickListener {
            uploadFileToDrive()
        }
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        filePickerLauncher.launch(intent)
    }

    @SuppressLint("SetTextI18n")
    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                fileUri = result.data?.data
                textFileName.text = "Arquivo selecionado: ${fileUri?.lastPathSegment}"
                findViewById<Button>(R.id.button_upload_file).isEnabled = true
            }
        }

    // Inicializa o serviço do Google Drive utilizando a conta Google recebida
    private fun initializeDriveService() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                googleDriveService = initializeDrive(googleAccount)
                Log.d("PrincipalActivity", "Drive API inicializado com sucesso.")
            } catch (e: GoogleAuthException) {
                Log.e("PrincipalActivity", "Erro de autenticação: ${e.message}")
            } catch (e: IOException) {
                Log.e("PrincipalActivity", "Erro de IO: ${e.message}")
            } catch (e: Exception) {
                Log.e("PrincipalActivity", "Erro geral: ${e.message}")
            }
        }
    }

    // Exemplo simples de exibir informações da conta logada
    private fun displayAccountInfo() {
        Toast.makeText(this, "Bem-vindo, ${googleAccount.displayName}", Toast.LENGTH_LONG).show()
        Log.d("PrincipalActivity", "Usuário logado: ${googleAccount.email}")
    }

    private fun uploadFileToDrive() {
        fileUri?.let { uri ->
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val inputStream: InputStream? = contentResolver.openInputStream(uri)
                    val fileName = uri.lastPathSegment ?: "Arquivo sem nome"

                    val fileMetadata = File().apply {
                        name = fileName
                    }

                    val file = googleDriveService.files().create(
                        fileMetadata,
                        InputStreamContent("application/octet-stream", inputStream)
                    )
                        .setFields("id")
                        .execute()

                    Log.d("Drive", "Arquivo enviado com ID: ${file.id}")

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@PrincipalActivity, "Arquivo enviado com sucesso!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: IOException) {
                    Log.e("Drive", "Erro ao enviar arquivo", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@PrincipalActivity, "Erro ao enviar arquivo: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun signOut() {
        googleSignInClient.signOut().addOnCompleteListener(this) {
            // Ação a ser tomada após o usuário ser deslogado
            Toast.makeText(this, "Usuário deslogado com sucesso!", Toast.LENGTH_SHORT).show()

            // Redirecionar para a tela de login ou outra ação desejada
            val intent =
                Intent(this, MainActivity::class.java) // Substitua pela sua Activity de login
            startActivity(intent)
            finish() // Opcional: terminar a Activity atual
        }.addOnFailureListener {
            Toast.makeText(this, "Erro ao deslogar: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Função para inicializar o Google Drive com a conta Google logada
    private fun initializeDrive(googleAccount: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(DriveScopes.DRIVE, DriveScopes.DRIVE_FILE)
        ).setSelectedAccount(googleAccount.account)

        return Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName(context.getString(R.string.app_name))
            .build()
    }
    private fun createGoogleSignInClient() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE), Scope(DriveScopes.DRIVE))
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }
}
