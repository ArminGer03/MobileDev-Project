//
//  SimpleNoteApp.swift
//  SimpleNote
//
//  Created by Armin on 9/13/25.
//

import SwiftUI

@main
struct SimpleNoteApp: App {
    enum Screen { case splash, onboarding, login, register, home, newNote, editNote(Note) }

    @State private var screen: Screen = .splash
    @State private var access: String?

    var body: some Scene {
        WindowGroup { contentView }
    }

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
            HomeView(
                onAdd: { screen = .newNote },
                onOpen: { note in screen = .editNote(note) },
                onLogout: { TokenStore.clear(); access = nil; screen = .login }
            )

        case .newNote:
            NoteEditorView(existing: nil) { screen = .home }

        case .editNote(let note):
            NoteEditorView(existing: note) { screen = .home }
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

