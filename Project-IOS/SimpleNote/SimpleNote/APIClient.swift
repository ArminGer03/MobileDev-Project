//
//  APIClient.swift
//  SimpleNote
//
//  Created by Armin on 9/13/25.
//

import Foundation

private struct AnyEncodable: Encodable {
    private let _encode: (Encoder) throws -> Void
    init<T: Encodable>(_ value: T) { _encode = value.encode }
    func encode(to encoder: Encoder) throws { try _encode(encoder) }
}

final class APIClient {
    static let shared = APIClient()
    private init() {}

    private let jsonEncoder = JSONEncoder()
    private let jsonDecoder = JSONDecoder()

    private lazy var session: URLSession = {
        let cfg = URLSessionConfiguration.default
        cfg.timeoutIntervalForRequest = 3
        cfg.timeoutIntervalForResource = 3
        return URLSession(configuration: cfg)
    }()

    // MARK: Core request
    func request<T: Decodable>(
        _ path: String,
        method: String = "GET",
        body: Encodable? = nil,
        authorized: Bool = true,
        decode: T.Type = T.self
    ) async throws -> T {
        var req = URLRequest(url: BASE_URL.appendingPathComponent(path))
        req.httpMethod = method
        req.setValue("application/json", forHTTPHeaderField: "Content-Type")
        if authorized, let access = TokenStore.access() {
            req.setValue("Bearer \(access)", forHTTPHeaderField: "Authorization")
        }
        if let body = body {
            req.httpBody = try jsonEncoder.encode(AnyEncodable(body))
        }

        let (data, resp) = try await session.data(for: req)
        if let http = resp as? HTTPURLResponse, http.statusCode == 401, authorized {
            try await refreshAccessOrThrow()
            return try await request(path, method: method, body: body, authorized: authorized, decode: decode)
        }
        return try jsonDecoder.decode(T.self, from: data)
    }

    /// For DELETE (empty body)
    func requestVoid(_ path: String, method: String = "DELETE", authorized: Bool = true) async throws {
        var req = URLRequest(url: BASE_URL.appendingPathComponent(path))
        req.httpMethod = method
        req.setValue("application/json", forHTTPHeaderField: "Content-Type")
        if authorized, let access = TokenStore.access() {
            req.setValue("Bearer \(access)", forHTTPHeaderField: "Authorization")
        }
        let (_, resp) = try await session.data(for: req)
        if let http = resp as? HTTPURLResponse, http.statusCode == 401, authorized {
            try await refreshAccessOrThrow()
            try await requestVoid(path, method: method, authorized: authorized)
        }
    }

    // MARK: Endpoints
    func login(username: String, password: String) async throws -> TokenPair {
        struct Body: Encodable { let username: String; let password: String }
        return try await request("api/auth/token/", method: "POST", body: Body(username: username, password: password), authorized: false)
    }

    func register(first: String, last: String, email: String, username: String, password: String) async throws -> [String: String] {
        struct Body: Encodable { let username: String; let password: String; let email: String; let first_name: String; let last_name: String }
        return try await request("api/auth/register/", method: "POST",
                                 body: Body(username: username, password: password, email: email, first_name: first, last_name: last),
                                 authorized: false)
    }

    func refreshAccess(refresh: String) async throws -> AccessOnly {
        struct Body: Encodable { let refresh: String }
        return try await request("api/auth/token/refresh/", method: "POST", body: Body(refresh: refresh), authorized: false)
    }

    func listNotes(page: Int, pageSize: Int = 6) async throws -> PagedNotes {
        try await request("api/notes/?page=\(page)&page_size=\(pageSize)")
    }

    func getNote(id: Int64) async throws -> Note { try await request("api/notes/\(id)/") }

    func createNote(title: String, description: String) async throws -> Note {
        struct Body: Encodable { let title: String; let description: String }
        return try await request("api/notes/", method: "POST", body: Body(title: title, description: description))
    }

    func updateNote(id: Int64, title: String, description: String) async throws -> Note {
        struct Body: Encodable { let title: String; let description: String }
        return try await request("api/notes/\(id)/", method: "PATCH", body: Body(title: title, description: description))
    }

    func deleteNote(id: Int64) async throws { try await requestVoid("api/notes/\(id)/", method: "DELETE") }

    // MARK: refresh helper
    private func refreshAccessOrThrow() async throws {
        guard let refresh = TokenStore.refresh() else { throw URLError(.userAuthenticationRequired) }
        let accessOnly: AccessOnly = try await refreshAccess(refresh: refresh)
        TokenStore.saveAccess(accessOnly.access)
    }
}
