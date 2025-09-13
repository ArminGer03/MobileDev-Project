//
//  HomeViewModel.swift
//  SimpleNote
//
//  Created by Armin on 9/13/25.
//

// HomeViewModel.swift
import Foundation

@MainActor
final class HomeViewModel: ObservableObject {
    @Published var pages: [Int: [Note]] = [:]
    @Published var count: Int = 0
    @Published var loading = false
    @Published var error: String?
    @Published var status: NetStatus = .connecting

    private var retryTask: Task<Void, Never>?
    private let pageSize = 6

    var totalPages: Int { max(1, Int(ceil(Double(count) / Double(pageSize)))) }
    var hasNotes: Bool { count > 0 || !pages.values.allSatisfy { $0.isEmpty } }

    func initLoad() async {
        loading = true
        do {
            let resp = try await APIClient.shared.listNotes(page: 1, pageSize: pageSize)
            pages = [1: resp.results]; count = resp.count; loading = false
            status = .available
            stopRetry()
        } catch {
            loading = false
            if (error as? URLError)?.code == .timedOut {
                activateTimeout()
            } else {
                self.error = error.localizedDescription
            }
        }
    }

    func ensure(page: Int) async {
        guard page >= 1, page <= totalPages, pages[page] == nil else { return }
        do {
            let resp = try await APIClient.shared.listNotes(page: page, pageSize: pageSize)
            pages[page] = resp.results
            status = .available
            stopRetry()
        } catch {
            if (error as? URLError)?.code == .timedOut { activateTimeout() }
        }
    }

    func refresh() async { await initLoad() }

    private func activateTimeout() {
        startRetry()
    }

    private func startRetry() {
        guard retryTask == nil else { return }
        retryTask = Task { [weak self] in
            while let self, case .timeout = self.status {
                for sec in stride(from: 30, to: 0, by: -1) {
                    await MainActor.run { self.status = .timeout(retryIn: sec) }
                    try? await Task.sleep(nanoseconds: 1_000_000_000)
                    if Task.isCancelled { return }
                }
                await self.initLoad()
                // if still timing out, loop continues; otherwise .available breaks loop automatically
            }
        }
        // kick it
        status = .timeout(retryIn: 30)
    }

    private func stopRetry() {
        retryTask?.cancel(); retryTask = nil
    }
}
