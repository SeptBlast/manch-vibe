import SwiftUI
import PhotosUI

// Two-step flow: .photo → .preview
private enum CreationStep { case photo, preview }

struct ProfileCardCreationView: View {
    let uid: String
    let answers: OnboardingAnswers
    var onComplete: () -> Void = {}

    @StateObject private var vm = ProfileCardCreationViewModel()
    @State private var step: CreationStep = .photo
    @State private var cardFace: CardFace = .front

    var body: some View {
        Group {
            switch step {
            case .photo:
                PhotoPickerStepView(
                    selectedPhoto: $vm.selectedPhoto,
                    image: vm.photoImage,
                    onNext: { step = .preview }
                )
                .task(id: vm.selectedPhoto) { await vm.loadPhoto(from: vm.selectedPhoto) }

            case .preview:
                CardPreviewStepView(
                    uid: uid,
                    answers: answers,
                    image: vm.photoImage,
                    cardFace: $cardFace,
                    isPublishing: vm.isUploading,
                    onPublish: { Task { await vm.publishCard(uid: uid, answers: answers) } },
                    onBack: { step = .photo }
                )
            }
        }
        .onChange(of: vm.isSaved) { _, saved in if saved { onComplete() } }
        .alert("Something went wrong", isPresented: .constant(vm.error != nil)) {
            Button("OK") { vm.error = nil }
        } message: {
            Text(vm.error ?? "")
        }
    }
}

// ---------------------------------------------------------------------------
// Step 1 — Photo picker
// ---------------------------------------------------------------------------

private struct PhotoPickerStepView: View {
    @Binding var selectedPhoto: PhotosPickerItem?
    let image: UIImage?
    let onNext: () -> Void

    var body: some View {
        ZStack {
            DesignTokens.Color.backgroundDark.ignoresSafeArea()

            VStack(spacing: DesignTokens.Spacing.lg) {
                Spacer()

                Text("Add your photo")
                    .font(DesignTokens.Font.displayLarge)
                    .foregroundStyle(DesignTokens.Color.textOnDark)
                    .multilineTextAlignment(.center)

                Text("This is the first thing people see on your card")
                    .font(DesignTokens.Font.bodyMedium)
                    .foregroundStyle(DesignTokens.Color.textOnDark.opacity(0.7))
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, DesignTokens.Spacing.xl)

                Spacer().frame(height: DesignTokens.Spacing.md)

                // Avatar picker
                PhotosPicker(selection: $selectedPhoto, matching: .images) {
                    avatarPreview
                }
                .buttonStyle(.plain)

                Spacer()

                PrimaryButton(
                    label: image != nil ? "Preview my card" : "Skip, preview card",
                    action: onNext
                )
                .padding(.horizontal, DesignTokens.Spacing.xl)
                .padding(.bottom, DesignTokens.Spacing.xl)
            }
        }
    }

    private var avatarPreview: some View {
        ZStack {
            RoundedRectangle(cornerRadius: DesignTokens.Radius.card)
                .fill(DesignTokens.Color.chipUnselectedBg)
                .frame(width: 200, height: 200)
                .overlay(
                    RoundedRectangle(cornerRadius: DesignTokens.Radius.card)
                        .stroke(DesignTokens.Color.solaceTeal, lineWidth: 2)
                )

            if let img = image {
                Image(uiImage: img)
                    .resizable()
                    .scaledToFill()
                    .frame(width: 200, height: 200)
                    .clipShape(RoundedRectangle(cornerRadius: DesignTokens.Radius.card))
            } else {
                VStack(spacing: DesignTokens.Spacing.sm) {
                    Image(systemName: "camera.fill")
                        .font(.system(size: 36))
                        .foregroundStyle(DesignTokens.Color.solaceTeal)
                    Text("Tap to choose")
                        .font(DesignTokens.Font.labelMedium)
                        .foregroundStyle(DesignTokens.Color.solaceTeal)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Step 2 — Card preview with 3-D flip
// ---------------------------------------------------------------------------

private struct CardPreviewStepView: View {
    let uid: String
    let answers: OnboardingAnswers
    let image: UIImage?
    @Binding var cardFace: CardFace
    let isPublishing: Bool
    let onPublish: () -> Void
    let onBack: () -> Void

    private var profile: UserProfile {
        let moodEntry = defaultMoodPalette.first { $0.key == answers.moodColor }
        return UserProfile(
            id: uid,
            username: answers.username.isEmpty ? "Anonymous" : answers.username,
            avatarUrl: nil, // local UIImage displayed directly via PhotoPickerPreview
            moodColor: answers.moodColor,
            moodLabel: moodEntry?.label ?? "Calm",
            bio: answers.feelingSentence,
            vibes: answers.vibes
        )
    }

    var body: some View {
        ZStack {
            DesignTokens.Color.backgroundDark.ignoresSafeArea()

            VStack(spacing: DesignTokens.Spacing.md) {
                Spacer().frame(height: DesignTokens.Spacing.md)

                Text("Your profile card")
                    .font(DesignTokens.Font.displayLarge)
                    .foregroundStyle(DesignTokens.Color.textOnDark)

                Text("Tap the card to flip it")
                    .font(DesignTokens.Font.bodySmall)
                    .foregroundStyle(DesignTokens.Color.textOnDark.opacity(0.6))

                // 3-D flip card
                FlipCardView(face: $cardFace) {
                    ProfileCardFrontView(
                        profile: profile,
                        onEditClick: onBack,
                        imageOverride: image
                    )
                } back: {
                    ProfileCardBackView(
                        profile: profile,
                        soughtSupport: answers.soughtSupport,
                        openness: answers.openness,
                        feelingNow: answers.feelingNow,
                        onEditClick: onBack,
                        imageOverride: image
                    )
                }
                .padding(.horizontal, DesignTokens.Spacing.lg)

                Spacer()

                if isPublishing {
                    ProgressView()
                        .tint(DesignTokens.Color.solaceTeal)
                        .scaleEffect(1.4)
                } else {
                    PrimaryButton(label: "Publish my card ✨", action: onPublish)
                        .padding(.horizontal, DesignTokens.Spacing.xl)
                }

                Spacer().frame(height: DesignTokens.Spacing.xl)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Preview
// ---------------------------------------------------------------------------

#Preview {
    ProfileCardCreationView(
        uid: "preview",
        answers: OnboardingAnswers(
            username: "CalmWaves27",
            feelingSentence: "There's a part of me that wants to stay in bed all day.",
            moodColor: .calmRelaxed,
            vibes: ["Relationships", "Growth"],
            soughtSupport: "Yes, I'm in therapy",
            openness: "I'd like to connect with a few like-minded people"
        )
    )
    .solaceTheme()
}
