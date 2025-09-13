//
//  AuthViewModel.swift
//  SimpleNote
//
//  Created by Armin on 9/13/25.
//

// AuthViewModel.swift
import Foundation

@MainActor
final class AuthViewModel: ObservableObject {
    @Published var loading = false
    @Published var error: String?

    func login(username: String, password: String) async -> Bool {
        loading = true; defer { loading = false }
        do {
            let pair = try await APIClient.shared.login(username: username, password: password)
            TokenStore.save(access: pair.access, refresh: pair.refresh)
            return true
        } catch {
            self.error = error.localizedDescription
            return false
        }
    }

    func register(first: String, last: String, email: String, username: String, password: String) async -> Bool {
        loading = true; defer { loading = false }
        do {
            _ = try await APIClient.shared.register(first: first, last: last, email: email, username: username, password: password)
            return true
        } catch {
            self.error = error.localizedDescription
            return false
        }
    }
}
