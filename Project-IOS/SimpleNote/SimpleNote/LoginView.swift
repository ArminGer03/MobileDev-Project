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

    var onLogin: (_ access: String, _ refresh: String) -> Void
    var onRegister: () -> Void

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Text("Let’s Login")
                    .font(.system(size: 32, weight: .bold))

                // Fields
                VStack(spacing: 12) {
                    TextField("Username", text: $username)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled(true)
                        .padding(12)
                        .background(Color(.systemGray6))
                        .clipShape(RoundedRectangle(cornerRadius: 12))

                    SecureField("Password", text: $password)
                        .padding(12)
                        .background(Color(.systemGray6))
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                }

                // Login button
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
                        .frame(height: 56)
                        .background(Color(hex: 0x504EC3))
                        .foregroundStyle(.white)
                        .clipShape(RoundedRectangle(cornerRadius: 28))
                }
                .disabled(vm.loading)

                // Nice centered "Or" with horizontal rules
                OrDivider()
                    .padding(.vertical, 6)

                // Register link
                Button(action: onRegister) {
                    Text("Don’t have any account? Register here")
                        .foregroundStyle(Color(hex: 0x504EC3))
                }
                .padding(.top, 4)

                Spacer(minLength: 0)
            }
            .padding(20)
        }
        .alert("Error",
               isPresented: Binding(get: { vm.error != nil },
                                   set: { _ in vm.error = nil })) {
            Button("OK", role: .cancel) { vm.error = nil }
        } message: { Text(vm.error ?? "") }
    }
}

// MARK: - Components

/// Horizontal "—  Or  —" row (Dividers are inside VStacks to render horizontally)
private struct OrDivider: View {
    var body: some View {
        HStack(alignment: .center, spacing: 8) {
            VStack { Divider() }
            Text("Or")
                .foregroundStyle(.secondary)
            VStack { Divider() }
        }
    }
}

// MARK: - Helpers

extension Color {
    init(hex: UInt32) {
        self.init(red: Double((hex >> 16) & 0xFF) / 255.0,
                  green: Double((hex >> 8) & 0xFF) / 255.0,
                  blue: Double(hex & 0xFF) / 255.0)
    }
}
