//
//  Constants.swift
//  SimpleNote
//
//  Created by Armin on 9/13/25.
//

import Foundation

#if targetEnvironment(simulator)
let BASE_URL = URL(string: "http://localhost:8000/")!
#else
// physical device -> your Mac's LAN IP
let BASE_URL = URL(string: "http://192.168.1.10:8000/")!  // <-- change me
#endif
