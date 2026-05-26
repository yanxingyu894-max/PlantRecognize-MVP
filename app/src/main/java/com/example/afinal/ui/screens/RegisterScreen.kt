package com.example.afinal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.afinal.ui.theme.PlantGreenPrimary
import com.example.afinal.ui.viewmodel.PlantViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(viewModel: PlantViewModel, onNavigateToLogin: () -> Unit, onBack: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var agree by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val isUsernameValid = username.isNotBlank() && username.length >= 3
    val isEmailValid = email.isNotBlank() && email.contains("@")
    val isPasswordValid = password.isNotBlank() && password.length >= 6
    val isPasswordMatching = password == confirmPassword && password.isNotBlank()
    val isFormValid = isUsernameValid && isEmailValid && isPasswordValid && isPasswordMatching && agree

    Surface(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text("Create Account", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = PlantGreenPrimary)
            Text("Join the plant community today", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it; errorMessage = "" },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = username.isNotBlank() && !isUsernameValid,
                supportingText = { if (username.isNotBlank() && !isUsernameValid) Text("Minimum 3 characters", color = Color.Red, fontSize = 11.sp) }
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = "" },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = email.isNotBlank() && !isEmailValid,
                supportingText = { if (email.isNotBlank() && !isEmailValid) Text("Please enter a valid email", color = Color.Red, fontSize = 11.sp) }
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = "" },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = "Toggle password")
                    }
                },
                isError = password.isNotBlank() && !isPasswordValid,
                supportingText = { if (password.isNotBlank() && !isPasswordValid) Text("Minimum 6 characters", color = Color.Red, fontSize = 11.sp) }
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; errorMessage = "" },
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    if (confirmPassword.isNotBlank()) {
                        Icon(
                            if (isPasswordMatching) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = "Match Status",
                            tint = if (isPasswordMatching) Color.Green else Color.Red
                        )
                    }
                },
                isError = confirmPassword.isNotBlank() && !isPasswordMatching,
                supportingText = { if (confirmPassword.isNotBlank() && !isPasswordMatching) Text("Passwords do not match", color = Color.Red, fontSize = 11.sp) }
            )

            if (errorMessage.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)), shape = RoundedCornerShape(8.dp)) {
                    Text(errorMessage, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                }
            }

            if (successMessage.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)), shape = RoundedCornerShape(8.dp)) {
                    Text(successMessage, color = Color(0xFF2E7D32), fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = agree, onCheckedChange = { agree = it }, colors = CheckboxDefaults.colors(checkedColor = PlantGreenPrimary))
                Text("I agree to the Terms and Privacy Policy", fontSize = 13.sp, modifier = Modifier.clickable { agree = !agree })
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (!isFormValid) {
                        errorMessage = when {
                            !isUsernameValid -> "Username format is invalid."
                            !isEmailValid -> "Email format is invalid."
                            !isPasswordValid -> "Password must be at least 6 characters."
                            !isPasswordMatching -> "Passwords do not match."
                            !agree -> "Please agree to the Terms."
                            else -> "Please fill in all information."
                        }
                    } else {
                        isLoading = true
                        coroutineScope.launch {
                            val result = viewModel.registerUser(username, password, agree)
                            isLoading = false
                            if (result.isSuccess) {
                                successMessage = "Registration successful! Redirecting..."
                                errorMessage = ""
                                delay(1200) // Small delay so users can read the success message
                                onNavigateToLogin()
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "Registration Failed"
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PlantGreenPrimary, disabledContainerColor = Color.Gray)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Register Account", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Back to Login", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}