//
//  TokenStore.swift
//  SimpleNote
//
//  Created by Armin on 9/13/25.
//

// TokenStore.swift
import Foundation
import Security

enum KeychainKey: String { case access = "sn_access", refresh = "sn_refresh" }

enum TokenStore {
    static func save(access: String, refresh: String) {
        save(key: .access, value: access)
        save(key: .refresh, value: refresh)
    }
    static func saveAccess(_ access: String) { save(key: .access, value: access) }
    static func access() -> String? { read(key: .access) }
    static func refresh() -> String? { read(key: .refresh) }
    static func clear() { delete(key: .access); delete(key: .refresh) }

    private static func save(key: KeychainKey, value: String) {
        delete(key: key)
        let data = Data(value.utf8)
        let q: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key.rawValue,
            kSecValueData as String: data
        ]
        SecItemAdd(q as CFDictionary, nil)
    }
    private static func read(key: KeychainKey) -> String? {
        let q: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key.rawValue,
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne
        ]
        var ref: CFTypeRef?
        let status = SecItemCopyMatching(q as CFDictionary, &ref)
        guard status == errSecSuccess, let data = ref as? Data else { return nil }
        return String(data: data, encoding: .utf8)
    }
    private static func delete(key: KeychainKey) {
        let q: [String: Any] = [ kSecClass as String: kSecClassGenericPassword,
                                 kSecAttrAccount as String: key.rawValue ]
        SecItemDelete(q as CFDictionary)
    }
}
