//
//  Connectivity.swift
//  SimpleNote
//
//  Created by Armin on 9/13/25.
//

// Connectivity.swift
import Foundation
import Network

enum NetStatus { case available, connecting, timeout(retryIn: Int) }

final class Connectivity: ObservableObject {
    private let monitor = NWPathMonitor()
    private let queue = DispatchQueue(label: "net.monitor")
    @Published var isOnline: Bool = false

    init() {
        monitor.pathUpdateHandler = { [weak self] path in
            DispatchQueue.main.async {
                self?.isOnline = (path.status == .satisfied)
            }
        }
        monitor.start(queue: queue)
    }
}
