package com.example.applogindrive

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes

class MainActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val context: Context get() = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        setupGoogleSignIn()
        checkLoggedInUser()

        // Configurar o botão de login
        val loginButton: Button = findViewById(R.id.login_button)
        loginButton.setOnClickListener {
            signIn()
        }

        // Ajuste de padding para as barras do sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Configura o GoogleSignInOptions e o GoogleSignInClient
    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE), Scope(DriveScopes.DRIVE))  // Permissões para Google Drive
            //.requestIdToken("CLIENT_ID")  // Substitua pelo seu ID de cliente, se necessário
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

    }

    // Verifica se o usuário já está logado e, caso positivo, inicia a PrincipalActivity
    private fun checkLoggedInUser() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            Log.d("MainActivity", "Usuário já logado: ${account.email}")
            Toast.makeText(this, "Usuário logado: ${account.serverAuthCode}", Toast.LENGTH_LONG).show()
            val intent = Intent(this, PrincipalActivity::class.java).apply {
                putExtra("account", account) // Passa a conta
                putExtra("clientId", "237352169338-3ss4fusv3j8vct4m80islgo913t1fobs.apps.googleusercontent.com") // Passa o Client ID
            }
            startActivity(intent)
            finish()  // Fecha a atividade de login
        }
    }

    // Inicia o processo de login
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // Lidar com o resultado do login
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    // Trata o resultado do login
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            Toast.makeText(this, "Login bem-sucedido: ${account.idToken}", Toast.LENGTH_LONG).show()
            val intent = Intent(this, PrincipalActivity::class.java).apply {
                putExtra("account", account) // Passa a conta
                putExtra("clientId", "237352169338-3ss4fusv3j8vct4m80islgo913t1fobs.apps.googleusercontent.com") // Passa o Client ID
            }
            startActivity(intent)
            finish()
        } catch (e: ApiException) {
            Log.w("MainActivity", "Login falhou com código: ${e.statusCode}")
            Toast.makeText(this, "Login falhou. Tente novamente.", Toast.LENGTH_LONG).show()
        }
    }




    companion object {
        private const val RC_SIGN_IN = 9001
    }
}
