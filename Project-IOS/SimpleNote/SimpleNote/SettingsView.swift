//
//  SettingsView.swift
//  SimpleNote
//
//  Created by Armin on 9/13/25.
//

import SwiftUI

struct SettingsView: View {
    // Navigation callbacks
    var onBack: () -> Void
    var onChangePassword: () -> Void
    var onLogout: () -> Void

    @State private var loading = true
    @State private var error: String?
    @State private var user: UserInfo?
    @State private var showLogoutConfirm = false

    var body: some View {
        List {
            if let user {
                Section {
                    HStack(spacing: 14) {
                        ZStack {
                            Circle().fill(Color.gray.opacity(0.15))
                                .frame(width: 56, height: 56)
                            Text(initials(user))
                                .font(.headline)
                        }
                        VStack(alignment: .leading, spacing: 2) {
                            Text(displayName(user))
                                .font(.headline)
                            Text(user.email)
                                .foregroundStyle(.secondary)
                                .font(.subheadline)
                        }
                    }
                    .padding(.vertical, 4)
                }

                Section("Account") {
                    Button {
                        onChangePassword()
                    } label: {
                        HStack {
                            Image(systemName: "lock")
                            Text("Change Password")
                        }
                    }

                    Button(role: .destructive) {
                        showLogoutConfirm = true
                    } label: {
                        HStack {
                            Image(systemName: "rectangle.portrait.and.arrow.right")
                            Text("Log Out")
                        }
                    }
                }
            }
        }
        .overlay {
            if loading { ProgressView().controlSize(.large) }
        }
        .navigationTitle("Settings")
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button(action: onBack) {
                    Label("Back", systemImage: "chevron.left")
                }
            }
        }
        .alert("Error", isPresented: Binding(get: { error != nil }, set: { _ in error = nil })) {
            Button("OK", role: .cancel) { error = nil }
        } message: {
            Text(error ?? "")
        }
        .confirmationDialog("Log out from the application?",
                            isPresented: $showLogoutConfirm,
                            titleVisibility: .visible) {
            Button("Log Out", role: .destructive) {
                TokenStore.clear()
                onLogout()
            }
            Button("Cancel", role: .cancel) {}
        }
        .task { await loadUser() }
    }

    private func loadUser() async {
        loading = true; error = nil
        do {
            user = try await APIClient.shared.userInfo()
        } catch {
            self.error = error.localizedDescription
        }
        loading = false
    }

    private func displayName(_ u: UserInfo) -> String {
        let first = u.first_name?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        let last  = u.last_name?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        let full = [first, last].filter { !$0.isEmpty }.joined(separator: " ")
        return full.isEmpty ? u.username : full
    }

    private func initials(_ u: UserInfo) -> String {
        let f = (u.first_name?.first).map { String($0) } ?? ""
        let l = (u.last_name?.first).map { String($0) } ?? ""
        let res = (f + l)
        return res.isEmpty ? String(u.username.prefix(2)).uppercased() : res.uppercased()
    }
}
