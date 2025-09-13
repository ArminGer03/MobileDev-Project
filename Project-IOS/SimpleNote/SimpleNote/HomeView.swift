import SwiftUI

struct HomeView: View {
    @StateObject private var vm  = HomeViewModel()
    @StateObject private var net = Connectivity()

    var onAdd: () -> Void
    var onOpen: (Note) -> Void
    var onOpenSettings: () -> Void
    var onLogout: () -> Void

    @State private var query = ""

    var body: some View {
        ZStack {
            VStack(spacing: 10) {

                // Compose an effective status for the pill
                let effectiveStatus: NetStatus = {
                    if case .timeout = vm.status { return vm.status }
                    return net.isOnline ? .available : .connecting
                }()

                NetworkPill(status: effectiveStatus)
                    .padding(.horizontal, 16)

                if let err = vm.error, !err.isEmpty {
                    Text(err)
                        .font(.footnote)
                        .foregroundStyle(.red)
                        .padding(.horizontal, 16)
                }

                if !vm.hasNotes {
                    EmptyHome()
                } else {
                    // Search (local)
                    HStack {
                        Image(systemName: "magnifyingglass")
                        TextField("Search…", text: $query)
                    }
                    .padding(12)
                    .background(.ultraThinMaterial)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .padding(.horizontal, 16)

                    Text("Notes")
                        .font(.headline)
                        .frame(maxWidth: .infinity)

                    // Pager (one grid per page)
                    Pager(total: vm.totalPages) { pageWidth in
                        ForEach(1...vm.totalPages, id: \.self) { page in
                            PageGrid(
                                notes: (vm.pages[page] ?? []).filter { n in
                                    let q = query.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
                                    guard !q.isEmpty else { return true }
                                    return n.title.lowercased().contains(q)
                                        || n.description.lowercased().contains(q)   // <- no ??
                                },
                                onOpen: onOpen
                            )
                            .frame(width: pageWidth)
                            .task { await vm.ensure(page: page) }
                        }
                    }
                    .padding(.horizontal, 16)
                }

                Spacer(minLength: 0)
                Color.clear.frame(height: 80)
            }

            // Centered FAB
            VStack {
                Spacer()
                Button(action: onAdd) {
                    Image(systemName: "plus")
                        .font(.system(size: 24, weight: .bold))
                        .frame(width: 72, height: 72)
                        .background(Color(red: 0.314, green: 0.306, blue: 0.765)) // #504EC3
                        .foregroundStyle(.white)
                        .clipShape(Circle())
                        .shadow(radius: 6)
                }
                .offset(y: -40)
            }
        }
        // First load on appear
        .task {
            if vm.pages.isEmpty { await vm.initLoad() }
        }
        // Retry when OS connectivity becomes online
        .task(id: net.isOnline) {
            if net.isOnline && vm.pages.isEmpty { await vm.initLoad() }
        }
        .navigationTitle("Home")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            // ✅ Use legacy placements to avoid the Visibility overload
            ToolbarItem(placement: .navigationBarLeading) {
                Button("Logout", action: onLogout)
            }
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: onOpenSettings) {
                    Image(systemName: "gearshape")
                }
            }
        }
    }
}

//
// MARK: - Helpers (same file)
//

private struct NetworkPill: View {
    let status: NetStatus

    var body: some View {
        let (bg, dot, text): (Color, Color, String) = {
            switch status {
            case .available:
                return (Color.green.opacity(0.15), .green, "Online")
            case .connecting:
                return (Color.orange.opacity(0.15), .orange, "Connecting…")
            case .timeout(let sec):
                return (Color.red.opacity(0.12), .red,
                        sec > 0 ? "Timeout — retry in \(sec)s" : "Timeout — retrying…")
            }
        }()

        HStack(spacing: 8) {
            Circle().fill(dot).frame(width: 8, height: 8)
            Text(text).font(.footnote).fontWeight(.medium).foregroundStyle(.primary)
            Spacer()
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .background(bg)
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(Color.black.opacity(0.05))
        )
    }
}

private struct EmptyHome: View {
    var body: some View {
        VStack(spacing: 16) {
            Spacer()
            Text("Start Your Journey").font(.title).bold()
            Text("Every big step start with small step.\nNotes your first idea and start your journey!")
                .multilineTextAlignment(.center)
                .foregroundStyle(.secondary)
                .padding(.horizontal, 24)
            Spacer()
        }
        .padding()
    }
}

private struct Pager<Content: View>: View {
    let total: Int
    @ViewBuilder var contentBuilder: (CGFloat) -> Content

    init(total: Int, @ViewBuilder content: @escaping (CGFloat) -> Content) {
        self.total = total
        self.contentBuilder = content
    }

    var body: some View {
        GeometryReader { geo in
            TabView {
                contentBuilder(geo.size.width)
            }
            .tabViewStyle(.page(indexDisplayMode: .automatic))
        }
        .frame(height: 520)
    }
}

private struct PageGrid: View {
    let notes: [Note]
    var onOpen: (Note) -> Void

    var body: some View {
        ScrollView {
            LazyVGrid(
                columns: [GridItem(.flexible(), spacing: 12),
                          GridItem(.flexible(), spacing: 12)],
                spacing: 12
            ) {
                ForEach(notes) { n in
                    Button {
                        onOpen(n)
                    } label: {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("💡 \(n.title)")
                                .font(.headline)
                            Text(n.description)                // <- no coalescing
                                .font(.subheadline)
                                .foregroundStyle(.secondary)
                                .lineLimit(6)
                        }
                        .padding(14)
                        .frame(maxWidth: .infinity, minHeight: 160, alignment: .topLeading)
                        .background(Color.yellow.opacity(0.25))
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                    }
                }
            }
            .padding(.bottom, 72/2 + 24)
        }
    }
}
