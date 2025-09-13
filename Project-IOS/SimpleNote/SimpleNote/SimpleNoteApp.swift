//
//  SimpleNoteApp.swift
//  SimpleNote
//
//  Created by Armin on 9/13/25.
//

import SwiftUI

@main
struct SimpleNoteApp: App {
    enum Screen {
        case splash
        case onboarding
        case login
        case register
        case home
        case newNote
        case editNote(Note)
        case settings
        case changePassword
    }

    @State private var screen: Screen = .splash
    @State private var access: String?

    var body: some Scene { WindowGroup { contentView } }

    @ViewBuilder
    private var contentView: some View {
        switch screen {
        case .splash:
            Color.clear.onAppear { bootstrap() }

        case .onboarding:
            OnboardingView { screen = .login }

        case .login:
            LoginView(
                onLogin: { accessToken, refresh in
                    TokenStore.save(access: accessToken, refresh: refresh)
                    access = accessToken
                    screen = .home
                },
                onRegister: { screen = .register }
            )

        case .register:
            RegisterView(
                onDone: { screen = .login },
                onBack: { screen = .login }
            )

        case .home:
            NavigationStack {
                HomeView(
                    onAdd: { screen = .newNote },
                    onOpen: { note in screen = .editNote(note) },
                    onOpenSettings: { screen = .settings }
                )
            }

        case .newNote:
            NavigationStack {
                NoteEditorView(existing: nil) { screen = .home }
            }

        case .editNote(let note):
            NavigationStack {
                NoteEditorView(existing: note) { screen = .home }
            }

        case .settings:
            NavigationStack {
                SettingsView(
                    onBack: { screen = .home },
                    onChangePassword: { screen = .changePassword },
                    onLogout: { screen = .login }
                )
            }

        case .changePassword:
            NavigationStack {
                ChangePasswordView(
                    onBack: { screen = .settings },
                    onDone: { screen = .settings }
                )
            }
        }
    }

    private func bootstrap() {
        if let refresh = TokenStore.refresh() {
            Task {
                do {
                    let new = try await APIClient.shared.refreshAccess(refresh: refresh)
                    TokenStore.saveAccess(new.access)
                    access = new.access
                    screen = .home
                } catch {
                    screen = .onboarding
                }
            }
        } else {
            screen = .onboarding
        }
    }
}
