//
//  NoteEditorView.swift
//  SimpleNote
//
//  Created by Armin on 9/13/25.
//

// NoteEditorView.swift
import SwiftUI

struct NoteEditorView: View {
    @Environment(\.dismiss) var dismiss
    @State private var noteId: Int64?
    @State private var title: String = ""
    @State private var bodyText: String = ""
    @State private var showDelete = false

    let existing: Note?
    var onDone: () -> Void

    var body: some View {
        VStack(alignment: .leading) {
            HStack {
                Button { dismiss(); onDone() } label: { Label("Back", systemImage: "chevron.left") }
                Spacer()
                Button(role: .destructive) { showDelete = true } label: { Image(systemName: "trash") }
            }
            .padding(.horizontal)

            TextField("Title", text: $title)
                .font(.largeTitle.weight(.bold))
                .padding(.horizontal)

            Divider().padding(.horizontal)

            TextEditor(text: $bodyText)
                .padding(.horizontal)
                .frame(maxWidth: .infinity, maxHeight: .infinity)

            HStack {
                Text("Last edited just now").font(.footnote).foregroundStyle(.secondary)
                Spacer()
            }
            .padding()
        }
        .onAppear {
            if let n = existing {
                noteId = n.id; title = n.title; bodyText = n.description
            }
        }
        .task(id: title + "|" + bodyText) {
            // AUTOSAVE debounce ~0.9s
            try? await Task.sleep(nanoseconds: 900_000_000)
            await save()
        }
        .confirmationDialog("Want to Delete this Note?", isPresented: $showDelete, titleVisibility: .visible) {
            Button("Delete Note", role: .destructive) {
                Task { await delete() }
            }
            Button("Cancel", role: .cancel) {}
        }
    }

    private func saveNow() async {
        if title.trimmingCharacters(in: .whitespaces).isEmpty && bodyText.trimmingCharacters(in: .whitespaces).isEmpty { return }
        do {
            if let id = noteId {
                let n = try await APIClient.shared.updateNote(id: id, title: title, description: bodyText)
                noteId = n.id
            } else {
                let n = try await APIClient.shared.createNote(title: title, description: bodyText)
                noteId = n.id
            }
        } catch { /* show toast if you want */ }
    }
    private func save() async { await saveNow() }

    private func delete() async {
        guard let id = noteId else { return }
        do { try await APIClient.shared.deleteNote(id: id); onDone() } catch { }
    }
}
