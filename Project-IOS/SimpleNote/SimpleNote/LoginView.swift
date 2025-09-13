//
//  LoginView.swift
//  SimpleNote
//
//  Created by Armin on 9/13/25.
//

import SwiftUI

struct LoginView: View {
    @StateObject private var vm = AuthViewModel()

    @State private var username = ""
    @State private var password = ""

    /// Called when login succeeds. You get `access` and `refresh`.
    var onLogin: (_ access: String, _ refresh: String) -> Void
    /// Navigate to Register.
    var onRegister: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Let’s Login")
                .font(.system(size: 32, weight: .bold))

            TextField("Username", text: $username)
                .textFieldStyle(.roundedBorder)

            SecureField("Password", text: $password)
                .textFieldStyle(.roundedBorder)

            // ✅ Use the explicit Button(action:){ label } form
            Button(action: {
                Task {
                    guard !username.isEmpty, !password.isEmpty else { return }
                    if await vm.login(username: username, password: password) {
                        if let access = TokenStore.access(),
                           let refresh = TokenStore.refresh() {
                            onLogin(access, refresh)
                        }
                    }
                }
            }) {
                HStack { Spacer(); Text(vm.loading ? "…" : "Login"); Spacer() }
            }
            .frame(height: 52)
            .background(Color(hex: 0x504EC3))
            .foregroundStyle(.white)
            .clipShape(RoundedRectangle(cornerRadius: 26))

            HStack { Divider(); Text("Or"); Divider() }

            Button(action: onRegister) {
                Text("Don’t have any account? Register here")
            }
            .tint(Color(hex: 0x504EC3))

            Spacer()
        }
        .padding(20)
        .alert("Error",
               isPresented: Binding(get: { vm.error != nil },
                                   set: { _ in vm.error = nil })) {
            Button("OK", role: .cancel) { vm.error = nil }
        } message: { Text(vm.error ?? "") }
    }
}

// Hex helper (use it once in the project)
extension Color {
    init(hex: UInt32) {
        self.init(red: Double((hex >> 16) & 0xFF) / 255.0,
                  green: Double((hex >> 8) & 0xFF) / 255.0,
                  blue: Double(hex & 0xFF) / 255.0)
    }
}
