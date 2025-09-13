//
//  HomeView.swift
//  SimpleNote
//
//  Created by Armin on 9/13/25.
//

// HomeView.swift
import SwiftUI

struct HomeView: View {
    @StateObject private var vm = HomeViewModel()
    @StateObject private var net = Connectivity()

    var onAdd: () -> Void
    var onOpen: (Note) -> Void
    var onLogout: () -> Void

    @State private var query = ""

    var body: some View {
        ZStack {
            VStack(spacing: 10) {
                // Network pill
                NetworkPill(status: vm.status, isOnline: net.isOnline)
                    .padding(.horizontal, 16)

                if !vm.hasNotes {
                    EmptyHome()
                        .task { await vm.initLoad() }
                } else {
                    // Search (local)
                    HStack {
                        Image(systemName: "magnifyingglass")
                        TextField("Search…", text: $query)
                    }
                    .padding(12).background(.ultraThinMaterial)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .padding(.horizontal, 16)

                    Text("Notes").font(.headline).frame(maxWidth: .infinity)

                    // Horizontal pages
                    Pager(total: vm.totalPages) { pageWidth in
                        ForEach(1...vm.totalPages, id: \.self) { page in
                            PageGrid(notes: (vm.pages[page] ?? []).filter { n in
                                query.isEmpty ||
                                n.title.lowercased().contains(query.lowercased()) ||
                                n.description.lowercased().contains(query.lowercased())
                            }, onOpen: onOpen)
                            .frame(width: pageWidth)
                            .task { await vm.ensure(page: page) }
                        }
                    }
                    .padding(.horizontal, 16)
                }
                Spacer(minLength: 0)
                // Tab bar space
                Color.clear.frame(height: 80)
            }

            // Center FAB straddling top of tab bar
            VStack {
                Spacer()
                Button(action: onAdd) {
                    Image(systemName: "plus").font(.system(size: 24, weight: .bold))
                        .frame(width: 72, height: 72)
                        .background(Color(hex: 0x504EC3)).foregroundStyle(.white)
                        .clipShape(Circle())
                        .shadow(radius: 6)
                }
                .offset(y: -40) // half over the (imaginary) tab bar
            }
        }
        .task { if vm.pages.isEmpty { await vm.initLoad() } }
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button("Logout", action: onLogout)
            }
        }
    }
}

private struct EmptyHome: View {
    var body: some View {
        VStack(spacing: 16) {
            Spacer()
            Text("Start Your Journey").font(.title).bold()
            Text("Every big step start with small step.\nNotes your first idea and start your journey!")
                .multilineTextAlignment(.center).foregroundStyle(.secondary)
            Spacer()
        }
        .padding()
    }
}

private struct PageGrid: View {
    let notes: [Note]
    var onOpen: (Note) -> Void
    var body: some View {
        ScrollView {
            LazyVGrid(columns: [GridItem(.flexible(), spacing: 12),
                                GridItem(.flexible(), spacing: 12)], spacing: 12) {
                ForEach(notes) { n in
                    Button {
                        onOpen(n)
                    } label: {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("💡 \(n.title)").font(.headline)
                            Text(n.description).font(.subheadline).foregroundStyle(.secondary)
                                .lineLimit(6)
                        }
                        .padding(14)
                        .frame(maxWidth: .infinity, minHeight: 160, alignment: .topLeading)
                        .background(Color.yellow.opacity(0.25))
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                    }
                }
            }
            .padding(.bottom, 72/2 + 24) // room for FAB
        }
    }
}

private struct Pager<Content: View>: View {
    let total: Int
    @ViewBuilder var contentBuilder: (CGFloat) -> Content
    init(total: Int, @ViewBuilder content: @escaping (CGFloat) -> Content) {
        self.total = total; self.contentBuilder = content
    }
    var body: some View {
        GeometryReader { geo in
            TabView {
                contentBuilder(geo.size.width)
            }
            .tabViewStyle(.page(indexDisplayMode: .always))
        }
        .frame(height: 520) // enough for grid
    }
}

private struct NetworkPill: View {
    let status: NetStatus
    let isOnline: Bool

    var body: some View {
        let bg: Color
        let dot: Color
        let text: String
        switch status {
        case .timeout(let sec):
            bg = Color.red.opacity(0.1); dot = .red
            text = sec > 0 ? "Timeout — retry in \(sec)s" : "Timeout — retrying…"
        case .available:
            bg = Color.green.opacity(0.15); dot = .green; text = "Online"
        case .connecting:
            bg = Color.orange.opacity(0.15); dot = .orange; text = "Connecting…"
        }
        return HStack(spacing: 8) {
            Circle().fill(dot).frame(width: 8, height: 8)
            Text(text).font(.footnote).fontWeight(.medium).foregroundStyle(.primary)
            Spacer()
        }
        .padding(.horizontal, 12).padding(.vertical, 8)
        .background(bg).clipShape(RoundedRectangle(cornerRadius: 12))
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.black.opacity(0.05)))
    }
}
