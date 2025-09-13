//
//  Models.swift
//  SimpleNote
//
//  Created by Armin on 9/13/25.
//

import Foundation

public struct TokenPair: Codable { public let access: String; public let refresh: String }
public struct AccessOnly: Codable { public let access: String }

public struct Note: Identifiable, Codable, Hashable {
    public let id: Int64
    public var title: String
    public var description: String
    public var created_at: String?
    public var updated_at: String?
}

public struct PagedNotes: Codable {
    public let count: Int
    public let next: String?
    public let previous: String?
    public let results: [Note]
}
