package com.app.quiz.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SignupScreen(
    onSigned: () -> Unit,
    onGoToLogin: () -> Unit,
    vm: AuthVm = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    var nickname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state) {
        if (state is AuthState.Logged) onSigned()
    }

    fun validateLocal(): Boolean {
        val lower = nickname.trim().lowercase().replace("\\s+".toRegex(), "")
        val ok = lower.matches(Regex("^[a-z0-9._-]{3,20}$"))
        localError = if (!ok) "Use 3–20 caracteres: letras, números, '.', '_' ou '-'." else null
        return ok
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Criar conta", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it; if (localError != null) validateLocal() },
                label = { Text("Nickname (único)") },
                singleLine = true,
                supportingText = {
                    val msg = localError
                    if (msg != null) Text(msg, color = MaterialTheme.colorScheme.error)
                    else Text("Ex.: joao.silva, mari_23")
                },
                isError = localError != null,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = pass,
                onValueChange = { pass = it },
                label = { Text("Senha") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (validateLocal()) vm.signupWithNickname(nickname, email, pass)
                },
                enabled = state !is AuthState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cadastrar") }

            Spacer(Modifier.height(8.dp))
            Text(
                "Já tenho conta (Entrar)",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    vm.clearError()
                    onGoToLogin()
                }
            )

            if (state is AuthState.Loading) {
                Spacer(Modifier.height(12.dp)); CircularProgressIndicator()
            }
            if (state is AuthState.Error) {
                Spacer(Modifier.height(8.dp))
                Text((state as AuthState.Error).msg, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
