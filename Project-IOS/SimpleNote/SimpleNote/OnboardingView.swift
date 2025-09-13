//
//  OnboardingView.swift
//  SimpleNote
//
//  Created by Armin on 9/13/25.
//

import SwiftUI

struct OnboardingView: View {
    var onGetStarted: () -> Void
    var body: some View {
        VStack(spacing: 20) {
            Spacer()
            Text("Welcome to SimpleNote").font(.title).bold()
            Text("Jot down anything you want to achieve, today or in the future.")
                .multilineTextAlignment(.center)
                .foregroundStyle(.secondary)
                .padding(.horizontal, 24)
            Spacer()
            Button(action: onGetStarted) {
                HStack { Spacer(); Text("Let’s Get Started"); Spacer() }
            }
            .frame(height: 56)
            .background(Color(red: 0.31, green: 0.31, blue: 0.76))
            .foregroundStyle(.white)
            .clipShape(RoundedRectangle(cornerRadius: 28))
            .padding(.horizontal, 20)
            .padding(.bottom, 30)
        }
    }
}
