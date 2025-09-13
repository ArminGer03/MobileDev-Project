import SwiftUI

struct HomeView: View {
    @StateObject private var vm  = HomeViewModel()
    @StateObject private var net = Connectivity()

    // Navigation callbacks
    var onAdd: () -> Void
    var onOpen: (Note) -> Void
    var onOpenSettings: () -> Void

    @State private var query = ""

    var body: some View {
        ZStack {
            VStack(spacing: 10) {
                // Pill status: timeout (from VM) wins; otherwise OS connectivity
                let effectiveStatus: NetStatus = {
                    if case .timeout = vm.status { return vm.status }
                    return net.isOnline ? .available : .connecting
                }()
                NetworkPill(status: effectiveStatus)
                    .padding(.horizontal, 16)

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
                                        || n.description.lowercased().contains(q)
                                },
                                onOpen: onOpen
                            )
                            .frame(width: pageWidth)
                            .task { await vm.ensure(page: page) }
                            .tag(page) // <-- help TabView index dots
                        }
                    }
                    .padding(.horizontal, 16)
                }

                Spacer(minLength: 0)
                Color.clear.frame(height: 80) // breathing room for bottom buttons
            }

            // Bottom controls overlay
            VStack {
                Spacer()
                ZStack {
                    // Center FAB (+)
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

                    // Bottom-right Settings button (gear)
                    HStack {
                        Spacer()
                        Button(action: onOpenSettings) {
                            Image(systemName: "gearshape.fill")
                                .font(.system(size: 20, weight: .semibold))
                                .frame(width: 52, height: 52)
                                .background(.ultraThinMaterial)
                                .clipShape(Circle())
                                .shadow(radius: 3)
                        }
                        .padding(.trailing, 20)
                        .padding(.bottom, 10)
                    }
                    .frame(maxWidth: .infinity, alignment: .trailing)
                }
            }
        }
        // First load on appear
        .task {
            if vm.pages.isEmpty { await vm.initLoad() }
        }
        // If connectivity flips online and we still have nothing, try again
        .task(id: net.isOnline) {
            if net.isOnline && vm.pages.isEmpty { await vm.initLoad() }
        }
        .navigationTitle("Home")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { } // no top buttons
    }
}

//
// MARK: - Helpers kept inline
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
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.black.opacity(0.05)))
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

/// A full-width pager that ALWAYS shows dots when there’s more than 1 page.
/// Works on iOS 14–18.
private struct Pager<PageContent: View>: View {
    let total: Int
    @ViewBuilder var contentBuilder: (CGFloat) -> PageContent

    init(total: Int, @ViewBuilder content: @escaping (CGFloat) -> PageContent) {
        self.total = total
        self.contentBuilder = content
    }

    var body: some View {
        GeometryReader { geo in
            Group {
                if #available(iOS 16.0, *) {
                    TabView {
                        contentBuilder(geo.size.width)
                    }
                    .tabViewStyle(.page(indexDisplayMode: total > 1 ? .always : .never))
                } else {
                    TabView {
                        contentBuilder(geo.size.width)
                    }
                    .tabViewStyle(PageTabViewStyle(indexDisplayMode: total > 1 ? .always : .never))
                    .indexViewStyle(PageIndexViewStyle(backgroundDisplayMode: .always))
                }
            }
            .frame(width: geo.size.width, height: geo.size.height)
            .padding(.bottom, 8) // keep dots from being clipped
        }
        .frame(height: 520)
    }
}

private struct PageGrid: View {
    let notes: [Note]
    var onOpen: (Note) -> Void
    var body: some View {
        ScrollView {
            LazyVGrid(columns: [GridItem(.flexible(), spacing: 12),
                                GridItem(.flexible(), spacing: 12)],
                      spacing: 12) {
                ForEach(notes) { n in
                    Button { onOpen(n) } label: {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("💡 \(n.title)")
                                .font(.headline)
                                .multilineTextAlignment(.leading)
                            Text(n.description)
                                .font(.subheadline)
                                .foregroundStyle(.secondary)
                                .lineLimit(6)
                                .multilineTextAlignment(.leading)
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
