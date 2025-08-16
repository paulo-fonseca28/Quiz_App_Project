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
fun LoginScreen(
    onLogged: () -> Unit,
    onGoToSignup: () -> Unit,
    vm: AuthVm = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    LaunchedEffect(state) { if (state is AuthState.Logged) onLogged() }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Entrar", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("E-mail") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Senha") },
                singleLine = true, visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(24.dp))
            Button(onClick = { vm.login(email, pass) }, enabled = state !is AuthState.Loading,
                modifier = Modifier.fillMaxWidth()) { Text("Entrar") }
            Spacer(Modifier.height(8.dp))
            Text("Cadastrar-se", color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { vm.clearError(); onGoToSignup() })
            if (state is AuthState.Loading) { Spacer(Modifier.height(12.dp)); CircularProgressIndicator() }
            if (state is AuthState.Error) {
                Spacer(Modifier.height(8.dp))
                Text((state as AuthState.Error).msg, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
