//
//  APIClient.swift
//  SimpleNote
//
//  Created by Armin on 9/13/25.
//

import Foundation

final class APIClient {
    static let shared = APIClient()
    private init() {}

    // MARK: - JSON coders
    private let jsonEncoder: JSONEncoder = {
        let e = JSONEncoder()
        return e
    }()
    private let jsonDecoder: JSONDecoder = {
        let d = JSONDecoder()
        return d
    }()

    // MARK: - URLSession
    private lazy var session: URLSession = {
        let cfg = URLSessionConfiguration.default
        cfg.timeoutIntervalForRequest = 3
        cfg.timeoutIntervalForResource = 3
        return URLSession(configuration: cfg)
    }()

    // MARK: - Public endpoints

    // Auth
    func login(username: String, password: String) async throws -> TokenPair {
        struct Body: Encodable { let username: String; let password: String }
        return try await request(
            "api/auth/token/",
            method: "POST",
            body: Body(username: username, password: password),
            authorized: false
        )
    }

    func register(first: String, last: String, email: String, username: String, password: String) async throws -> [String: String] {
        struct Body: Encodable { let username: String; let password: String; let email: String; let first_name: String; let last_name: String }
        return try await request(
            "api/auth/register/",
            method: "POST",
            body: Body(username: username, password: password, email: email, first_name: first, last_name: last),
            authorized: false
        )
    }

    func refreshAccess(refresh: String) async throws -> AccessOnly {
        struct Body: Encodable { let refresh: String }
        return try await request(
            "api/auth/token/refresh/",
            method: "POST",
            body: Body(refresh: refresh),
            authorized: false
        )
    }

    // Notes
    func listNotes(page: Int, pageSize: Int = 6) async throws -> PagedNotes {
        try await request(
            "api/notes/",
            query: [
                URLQueryItem(name: "page", value: String(page)),
                URLQueryItem(name: "page_size", value: String(pageSize))
            ]
        )
    }

    func getNote(id: Int64) async throws -> Note {
        try await request("api/notes/\(id)/")
    }

    func createNote(title: String, description: String) async throws -> Note {
        struct Body: Encodable { let title: String; let description: String }
        return try await request("api/notes/", method: "POST", body: Body(title: title, description: description))
    }

    func updateNote(id: Int64, title: String, description: String) async throws -> Note {
        struct Body: Encodable { let title: String; let description: String }
        return try await request("api/notes/\(id)/", method: "PATCH", body: Body(title: title, description: description))
    }

    func deleteNote(id: Int64) async throws {
        try await requestVoid("api/notes/\(id)/", method: "DELETE")
    }

    // MARK: - Core request helpers

    /// Build URL correctly (handles query items; avoids percent-encoding `?` and `&` in a path).
    private func makeURL(path: String, query: [URLQueryItem]? = nil) -> URL {
        var comps = URLComponents(url: BASE_URL.appendingPathComponent(path), resolvingAgainstBaseURL: false)!
        if let query, !query.isEmpty { comps.queryItems = query }
        return comps.url!
    }

    /// JSON request that decodes a response body.
    func request<T: Decodable>(
        _ path: String,
        method: String = "GET",
        body: Encodable? = nil,
        authorized: Bool = true,
        decode: T.Type = T.self,
        query: [URLQueryItem]? = nil
    ) async throws -> T {
        var req = URLRequest(url: makeURL(path: path, query: query))
        req.httpMethod = method
        req.setValue("application/json", forHTTPHeaderField: "Content-Type")

        if authorized, let access = TokenStore.access() {
            req.setValue("Bearer \(access)", forHTTPHeaderField: "Authorization")
        }
        if let body {
            req.httpBody = try jsonEncoder.encode(AnyEncodable(body))
        }

        let (data, resp) = try await session.data(for: req)

        // Auto-refresh on 401 once
        if let http = resp as? HTTPURLResponse, http.statusCode == 401, authorized {
            try await refreshAccessOrThrow()
            return try await request(path, method: method, body: body, authorized: authorized, decode: decode, query: query)
        }

        #if DEBUG
        if let s = String(data: data, encoding: .utf8) {
            print("API \(method) \(path) \(query?.description ?? "") -> \(s)")
        }
        #endif

        return try jsonDecoder.decode(T.self, from: data)
    }

    /// Request for endpoints that return no body (e.g., DELETE).
    func requestVoid(
        _ path: String,
        method: String = "DELETE",
        authorized: Bool = true
    ) async throws {
        var req = URLRequest(url: makeURL(path: path))
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

    // MARK: - Refresh helper

    private func refreshAccessOrThrow() async throws {
        guard let refresh = TokenStore.refresh() else {
            throw URLError(.userAuthenticationRequired)
        }
        let accessOnly: AccessOnly = try await refreshAccess(refresh: refresh)
        TokenStore.saveAccess(accessOnly.access)
    }
}

// MARK: - Light wrapper so we can pass any Encodable as the body

private struct AnyEncodable: Encodable {
    private let encodeFunc: (Encoder) throws -> Void
    init<T: Encodable>(_ value: T) { self.encodeFunc = value.encode }
    func encode(to encoder: Encoder) throws { try encodeFunc(encoder) }
}
