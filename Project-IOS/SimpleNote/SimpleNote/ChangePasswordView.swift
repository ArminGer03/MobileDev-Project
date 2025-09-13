//
//  ChangePasswordView.swift
//  SimpleNote
//
//  Created by Armin on 9/13/25.
//

import SwiftUI

struct ChangePasswordView: View {
    var onBack: () -> Void
    var onDone: () -> Void

    @State private var current = ""
    @State private var newPwd = ""
    @State private var confirm = ""

    @State private var loading = false
    @State private var error: String?
    @State private var success: String?

    var body: some View {
        Form {
            Section {
                SecureField("Current Password", text: $current)
                SecureField("New Password", text: $newPwd)
                SecureField("Retype New Password", text: $confirm)
            }

            Section {
                Button {
                    Task { await submit() }
                } label: {
                    if loading { ProgressView() } else { Text("Change Password") }
                }
                .disabled(loading)
            }

            if let success {
                Section { Text(success).foregroundStyle(.green) }
            }
            if let error {
                Section { Text(error).foregroundStyle(.red) }
            }
        }
        .navigationTitle("Change Password")
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button(action: onBack) { Label("Back", systemImage: "chevron.left") }
            }
        }
    }

    private func submit() async {
        error = nil; success = nil

        guard !current.isEmpty, !newPwd.isEmpty else {
            error = "Please fill all fields."; return
        }
        guard newPwd.count >= 8 else {
            error = "New password must be at least 8 characters."; return
        }
        guard newPwd == confirm else {
            error = "New passwords don’t match."; return
        }
        guard newPwd != current else {
            error = "New password must be different from the current one."; return
        }

        loading = true
        do {
            let msg = try await APIClient.shared.changePassword(oldPassword: current, newPassword: newPwd)
            success = msg.isEmpty ? "Password changed" : msg
            try? await Task.sleep(nanoseconds: 900_000_000) // small UX delay
            onDone()
        } catch {
            self.error = error.localizedDescription
        }
        loading = false
    }
}
