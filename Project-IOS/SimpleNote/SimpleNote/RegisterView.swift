import SwiftUI

struct RegisterView: View {
    @StateObject var vm = AuthViewModel()

    @State private var first = ""
    @State private var last = ""
    @State private var email = ""
    @State private var username = ""
    @State private var password = ""
    @State private var confirm = ""

    var onDone: () -> Void
    var onBack: () -> Void

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 14) {
                Button("‹ Back to Login", action: onBack)
                    .tint(Color(hex: 0x504EC3))

                Text("Register")
                    .font(.system(size: 32, weight: .bold))

                Group {
                    TextField("First Name", text: $first).textFieldStyle(.roundedBorder)
                    TextField("Last Name", text: $last).textFieldStyle(.roundedBorder)
                    TextField("Username", text: $username).textFieldStyle(.roundedBorder)
                    TextField("Email", text: $email).textFieldStyle(.roundedBorder)
                    SecureField("Password", text: $password).textFieldStyle(.roundedBorder)
                    SecureField("Retype Password", text: $confirm).textFieldStyle(.roundedBorder)
                }

                Button(action: {
                    Task {
                        guard !first.isEmpty, !last.isEmpty, !email.isEmpty,
                              !username.isEmpty, !password.isEmpty, password == confirm else { return }
                        if await vm.register(first: first, last: last, email: email, username: username, password: password) {
                            onDone()
                        }
                    }
                }) {
                    HStack { Spacer(); Text(vm.loading ? "…" : "Register"); Spacer() }
                }
                .frame(height: 52)
                .background(Color(hex: 0x504EC3))
                .foregroundStyle(.white)
                .clipShape(RoundedRectangle(cornerRadius: 26))

                if let err = vm.error {
                    Text(err).foregroundStyle(.red).font(.footnote)
                }
            }
            .padding(20)
        }
    }
}
